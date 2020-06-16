package com.elliot.kim.kotlin.dimcatcamnote.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.elliot.kim.kotlin.dimcatcamnote.PATTERN_YYYY_MM_dd
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.fragments.AlarmedNoteSelectionFragment
import com.elliot.kim.kotlin.dimcatcamnote.fragments.CalendarFragment
import java.text.SimpleDateFormat
import java.util.*

class CalendarAdapter(private val activity: MainActivity, private val rowViewId: Int,
                      private val calendar: Calendar, private val alarmedNotes: MutableList<Note>,
                      private val todayDate: Int): BaseAdapter() {

    private lateinit var inflater: LayoutInflater
    private val itemCount = 42

    private var noteIdAlarmDatePairs = arrayListOf<Pair<Int, Long>>()

    private var lastDay = 0

    private var dateArray = arrayOfNulls<Number>(itemCount)

    init {
        // Day of the week on which the calendar starts
        val calendarStartDay = calendar.get(Calendar.DAY_OF_WEEK) - 1
        var j = 1
        for(i in calendarStartDay until itemCount) {
            dateArray[i] = j++
        }
        getAlarmTimeFromNotes()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1


        lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)


        inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val rowView = inflater.inflate(rowViewId, null) // 뷰 홀더 패턴..

        val textView = rowView.findViewById<TextView>(R.id.text_view)
        val imageView = rowView.findViewById<ImageView>(R.id.image_view)
        imageView.visibility = View.INVISIBLE

        if (dateArray[position] == null || dateArray[position]!!.toInt() > lastDay)
        {
            textView.text = null
            rowView.foreground = null
        }
        else {

            // Date displayed
            val currentDate = convertDateIntToLong(currentYear, currentMonth, dateArray[position] as Int)
            val today = convertDateIntToLong(currentYear, currentMonth, todayDate)

            if (currentYear == CalendarFragment.thisYear
                && currentMonth == CalendarFragment.thisMonth && currentDate == today)
                rowView.setBackgroundColor(activity.getColor(R.color.backgroundColorLightBlue))

            for (idAlarmDatePair in noteIdAlarmDatePairs) {
                if(currentDate == idAlarmDatePair.second) {
                    imageView.visibility = View.VISIBLE

                    // The code below is expected to raise the ConcurrentModificationException.
                    // noteIdAlarmDatePairs.remove(idAlarmDatePair)
                }
            }

            val simpleDateFormat = SimpleDateFormat(
                PATTERN_YYYY_MM_dd, Locale.getDefault())

            val alarmedNoteSelectionFragment = AlarmedNoteSelectionFragment(alarmedNotes.filter {
                simpleDateFormat.parse(simpleDateFormat.format(it.alarmTime))?.time!! == currentDate
            } as ArrayList<Note>, MainActivity.longTimeToString(currentDate, PATTERN_YYYY_MM_dd), imageView)

            textView.text = dateArray[position].toString()

            rowView.setOnClickListener {
                startAlarmedNoteSelectionFragment(alarmedNoteSelectionFragment)
            }

        }
        return rowView
    }

    override fun getItem(position: Int): Any {
        return 0
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = itemCount

    fun getAlarmTimeFromNotes() {
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

    fun updateCalendar() {
        // 얘가 노트 추가나 변경시 호출될 차기 에이스
        // 데이터셋 정리하고,

        notifyDataSetChanged()
    }

   private fun convertDateIntToLong(year: Int, month: Int, date: Int): Long {
       val simpleDateFormat = SimpleDateFormat(
           "yyyy-MM-dd", Locale.getDefault())

       return simpleDateFormat.parse(String.format("%d-%d-%d", year, month, date))?.time!!
   }

    private fun startAlarmedNoteSelectionFragment(alarmedNoteSelectionFragment: AlarmedNoteSelectionFragment) {
        activity.fragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(R.anim.anim_slide_in_left_enter,
                R.anim.anim_slide_in_left_exit,
                R.anim.anim_slide_down_pop_enter,
                R.anim.anim_slide_down_pop_exit)
            .replace(R.id.calendar_container, alarmedNoteSelectionFragment).commit()
    }
}