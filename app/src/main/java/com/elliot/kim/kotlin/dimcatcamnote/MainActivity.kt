package com.elliot.kim.kotlin.dimcatcamnote

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.elliot.kim.kotlin.dimcatcamnote.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

const val KEY_EVENT_ACTION = "key_event_action"
const val KEY_EVENT_EXTRA = "key_event_extra"

class MainActivity : AppCompatActivity(), LifecycleOwner {

    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: MainViewModel
    private lateinit var adapter: NoteAdapter
    var pressedTime = 0L

    private val addFragment = AddFragment()
    private val editFragment = EditFragment()
    val alarmFragment = AlarmFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.cameraFloatingActionButton.setOnClickListener {
        }
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
                isAddFragment -> addFragment.finish(AddFragment.BACK_PRESSED)
                isAlarmFragment -> ""
                isEditFragment -> if (editFragment.isContentChanged()) editFragment.showCheckMessage()
                else super.onBackPressed()
                else -> super.onBackPressed()
            }
        }
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
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_up, R.anim.slide_down, R.anim.slide_down)
            .addToBackStack(null)
            .replace(R.id.container, addFragment).commit()
        hideFloatingActionButton()
    }

    fun startEditFragment(note: Note) {
        editFragment.setNote(note)
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_up, R.anim.slide_down, R.anim.slide_down)
            .addToBackStack(null)
            .replace(R.id.container, editFragment).commit()
        hideFloatingActionButton()
    }

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
        if (view != null) manager.hideSoftInputFromWindow(view.windowToken, 0);
    }

    companion object {

        private var initialization = true

        var isFragment = false
        var isAddFragment = false
        var isAlarmFragment = false
        var isEditFragment = false

        private const val pattern = "yyyy-MM-dd-a-hh:mm:ss"

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
