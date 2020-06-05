package com.elliot.kim.kotlin.dimcatcamnote.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.elliot.kim.kotlin.dimcatcamnote.PATTERN_YYYY_MM_dd
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.fragments.AlarmedNoteSelectionFragment
import java.text.SimpleDateFormat
import java.util.*

class CalendarAdapter(private val activity: MainActivity, private val rowView: Int,
                      private val calendar: Calendar, private val alarmedNotes: MutableList<Note>): BaseAdapter() {

    // 노트 어댑터 정보도 갖고 있도록...

    private lateinit var inflater: LayoutInflater
    private val itemCount = 42

    private var noteIdAlarmDatePairs = arrayListOf<Pair<Int, Long>>()

    private var numberOfDate = 0

    //private lateinit var alarmedNoteSelectionFragment: AlarmedNoteSelectionFragment

    private var res = 0

    var dateArray = arrayOfNulls<Number>(itemCount)

    init {
        var j = 1
        val startDay = calendar.get(Calendar.DAY_OF_WEEK) - 1
        for(i in startDay until itemCount) {
            dateArray[i] = j++ // 그래도 빼는게 맞는듯.
        }
        getAlarmTimeFromNotes()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        res = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
         // 요일.
        numberOfDate = res

        var aaaa = arrayListOf<Note>() // 노트 어댑터에 던져줄 배열.

        inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val vv = inflater.inflate(rowView, null) // 뷰 홀더 패턴..

        val textView = vv.findViewById<TextView>(R.id.text_view)
        // 표시하고 처리하는 부분...
        //if dateArray[position] == null
        if (dateArray[position] == null || dateArray[position]!!.toInt() > res)
        {
             textView.text = null
            vv.foreground = null
        }
        else {

            // 달력에 표시되는 날짜.
            val currentDate = convertDateIntToLong(currentYear, currentMonth, dateArray[position] as Int)

            // 순회 탐색하면서, 색상 표기.
            // for 돌면서,
            for (idAlarmDatePair in noteIdAlarmDatePairs) {
                if(currentDate == idAlarmDatePair.second) {
                    vv.setBackgroundColor(activity.getColor(R.color.colorBlueGrey263238))
                }
            }


            val simpleDateFormat = SimpleDateFormat(
                PATTERN_YYYY_MM_dd, Locale.getDefault())

            for (alarmedNote in alarmedNotes) {
                val alarmedDate = simpleDateFormat.format(alarmedNote.alarmTime)
                val date = simpleDateFormat.parse(alarmedDate)?.time!!

                // 각 칸 클릭리스너에 등록..

            } // 재를 바로 변환해주는 문법 필요.


            // 만들어짐. 얘를 실행시켜주면 된다.
            val alarmedNoteSelectionFragment = AlarmedNoteSelectionFragment(alarmedNotes.filter {
                simpleDateFormat.parse(simpleDateFormat.format(it.alarmTime))?.time!! == currentDate
            } as ArrayList<Note>)

            Log.d("Current Date", currentDate.toString())
            for(note in alarmedNotes) {
                val kk = simpleDateFormat.parse(simpleDateFormat.format(note.alarmTime))?.time!!

                Log.d("Registed", kk.toString())
            }
            // 얘한테 제대로 전달안되는 문제일 수도..
            // Display 문제인지. 전달 문제인지..

            textView.text = dateArray[position].toString() // 널이 아니면 숫자 표기 및 리스너 등록
            // 만약, 현재 블럭의 날짜와 일치하는 알림 설정된 노트가 있다면 표시. 리스너 등록시 파라미터 전달.
            // 일치하는 노트 어떻게 찾을래,

            /*
            for (note in alarmedNotes) {
                if (note.alarmTime == currentDate)
                    aaaa.add(note)
            }

             */
            if (alarmedNoteSelectionFragment.alarmedNotes.isNotEmpty())
                Log.d("FUCK", alarmedNoteSelectionFragment.alarmedNotes[0].title) // 여기까진 전달된거같음..


            vv.setOnClickListener {
                startAlarmedNoteSelectionFragment(alarmedNoteSelectionFragment) // 제대로 넘어간거 같은데,
            }

        }
        return vv
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