package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.R

class SortDialogFragment : DialogFragment() {

    private lateinit var activity: MainActivity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity = requireActivity() as MainActivity

        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_more_options)

        dialog.

                // 커스텀 진행하는 걸로.

        val addToCalendarLayout = dialog.findViewById<LinearLayout>(R.id.item_add_to_calendar)
        addToCalendarLayout.setOnClickListener {
            AddToCalendarDialogFragment(noteAdapter.selectedNote!!)
                .show(activity.fragmentManager, tag)
        }

        return dialog
    }

    val builder = AlertDialog.Builder(activity)
    builder.setTitle(R.string.sort_by)
    builder.setItems(
    activity.resources.getStringArray(R.array.sort_by)) { _: DialogInterface?,
        which: Int ->
        noteAdapter.sort(which) // 옵션 기억하도록.
        //noteAdapter.notifyDataSetChanged()
    }


    builder.create()
    builder.show()
}