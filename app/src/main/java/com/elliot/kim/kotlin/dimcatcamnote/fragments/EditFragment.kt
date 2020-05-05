package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.elliot.kim.kotlin.dimcatcamnote.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.Note
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.databinding.FragmentEditBinding

class EditFragment : Fragment() {

    private lateinit var binding: FragmentEditBinding
    private lateinit var modeIcon: MenuItem
    private lateinit var note: Note
    private lateinit var originContent: String

    private var isEditMode = false

    fun setNote(note: Note) {
        this.note = note
        originContent = note.content
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
    inflater.inflate(R.layout.fragment_edit, container, false)

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditBinding.bind(view)
        textViewTime = binding.textViewTime

        (activity as MainActivity).setSupportActionBar(binding.toolBar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

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

        setText(note)

        MainActivity.isFragment = true
        MainActivity.isEditFragment = true
    }

    override fun onStop() {
        super.onStop()

        MainActivity.isFragment = false
        MainActivity.isEditFragment = false

        (activity as MainActivity).showFloatingActionButton()
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

        if (note.isDone) menuDone.title = "완료해제" else menuDone.title = "완료체크"
        if (note.alarmTime == null) {
            menuAlarm.title = "알림설정"
            menuChangeAlarm.isVisible = false
        } else {
            menuAlarm.title = "알림해제"
            menuChangeAlarm.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val isContentChanged = isContentChanged()
        when (item.itemId) {
            android.R.id.home -> {
                (activity as MainActivity).hideKeyboard()

                if (isContentChanged) showCheckMessage()
                else activity?.supportFragmentManager?.popBackStack()
            }
            R.id.menu_mode_icon -> {
                if (isEditMode) {
                    item.setIcon(R.drawable.pencil)

                    binding.focusBlock.visibility = View.VISIBLE
                    binding.editTextContent.isFocusable = false

                    if (isContentChanged) {
                        note.editTime =
                            MainActivity.getCurrentTime()
                        note.content = binding.editTextContent.text.toString()
                        (activity as MainActivity).viewModel.update(note)

                        Toast.makeText(context, "노트가 수정되었습니다.", Toast.LENGTH_SHORT).show()

                        activity?.supportFragmentManager?.popBackStack()
                    } else
                        Toast.makeText(context, "변경사항이 없습니다.", Toast.LENGTH_SHORT).show()
                } else
                    getFocus()

                isEditMode = !isEditMode
            }
            R.id.menu_alarm -> if (note.alarmTime == null)
                startAlarmFragment(note)
            else {
                (activity as MainActivity).cancelAlarm(note, false)
                setTimeText(
                    note
                )
            }
            R.id.menu_change_alarm -> startAlarmFragment(note)
            R.id.menu_share -> (activity as MainActivity).share(note)
            R.id.menu_done -> {
                note.isDone = !note.isDone
                (activity as MainActivity).viewModel.update(note)
            }
            R.id.menu_delete -> {
                (activity as MainActivity).viewModel.delete(note)

                Toast.makeText(context, "노트가 삭제되었습니다.", Toast.LENGTH_SHORT).show()

                activity?.supportFragmentManager?.popBackStack()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun showCheckMessage() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("노트 수정")
        builder.setMessage("지금까지 편집한 내용을 저장하시겠습니까?")
        builder.setPositiveButton("저장") { _: DialogInterface?, _: Int ->
            (activity as MainActivity).viewModel.update(note)

            Toast.makeText(context, "노트가 수정되었습니다.", Toast.LENGTH_SHORT).show()
            //activity.originalOnBackPressed()
        }.setNeutralButton("계속쓰기"
        ) { _: DialogInterface?, _: Int -> }
        builder.setNegativeButton("아니요"
        ) { _: DialogInterface?, _: Int ->
            Toast.makeText(context, "저장되지 않았습니다.", Toast.LENGTH_SHORT).show()
            //activity.originalOnBackPressed()
        }
        builder.create()
        builder.show()
    }

    private fun getFocus() {
        //editModeItem.setIcon(R.drawable.check_mark_8c9eff_120)

        binding.focusBlock.visibility = View.GONE
        binding.editTextContent.isEnabled = true
        binding.editTextContent.requestFocus()
        binding.editTextContent.setSelection(binding.editTextContent.text.length)

        (activity as MainActivity).showKeyboard()
    }

    private fun setText(note: Note) {
        binding.toolBar.title = note.title
        binding.editTextContent.setText(note.content)
        binding.editTextContent.isEnabled = false

        setTimeText(
            note
        )
    }



    private fun startAlarmFragment(note: Note) {
        (activity as MainActivity).alarmFragment.setNote(note)
        (activity as MainActivity).supportFragmentManager.beginTransaction().addToBackStack(null)
            .setCustomAnimations(
                R.anim.slide_up,
                R.anim.slide_down
            )
            .replace(
                R.id.edit_note_container,
                (activity as MainActivity).alarmFragment).commit()
    }

    fun isContentChanged() = originContent != binding.editTextContent.text.toString()

    companion object {
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
