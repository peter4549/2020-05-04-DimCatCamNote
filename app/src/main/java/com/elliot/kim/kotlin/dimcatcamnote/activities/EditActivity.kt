package com.elliot.kim.kotlin.dimcatcamnote.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.adapters.NoteAdapter
import com.elliot.kim.kotlin.dimcatcamnote.broadcast_receivers.AlarmReceiver
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.databinding.ActivityEditBinding
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.ConfirmPasswordDialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.SetPasswordDialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.fragments.AlarmFragment
import com.elliot.kim.kotlin.dimcatcamnote.fragments.PhotoFragment
import com.elliot.kim.kotlin.dimcatcamnote.view_model.MainViewModel

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
    private var initialized = false
    private var originAlarmTime: Long? = null
    private var dataLoadingComplete = false
    private var passwordConfirmationResult = false
    private var shortAnimationDuration = 0

    private val receiver: BroadcastReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.action == ACTION_PASSWORD_CONFIRMED) {
                passwordConfirmationResult = intent.getBooleanExtra(KEY_PASSWORD_CONFIRMED, false)
                if(passwordConfirmationResult) {
                    crossFadeLockView(false)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
            IntentFilter(ACTION_PASSWORD_CONFIRMED)
        )

        fragmentManager = supportFragmentManager
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit)
        textViewTime = binding.textViewTime
        setSupportActionBar(binding.toolBar)

        setViewDesign()

        val intent = intent!!  // Get intent
        var id = 0
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

        val action = intent.action
        if (action!!.startsWith(ACTION_APP_WIDGET_ATTACHED))
            id = action.substring(ACTION_APP_WIDGET_ATTACHED.length).toInt()
        else if (action.startsWith(ACTION_ALARM_NOTIFICATION_CLICKED))
            id = action.substring(ACTION_ALARM_NOTIFICATION_CLICKED.length).toInt()

        val viewModelFactory =
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        viewModel.setContext(this)

        viewModel.getAll().observe(this, androidx.lifecycle.Observer { notes ->
            if (!initialized) {
                noteAdapter =
                    NoteAdapter(
                        this,
                        notes
                    )
                note = noteAdapter.getNoteById(id)
                noteAdapter.selectedNote = note

                // Password confirmation
                if (note.isLocked) {
                        ConfirmPasswordDialogFragment(noteAdapter, this)
                            .show(fragmentManager, tag)
                } else {
                    crossFadeLockView(false)
                }

                binding.editTextContent.setText(note.content)
                originAlarmTime = note.alarmTime
                originContent = note.content
                setText(note)
                showImage()

                if (note.uri == null) {
                    binding.imageView.visibility = View.GONE
                } else {
                    binding.editTextContent.viewTreeObserver.addOnGlobalLayoutListener {
                        if (keyboardShown(binding.editTextContent.rootView) && isEditMode)
                            crossFadeImageView(false)
                        else if (binding.imageView.visibility != View.VISIBLE)
                                showImage()
                    }
                }

                dataLoadingComplete = true
                initialized = true
            }
            // 보류.
        })

        binding.imageView.setOnClickListener { startPhotoFragment() }

        binding.focusBlock.setOnTouchListener(object : View.OnTouchListener {
            private val gestureDetector = GestureDetector(applicationContext,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        showToast("더블 탭하여 노트를 편집하세요.")
                        return super.onSingleTapUp(e)
                    }

                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        isEditMode = true
                        getFocus()
                        return super.onDoubleTap(e)
                    }
                })

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(event)
                return true
            }
        })
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun keyboardShown(rootView: View): Boolean {
        val softKeyboardHeight = 100
        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)

        val metrics = rootView.resources.displayMetrics
        val heightDiff: Int = rootView.bottom - rect.bottom

        return heightDiff > softKeyboardHeight * metrics.density
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
                MainActivity.hideKeyboard(this, binding.editTextContent)
                finish()
            }
            R.id.menu_mode_icon -> {
                if (isEditMode) {
                    item.setIcon(R.drawable.ic_edit_white_24dp)

                    binding.focusBlock.visibility = View.VISIBLE
                    binding.editTextContent.isEnabled = false

                    if (isContentChanged()) finishWithSaving()
                    else showToast("변경사항이 없습니다.")
                    MainActivity.hideKeyboard(this, binding.editTextContent)
                } else getFocus()

                isEditMode = !isEditMode
            }
            R.id.menu_alarm -> {
                if (note.alarmTime == null)
                    startAlarmFragment(note)
                else {
                    cancelAlarm(note)
                    // Not required for EditActivity
                    // EditFragment.setTimeText(note)
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
        SetPasswordDialogFragment(noteAdapter).show(fragmentManager, tag)
    }

    private fun unlock() {
        note.isLocked = false
        note.password = ""
        viewModel.update(note)
        showToast("잠금이 해제되었습니다.")
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
            .setCustomAnimations(R.anim.anim_slide_in_left_enter,
                R.anim.anim_slide_in_left_exit,
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
        modeIcon.setIcon(R.drawable.ic_done_white_24dp)

        binding.focusBlock.visibility = View.GONE
        binding.editTextContent.isEnabled = true
        binding.editTextContent.requestFocus()
        binding.editTextContent.setSelection(binding.editTextContent.text.length)

        MainActivity.showKeyboard(this, binding.editTextContent)
    }

    private fun isContentChanged() = originContent != binding.editTextContent.text.toString()

    private fun cancelAlarm(note: Note) {
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

        note.alarmTime = null
        viewModel.update(note)

        Toast.makeText(this, "알림이 해제되었습니다.", Toast.LENGTH_SHORT).show()
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

    private fun showImage() {
        if(note.uri == null) return
        else crossFadeImageView(true)
    }

    private fun crossFadeImageView(fadeIn: Boolean) {
        if (fadeIn) {
            binding.imageView.apply {
                alpha = 0F
                visibility = View.VISIBLE

                animate()
                    .alpha(1F)
                    .setDuration(shortAnimationDuration.toLong())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator) {
                            Glide.with(binding.imageView.context)
                                .load(Uri.parse(note.uri))
                                .into(binding.imageView)
                        }
                    })
            }
        } else {
            binding.imageView.animate()
                .alpha(0F)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.imageView.visibility = View.GONE
                    }
                })
        }
    }

    private fun setViewDesign() {
        val preferences = getSharedPreferences(
            PREFERENCES_SET_COLOR,
            Context.MODE_PRIVATE
        )

        binding.toolBar.setBackgroundColor(preferences.getInt(KEY_COLOR_TOOLBAR,
            getColor(R.color.defaultColorToolbar)))
        binding.textViewTime.setBackgroundColor(preferences.getInt(KEY_COLOR_BACKGROUND,
            getColor(R.color.defaultColorBackground)))
        /*
        binding.editTextContainer.setBackgroundColor(preferences.getInt(KEY_COLOR_BACKGROUND,
            getColor(R.color.defaultColorBackground)))
         */
        binding.viewLock.setBackgroundColor(preferences.getInt(KEY_COLOR_BACKGROUND,
            getColor(R.color.defaultColorBackground)))
    }

    private fun crossFadeLockView(fadeIn: Boolean) {
        if (fadeIn) {
            binding.viewLock.apply {
                alpha = 0F
                visibility = View.VISIBLE

                animate()
                    .alpha(1F)
                    .setDuration(shortAnimationDuration.toLong())
                    .setListener(null)
            }
        } else {
            binding.viewLock.animate()
                .alpha(0F)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.viewLock.visibility = View.GONE
                    }
                })
        }
    }


    companion object {
        const val SAVE = 0
        const val BACK_PRESSED = 1

        const val ACTION_PASSWORD_CONFIRMED = "action_password_confirmed"

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