package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.RadioGroup
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.databinding.DataBindingUtil.bind
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.OrientationHelper
import com.elliot.kim.kotlin.dimcatcamnote.PATTERN_YYYY_MM_dd
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.adapter.CalendarAdapter
import com.elliot.kim.kotlin.dimcatcamnote.databinding.FragmentCalendarBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private lateinit var binding: FragmentCalendarBinding
    private var currentTime = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        currentTime = MainActivity.getCurrentTime()

        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCalendarBinding.bind(view)

        var currentYear = SimpleDateFormat("yyyy",
            Locale.getDefault()).format(currentTime).toInt()
        var currentMonth = SimpleDateFormat("MM",
            Locale.getDefault()).format(currentTime).toInt()
        var currentDayOfMonth = SimpleDateFormat("dd",
            Locale.getDefault()).format(currentTime).toInt()

        //
        currentMonth--

        val dateFormat = SimpleDateFormat(PATTERN_YYYY_MM_dd, Locale.getDefault())
        val calendar = Calendar.getInstance()
        val stringCurrentMonth = if (currentMonth + 1 > 9) "${currentMonth + 1}"
        else "0${currentMonth + 1}"

        val kkk = MainActivity.longTimeToString(currentTime, PATTERN_YYYY_MM_dd)
        Log.d("KKKKK", kkk)

        calendar.time = dateFormat.parse("${currentYear}년 ${stringCurrentMonth}월 01일")!!

        // UI

        // set title
        binding.calendarHeader.text =
            "${currentYear}년 ${currentMonth + 1}월" // 변수화 하라는 듯.

        var cal = CalendarAdapter(requireContext(), R.layout.calendar_view_row, calendar)
        binding.gridView.adapter = cal // 여기가 그리드 뷰 업데이트 부분. 고칠 필요 없음.

        binding.buttonNextMonth.setOnClickListener {
            if (currentMonth > 10) {
                currentMonth = 0
                calendar.set(++currentYear, currentMonth, 1)
            }
            else
                calendar.set(currentYear, ++currentMonth, 1)

            Log.d("TIME", MainActivity.longTimeToString(calendar.timeInMillis, PATTERN_YYYY_MM_dd))


            cal = CalendarAdapter(requireContext(), R.layout.calendar_view_row, calendar)
            binding.gridView.adapter = cal
            binding.calendarHeader.text =
                "${currentYear}년 ${currentMonth + 1}월"
        }

        binding.buttonPreviousMonth.setOnClickListener {
            if (currentMonth < 1) {
                currentMonth = 11
                calendar.set(--currentYear, currentMonth, 1)
            } else {
                Log.d("WHAR CURRRR", currentMonth.toString())
                calendar.set(currentYear, --currentMonth, 1)
                Log.d("WHAR CURRRR--", currentMonth.toString())
            }


            Log.d("PREV", MainActivity.longTimeToString(calendar.timeInMillis, PATTERN_YYYY_MM_dd))
            cal = CalendarAdapter(requireContext(), R.layout.calendar_view_row, calendar)
            binding.gridView.adapter = cal
            binding.calendarHeader.text =
                "${currentYear}년 ${currentMonth + 1}월"
        }

    }

    companion object {
        private const val PATTERN = "yyyy-MM-dd"
    }
}
