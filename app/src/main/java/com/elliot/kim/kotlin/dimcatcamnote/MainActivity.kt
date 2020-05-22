package com.elliot.kim.kotlin.dimcatcamnote

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.*
import android.view.animation.LayoutAnimationController
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elliot.kim.kotlin.dimcatcamnote.broadcast_receivers.AlarmReceiver
import com.elliot.kim.kotlin.dimcatcamnote.databinding.ActivityMainBinding
import com.elliot.kim.kotlin.dimcatcamnote.fragments.*
import com.elliot.kim.kotlin.dimcatcamnote.item_touch_helper.RecyclerViewTouchHelper
import com.elliot.kim.kotlin.dimcatcamnote.item_touch_helper.UnderlayButton
import com.elliot.kim.kotlin.dimcatcamnote.item_touch_helper.UnderlayButtonClickListener
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

const val KEY_EVENT_ACTION = "key_event_action"
const val KEY_EVENT_EXTRA = "key_event_extra"

private const val DEFAULT_FOLDER_ID = 0

class MainActivity : AppCompatActivity(), LifecycleOwner {

    private lateinit var binding: ActivityMainBinding

    lateinit var viewModel: MainViewModel

    private lateinit var noteAdapter: NoteAdapter
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var recyclerViewTouchHelper: RecyclerViewTouchHelper
    lateinit var folderManager: FolderManager
    lateinit var folderGroup: MenuItem

    var currentFolderName: String? = null

    lateinit var fragmentManager: FragmentManager

    val alarmFragment = AlarmFragment()
    val cameraFragment = CameraFragment()
    val editFragment = EditFragment()
    val photoFragment = PhotoFragment()
    val writeFragment = WriteFragment()

    lateinit var animationController: LayoutAnimationController

    private var initialization = true
    private var pressedTime = 0L

    private lateinit var dialogManager: DialogManager

    private val receiver: BroadcastReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent!!.getIntExtra(AlarmFragment.KEY_ID_EXTRA,
                AlarmFragment.DEFAULT_VALUE_EXTRA)
            cancelAlarm(getNoteById(id), false)
        }
    }

    enum class CurrentFragment {
        ALARM_FRAGMENT,
        CAMERA_FRAGMENT,
        EDIT_FRAGMENT,
        WRITE_FRAGMENT
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionsRequired = getPermissionsRequired(this)
        if (permissionsRequired.isNotEmpty()) {
            requestPermissions(permissionsRequired, PERMISSIONS_REQUEST_CODE)
        }

        fragmentManager = supportFragmentManager
        animationController = android.view.animation.AnimationUtils.loadLayoutAnimation(
            applicationContext,
            R.anim.layout_animation)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolBar)
        binding.writeFloatingActionButton.setOnClickListener { startWriteFragment() }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        folderManager = FolderManager(this)
        folderAdapter = FolderAdapter(this, folderManager)
        binding.navDrawerRecyclerview.apply {
            setHasFixedSize(true)
            adapter = folderAdapter
            layoutManager = LinearLayoutManager(context)
        }

        initializeNavigationDrawer()


        val viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        var notesSize = 0
        viewModel.getAll().observe(this, androidx.lifecycle.Observer { notes ->
            if (initialization) {// 초기화 로직 정리할 것.
                noteAdapter = NoteAdapter(this, notes)
                initializeDialogManager()
                initialize()
                binding.recyclerView.apply {
                    setHasFixedSize(true)
                    adapter = noteAdapter
                    layoutManager = LinearLayoutManager(context)
                }

                createUnderlayButtons()

                initialization = false
            } else {
                when {
                    notesSize < notes.size -> noteAdapter.insert(notes[notes.size - 1])
                    notesSize == notes.size -> noteAdapter.update(viewModel.targetNote!!)
                    notesSize > notes.size -> noteAdapter.delete(viewModel.targetNote!!)
                }
            }
            notesSize = notes.size
        })
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
            IntentFilter(ACTION_IS_APP_RUNNING))
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
            receiver);
        super.onPause()
    }

    private fun createUnderlayButtons() {
        recyclerViewTouchHelper = object: RecyclerViewTouchHelper(this,
            binding.recyclerView, 256, 512, noteAdapter) {
            override fun instantiateRightUnderlayButton(
                viewHolder: RecyclerView.ViewHolder,
                rightButtonBuffer: MutableList<UnderlayButton>
            ) {
                rightButtonBuffer.add(
                    UnderlayButton(this@MainActivity,
                        UnderlayButtonIds.EDIT,
                        "편집",
                        30,
                        0,
                        getColor(R.color.colorUnderlayButtonEdit),
                        object : UnderlayButtonClickListener {
                            override fun onClick(position: Int) {
                                startEditFragment(noteAdapter.getNoteByPosition(position))

                                noteAdapter.notifyItemChanged(position)
                            }
                        })
                )

                rightButtonBuffer.add(
                    UnderlayButton(this@MainActivity,
                        UnderlayButtonIds.SHARE,
                        "공유",
                        30,
                        0,
                        getColor(R.color.colorUnderlayButtonShare),
                        object : UnderlayButtonClickListener {
                            override fun onClick(position: Int) {
                                share(noteAdapter.getNoteByPosition(position))

                                noteAdapter.notifyItemChanged(position)
                            }
                        })
                )

                rightButtonBuffer.add(
                    UnderlayButton(this@MainActivity,
                        UnderlayButtonIds.ALARM,
                        "알림",
                        30,
                        0,
                        getColor(R.color.colorUnderlayButtonAlarm),
                        object : UnderlayButtonClickListener {
                            override fun onClick(position: Int) {
                                if (noteAdapter.getNoteByPosition(position).alarmTime == null)
                                    startAlarmFragment(noteAdapter.getNoteByPosition(position))
                                else
                                    cancelAlarm(noteAdapter.getNoteByPosition(position), false)

                                noteAdapter.notifyItemChanged(position)
                            }
                        })
                )

                rightButtonBuffer.add(
                    UnderlayButton(this@MainActivity,
                        UnderlayButtonIds.DONE,
                        "완료",
                        30,
                        0,
                        getColor(R.color.colorUnderlayButtonEdit),
                        object : UnderlayButtonClickListener {
                            override fun onClick(position: Int) {
                                if (noteAdapter.getNoteByPosition(position).isDone) {
                                    noteAdapter.getNoteByPosition(position).isDone = false
                                    showToast("완료처리를 해제하셨습니다.")
                                } else {
                                    noteAdapter.getNoteByPosition(position).isDone = true
                                    showToast("완료처리 되었습니다.")
                                }

                                viewModel.update(noteAdapter.getNoteByPosition(position))
                            }
                        })
                )

                rightButtonBuffer.add(
                    UnderlayButton(this@MainActivity,
                        UnderlayButtonIds.DONE,
                        "폴더 이동",
                        20,
                        0,
                        getColor(R.color.colorYellowfff176),
                        object : UnderlayButtonClickListener {
                            override fun onClick(position: Int) {
                                dialogManager.showDialog(DialogManager
                                    .Companion.DialogType.FOLDER_OPTIONS)
                            }
                        })
                )
            }

            override fun instantiateLeftUnderlayButton(
                viewHolder: RecyclerView.ViewHolder,
                leftButtonBuffer: MutableList<UnderlayButton>
            ) {
                leftButtonBuffer.add(
                    UnderlayButton(this@MainActivity,
                        UnderlayButtonIds.DELETE,
                        "삭제",
                        30,
                        R.drawable.dimcat100,
                        getColor(R.color.colorUnderlayButtonDelete),
                        object : UnderlayButtonClickListener {
                            override fun onClick(position: Int) {
                                viewModel.delete(noteAdapter
                                    .getNoteByPosition(position))
                            }
                        })
                )
            }
        }
    }

    override fun onBackPressed() {
        if (currentFragment == null) finishApplication()
        else {
            when (currentFragment) {
                CurrentFragment.ALARM_FRAGMENT -> super.onBackPressed()
                CurrentFragment.CAMERA_FRAGMENT -> super.onBackPressed()
                CurrentFragment.EDIT_FRAGMENT -> editFragment.finish(EditFragment.BACK_PRESSED)
                CurrentFragment.WRITE_FRAGMENT -> writeFragment.finish(WriteFragment.BACK_PRESSED)
            }
        }
    }

    fun backPressed() { super.onBackPressed() }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                if (hasCameraPermissions(this))
                    Toast.makeText(this, "CAM permission request granted", Toast.LENGTH_LONG).show()
                if (hasRecordAudioPermission(this))
                    Toast.makeText(this, "Audio permission request granted", Toast.LENGTH_LONG).show()
            } else {
                if (!hasCameraPermissions(this))
                    Toast.makeText(this, "카메라를 승인해야 쓸수잇음 ㅋ", Toast.LENGTH_LONG).show()
                if (!hasRecordAudioPermission(this))
                    Toast.makeText(this, "음성 녹음을 승인해야 쓸수잇다네 소년", Toast.LENGTH_LONG).show()
            }
        }
    }

    /** When key down event is triggered, relay it via local broadcast so fragments can handle it */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                val intent = Intent(KEY_EVENT_ACTION).apply { putExtra(KEY_EVENT_EXTRA, keyCode) }
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchView = menu.findItem(R.id.menu_search)
            .actionView as SearchView
        searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                noteAdapter.getFilter().filter(newText)
                return true
            }
        })
        searchView.setOnSearchClickListener {
            binding.imageViewLogo.visibility = View.GONE
        }

        searchView.setOnCloseListener {
            binding.imageViewLogo.visibility = View.VISIBLE
            false
        }
        searchView.queryHint = "찾을 노트 제목을 입력해주세요."
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_write -> {
                startWriteFragment()
                true
            }
            R.id.menu_sort -> {
                dialogManager.showDialog(DialogManager.Companion.DialogType.SORT)
                binding.recyclerView.layoutAnimation = animationController
                binding.recyclerView.scheduleLayoutAnimation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun showFloatingActionButton() {
        binding.writeFloatingActionButton.show()
    }

    private fun hideFloatingActionButton() {
        binding.writeFloatingActionButton.hide()
    }

    private fun startAlarmFragment(note: Note) {
        alarmFragment.isFromEditFragment = false
        alarmFragment.note = note
        startFragment(alarmFragment)
    }

    fun startEditFragment(note: Note) {
        editFragment.setNote(note)
        startFragment(editFragment)
    }

    private fun startWriteFragment() {
        startFragment(writeFragment)
    }

    private fun startFragment(fragment: Fragment) {
        hideFloatingActionButton()
        fragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_up, R.anim.slide_down, R.anim.slide_down)
            .addToBackStack(null)
            .replace(R.id.main_container, fragment).commit()
        hideFloatingActionButton()
    }

    fun setCurrentFragment(fragment: CurrentFragment?) {
        currentFragment = fragment
    }

    private fun showFolderManagementDialog(folder: Folder) {
        /*
        val builder = AlertDialog.Builder(this)
        builder.setTitle("\"${folder.second}\" 폴더 관리")
            .setItems(R.array.colors_array,
                DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        0 -> {}
 g
                    }
                })
        builder.create()

         */


    }

    fun getNoteById(id: Int): Note = noteAdapter.getNoteById(id)

    fun cancelAlarm(note: Note, isDelete: Boolean) {
        val id: Int = note.id

        val alarmManager =
            getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            id,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        alarmManager.cancel(pendingIntent)
        removeAlarmPreferences(id)

        if (!isDelete) {
            note.alarmTime = null
            viewModel.update(note)

            Toast.makeText(this, "알림이 해제되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeAlarmPreferences(number: Int) {
        val sharedPreferences = getSharedPreferences(
            "alarm_information",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.remove(number.toString() + "0")
        editor.remove(number.toString() + "1")
        editor.remove(number.toString() + "2")
        editor.remove(number.toString() + "3")
        editor.apply()
    }

    fun share(note: Note) {
        val intent = Intent(Intent.ACTION_SEND)
        val text: String = note.toSharedString()

        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "Cat Note\n")
        intent.putExtra(Intent.EXTRA_TEXT, text)

        val chooser = Intent.createChooser(intent, "공유하기")
        this.startActivity(chooser)
    }

    private fun finishApplication() {
        if (pressedTime == 0L) {
            Snackbar.make(
                findViewById(R.id.main_container),
                "한 번 더 누르면 종료됩니다.", Snackbar.LENGTH_LONG
            ).show()
            pressedTime = System.currentTimeMillis()
        } else {
            if (System.currentTimeMillis() - pressedTime > 2500L) {
                Snackbar.make(
                    findViewById(R.id.main_container),
                    " 한 번 더 누르면 종료됩니다.", Snackbar.LENGTH_LONG
                ).show()
                pressedTime = 0L
            } else {
                super.onBackPressed()
                finish()
                Process.killProcess(Process.myPid())
            }
        }
    }

    fun deleteFileFromUri(stringUri: String): Boolean {
        val uri = Uri.parse(stringUri)
        val file = File(uri.path!!)
        if (file.delete()) {
            refreshGallery(file)
            return true
        }
        else {
            if (file.canonicalFile.delete()) {
                refreshGallery(file)
                return true
            }
            else applicationContext.deleteFile(file.name)
            return if (file.exists()) false
            else {
                refreshGallery(file)
                true
            }
        }
    }

    private fun refreshGallery(file: File) {
        MediaScannerConnection.scanFile(this, arrayOf(file.toString()), null
        ) { path, uri ->
            Log.i("ExternalStorage", "Scanned $path:")
            Log.i("ExternalStorage", "uri=$uri");}
    }

    fun showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, text, duration).show()
    }

    private fun initialize() {
        loadCurrentFolderName()
    }

    private fun initializeDialogManager() {
        dialogManager = DialogManager(this)
        dialogManager.setFolderAdapter(folderAdapter)
        dialogManager.setFolderManager(folderManager)
        dialogManager.setNoteAdapter(noteAdapter)
    }

    private fun initializeNavigationDrawer() {
        binding.buttonAddFolder.setOnClickListener {
            dialogManager.showDialog(DialogManager.Companion.DialogType.ADD_FOLDER)
        }
    }

    fun _setCurrentFolderName(name: String?) { // showCurrentFolderItems 로 바꾸는게 나은듯.
        currentFolderName = name
        saveCurrentFolderName(currentFolderName)
        binding.textViewCurrentFolderName.text = currentFolderName ?: "전체"
        noteAdapter.getFilter().filter("")
    }

    private fun saveCurrentFolderName(name: String?) {
        val preferences = getSharedPreferences(
            PREFERENCES_CURRENT_FOLDER,
            Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(KEY_CURRENT_FOLDER, name)
        editor.apply()
    }

    // create 단계에서 실행하면 notify없이 바로 적용 가능할 것..
    private fun loadCurrentFolderName() {
        val preferences = getSharedPreferences(
            PREFERENCES_CURRENT_FOLDER,
            Context.MODE_PRIVATE)
        _setCurrentFolderName(preferences.getString(KEY_CURRENT_FOLDER, null))
    }

    companion object {
        const val DATABASE_NAME = "dim_cat_cam_notes_3"

        const val PREFERENCES_CURRENT_FOLDER = "current_folder"

        const val KEY_CURRENT_FOLDER = "key_current_folder"

        const val ACTION_IS_APP_RUNNING = "action_is_app_running"

        const val PERMISSIONS_REQUEST_CODE = 10
        const val CAMERA_PERMISSIONS_REQUEST_CODE = 11
        const val RECORD_AUDIO_PERMISSIONS_REQUEST_CODE = 12

        val CAMERA_PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)
        val RECORD_AUDIO_PERMISSIONS_REQUESTED = arrayOf(Manifest.permission.RECORD_AUDIO)

        var currentFragment: CurrentFragment? = null

        enum class UnderlayButtonIds {
            DONE,
            ALARM,
            SHARE,
            EDIT,
            DELETE
        }

        private const val pattern = "yyyy-MM-dd-a-hh:mm:ss"

        fun getPermissionsRequired(context: Context): Array<String> {
            var permissionsRequired = arrayOf<String>()
            if (!hasCameraPermissions(context)) permissionsRequired += CAMERA_PERMISSIONS_REQUIRED
            if (!hasRecordAudioPermission(context)) permissionsRequired += RECORD_AUDIO_PERMISSIONS_REQUESTED
            return permissionsRequired
        }

        fun hasCameraPermissions(context: Context) = CAMERA_PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        fun hasRecordAudioPermission(context: Context) = RECORD_AUDIO_PERMISSIONS_REQUESTED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() } }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }

        fun getCurrentTime(): Long {
            return System.currentTimeMillis()
        }

        fun timeToString(time: Long?): String = SimpleDateFormat(pattern, Locale.getDefault()).
            format(time ?: 0L)

        fun showKeyboard(context: Context?, view: View?) {
            val manager =
                context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (view != null) manager.showSoftInput(view, 0)
        }

        fun hideKeyboard(context: Context?, view: View?) {
            val manager =
                context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (view != null) manager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}