package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.EditActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.broadcast_receivers.AlarmReceiver
import com.elliot.kim.kotlin.dimcatcamnote.broadcast_receivers.DeviceBootReceiver
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.databinding.FragmentAlarmBinding
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class AlarmFragment(private val activity: AppCompatActivity) : Fragment() {

    private lateinit var binding: FragmentAlarmBinding
    private lateinit var note: Note
    var isFromEditFragment = false

    fun setNote(note: Note) { this.note = note }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_alarm, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendar: Calendar = GregorianCalendar()
        val currentTime = calendar.time
        val currentYear = SimpleDateFormat("yyyy",
            Locale.getDefault()).format(currentTime).toInt()
        val currentMonth = SimpleDateFormat("MM",
            Locale.getDefault()).format(currentTime).toInt()
        val currentDayOfMonth = SimpleDateFormat("dd",
            Locale.getDefault()).format(currentTime).toInt()
        val currentHour = SimpleDateFormat("kk",
            Locale.getDefault()).format(currentTime).toInt()
        val currentMinute = SimpleDateFormat("mm",
            Locale.getDefault()).format(currentTime).toInt()

        binding = FragmentAlarmBinding.bind(view)
        setTimePickerTextColor(binding.timePicker)
        binding.buttonSetDate.text = String.format("%d년 %d월 %d일",
            currentYear, currentMonth, currentDayOfMonth
        )
        binding.timePicker.setIs24HourView(false)
        binding.timePicker.hour = currentHour
        binding.timePicker.minute = currentMinute
        binding.buttonSetAlarm.setOnClickListener(onClickListener)
        binding.buttonSetDate.setOnClickListener(onClickListener)
        binding.imageViewClose.setOnClickListener(onClickListener)
    }

    private val onClickListener =
        View.OnClickListener { v ->
            when (v.id) {
                R.id.button_set_date -> showDatePicker()
                R.id.button_set_alarm -> {
                    val buttonText = binding.buttonSetDate.text
                        .toString().replace("[^0-9]".toRegex(), "")
                    val year = buttonText.substring(0, 4).toInt()
                    val month = buttonText.substring(4, 5).toInt()
                    val dayOfMonth = buttonText.substring(5).toInt()
                    val hour: Int
                    val minute: Int

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        hour = binding.timePicker.hour
                        minute = binding.timePicker.minute
                    } else {
                        hour = binding.timePicker.currentHour
                        minute = binding.timePicker.currentMinute
                    }

                    val calendar: Calendar = GregorianCalendar(
                        year, month - 1, dayOfMonth,
                        hour, minute, 0
                    )

                    if (System.currentTimeMillis() < calendar.timeInMillis) {
                        setAlarm(calendar)
                        when(activity) {
                            is EditActivity -> activity.onBackPressed()
                            is MainActivity -> {
                                if (isFromEditFragment) EditFragment.setTimeText(note)
                                activity.backPressed()
                            }
                        }

                    } else
                        Toast.makeText(activity, "이미 지니간 시간입니다.", Toast.LENGTH_SHORT).show()
                }
                R.id.image_view_close -> activity.supportFragmentManager.popBackStack()
            }
        }

    override fun onResume() {
        super.onResume()
        if (activity is MainActivity)
            activity.setCurrentFragment(CurrentFragment.ALARM_FRAGMENT)
    }

    override fun onStop() {
        super.onStop()
        if (activity is MainActivity) {
            if (isFromEditFragment) {
                activity.editFragment.setContent(note)
                activity.setCurrentFragment(CurrentFragment.EDIT_FRAGMENT)
            } else activity.setCurrentFragment(null)
        }
    }

    private fun setAlarm(calendar: Calendar): Boolean {
        val alarmManager =
            requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val manager = requireActivity().packageManager
        val intent = Intent(activity, AlarmReceiver::class.java)
        val receiver = ComponentName(
            requireActivity(),
            DeviceBootReceiver::class.java
        )

        val id = note.id
        val title = note.title
        var content = note.content
        if (content.length > 16) content = content.substring(0, 16)

        intent.putExtra(KEY_NOTE_ID, id)
        intent.putExtra(KEY_NOTE_TITLE, title)
        intent.putExtra(KEY_NOTE_CONTENT, content)

        val pendingIntent = PendingIntent.getBroadcast(
            activity,
            id,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        alarmManager[AlarmManager.RTC_WAKEUP, calendar.timeInMillis] = pendingIntent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }

        saveAlarmPreferences(id, calendar.timeInMillis, title, content)

        note.alarmTime = calendar.timeInMillis

        when(activity) {
            is EditActivity -> activity.viewModel.update(note)
            is MainActivity -> activity.viewModel.update(note)
        }

        manager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        Toast.makeText(context, SimpleDateFormat(
            "yyyy년 MM월 dd일 EE요일 a hh시 mm분 ",
            Locale.getDefault()
        ).format(calendar.time) + "으로 알림이 설정되었습니다.", Toast.LENGTH_SHORT).show()

        return true
    }

    private fun saveAlarmPreferences(
        number: Int,
        alarmTime: Long,
        title: String,
        content: String
    ) {
        val sharedPreferences = activity.getSharedPreferences(
            PREFERENCES_NAME_ALARM,
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putInt("${number}0", number)
        editor.putLong("${number}1", alarmTime)
        editor.putString("${number}2", title)
        editor.putString("${number}3", content)
        editor.apply()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            activity,
            OnDateSetListener { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                binding.buttonSetDate.text =
                    String.format("%d년 %d월 %d일", year, month + 1, dayOfMonth)
            },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )
        dialog.show()
    }

    private fun setTimePickerTextColor(timePicker: TimePicker) {
        val color = ContextCompat.getColor(requireContext(),
            R.color.colorYellowfffde7
        )
        val system = Resources.getSystem()
        val hourNumberPickerId = system.getIdentifier("hour",
            "id", "android")
        val minuteNumberPickerId = system.getIdentifier("minute",
            "id", "android")
        val amPmNumberPickerId = system.getIdentifier("amPm",
            "id", "android")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            (timePicker.findViewById<View>(hourNumberPickerId) as NumberPicker).textColor = color
            (timePicker.findViewById<View>(minuteNumberPickerId) as NumberPicker).textColor = color
            (timePicker.findViewById<View>(amPmNumberPickerId) as NumberPicker).textColor = color
        } else {
            val hourNumberPicker =
                timePicker.findViewById<NumberPicker>(hourNumberPickerId)
            val minuteNumberPicker =
                timePicker.findViewById<NumberPicker>(minuteNumberPickerId)
            val amPmNumberPicker =
                timePicker.findViewById<NumberPicker>(amPmNumberPickerId)

            setNumberPickerTextColor(hourNumberPicker, color)
            setNumberPickerTextColor(minuteNumberPicker, color)
            setNumberPickerTextColor(amPmNumberPicker, color)
        }
    }

    private fun setNumberPickerTextColor(numberPicker: NumberPicker, color: Int) {
        val count = numberPicker.childCount
        for (i in 0 until count) {
            val child = numberPicker.getChildAt(i)

            try {
                val wheelPaintField =
                    numberPicker.javaClass.getDeclaredField("mSelectorWheelPaint")
                wheelPaintField.isAccessible = true
                (wheelPaintField[numberPicker] as Paint).color = color
                (child as EditText).setTextColor(color)

                numberPicker.invalidate()
            } catch (e: NoSuchFieldException ) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }
    }
}