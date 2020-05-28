package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.NoteAdapter
import com.elliot.kim.kotlin.dimcatcamnote.R

class MoreOptionsDialogFragment(private val noteAdapter: NoteAdapter) : DialogFragment() {

    private lateinit var activity: MainActivity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity = requireActivity() as MainActivity

        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_more_options)

        val noteLockLayout = dialog.findViewById<LinearLayout>(R.id.linear_layout_lock)
        noteLockLayout.setOnClickListener {
            PasswordSettingDialogFragment(noteAdapter).show(activity.fragmentManager, tag)
            dialog.dismiss()
        }

        val addToCalendarLayout = dialog.findViewById<LinearLayout>(R.id.item_add_to_calendar)
        addToCalendarLayout.setOnClickListener {
            AddToCalendarDialogFragment(noteAdapter.selectedNote!!)
                .show(activity.fragmentManager, tag)
        }

        return dialog
    }
}