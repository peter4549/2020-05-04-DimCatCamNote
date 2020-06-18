package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.adapters.NoteAdapter
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.SortingCriteria
import com.elliot.kim.kotlin.dimcatcamnote.adjustDialogItemTextSize
import com.elliot.kim.kotlin.dimcatcamnote.adjustDialogTitleTextSize

class SortDialogFragment(noteAdapter: NoteAdapter) : DialogFragment() {

    private lateinit var activity: MainActivity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity = requireActivity() as MainActivity

        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_fragment_sort)
        val textView = dialog.findViewById<TextView>(R.id.text_view_title)
        val textViewByCreationTime = dialog.findViewById<TextView>(R.id.by_creation_time)
        val textViewByEditTime = dialog.findViewById<TextView>(R.id.by_edit_time)
        val textViewByName = dialog.findViewById<TextView>(R.id.by_name)

        // Apply design.
        dialog.findViewById<RelativeLayout>(R.id.sort_container).setBackgroundColor(MainActivity.backgroundColor)
        textView.setBackgroundColor(MainActivity.toolbarColor)

        textView.adjustDialogTitleTextSize(MainActivity.fontId)
        textViewByCreationTime.adjustDialogItemTextSize(MainActivity.fontId)
        textViewByEditTime.adjustDialogItemTextSize(MainActivity.fontId)
        textViewByName.adjustDialogItemTextSize(MainActivity.fontId)

        textView.typeface = MainActivity.font
        textViewByCreationTime.typeface = MainActivity.font
        textViewByEditTime.typeface = MainActivity.font
        textViewByName.typeface = MainActivity.font

        textViewByCreationTime.setOnClickListener(onClickListener)
        textViewByEditTime.setOnClickListener(onClickListener)
        textViewByName.setOnClickListener(onClickListener)

        return dialog
    }

    private val onClickListener = View.OnClickListener { v ->
        when(v!!.id) {
            R.id.by_edit_time -> {
                noteAdapter.sort(SortingCriteria.EDIT_TIME.index)
                activity.setTextViewSortText(SortingCriteria.EDIT_TIME.index)
            }
            R.id.by_creation_time -> {
                noteAdapter.sort(SortingCriteria.CREATION_TIME.index)
                activity.setTextViewSortText(SortingCriteria.CREATION_TIME.index)
            }
            R.id.by_name -> {
                noteAdapter.sort(SortingCriteria.NAME.index)
                activity.setTextViewSortText(SortingCriteria.NAME.index)
            }
        }

        dialog!!.dismiss()
    }
}