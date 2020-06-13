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
    lateinit var calendarAdapter: CalendarAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity = requireActivity() as MainActivity
        currentTime = MainActivity.getCurrentTime()
        alarmedNotes = activity.getNoteAdapter().getAlarmedNotes() as MutableList<Note>

        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCalendarBinding.bind(view)
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        var currentYear = SimpleDateFormat("yyyy",
            Locale.getDefault()).format(currentTime).toInt()
        var currentMonth = SimpleDateFormat("MM",
            Locale.getDefault()).format(currentTime).toInt() - 1
        val todayDate = SimpleDateFormat("dd",
            Locale.getDefault()).format(currentTime).toInt()

        var currentYearMonthText = "${currentYear}년 ${currentMonth + 1}월"
        setYearMonthText(currentYearMonthText)

        val dateFormat = SimpleDateFormat(PATTERN_YYYY_MM_dd, Locale.getDefault())
        val calendar = Calendar.getInstance()
        val stringCurrentMonth = if (currentMonth + 1 > 9) "${currentMonth + 1}"
        else "0${currentMonth + 1}"

        calendar.time = dateFormat.parse("${currentYear}년 ${stringCurrentMonth}월 01일")!!

        calendarAdapter = CalendarAdapter(activity, R.layout.calendar_view_row, calendar,
            alarmedNotes, todayDate)
        binding.gridView.adapter = calendarAdapter // 여기가 그리드 뷰 업데이트 부분. 고칠 필요 없음.

        binding.imageButtonNext.setOnClickListener {
            if (currentMonth > 10) {
                currentMonth = 0
                calendar.set(++currentYear, currentMonth, 1)
            }
            else
                calendar.set(currentYear, ++currentMonth, 1)

            calendarAdapter = CalendarAdapter(activity, R.layout.calendar_view_row, calendar,
                alarmedNotes, todayDate)
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
                alarmedNotes, todayDate)
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
}
