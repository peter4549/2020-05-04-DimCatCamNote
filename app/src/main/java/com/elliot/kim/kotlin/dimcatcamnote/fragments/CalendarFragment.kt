package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.elliot.kim.kotlin.dimcatcamnote.CurrentFragment
import com.elliot.kim.kotlin.dimcatcamnote.PATTERN_YYYY_MM_dd
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.adapters.CalendarAdapter
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.databinding.FragmentCalendarBinding
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private lateinit var activity: MainActivity
    private lateinit var alarmedNotes: MutableList<Note>
    private lateinit var binding: FragmentCalendarBinding
    private var currentTime = 0L
    private var currentYear = 0
    private var currentMonth = 0
    private var todayDate = 0
    private var currentYearMonthText = ""
    private var noteIdAlarmDatePairs = arrayListOf<Pair<Int, Long>>()
    lateinit var calendarAdapter: CalendarAdapter

    fun setAlarmedNotes(alarmedNotes: MutableList<Note>) {
        this.alarmedNotes = alarmedNotes
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity = requireActivity() as MainActivity
        currentTime = MainActivity.getCurrentTime()

        currentYear = SimpleDateFormat("yyyy",
            Locale.getDefault()).format(currentTime).toInt()
        currentMonth = SimpleDateFormat("MM",
            Locale.getDefault()).format(currentTime).toInt() - 1
        todayDate = SimpleDateFormat("dd",
            Locale.getDefault()).format(currentTime).toInt()

        thisYear = currentYear
        thisMonth = currentMonth + 1

        currentYearMonthText = "${currentYear}년 ${currentMonth + 1}월"

        val dateFormat = SimpleDateFormat(PATTERN_YYYY_MM_dd, Locale.getDefault())
        val calendar = Calendar.getInstance()
        val stringCurrentMonth = if (currentMonth + 1 > 9) "${currentMonth + 1}"
        else "0${currentMonth + 1}"

        // Initialize noteIdAlarmDatePairs.
        getAlarmTimeFromNotes()

        calendar.time = dateFormat.parse("${currentYear}년 ${stringCurrentMonth}월 01일")!!
        calendarAdapter = CalendarAdapter(activity, R.layout.calendar_view_row, calendar,
            noteIdAlarmDatePairs, alarmedNotes, todayDate)

        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentCalendarBinding.bind(view)
        binding.gridView.adapter = calendarAdapter
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        setYearMonthText(currentYearMonthText)

        val calendar = Calendar.getInstance()

        binding.imageButtonNext.setOnClickListener {
            if (currentMonth > 10) {
                currentMonth = 0
                calendar.set(++currentYear, currentMonth, 1)
            }
            else
                calendar.set(currentYear, ++currentMonth, 1)

            calendarAdapter = CalendarAdapter(activity, R.layout.calendar_view_row, calendar,
                noteIdAlarmDatePairs, alarmedNotes, todayDate)
            binding.gridView.adapter = calendarAdapter
            currentYearMonthText = "${currentYear}년 ${currentMonth + 1}월"
            setYearMonthText(currentYearMonthText)
        }

        binding.imageButtonPrevious.setOnClickListener {
            if (currentMonth < 1) {
                currentMonth = 11
                calendar.set(--currentYear, currentMonth, 1)
            } else {
                calendar.set(currentYear, --currentMonth, 1)
            }

            calendarAdapter = CalendarAdapter(activity, R.layout.calendar_view_row, calendar,
                noteIdAlarmDatePairs, alarmedNotes, todayDate)
            binding.gridView.adapter = calendarAdapter
            currentYearMonthText = "${currentYear}년 ${currentMonth + 1}월"
            setYearMonthText(currentYearMonthText)

        }
    }

    override fun onResume() {
        super.onResume()
        binding.toolbar.setBackgroundColor(MainActivity.toolbarColor)
        activity.setCurrentFragment(CurrentFragment.CALENDAR_FRAGMENT)
    }

    override fun onStop() {
        super.onStop()
        activity.setCurrentFragment(null)
        activity.showFloatingActionButton()
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {

        val animation = AnimationUtils.loadAnimation(activity, nextAnim)

        animation!!.setAnimationListener( object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                activity.closeDrawer()
            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })

        return animation
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity.backPressed()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setYearMonthText(text: String) {
        binding.toolbar.title = text
        binding.calendarHeader.text = text
    }

    private fun getAlarmTimeFromNotes() {
        // year, month랑 date 추출하여... 튜플에 저장???
        // 알람 기능부에 전부 알림 어레이에 더할 수 있도록..
        // 키 데이트 얻으면,, 키로 접근 삽 가능.

        val simpleDateFormat = SimpleDateFormat(
            PATTERN_YYYY_MM_dd, Locale.getDefault())

        for (alarmedNote in alarmedNotes) {
            val alarmedDate = simpleDateFormat.format(alarmedNote.alarmTime)
            val date = simpleDateFormat.parse(alarmedDate)?.time!!

            noteIdAlarmDatePairs.add(Pair(alarmedNote.id, date))
        }
    }

    companion object {
        var thisYear = 0
        var thisMonth = 0
    }
}
