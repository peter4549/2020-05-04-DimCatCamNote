package com.elliot.kim.kotlin.dimcatcamnote.activities

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.adapters.AlarmedNoteAdapter
import com.elliot.kim.kotlin.dimcatcamnote.adapters.FolderAdapter
import com.elliot.kim.kotlin.dimcatcamnote.adapters.NoteAdapter
import com.elliot.kim.kotlin.dimcatcamnote.broadcast_receivers.AlarmReceiver
import com.elliot.kim.kotlin.dimcatcamnote.data.Folder
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.databinding.ActivityMainBinding
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.DialogFragmentManager
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.DialogFragments
import com.elliot.kim.kotlin.dimcatcamnote.fragments.*
import com.elliot.kim.kotlin.dimcatcamnote.item_touch_helper.RecyclerViewTouchHelper
import com.elliot.kim.kotlin.dimcatcamnote.item_touch_helper.UnderlayButton
import com.elliot.kim.kotlin.dimcatcamnote.item_touch_helper.UnderlayButtonClickListener
import com.elliot.kim.kotlin.dimcatcamnote.view_model.MainViewModel
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

const val KEY_EVENT_ACTION = "key_event_action"
const val KEY_EVENT_EXTRA = "key_event_extra"

class MainActivity : AppCompatActivity(), LifecycleOwner {

    lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var recyclerViewTouchHelper: RecyclerViewTouchHelper

    lateinit var currentFolder: Folder

    var alarmedNoteAdapter: AlarmedNoteAdapter? = null
    private var sortingCriteria = SortingCriteria.EDIT_TIME.index

    lateinit var fragmentManager: FragmentManager

    val alarmFragment = AlarmFragment(this)
    private val calendarFragment = CalendarFragment()
    val cameraFragment = CameraFragment()
    val configureFragment = ConfigureFragment()
    val editFragment = EditFragment(this)
    val writeFragment = WriteFragment()

    private var initialization = true
    private var isCalendarClicked = false
    private var pressedTime = 0L
    var isClearing = false

    private lateinit var dialogFragmentManager: DialogFragmentManager

    // If an alarm is activated while the application is running,
    // cancel the alarm.
    private val receiver: BroadcastReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent!!.getIntExtra(KEY_NOTE_ID, DEFAULT_VALUE_NOTE_ID)
            cancelAlarm(getNoteById(id), false)
        }
    }

    override fun onStart() {
        isAppRunning = true

        // Release fragments to prevent errors
        // that can occur when changing the theme of the application
        while (fragmentManager.backStackEntryCount > 0)
            fragmentManager.popBackStackImmediate()

        for (fragment in fragmentManager.fragments)
            fragmentManager.beginTransaction().remove(fragment!!).commit()

        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionsRequired = getPermissionsRequired(this)
        if (permissionsRequired.isNotEmpty()) {
            requestPermissions(permissionsRequired,
                PERMISSIONS_REQUEST_CODE
            )
        }

        fragmentManager = supportFragmentManager

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.writeFloatingActionButton.setOnClickListener { startWriteFragment() }
        binding.sortContainer.setOnClickListener {
            dialogFragmentManager.showDialogFragment(DialogFragments.SORT)
        }
        setSupportActionBar(binding.toolbar)

        initSortingCriteria()
        initColor()
        initFont()
        initNavigationDrawer()

        val viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        viewModel.setContext(this)

        var notesSize = 0
        viewModel.getAll().observe(this, androidx.lifecycle.Observer { notes ->
            if (initialization) {// 초기화 로직 정리할 것.
                initialize(notes)
                createUnderlayButtons()
                viewModel.itemCount = notes.count()
                initialization = false
            } else {
                when {
                    notesSize < notes.size -> {
                        noteAdapter.insert(notes[notes.size - 1])
                    }
                    notesSize == notes.size ->
                        noteAdapter.update(viewModel.targetNote!!)
                    notesSize > notes.size -> {
                        if (isClearing && viewModel.itemCount == 0) isClearing = false
                        else {
                            if (editFragment.isFromAlarmedNoteSelectionFragment)
                                alarmedNoteAdapter!!.delete(alarmedNoteAdapter!!
                                            .getSelectedNoteByCreationTime(viewModel.targetNote!!.creationTime)
                                )
                            noteAdapter.delete(viewModel.targetNote!!)
                        }
                    }
                }
            }
            notesSize = notes.size
        })
    }

    fun recyclerViewScrollToTop() {
        binding.recyclerView.smoothScrollToPosition(0)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
            IntentFilter(ACTION_IS_APP_RUNNING))
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        super.onPause()
    }

    override fun onDestroy() {
        isAppRunning = false
        super.onDestroy()
    }

    private fun createUnderlayButtons() {
        recyclerViewTouchHelper = object: RecyclerViewTouchHelper(this,
            binding.recyclerView, 240, 640, noteAdapter) {
            override fun instantiateRightUnderlayButton(
                viewHolder: RecyclerView.ViewHolder,
                rightButtonBuffer: MutableList<UnderlayButton>
            ) {
                rightButtonBuffer.add(
                    UnderlayButton(this@MainActivity,
                        UnderlayButtonIds.EDIT,
                        "더보기",
                        30,
                        R.drawable.ic_more_vert_white_24dp,
                        getColor(R.color.colorUnderlayButtonMore),
                        object : UnderlayButtonClickListener {
                            override fun onClick(position: Int) {
                                showDialogFragment(DialogFragments.MORE_OPTIONS)
                            }
                        })
                )

                rightButtonBuffer.add(
                    UnderlayButton(this@MainActivity,
                        UnderlayButtonIds.EDIT,
                        "편집",
                        30,
                        R.drawable.ic_edit_white_24dp,
                        getColor(R.color.colorUnderlayButtonEdit),
                        object : UnderlayButtonClickListener {
                            override fun onClick(position: Int) {
                                startEditFragment()

                                noteAdapter.notifyItemChanged(position)
                            }
                        })
                )

                rightButtonBuffer.add(
                    UnderlayButton(this@MainActivity,
                        UnderlayButtonIds.ALARM,
                        "알림",
                        30,
                        R.drawable.ic_add_alarm_white_24dp,
                        getColor(R.color.colorUnderlayButtonAlarm),
                        object : UnderlayButtonClickListener {
                            override fun onClick(position: Int) {
                                if (noteAdapter.getNoteByPosition(position).alarmTime == null) {
                                    startAlarmFragment(noteAdapter.getNoteByPosition(position))
                                    noteAdapter.notifyItemChanged(position)
                                } else {
                                    cancelAlarm(noteAdapter.getNoteByPosition(position),
                                        isDelete = false, isByUser = true)
                                }
                            }
                        })
                )

                rightButtonBuffer.add(
                    UnderlayButton(this@MainActivity,
                        UnderlayButtonIds.DONE,
                        "완료",
                        30,
                        R.drawable.ic_done_white_24dp,
                        getColor(R.color.colorUnderlayButtonDone),
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
                        R.drawable.ic_delete_white_24dp,
                        getColor(R.color.colorUnderlayButtonDelete),
                        object : UnderlayButtonClickListener {
                            override fun onClick(position: Int) {
                                showDialogFragment(DialogFragments.CONFIRM_DELETE)
                                noteAdapter.notifyItemChanged(position)
                            }
                        })
                )
            }
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START, true)
        else {
            if (currentFragment == null) finishApplication()
            else {
                when (currentFragment) {
                    CurrentFragment.ALARM_FRAGMENT -> super.onBackPressed()
                    CurrentFragment.CALENDAR_FRAGMENT -> super.onBackPressed()
                    CurrentFragment.CAMERA_FRAGMENT -> super.onBackPressed()
                    CurrentFragment.CONFIGURE_FRAGMENT -> super.onBackPressed()
                    CurrentFragment.EDIT_FRAGMENT -> editFragment.finish(EditFragment.BACK_PRESSED)
                    CurrentFragment.PHOTO_FRAGMENT -> super.onBackPressed()
                    CurrentFragment.WRITE_FRAGMENT -> writeFragment.finish(WriteFragment.BACK_PRESSED)
                }
            }
        }
    }

    fun backPressed() { super.onBackPressed() }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                if (hasCameraPermissions(
                        this
                    )
                )
                    Toast.makeText(this, "CAM permission request granted", Toast.LENGTH_LONG).show()
                if (hasRecordAudioPermission(
                        this
                    )
                )
                    Toast.makeText(this, "Audio permission request granted", Toast.LENGTH_LONG).show()
            } else {
                if (!hasCameraPermissions(
                        this
                    )
                )
                    Toast.makeText(this, "카메라를 승인해야 쓸수잇음 ㅋ", Toast.LENGTH_LONG).show()
                if (!hasRecordAudioPermission(
                        this
                    )
                )
                    Toast.makeText(this, "음성 녹음을 승인해야 쓸수잇다네 소년", Toast.LENGTH_LONG).show()
            }
        }
    }

    /** When key down event is triggered, relay it via local broadcast so fragments can handle it */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                val intent = Intent(KEY_EVENT_ACTION).apply { putExtra(
                    KEY_EVENT_EXTRA, keyCode) }
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
                if (noteAdapter.isFirstBinding) noteAdapter.isFirstBinding = false
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
                showDialogFragment(DialogFragments.SORT)
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
        alarmFragment.setNote(note)
        startFragment(alarmFragment)
    }

    private fun startCalendarFragment() {
        // 여기에 알람 설정된 애들.. 담아서 던지고,,
        // 달력뷰는 바인딩 하는 중에, 날짜 당일 비교,, 어떤식으로.. current month와 current date를 비교해서.
        // 뭐랑 비교 => 각 노트의 알람시간으로부터 추출.
        isCalendarClicked = true
        calendarFragment.setAlarmedNotes(noteAdapter.getAlarmedNotes() as MutableList<Note>)

        fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.anim_slide_in_fast_left_enter,
                R.anim.anim_slide_in_fast_left_exit,
                R.anim.anim_slide_down_pop_enter,
                R.anim.anim_slide_down_pop_exit
            )
            .addToBackStack(null)
            .replace(R.id.main_container, calendarFragment).commit()
        hideFloatingActionButton()

    }

    fun startEditFragment() {
        editFragment.setNote(noteAdapter.selectedNote!!)
        editFragment.isFromAlarmedNoteSelectionFragment = false
        startFragment(editFragment)
    }

    private fun startWriteFragment() {
        writeFragment.isFromAlarmedNoteSelectionFragment = false
        startFragment(writeFragment)
    }

    private fun startFragment(fragment: Fragment) {
        fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.anim_slide_in_left_enter,
                R.anim.anim_slide_in_left_exit,
                R.anim.anim_slide_down_pop_enter,
                R.anim.anim_slide_down_pop_exit
            )
            .addToBackStack(null)
            .replace(R.id.main_container, fragment).commit()
        hideFloatingActionButton()
    }

    fun setCurrentFragment(fragment: CurrentFragment?) {
        currentFragment = fragment
    }

    fun getNoteById(id: Int): Note = noteAdapter.getNoteById(id)

    fun cancelAlarm(note: Note, isDelete: Boolean, isByUser: Boolean = false) {
        val id: Int = note.id

        val alarmManager =
            getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)

        if (isByUser) {
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                id,
                intent,
                PendingIntent.FLAG_ONE_SHOT
            )

            alarmManager.cancel(pendingIntent)
        }

        removeAlarmPreferences(id)

        if (!isDelete) {
            note.alarmTime = null
            viewModel.update(note)
            Toast.makeText(this, "알림이 해제되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeAlarmPreferences(id: Int) {
        val sharedPreferences = getSharedPreferences(
            PREFERENCES_NAME_ALARM,
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.remove(id.toString() + "0")
        editor.remove(id.toString() + "1")
        editor.remove(id.toString() + "2")
        editor.remove(id.toString() + "3")
        editor.apply()
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
                // Process.killProcess(Process.myPid())
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

    fun deleteFileFromUri(uri: Uri?): Boolean {
        if (uri == null) return false

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

    private fun initialize(notes: MutableList<Note>) {

        initRecyclerView(notes)
        initDialogFragmentManager()
        // initializeSortingCriteria()

    }

    // Must be called after folderAdapter and NoteAdapter are initialized.
    private fun initDialogFragmentManager() {
        dialogFragmentManager = DialogFragmentManager(this, folderAdapter, noteAdapter)
    }

    private fun initNavigationDrawer() {
        folderAdapter = FolderAdapter(this)

        setNavigationDrawerColor()

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        binding.navDrawerRecyclerview.apply {
            setHasFixedSize(true)
            adapter = folderAdapter
            layoutManager = LinearLayoutManager(context)
        }

        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navigationConfigureContainer.setOnClickListener {
            startFragment(configureFragment)
        }

        binding.navigationCalendarContainer.setOnClickListener {
            startCalendarFragment()
        }

        binding.navigationAddFolderContainer.setOnClickListener {
            showDialogFragment(DialogFragments.ADD_FOLDER) }
    }

    fun setNavigationDrawerColor() {
        binding.navigationConfigureContainer.setBackgroundColor(toolbarColor)
        binding.navigationCalendarContainer.setBackgroundColor(toolbarColor)
        binding.navigationAddFolderContainer.setBackgroundColor(toolbarColor)
        folderAdapter.notifyDataSetChanged()
    }

    private fun initRecyclerView(notes: MutableList<Note>) {
        noteAdapter = NoteAdapter(this, notes)
        noteAdapter.sortingCriteria = sortingCriteria

        val layoutAnimationController = android.view.animation.AnimationUtils
            .loadLayoutAnimation(this,
                R.anim.layout_animation
            )

        binding.recyclerView.apply {
            setHasFixedSize(true)
            adapter = noteAdapter
            // Replaced from LinearLayoutManager to LinearLayoutManagerWrapper
            layoutManager = LinearLayoutManagerWrapper(context)
            layoutAnimation = layoutAnimationController
        }

        loadCurrentFolder()
        showCurrentFolderItems(currentFolder)
    }

    private fun initSortingCriteria() {
        val preferences = getSharedPreferences(PREFERENCES_SORTING_CRITERIA, Context.MODE_PRIVATE)
        sortingCriteria = preferences.getInt(KEY_SORTING_CRITERIA, SortingCriteria.EDIT_TIME.index)
        binding.textViewSort.text = getTextByCriteria(sortingCriteria)
    }

    fun setTextViewSortText(sortingCriteria: Int) {
        binding.textViewSort.text = getTextByCriteria(sortingCriteria)
    }

    private fun getTextByCriteria(sortingCriteria: Int): String {
        return when(sortingCriteria) {
            SortingCriteria.CREATION_TIME.index ->  "생성시간 기준으로 정렬"
            SortingCriteria.EDIT_TIME.index -> "수정시간 기준으로 정렬"
            SortingCriteria.NAME.index ->  "이름 기준으로 정렬"
            else -> throw Exception("Invalid sorting criteria.")
        }
    }

    private fun initColor() {

        val defaultToolbarColor = getColor(R.color.defaultColorToolbar)
        val defaultBackgroundColor = getColor(R.color.defaultColorBackground)
        val defaultNoteColor = getColor(R.color.defaultColorNote)
        val defaultInlayColor = getColor(R.color.defaultColorInlay)
        val defaultAppWidgetTitleColor = getColor(R.color.defaultColorAppWidgetTitle)
        val defaultAppWidgetBackgroundColor = getColor(R.color.defaultColorAppWidgetBackground)

        val preferences = getSharedPreferences(
            PREFERENCES_SET_COLOR,
            Context.MODE_PRIVATE
        )

        toolbarColor = preferences.getInt(KEY_COLOR_TOOLBAR, defaultToolbarColor)
        backgroundColor = preferences.getInt(KEY_COLOR_BACKGROUND, defaultBackgroundColor)
        noteColor = preferences.getInt(KEY_COLOR_NOTE, defaultNoteColor)
        inlayColor = preferences.getInt(KEY_COLOR_INLAY, defaultInlayColor)
        appWidgetTitleColor =
            preferences.getInt(KEY_COLOR_APP_WIDGET_TITLE, defaultAppWidgetTitleColor)
        appWidgetBackgroundColor =
            preferences.getInt(KEY_COLOR_APP_WIDGET_BACKGROUND, defaultAppWidgetBackgroundColor)

        binding.mainContainer.setBackgroundColor(backgroundColor)
        binding.sortContainer.setBackgroundColor(toolbarColor)
        binding.toolbar.setBackgroundColor(toolbarColor)
        binding.writeFloatingActionButton.backgroundTintList = ColorStateList.valueOf(toolbarColor)
    }

    fun setViewColor() {
        binding.mainContainer.setBackgroundColor(backgroundColor)
        binding.sortContainer.setBackgroundColor(toolbarColor)
        binding.toolbar.setBackgroundColor(toolbarColor)
        binding.writeFloatingActionButton.backgroundTintList = ColorStateList.valueOf(toolbarColor)
    }

    fun showCurrentFolderItems(folder: Folder, showAnimation: Boolean = true) {
        currentFolder = folder
        saveCurrentFolder(currentFolder)
        binding.textViewCurrentFolderName.text = currentFolder.name

        if (showAnimation) binding.recyclerView.scheduleLayoutAnimation()

        noteAdapter.getFilter().filter("")
    }

    fun refreshCurrentFolderItem(showAnimation: Boolean = true) {
        if (showAnimation) binding.recyclerView.scheduleLayoutAnimation()

        noteAdapter.getFilter().filter("")
    }

    private fun saveCurrentFolder(folder: Folder) {
        val preferences = getSharedPreferences(
            PREFERENCES_CURRENT_FOLDER,
            Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt(KEY_CURRENT_FOLDER, folder.id)
        editor.apply()
    }

    private fun loadCurrentFolder() {
        val preferences = getSharedPreferences(
            PREFERENCES_CURRENT_FOLDER,
            Context.MODE_PRIVATE)
        currentFolder = folderAdapter.getFolderById(preferences.getInt(KEY_CURRENT_FOLDER, 0))
    }

    fun showDialogFragment(dialogFragment: DialogFragments, toolbar: androidx.appcompat.widget.Toolbar? = null) {
        dialogFragmentManager.showDialogFragment(dialogFragment, toolbar)
    }

    fun getFolderAdapter(): FolderAdapter = folderAdapter
    fun getNoteAdapter(): NoteAdapter = noteAdapter

    fun closeDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START, true)
    }

    private fun initFont() {
        val preferences = getSharedPreferences(PREFERENCES_FONT,
            Context.MODE_PRIVATE)
        fontId = preferences.getInt(KEY_FONT_ID, R.font.nanum_gothic_font_family)
        fontStyleId = preferences.getInt(KEY_FONT_STYLE_ID, R.style.FontNanumGothic)
        font = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            resources.getFont(fontId)
        else ResourcesCompat.getFont(this, fontId)

        setFont()
    }

    fun setFont() {
        binding.textViewCurrentFolderName.adjustDialogTitleTextSize(fontId)
        binding.textViewSort.adjustDialogItemTextSize(fontId)

        binding.toolbar.setTitleTextAppearance(this, fontStyleId)
        binding.textViewCurrentFolderName.typeface = font
        binding.textViewSort.typeface = font
    }

    fun clear() {
        isClearing = true
        val message = configureFragment.progressDialogHandler.obtainMessage()
        message.what = ConfigureFragment.START_PROGRESS_DIALOG
        configureFragment.progressDialogHandler.sendMessage(message)
        for(note in noteAdapter.getAllNotes())
            viewModel.delete(note)
        noteAdapter.clear()
    }

    companion object {
        var font: Typeface? = null
        var fontId = 0
        var fontStyleId = 0
        var toolbarColor = 0
        var backgroundColor = 0
        var noteColor = 0
        var inlayColor = 0
        var appWidgetTitleColor = 0
        var appWidgetBackgroundColor = 0

        var isAppRunning = false

        const val DATABASE_NAME = "dim_cat_cam_notes_13"

        const val PREFERENCES_CURRENT_FOLDER = "current_folder"

        const val KEY_CURRENT_FOLDER = "key_current_folder"

        const val ACTION_IS_APP_RUNNING = "action_is_app_running"

        const val PERMISSIONS_REQUEST_CODE = 10
        const val CAMERA_PERMISSIONS_REQUEST_CODE = 11
        const val RECORD_AUDIO_PERMISSIONS_REQUEST_CODE = 12
        // const val LOCATION_PERMISSIONS_REQUEST_CODE = 13

        val CAMERA_PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)
        val RECORD_AUDIO_PERMISSIONS_REQUESTED = arrayOf(Manifest.permission.RECORD_AUDIO)
        /*
        val LOCATION_PERMISSIONS_REQUESTED = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION)
         */

        var currentFragment: CurrentFragment? = null

        enum class UnderlayButtonIds {
            DONE,
            ALARM,
            EDIT,
            DELETE,
            MORE
        }

        fun getPermissionsRequired(context: Context): Array<String> {
            var permissionsRequired = arrayOf<String>()
            if (!hasCameraPermissions(
                    context
                )
            ) permissionsRequired += CAMERA_PERMISSIONS_REQUIRED
            if (!hasRecordAudioPermission(
                    context
                )
            ) permissionsRequired += RECORD_AUDIO_PERMISSIONS_REQUESTED
            /*
            if (!hasLocationPermissions(
                    context
                )
            ) permissionsRequired += LOCATION_PERMISSIONS_REQUESTED
             */
            return permissionsRequired
        }

        fun hasCameraPermissions(context: Context) = CAMERA_PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        fun hasRecordAudioPermission(context: Context) = RECORD_AUDIO_PERMISSIONS_REQUESTED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        /*
        fun hasLocationPermissions(context: Context) = LOCATION_PERMISSIONS_REQUESTED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
         */

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

        fun longTimeToString(time: Long?, pattern: String): String = SimpleDateFormat(pattern, Locale.getDefault()).
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

        fun share(context: Context, note: Note) {
            val intent = Intent(Intent.ACTION_SEND)
            val text: String = note.toSharedString()

            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, "Cat Note\n")
            intent.putExtra(Intent.EXTRA_TEXT, text)

            val chooser = Intent.createChooser(intent, "공유하기")
            context.startActivity(chooser)
        }
    }
}