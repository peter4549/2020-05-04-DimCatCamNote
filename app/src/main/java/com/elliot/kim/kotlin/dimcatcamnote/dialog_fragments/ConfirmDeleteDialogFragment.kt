package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.CurrentFragment
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity.Companion.toolbarColor

class ConfirmDeleteDialogFragment(private val note: Note) : DialogFragment() {

    private lateinit var activity: MainActivity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity = requireActivity() as MainActivity

        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_fragment_confirm_delete)

        val textViewTitle = dialog.findViewById<TextView>(R.id.text_view_title)
        textViewTitle.text = note.title
        textViewTitle.setBackgroundColor(toolbarColor)

        dialog.findViewById<Button>(R.id.button_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.button_ok).setOnClickListener {
            activity.viewModel.delete(note)

            activity.showToast(note.title)
            if (MainActivity.currentFragment == CurrentFragment.EDIT_FRAGMENT)
                activity.backPressed()

            dialog.dismiss()
        }

        return dialog
    }
}