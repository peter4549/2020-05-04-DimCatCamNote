package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.NoteAdapter
import com.elliot.kim.kotlin.dimcatcamnote.R

class MoreOptionsDialogFragment(private val noteAdapter: NoteAdapter) : DialogFragment() {

    private lateinit var activity: MainActivity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity = requireActivity() as MainActivity

        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_more_options)

        dialog.findViewById<LinearLayout>(R.id.linear_layout_add_to_calendar).setOnClickListener {
            AddToCalendarDialogFragment(noteAdapter.selectedNote!!)
                .show(activity.fragmentManager, tag)
        }

        dialog.findViewById<LinearLayout>(R.id.linear_layout_move_to_folder).setOnClickListener {
            activity.showDialogFragment(DialogFragments.FOLDER_OPTIONS)
        }

        dialog.findViewById<LinearLayout>(R.id.linear_layout_lock).setOnClickListener {
            PasswordSettingDialogFragment(noteAdapter).show(activity.fragmentManager, tag)
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.linear_layout_share).setOnClickListener {
            MainActivity.share(activity, noteAdapter.selectedNote!!)

            dialog.dismiss()
        }



        return dialog
    }
}