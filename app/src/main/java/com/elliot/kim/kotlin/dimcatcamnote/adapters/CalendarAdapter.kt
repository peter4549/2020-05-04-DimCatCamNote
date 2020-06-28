package com.elliot.kim.kotlin.dimcatcamnote.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.elliot.kim.kotlin.dimcatcamnote.PATTERN_YYYY_MM_dd
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.fragments.AlarmedNoteSelectionFragment
import com.elliot.kim.kotlin.dimcatcamnote.fragments.CalendarFragment
import java.text.SimpleDateFormat
import java.util.*

class CalendarAdapter(private val activity: MainActivity, private val convertViewId: Int,
                      private var calendar: Calendar, private var alarmedNotes: MutableList<Note>,
                      private val todayDate: Int): BaseAdapter() {

    private var noteIdAlarmDatePairs : ArrayList<Pair<Int, Long>> = arrayListOf()
    private lateinit var inflater: LayoutInflater
    private val itemCount = 42
    var alarmedNoteSelectionFragment = AlarmedNoteSelectionFragment()

    private var lastDay = 0

    private var dateArray = arrayOfNulls<Number>(itemCount)
    private val whiteColor: Int

    init {
        getNoteIdAlarmDatePair()
        // Day of the week on which the calendar starts
        val calendarStartDay = calendar.get(Calendar.DAY_OF_WEEK) - 1
        var j = 1
        for(i in calendarStartDay until itemCount) {
            dateArray[i] = j++
        }

        whiteColor = activity.getColor(android.R.color.white)
    }

    fun addAlarmedNote(note: Note) {
        alarmedNotes.add(note)
        getNoteIdAlarmDatePair()
    }

    fun setCalendar(calendar: Calendar) {
        this.calendar = calendar

        // Init dateArray
        dateArray = arrayOfNulls(itemCount)

        // Day of the week on which the calendar starts
        val calendarStartDay = calendar.get(Calendar.DAY_OF_WEEK) - 1
        var j = 1
        for(i in calendarStartDay until itemCount) {
            dateArray[i] = j++
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder
        val view: View
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1

        lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        if (convertView == null) {
            view = inflater.inflate(convertViewId, null)

            holder = ViewHolder()
            holder.container = view.findViewById(R.id.container)
            holder.textView = view.findViewById(R.id.text_view)
            holder.imageView = view.findViewById(R.id.image_view)

            view?.tag = holder
        } else
            holder = convertView.tag as ViewHolder

        holder.imageView.visibility = View.INVISIBLE

        holder.container.setBackgroundColor(whiteColor)
        if (dateArray[position] == null || dateArray[position]!!.toInt() > lastDay)
        {
            holder.textView.text = null
            holder.container.foreground = null
        } else {
            // Date displayed
            val currentDate = convertDateIntToLong(currentYear, currentMonth, dateArray[position] as Int)
            val today = convertDateIntToLong(currentYear, currentMonth, todayDate)

            if (currentYear == CalendarFragment.thisYear
                && currentMonth == CalendarFragment.thisMonth && currentDate == today)
                holder.container.setBackgroundColor(activity.getColor(R.color.backgroundColorLightBlue))

            for (idAlarmDatePair in noteIdAlarmDatePairs) {
                if(currentDate == idAlarmDatePair.second) {
                    holder.imageView.visibility = View.VISIBLE

                    // The code below is expected to raise the ConcurrentModificationException.
                    // noteIdAlarmDatePairs.remove(idAlarmDatePair)
                }
            }

            holder.textView.text = dateArray[position].toString()

            holder.container.setOnClickListener {
                startAlarmedNoteSelectionFragment(alarmedNoteSelectionFragment,
                    currentDate, holder.imageView)
            }

        }

        return holder.container
    }

    override fun getItem(position: Int): Any {
        return 0
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = itemCount

    class ViewHolder {
        lateinit var container: RelativeLayout
        lateinit var textView: TextView
        lateinit var  imageView: ImageView
    }

    private fun getNoteIdAlarmDatePair() {
        val simpleDateFormat = SimpleDateFormat(
            PATTERN_YYYY_MM_dd, Locale.getDefault())

        for (alarmedNote in alarmedNotes) {
            val alarmedDate = simpleDateFormat.format(alarmedNote.alarmTime)
            val date = simpleDateFormat.parse(alarmedDate)?.time!!

            noteIdAlarmDatePairs.add(Pair(alarmedNote.id, date))
        }
    }

   private fun convertDateIntToLong(year: Int, month: Int, date: Int): Long {
       val simpleDateFormat = SimpleDateFormat(
           "yyyy-MM-dd", Locale.getDefault())

       return simpleDateFormat.parse(String.format("%d-%d-%d", year, month, date))?.time!!
   }

    private fun startAlarmedNoteSelectionFragment(alarmedNoteSelectionFragment: AlarmedNoteSelectionFragment,
                                                  currentDate: Long, imageView: ImageView) {
        val simpleDateFormat = SimpleDateFormat(PATTERN_YYYY_MM_dd, Locale.getDefault())
        alarmedNoteSelectionFragment.setData(alarmedNotes.filter {
            simpleDateFormat.parse(simpleDateFormat.format(it.alarmTime))?.time!! == currentDate
        } as ArrayList<Note>, currentDate, imageView)
        activity.fragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(R.anim.anim_slide_in_left_enter,
                R.anim.anim_slide_in_left_exit,
                R.anim.anim_slide_out_right_enter,
                R.anim.anim_slide_out_right_exit)
            .replace(R.id.calendar_container, alarmedNoteSelectionFragment).commit()
    }
}