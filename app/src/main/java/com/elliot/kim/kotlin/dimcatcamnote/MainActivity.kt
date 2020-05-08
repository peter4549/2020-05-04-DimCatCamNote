package com.elliot.kim.kotlin.dimcatcamnote

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.Process
import android.util.TypedValue
import android.view.KeyEvent
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.elliot.kim.kotlin.dimcatcamnote.broadcast_receivers.AlarmReceiver
import com.elliot.kim.kotlin.dimcatcamnote.databinding.ActivityMainBinding
import com.elliot.kim.kotlin.dimcatcamnote.fragments.AddFragment
import com.elliot.kim.kotlin.dimcatcamnote.fragments.AlarmFragment
import com.elliot.kim.kotlin.dimcatcamnote.fragments.CameraFragment
import com.elliot.kim.kotlin.dimcatcamnote.fragments.EditFragment
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

const val KEY_EVENT_ACTION = "key_event_action"
const val KEY_EVENT_EXTRA = "key_event_extra"

class MainActivity : AppCompatActivity(), LifecycleOwner {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NoteAdapter

    private var initialization = true
    private var pressedTime = 0L

    private val editFragment = EditFragment()

    lateinit var viewModel: MainViewModel
    lateinit var fragmentManager: FragmentManager
    lateinit var addFragment: AddFragment
    lateinit var cameraFragment: CameraFragment


    val alarmFragment =
        AlarmFragment()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasPermissions(this))
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)

        fragmentManager = supportFragmentManager

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.cameraFloatingActionButton.setOnClickListener { startCameraFragment() }
        binding.addFloatingActionButton.setOnClickListener { startAddFragment() }

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        val viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        var notesSize = 0
        viewModel.getAll().observe(this, androidx.lifecycle.Observer { notes ->
            if (initialization) {
                adapter = NoteAdapter(this, notes)
                binding.recyclerView.adapter = adapter
                initialization = false
            } else {
                when {
                    notesSize < notes.size -> {
                        adapter.insert(notes[notes.size - 1])
                    }
                    notesSize == notes.size -> {
                        adapter.update(viewModel.targetNote!!)
                    }
                    notesSize > notes.size -> {
                        adapter.delete(viewModel.targetNote!!)
                    }
                }
            }
            notesSize = notes.size
        })
    }

    override fun onBackPressed() {
        if (isFragment) {
            when {
                isAddFragment -> {
                    addFragment.finish(AddFragment.BACK_PRESSED)
                    hideKeyboard()
                }
                isEditFragment -> if (editFragment.isContentChanged()) editFragment.showCheckMessage()
                else super.onBackPressed()
                isCameraFragment -> {
                    super.onBackPressed()
                    showFloatingActionButton()
                }
                else -> super.onBackPressed()
            }
        } else if (isAlarmFragment) super.onBackPressed()
        else {
            if (pressedTime == 0L) {
                Snackbar.make(
                    findViewById(R.id.container),
                    "한 번 더 누르면 종료됩니다.", Snackbar.LENGTH_LONG
                ).show()
                pressedTime = System.currentTimeMillis()
            } else {
                if (System.currentTimeMillis() - pressedTime > 2500L) {
                    Snackbar.make(
                        findViewById(R.id.container),
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
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                Toast.makeText(this, "Permission request granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this,
                    "카메라 권한을 승인하셔야 카메라 기능을 사용하실 수 있습니다.",
                    Toast.LENGTH_SHORT).show()
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

    fun showFloatingActionButton() {
        binding.cameraFloatingActionButton.show()
        binding.addFloatingActionButton.show()
    }

    private fun hideFloatingActionButton() {
        binding.cameraFloatingActionButton.hide()
        binding.addFloatingActionButton.hide()
    }

    private fun startAddFragment() {
        addFragment = AddFragment()
        fragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_up, R.anim.slide_down, R.anim.slide_down)
            .addToBackStack(null)
            .replace(R.id.container, addFragment).commit()
        hideFloatingActionButton()
    }

    fun startEditFragment(note: Note) {
        editFragment.setNote(note)
        fragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_up, R.anim.slide_down, R.anim.slide_down)
            .addToBackStack(null)
            .replace(R.id.container, editFragment).commit()
        hideFloatingActionButton()
    }

    private fun startCameraFragment() {
        cameraFragment = CameraFragment()
        fragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_up, R.anim.slide_down, R.anim.slide_down)
            .addToBackStack(null)
            .replace(R.id.container, cameraFragment).commit()
        hideFloatingActionButton()
    }

    /*
    fun removeCameraFragment() {
        fragmentManager.beginTransaction()
            .remove(null)
            .commit()
    }*/

    fun showKeyboard() {
        val manager =
            this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus
        if (view != null) manager.showSoftInput(view, 0)
    }

    fun hideKeyboard() {
        val manager =
            this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus
        if (view != null) manager.hideSoftInputFromWindow(view.windowToken, 0)
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

    companion object {
        const val DATABASE_NAME = "dim_cat_cam_notes"

        const val PERMISSIONS_REQUEST_CODE = 10
        val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

        var isFragment = false
        var isAddFragment = false
        var isAlarmFragment = false
        var isCameraFragment = false
        var isEditFragment = false

        private const val pattern = "yyyy-MM-dd-a-hh:mm:ss"

        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
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
    }
}
