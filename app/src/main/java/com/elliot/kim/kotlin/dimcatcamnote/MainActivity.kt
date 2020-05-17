package com.elliot.kim.kotlin.dimcatcamnote

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.speech.RecognitionListener
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.LayoutAnimationController
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
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

class MainActivity : AppCompatActivity(), LifecycleOwner {

    private lateinit var binding: ActivityMainBinding

    lateinit var viewModel: MainViewModel

    private lateinit var noteAdapter: NoteAdapter
    private lateinit var recyclerViewTouchHelper: RecyclerViewTouchHelper

    lateinit var fragmentManager: FragmentManager

    val alarmFragment = AlarmFragment()
    val cameraFragment = CameraFragment()
    val editFragment = EditFragment()
    val photoFragment = PhotoFragment()
    val writeFragment = WriteFragment()

    lateinit var animationController: LayoutAnimationController

    private var initialization = true
    private var pressedTime = 0L

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

        val viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        var notesSize = 0
        viewModel.getAll().observe(this, androidx.lifecycle.Observer { notes ->
            if (initialization) {
                noteAdapter = NoteAdapter(this, notes)
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

    private fun createUnderlayButtons() {
        recyclerViewTouchHelper = object: RecyclerViewTouchHelper(this,
            binding.recyclerView, 544, 256,noteAdapter) {
            override fun instantiateRightUnderlayButton(
                viewHolder: RecyclerView.ViewHolder,
                rightButtonBuffer: MutableList<UnderlayButton>
            ) {
                rightButtonBuffer.add(
                    UnderlayButton(this@MainActivity,
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
                        "알림",
                        30,
                        0,
                        getColor(R.color.colorUnderlayButtonAlarm),
                        object : UnderlayButtonClickListener {
                            override fun onClick(position: Int) {
                                startAlarmFragment(noteAdapter.getNoteByPosition(position))

                                noteAdapter.notifyItemChanged(position)
                            }
                        })
                )

                rightButtonBuffer.add(
                    UnderlayButton(this@MainActivity,
                        "완료",
                        30,
                        0,
                        getColor(R.color.colorUnderlayButtonEdit),
                        object : UnderlayButtonClickListener {
                            override fun onClick(position: Int) {
                                Toast.makeText(
                                    this@MainActivity, "완료 clicked",
                                    Toast.LENGTH_SHORT
                                ).show()

                                noteAdapter.notifyItemChanged(position)
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
                showSortDialog()
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

    private fun showSortDialog() {
        val builder =
            AlertDialog.Builder(this)
        builder.setTitle(R.string.sort_by)
            .setItems(
                resources.getStringArray(R.array.sort_by)
            ) { _: DialogInterface?, which: Int ->
                noteAdapter.sort(which) // 옵션 기억하도록.
                noteAdapter.notifyDataSetChanged()
                binding.recyclerView.layoutAnimation = animationController
                binding.recyclerView.scheduleLayoutAnimation()
            }
        builder.create()
        builder.show()
    }

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

    companion object {
        const val DATABASE_NAME = "dim_cat_cam_notes"

        const val PERMISSIONS_REQUEST_CODE = 10
        const val CAMERA_PERMISSIONS_REQUEST_CODE = 11
        const val RECORD_AUDIO_PERMISSIONS_REQUEST_CODE = 12

        val CAMERA_PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)
        val RECORD_AUDIO_PERMISSIONS_REQUESTED = arrayOf(Manifest.permission.RECORD_AUDIO)

        var currentFragment: CurrentFragment? = null

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