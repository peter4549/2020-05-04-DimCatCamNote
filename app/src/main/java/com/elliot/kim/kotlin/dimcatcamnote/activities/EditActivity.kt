package com.elliot.kim.kotlin.dimcatcamnote.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.broadcast_receivers.AlarmReceiver
import com.elliot.kim.kotlin.dimcatcamnote.databinding.ActivityEditBinding
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.PasswordSettingDialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.fragments.AlarmFragment
import com.elliot.kim.kotlin.dimcatcamnote.fragments.EditFragment
import com.elliot.kim.kotlin.dimcatcamnote.fragments.PhotoFragment

class EditActivity: AppCompatActivity() {

    lateinit var fragmentManager: FragmentManager
    lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityEditBinding
    private lateinit var modeIcon: MenuItem
    private lateinit var note: Note
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var originContent: String
    private lateinit var rootView: View
    private val alarmFragment = AlarmFragment(this)
    private val tag = "EditActivity"
    private var isEditMode = false
    private var originAlarmTime: Long? = null
    private var dataLoadingComplete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 패스워드 체크 로직. 필요하면 여기서 추가..

        val intent = intent
        var id = 0

        if (intent != null) {
            val action = intent.action
            if (action!!.startsWith(ACTION_APP_WIDGET_ATTACHED)) {
                id = action!!.substring(ACTION_APP_WIDGET_ATTACHED.length).toInt()
                Log.d("ISIN", id.toString())

            }
        }
        Log.d("OUT", id.toString())

        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit)
        fragmentManager = supportFragmentManager
        rootView = binding.editNoteContainer.rootView

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //binding.toolBar.visibility = View.GONE

        val viewModelFactory =
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        viewModel.setContext(this)

        viewModel.getAll().observe(this, androidx.lifecycle.Observer { notes ->
            noteAdapter = NoteAdapter(this, notes)
            note = noteAdapter.getNoteById(id)
            binding.editTextContent.setText(note.content)
            originAlarmTime = note.alarmTime
            originContent = note.content



            dataLoadingComplete = true
            // 보류.
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)

        modeIcon = menu.findItem(R.id.menu_mode_icon)

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val menuDone = menu.findItem(R.id.menu_done)
        val menuAlarm = menu.findItem(R.id.menu_alarm)
        val menuChangeAlarm = menu.findItem(R.id.menu_change_alarm)
        val menuLock = menu.findItem(R.id.menu_lock)

        if (dataLoadingComplete) {
            binding.toolBar.title = note.title
            if (note.isDone) menuDone.title = "완료해제" else menuDone.title = "완료체크"

            if (note.alarmTime == null) {
                menuAlarm.title = "알림설정"
                menuChangeAlarm.isVisible = false
            } else {
                menuAlarm.title = "알림해제"
                menuChangeAlarm.isVisible = true
            }

            if (note.isLocked) menuLock.title = "잠금해제" else menuLock.title = "잠금설정"

            binding.toolBar.invalidate()
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                MainActivity.hideKeyboard(this, rootView)
                finish()
            }
            R.id.menu_mode_icon -> {
                if (isEditMode) {
                    item.setIcon(R.drawable.pencil)

                    binding.focusBlock.visibility = View.VISIBLE
                    binding.editTextContent.isFocusable = false

                    if (isContentChanged()) finishWithSaving()
                    else showToast("변경사항이 없습니다.")
                } else getFocus()

                isEditMode = !isEditMode
            }
            R.id.menu_alarm -> {
                if (note.alarmTime == null)
                    startAlarmFragment(note)
                else {
                    cancelAlarm(note, false)
                    EditFragment.setTimeText(note)
                }
            }
            R.id.menu_change_alarm -> startAlarmFragment(note)
            R.id.menu_share -> MainActivity.share(this, note)
            R.id.menu_done -> {
                note.isDone = !note.isDone
                viewModel.update(note)
            }
            R.id.menu_lock -> {
                if (note.isLocked) unlock()
                else lock()
            }
            R.id.menu_delete -> {
                closeOptionsMenu()
                viewModel.delete(note)

                showToast("노트가 삭제되었습니다.")
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun lock() {
        PasswordSettingDialogFragment(noteAdapter).show(fragmentManager, tag)
    }

    private fun unlock() {
        note.isLocked = false
        note.password = ""
        viewModel.update(note)
        showToast("잠금이 해제되었습니다.")
    }

    fun setContent(note: Note) {
        setText(note)
        if (note.uri != null) {
            binding.imageView.visibility = View.VISIBLE
            Glide.with(this)
                .load(Uri.parse(note.uri))
                .into(binding.imageView)
        } else binding.imageView.visibility = View.GONE
    }

    private fun setText(note: Note) {
        binding.toolBar.title = note.title
        binding.editTextContent.setText(note.content)
        binding.editTextContent.isEnabled = false
        setTimeText(note)
    }


    private fun startAlarmFragment(note: Note) {
        alarmFragment.isFromEditFragment = false
        alarmFragment.setNote(note)
        fragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_up, R.anim.slide_down, R.anim.slide_down)
            .replace(R.id.edit_note_container,
                alarmFragment).commit()
    }

    private fun startPhotoFragment() {
        fragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(R.anim.anim_slide_up_enter,
                R.anim.anim_slide_up_exit,
                R.anim.anim_slide_down_pop_enter,
                R.anim.anim_slide_down_pop_exit)
            .replace(R.id.edit_note_container, PhotoFragment(this, note.uri!!)).commit()
    }

    private fun showCheckMessage() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("노트 수정")
        builder.setMessage("지금까지 편집한 내용을 저장하시겠습니까?")
        builder.setPositiveButton("저장") { _: DialogInterface?, _: Int ->
            finish(SAVE)
        }.setNeutralButton("계속쓰기"
        ) { _: DialogInterface?, _: Int -> }
        builder.setNegativeButton("아니요"
        ) { _: DialogInterface?, _: Int ->
            finishWithoutSaving()
        }
        builder.create()
        builder.show()
    }

    // 재정의... 아니면 opStop 에서 구현해도 될듯.

    private fun finish(action: Int) {
        if (isContentChanged()) {
            when (action) {
                SAVE -> finishWithSaving()
                BACK_PRESSED -> showCheckMessage()
            }
        }
        else finishWithoutSaving()
    }

    private fun finishWithSaving() {
        note.editTime =  MainActivity.getCurrentTime()
        note.content = binding.editTextContent.text.toString()
        viewModel.update(note)

        showToast("노트가 수정되었습니다.")
        finish()
    }

    private fun finishWithoutSaving() {
        showToast("변경된 내용이 없습니다.")
        finish()
    }

    private fun getFocus() {
        modeIcon.setIcon(R.drawable.check_mark)

        binding.focusBlock.visibility = View.GONE
        binding.editTextContent.isEnabled = true
        binding.editTextContent.requestFocus()
        binding.editTextContent.setSelection(binding.editTextContent.text.length)

        MainActivity.showKeyboard(this, binding.editTextContent)
    }

    private fun isContentChanged() = originContent != binding.editTextContent.text.toString()

    private fun cancelAlarm(note: Note, isDelete: Boolean) {
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

    private fun showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, text, duration).show()
    }

    companion object {
        const val SAVE = 0
        const val BACK_PRESSED = 1

        lateinit var textViewTime: TextView

        fun setTimeText(note: Note) {
            var timeText = "최초 작성일: " + MainActivity.longTimeToString(
                note.creationTime, PATTERN_UP_TO_SECONDS
            )

            if (note.editTime != null) timeText += "\n최근 수정일: ${MainActivity.longTimeToString(
                note.editTime, PATTERN_UP_TO_SECONDS
            )}"

            if (note.alarmTime != null) timeText += "\n알림 시간: ${MainActivity.longTimeToString(
                note.alarmTime, PATTERN_UP_TO_SECONDS
            )}"

            textViewTime.text = timeText
        }
    }

}