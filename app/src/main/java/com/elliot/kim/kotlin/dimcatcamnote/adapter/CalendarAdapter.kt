package com.elliot.kim.kotlin.dimcatcamnote.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.elliot.kim.kotlin.dimcatcamnote.R
import java.util.*
import kotlin.math.max

class CalendarAdapter(private val context: Context, private val rowView: Int,
                      private val calendar: Calendar): BaseAdapter() {

    private lateinit var inflater: LayoutInflater
    private var maxSize = 35
    private val arrMax = 42



    private var numberOfDate = 0

    private var res = 0


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        res = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val startDay = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 요일.
        numberOfDate = res

        var dateArray = arrayOfNulls<Number>(arrMax)

        var j = 1
        for(i in startDay until maxSize) {
            dateArray[i] = j++
        }
        if (dateArray[35] != null) maxSize = 42
        //var vv = convertView!!
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var vv = inflater.inflate(rowView, null) // 뷰 홀더 패턴..
        val textView = vv.findViewById<TextView>(R.id.text_view)

        // 여기에 데이터 어레이를 받음. for 문,, 아 아니라.. if문 비교,

        //if dateArray[position] == null
        if (dateArray[position] == null || dateArray[position]!!.toInt() > res)
        {
             textView.setText("DD") // 초과 동작은 다르게 처리.
        }
        else {
            textView.setText(dateArray[position].toString() ?: "A")
        }
        return vv
    }

    override fun getItem(position: Int): Any {
        return 0
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int {
        return maxSize // 항상 칸을 채울 것.
    }
}