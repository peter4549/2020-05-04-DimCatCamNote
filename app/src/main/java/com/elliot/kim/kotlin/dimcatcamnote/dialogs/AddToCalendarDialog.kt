package com.elliot.kim.kotlin.dimcatcamnote.dialogs

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Button
import android.widget.DatePicker
import com.elliot.kim.kotlin.dimcatcamnote.Note
import com.elliot.kim.kotlin.dimcatcamnote.R
import java.text.SimpleDateFormat
import java.util.*

private const val BUTTON_TEXT_PATTERN = "yyyy년 MM월 dd일"

// 이 클래스를 다이얼로그 매니저한테 던져주는 형식으로.
class AddToCalendarDialog(private val activity: Activity,
                          private val note: Note
): CustomDialog(activity) {
    private val TAG = "AddToCalendarDialog"

    private val dialog = Dialog(activity)
    private var year = 0
    private var month = 0
    private var dayOfMonth = 0
    private var hourOfDay = 0
    private var minute = 0
    //private lateinit var registerButton: Button
    private lateinit var setDateButton: Button

    override fun show() {
        super.show()
        dialog.show()
    }

    override fun buildDialog() {
        dialog.setContentView(R.layout.dialog_add_to_calendar)

        val registerButton = dialog.findViewById<Button>(R.id.button_register)
        setDateButton = dialog.findViewById<Button>(R.id.button_set_date)
        initializeButtonText(setDateButton)

        registerButton.setOnClickListener {
            addToCalendar()
            dialog.dismiss()
        }

        setDateButton.setOnClickListener {
            showDatePicker()
        }
    }

    private fun setButtonText(button: Button, text: String) { button.text = text }

    private fun initializeButtonText(button: Button) {
        if (note.alarmTime == null) initializeButtonTextToCurrentDate(button)
        else initializeButtonTextToAlarmTime(button)
    }

    private fun initializeButtonTextToAlarmTime(button: Button) {


        // 유틸 클래스 만들고 아래 함수, 패턴 적용 받아서 쓰는 방식으로.
        // 메인 액티비티 함수임.
        /*
        fun timeToString(time: Long?): String = SimpleDateFormat(BUTTON_TEXT_PATTERN, Locale.getDefault()).
        format(time ?: 0L)

         */
        val alarmTime = note.alarmTime
        val alarmYear = SimpleDateFormat("yyyy",
            Locale.getDefault()).format(alarmTime).toInt()
        val alarmMonth = SimpleDateFormat("MM",
            Locale.getDefault()).format(alarmTime).toInt()
        val alarmDayOfMonth = SimpleDateFormat("dd",
            Locale.getDefault()).format(alarmTime).toInt()
        val alarmHourOfDay = SimpleDateFormat("hh",
            Locale.getDefault()).format(alarmTime).toInt()
        val alarmMinute = SimpleDateFormat("mm",
            Locale.getDefault()).format(alarmTime).toInt()

        button.text = SimpleDateFormat(BUTTON_TEXT_PATTERN, Locale.getDefault())
            .format(alarmTime ?: 0L)

        setTime(alarmYear, alarmMonth - 1, alarmDayOfMonth, alarmHourOfDay, alarmMinute)
    }

    private fun initializeButtonTextToCurrentDate(button: Button) {
        val calendar: Calendar = GregorianCalendar()
        val currentTime = calendar.time
        val currentYear = SimpleDateFormat("yyyy",
            Locale.getDefault()).format(currentTime).toInt()
        val currentMonth = SimpleDateFormat("MM",
            Locale.getDefault()).format(currentTime).toInt()
        val currentDayOfMonth = SimpleDateFormat("dd",
            Locale.getDefault()).format(currentTime).toInt()

        button.text = String.format("${currentYear}년 ${currentMonth}월 ${currentDayOfMonth}일")
        //SimpleDateFormat(BUTTON_TEXT_PATTERN, Locale.getDefault()).format(currentTime, currentDayOfMonth, currentDayOfMonth)
        /* 이런형태로 정의하기? 적당한 이름 찾아서 하는것도 갠춘할듯.
        button.text = String.format(BUTTON_TEXT_PATTERN, )
        binding.buttonSetDate.text = String.format("%d년 %d월 %d일",
        currentYear, currentMonth, currentDayOfMonth
        )

         */

        setTime(currentYear, currentMonth - 1, currentDayOfMonth)
    }



    /*
    private fun show() {

        dialog.setContentView(R.layout.dialog_folder_options)
        val spinner = dialog.findViewById<Spinner>(R.id.spinner)
        setSpinner(spinner, folderManager.folders)
        dialog.findViewById<Button>(R.id.button_move).setOnClickListener {
            val folder = folderManager.getFolderByName(spinner.selectedItem as String)
            folderManager.moveNoteToFolder(noteAdapter.selectedNote, folder)
            (context as MainActivity).showToast("${noteAdapter.selectedNote!!.title}--폴더로 이동하였습니다.")
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.button_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

     */

    // story
    // 캘린더에 등록하기 => 얘를 부른다.
    // 어떤 값을 받을 것인가.
    // 날짜만? 받고 기본 세팅하는 것으로,
    // or 알림 설정되있는 경우. 알림 설정된 시간을 기본 시간으로. 해서 전달하기. ??
    // 알림이 있다면 어디 시점에서, 맨 처음 켜질 때 물어볼 것.
    // => show dialog => 메시지 하나 띄우기. 알림이 설정된 노트임니다. 해당 날짜 시간으로 캘린더에 등록할래?
    // yes or no.
    // 1. 단순 입력부터 구현.

    private fun addToCalendar() {
        val startMillis: Long = Calendar.getInstance().run {
            set(year, month, dayOfMonth, hourOfDay, minute)
            timeInMillis
        }

        val endMillis: Long = Calendar.getInstance().run {
            set(year, month, dayOfMonth, hourOfDay + 1, minute)
            timeInMillis
        }

        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            .putExtra(CalendarContract.Events.TITLE, note.title)
            .putExtra(CalendarContract.Events.DESCRIPTION, note.content)
            .putExtra(CalendarContract.Events.EVENT_LOCATION, "") // 차후 장소저장기능 넣으면 할당.
            .putExtra(
                CalendarContract.Events.AVAILABILITY,
                CalendarContract.Events.AVAILABILITY_BUSY
            )
            .putExtra(Intent.EXTRA_EMAIL, "") // 이메일. 차후 기능에 따라 ...
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    // 버튼 텍스트 읽는 방식 채용??
    // 아니면 언제마다 불려야되나.
    // 초기화 할떄 불리고,
    private fun setTime(year: Int, month: Int, dayOfMonth: Int,
                        hourOfDay: Int = 8, minute: Int = 0) {
        this.year = year
        this.month = month
        this.dayOfMonth = dayOfMonth
        this.hourOfDay = hourOfDay
        this.minute = minute
    }

    // 이 함수도 유틸 클래스로 돌리는게 맞는 듯..

}