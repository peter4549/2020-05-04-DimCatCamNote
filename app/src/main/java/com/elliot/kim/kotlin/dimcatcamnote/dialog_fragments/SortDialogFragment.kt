package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.NoteAdapter
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.SortingCriteria

class SortDialogFragment(noteAdapter: NoteAdapter) : DialogFragment() {

    private lateinit var activity: MainActivity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity = requireActivity() as MainActivity

        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_fragment_sort)
        dialog.findViewById<LinearLayout>(R.id.by_creation_time).setOnClickListener(onClickListener)
        dialog.findViewById<LinearLayout>(R.id.by_edit_time).setOnClickListener(onClickListener)
        dialog.findViewById<LinearLayout>(R.id.by_name).setOnClickListener(onClickListener)

        return dialog
    }

    private val onClickListener = View.OnClickListener { v ->
        when(v!!.id) {
            R.id.by_edit_time -> noteAdapter.sort(SortingCriteria.EDIT_TIME.index)
            R.id.by_creation_time -> noteAdapter.sort(SortingCriteria.CREATION_TIME.index)
            R.id.by_name -> noteAdapter.sort(SortingCriteria.NAME.index)
        }

        dialog!!.dismiss()
    }
}