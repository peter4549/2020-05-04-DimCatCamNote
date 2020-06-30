package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.Button
import android.widget.DatePicker
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.EditActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import java.text.SimpleDateFormat
import java.util.*

class AddToCalendarDialogFragment(private val note: Note) : DialogFragment() {

    private lateinit var setDateButton: Button
    private var year = 0
    private var month = 0
    private var dayOfMonth = 0
    private var hourOfDay = 0
    private var minute = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_fragment_add_to_calendar)

        val container = dialog.findViewById<RelativeLayout>(R.id.add_to_calendar_container)
        val textViewTitle = dialog.findViewById<TextView>(R.id.text_view_title)
        val registerButton = dialog.findViewById<Button>(R.id.button_register)
        setDateButton = dialog.findViewById(R.id.button_set_date)


        var toolbarColor = 0
        var backgroundColor = 0
        var font: Typeface? = null
        var fontId = 0

        if (activity is MainActivity) {
            toolbarColor = MainActivity.toolbarColor
            backgroundColor = MainActivity.backgroundColor
            font = MainActivity.font
            fontId = MainActivity.fontId
        } else if (activity is EditActivity) {
            toolbarColor = EditActivity.toolbarColor
            backgroundColor = EditActivity.backgroundColor
            font = EditActivity.font
            fontId = EditActivity.fontId
        }

        container.setBackgroundColor(backgroundColor)
        textViewTitle.setBackgroundColor(toolbarColor)
        registerButton.setBackgroundColor(toolbarColor)
        setDateButton.setBackgroundColor(toolbarColor)

        textViewTitle.adjustDialogTitleTextSize(fontId)
        registerButton.adjustDialogButtonTextSize(fontId)
        setDateButton.adjustDialogButtonTextSize(fontId)

        textViewTitle.typeface = font
        registerButton.typeface = font
        setDateButton.typeface = font

        initializeButtonText(setDateButton)

        registerButton.setOnClickListener {
            addToCalendar()
            dialog.dismiss()
        }

        setDateButton.setOnClickListener { showDatePicker() }

        return dialog
    }

    private fun showDatePicker() {

        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _: DatePicker?, year: Int,
                                                 month: Int, dayOfMonth: Int ->
                setButtonText(setDateButton,
                    String.format(dateToText(year, month, dayOfMonth)))
                setTime(year, month, dayOfMonth)
            },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )

        dialog.show()
    }

    private fun setButtonText(button: Button, text: String) { button.text = text }

    private fun setTime(year: Int, month: Int, dayOfMonth: Int,
                        hourOfDay: Int = 8, minute: Int = 0) {
        this.year = year
        this.month = month
        this.dayOfMonth = dayOfMonth
        this.hourOfDay = hourOfDay
        this.minute = minute
    }

    private fun initializeButtonText(button: Button) {
        if (note.alarmTime == null) initializeButtonTextToCurrentDate(button)
        else initializeButtonTextToAlarmTime(button)
    }

    private fun initializeButtonTextToAlarmTime(button: Button) {

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

        button.text = SimpleDateFormat(PATTERN_YYYY_MM_dd, Locale.getDefault())
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

        button.text = SimpleDateFormat(PATTERN_YYYY_MM_dd, Locale.getDefault())
            .format(currentTime)

        setTime(currentYear, currentMonth - 1, currentDayOfMonth)
    }

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

        if (intent.resolveActivity(requireContext().packageManager) != null)
            requireContext().startActivity(intent)
    }
}