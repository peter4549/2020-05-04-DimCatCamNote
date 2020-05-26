package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.databinding.FragmentEditBinding

class EditFragment() : Fragment() {

    private lateinit var activity: MainActivity
    private lateinit var binding: FragmentEditBinding
    private lateinit var modeIcon: MenuItem
    private lateinit var note: Note
    private lateinit var originContent: String
    private lateinit var viewModel: MainViewModel
    private var isEditMode = false
    private var originAlarmTime: Long? = null

    fun setNote(note: Note) {
        this.note = note
        originAlarmTime = note.alarmTime
        originContent = note.content
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity = requireActivity() as MainActivity
        viewModel = activity.viewModel

        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditBinding.bind(view)
        textViewTime = binding.textViewTime

        activity.setSupportActionBar(binding.toolBar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        binding.focusBlock.setOnTouchListener(object : OnTouchListener {
            private val gestureDetector = GestureDetector(activity,
                object : SimpleOnGestureListener() {
                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        Toast.makeText(activity, "더블 탭하여 노트를 편집하세요.", Toast.LENGTH_SHORT).show()
                        return super.onSingleTapUp(e)
                    }

                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        Toast.makeText(activity, "노트를 편집하세요.", Toast.LENGTH_SHORT).show()
                        getFocus()
                        return super.onDoubleTap(e)
                    }
                })

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(event)
                return true
            }
        })
    }

    override fun onResume() {
        super.onResume()
        setContent(note)
        activity.setCurrentFragment(CurrentFragment.EDIT_FRAGMENT)
    }

    override fun onStop() {
        super.onStop()
        activity.setCurrentFragment(null)
        activity.showFloatingActionButton()
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
                MainActivity.hideKeyboard(context, view)
                finish(BACK_PRESSED)
            }
            R.id.menu_mode_icon -> {
                if (isEditMode) {
                    item.setIcon(R.drawable.pencil)

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
                    activity.cancelAlarm(note, false)
                    setTimeText(note)
                }
            }
            R.id.menu_change_alarm -> startAlarmFragment(note)
            R.id.menu_share -> activity.share(note)
            R.id.menu_done -> {
                note.isDone = !note.isDone
                viewModel.update(note)
            }
            R.id.menu_lock -> {
                if (note.isLocked) {
                    unlock()
                } else {
                    lock()
                }
            }
            R.id.menu_delete -> {
                activity.closeOptionsMenu()
                activity.viewModel.delete(note)

                Toast.makeText(context, "노트가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                activity.backPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun lock() {
        activity.showDialog(DialogManager.Companion.DialogType.SET_PASSWORD)
    }

    private fun unlock() {
        note.isLocked = false
        note.password = ""
        activity.viewModel.update(note)
        activity.showToast("잠금이 해제되었습니다.")
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
        activity.alarmFragment.isFromEditFragment = true
        activity.alarmFragment.note = note
        activity.fragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_up, R.anim.slide_down, R.anim.slide_down)
            .replace(R.id.edit_note_container,
                activity.alarmFragment).commit()
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
        Toast.makeText(context, "변경사항이 없습니다.", Toast.LENGTH_SHORT).show()
        activity.backPressed()
    }

    private fun getFocus() {
        modeIcon.setIcon(R.drawable.check_mark)

        binding.focusBlock.visibility = View.GONE
        binding.editTextContent.isEnabled = true
        binding.editTextContent.requestFocus()
        binding.editTextContent.setSelection(binding.editTextContent.text.length)

        MainActivity.showKeyboard(context, binding.editTextContent)
    }

    private fun isContentChanged() = originContent != binding.editTextContent.text.toString()

    companion object {
        const val SAVE = 0
        const val BACK_PRESSED = 1

        lateinit var textViewTime: TextView

        fun setTimeText(note: Note) {
            var timeText = "최초 작성일: " + MainActivity.timeToString(
                note.creationTime
            )

            if (note.editTime != null) timeText += "\n최근 수정일: ${MainActivity.timeToString(
                note.editTime
            )}"

            if (note.alarmTime != null) timeText += "\n알림 시간: ${MainActivity.timeToString(
                note.alarmTime
            )}"

            textViewTime.text = timeText
        }
    }
}
