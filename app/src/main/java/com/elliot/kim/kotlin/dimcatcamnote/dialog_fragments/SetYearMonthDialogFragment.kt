package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.adapters.CalendarAdapter
import com.elliot.kim.kotlin.dimcatcamnote.adapters.YearMonthSpinnerAdapter
import com.elliot.kim.kotlin.dimcatcamnote.adjustDialogButtonTextSize
import com.elliot.kim.kotlin.dimcatcamnote.adjustDialogItemTextSize
import kotlinx.android.synthetic.main.dialog_fragment_set_year_month.view.*
import java.time.Year
import java.util.*

class SetYearMonthDialogFragment: DialogFragment() {

    private var selectedYear = 0
    private var selectedMonth = 0
    private var currentYear = 0
    private var currentMonth = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_fragment_set_year_month)

        dialog.findViewById<RelativeLayout>(R.id.set_year_month_container)
            .setBackgroundColor(MainActivity.backgroundColor)

        val textViewYear = dialog.findViewById<TextView>(R.id.text_view_year_spinner)
        val textViewMonth = dialog.findViewById<TextView>(R.id.text_view_month_spinner)
        textViewYear.adjustDialogItemTextSize(MainActivity.fontId)
        textViewMonth.adjustDialogItemTextSize(MainActivity.fontId)
        textViewYear.typeface = MainActivity.font
        textViewMonth.typeface = MainActivity.font

        val button = dialog.findViewById<Button>(R.id.button)

        button.setBackgroundColor(MainActivity.toolbarColor)
        button.adjustDialogButtonTextSize(MainActivity.fontId)
        button.typeface = MainActivity.font

        val yearSpinner = dialog.findViewById<Spinner>(R.id.year_spinner)
        val monthSpinner = dialog.findViewById<Spinner>(R.id.month_spinner)
        val yearAdapter = YearMonthSpinnerAdapter(activity as MainActivity, YearMonthSpinnerAdapter.year)
        val monthAdapter = YearMonthSpinnerAdapter(activity as MainActivity, YearMonthSpinnerAdapter.month)

        yearSpinner.adapter = yearAdapter
        monthSpinner.adapter = monthAdapter

        yearSpinner.setSelection(currentYear - 1980)
        monthSpinner.setSelection(currentMonth)

        yearSpinner.onItemSelectedListener = onYearSelectedListener
        monthSpinner.onItemSelectedListener = onMonthSelectedListener


        button.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.set(selectedYear, selectedMonth, 1)
            (activity as MainActivity).calendarFragment.calendarAdapter.setCalendar(calendar)
            (activity as MainActivity).calendarFragment.setCurrentYearMonth(selectedYear, selectedMonth)
            (activity as MainActivity).calendarFragment.calendarAdapter.notifyDataSetChanged()

            val yearMonthText = "${selectedYear}년 ${selectedMonth + 1}월"
            (activity as MainActivity).calendarFragment.setYearMonthText(yearMonthText)

            dismiss()
        }

        return dialog
    }

    fun setCurrentYearMonth(year: Int, month: Int) {
        currentYear = year
        currentMonth = month
    }

    private val onYearSelectedListener = object: AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            selectedYear = position + 1980
        }
    }

    private val onMonthSelectedListener = object: AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            selectedMonth = position
        }
    }
}