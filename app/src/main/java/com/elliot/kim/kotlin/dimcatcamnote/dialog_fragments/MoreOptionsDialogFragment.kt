package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.adapters.NoteAdapter
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.adjustDialogItemTextSize
import com.elliot.kim.kotlin.dimcatcamnote.adjustDialogTitleTextSize

class MoreOptionsDialogFragment(private val noteAdapter: NoteAdapter) : DialogFragment() {

    private lateinit var activity: MainActivity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity = requireActivity() as MainActivity

        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_fragment_more_options)

        val textView = dialog.findViewById<TextView>(R.id.text_view_title)
        textView.text = noteAdapter.selectedNote!!.title
        val textViewAddToCalendar =
            dialog.findViewById<TextView>(R.id.text_view_add_to_calendar)
        val textViewMoveToFolder =
            dialog.findViewById<TextView>(R.id.text_view_move_to_folder)
        val textViewLock =
            dialog.findViewById<TextView>(R.id.text_view_lock)
        val textViewShare =
            dialog.findViewById<TextView>(R.id.text_view_share)

        textView.setBackgroundColor(MainActivity.toolbarColor)
        dialog.findViewById<LinearLayout>(R.id.more_options_container)
            .setBackgroundColor(MainActivity.backgroundColor)

        textView.adjustDialogTitleTextSize(MainActivity.fontId)
        textViewAddToCalendar.adjustDialogItemTextSize(MainActivity.fontId)
        textViewMoveToFolder.adjustDialogItemTextSize(MainActivity.fontId)
        textViewLock.adjustDialogItemTextSize(MainActivity.fontId)
        textViewShare.adjustDialogItemTextSize(MainActivity.fontId)

        textView.typeface = MainActivity.font
        textViewAddToCalendar.typeface = MainActivity.font
        textViewMoveToFolder.typeface = MainActivity.font
        textViewLock.typeface = MainActivity.font
        textViewShare.typeface = MainActivity.font

        textViewAddToCalendar.setOnClickListener {
            AddToCalendarDialogFragment(noteAdapter.selectedNote!!)
                .show(activity.fragmentManager, tag)
            dialog.dismiss()
        }

        textViewMoveToFolder.setOnClickListener {
            activity.showDialogFragment(DialogFragments.MOVE_TO_FOLDER)
            dialog.dismiss()
        }

        textViewLock.setOnClickListener {
            SetPasswordDialogFragment(noteAdapter).show(activity.fragmentManager, tag)
            dialog.dismiss()
        }

        textViewShare.setOnClickListener {
            MainActivity.share(activity, noteAdapter.selectedNote!!)
            dialog.dismiss()
        }

        return dialog
    }
}