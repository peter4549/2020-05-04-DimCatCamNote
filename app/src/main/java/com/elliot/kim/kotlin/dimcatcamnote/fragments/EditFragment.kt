package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.elliot.kim.kotlin.dimcatcamnote.CurrentFragment
import com.elliot.kim.kotlin.dimcatcamnote.PATTERN_UP_TO_SECONDS
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.databinding.FragmentEditBinding
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.DialogFragments
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.SetPasswordDialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.view_model.MainViewModel

class EditFragment(private val activity: MainActivity) : Fragment() {

    private lateinit var binding: FragmentEditBinding
    private lateinit var modeIcon: MenuItem
    private lateinit var note: Note
    private lateinit var originContent: String
    private lateinit var viewModel: MainViewModel
    private var isEditMode = false
    private var originAlarmTime: Long? = null
    private var shortAnimationDuration = 0
    private var uri: String? = null
    var isFromAlarmedNoteSelectionFragment = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = activity.viewModel
        init()
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    private fun init() {
        originAlarmTime = note.alarmTime
        originContent = note.content
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
    }

    fun setNote(note: Note) {
        this.note = note
        originAlarmTime = note.alarmTime
        originContent = note.content
        uri = note.uri
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditBinding.bind(view)
        textViewTime = binding.textViewTime

        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        // Apply the design.
        binding.toolbar.setTitleTextAppearance(activity, MainActivity.fontStyleId)
        binding.textViewTime.typeface = MainActivity.font
        binding.editTextContent.typeface = MainActivity.font

        showImage()

        binding.imageView.setOnClickListener { startPhotoFragment() }

        binding.focusBlock.setOnTouchListener(object : OnTouchListener {
            private val gestureDetector = GestureDetector(activity,
                object : SimpleOnGestureListener() {
                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        activity.showToast("더블 탭하여 노트를 편집하세요.")
                        return super.onSingleTapUp(e)
                    }

                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        isEditMode = true
                        getFocus()
                        return super.onDoubleTap(e)
                    }
                })

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(event)
                return true
            }
        })

        binding.editTextContent.viewTreeObserver.addOnGlobalLayoutListener {
            if (keyboardShown(binding.editTextContent.rootView) && isEditMode) crossFadeImageView(false)
            else if (binding.imageView.visibility != View.VISIBLE)
                showImage()
        }
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {

        val animation = AnimationUtils.loadAnimation(activity, nextAnim)

        animation!!.setAnimationListener( object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                MainActivity.hideKeyboard(context, view)
            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })

        return animation
    }

    override fun onResume() {
        super.onResume()
        setViewDesign()
        uri = note.uri
        setContent(note)
        activity.setCurrentFragment(CurrentFragment.EDIT_FRAGMENT)
    }

    override fun onStop() {
        super.onStop()
        isEditMode = false

        if (isFromAlarmedNoteSelectionFragment)
            activity.setCurrentFragment(CurrentFragment.CALENDAR_FRAGMENT)
        else {
            activity.setCurrentFragment(null)
            activity.showFloatingActionButton()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_edit, menu)
        modeIcon = menu.findItem(R.id.menu_mode_icon)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val menuDone = menu.findItem(R.id.menu_done)
        val menuAlarm = menu.findItem(R.id.menu_alarm)
        val menuChangeAlarm = menu.findItem(R.id.menu_change_alarm)
        val menuLock = menu.findItem(R.id.menu_lock)

        if (note.isDone) menuDone.title = "완료해제" else menuDone.title = "완료체크"

        if (note.alarmTime == null) {
            menuAlarm.title = "알림설정"
            menuChangeAlarm.isVisible = false
        } else {
            menuAlarm.title = "알림해제"
            menuChangeAlarm.isVisible = true
        }

        if (note.isLocked) menuLock.title = "잠금해제" else menuLock.title = "잠금설정"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish(BACK_PRESSED)
            }
            R.id.menu_mode_icon -> {
                if (isEditMode) {
                    item.setIcon(R.drawable.ic_edit_white_24dp)

                    binding.focusBlock.visibility = View.VISIBLE
                    binding.editTextContent.isFocusable = false

                    if (isContentChanged()) finishWithSaving()
                    else Toast.makeText(context, "변경사항이 없습니다.", Toast.LENGTH_SHORT).show()
                } else getFocus()

                isEditMode = !isEditMode
            }
            R.id.menu_alarm -> {
                if (note.alarmTime == null)
                    startAlarmFragment(note)
                else {
                    activity.cancelAlarm(note, isDelete = false, isByUser = true)
                    setTimeText(note)
                }
            }
            R.id.menu_change_alarm -> startAlarmFragment(note)
            R.id.menu_share -> MainActivity.share(activity, note)
            R.id.menu_done -> {
                note.isDone = !note.isDone
                viewModel.update(note)
            }
            R.id.menu_lock -> {
                if (note.isLocked) unlock()
                else lock()
            }
            R.id.menu_delete -> {
                activity.closeOptionsMenu()
                activity.showDialogFragment(DialogFragments.CONFIRM_DELETE)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun lock() {
        SetPasswordDialogFragment(activity.getNoteAdapter()).show(activity.fragmentManager, tag)
    }

    private fun unlock() {
        note.isLocked = false
        note.password = ""
        activity.viewModel.update(note)
        activity.showToast("잠금이 해제되었습니다.")
    }

    fun setContent(note: Note) {
        setText(note)
    }

    private fun setText(note: Note) {
        binding.toolbar.title = note.title
        binding.editTextContent.setText(note.content)
        binding.editTextContent.isEnabled = false
        setTimeText(note)
    }

    private fun setViewDesign() {
        binding.toolbar.setBackgroundColor(MainActivity.toolbarColor)
        binding.textViewTime.setBackgroundColor(MainActivity.inlayColor)
        binding.editTextContent.setBackgroundColor(MainActivity.inlayColor)
    }

    private fun startAlarmFragment(note: Note) {
        activity.alarmFragment.isFromEditFragment = true
        activity.alarmFragment.setNote(note)
        activity.fragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_up, R.anim.slide_down, R.anim.slide_down)
            .replace(R.id.edit_note_container,
                activity.alarmFragment).commit()
    }

    private fun startPhotoFragment() {
        activity.fragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(R.anim.anim_slide_in_left_enter,
                R.anim.anim_slide_in_left_exit,
                R.anim.anim_slide_down_pop_enter,
                R.anim.anim_slide_down_pop_exit)
            .replace(R.id.edit_note_container, PhotoFragment(this, note.uri!!)).commit()
    }

    private fun showCheckMessage() {
        val builder = AlertDialog.Builder(requireContext())
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

    fun finish(action: Int) {
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
        activity.viewModel.update(note)

        Toast.makeText(context, "노트가 수정되었습니다.", Toast.LENGTH_SHORT).show()
        activity.backPressed()
    }

    private fun finishWithoutSaving() {
        Toast.makeText(context, "변경된 내용이 없습니다.", Toast.LENGTH_SHORT).show()
        activity.backPressed()
    }

    private fun getFocus() {
        modeIcon.setIcon(R.drawable.ic_done_white_24dp)

        binding.focusBlock.visibility = View.GONE
        binding.editTextContent.isEnabled = true
        binding.editTextContent.requestFocus()
        binding.editTextContent.setSelection(binding.editTextContent.text.length)

        MainActivity.showKeyboard(context, binding.editTextContent)
    }

    private fun isContentChanged() = originContent != binding.editTextContent.text.toString()

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

    private fun keyboardShown(rootView: View): Boolean {
        val softKeyboardHeight = 100
        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)

        val metrics = rootView.resources.displayMetrics
        val heightDiff: Int = rootView.bottom - rect.bottom

        return heightDiff > softKeyboardHeight * metrics.density
    }

    private fun showImage() {
        if(uri == null) return
        else crossFadeImageView(true)
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
