package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.DEFAULT_FOLDER_NAME
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.adapters.FolderAdapter
import com.elliot.kim.kotlin.dimcatcamnote.adapters.FolderSpinnerAdapter
import com.elliot.kim.kotlin.dimcatcamnote.adapters.NoteAdapter
import com.elliot.kim.kotlin.dimcatcamnote.adjustDialogButtonTextSize
import com.elliot.kim.kotlin.dimcatcamnote.adjustDialogTitleTextSize
import com.elliot.kim.kotlin.dimcatcamnote.data.Folder


class MoveToFolderDialogFragment(private val folderAdapter: FolderAdapter,
                                 private val noteAdapter: NoteAdapter
) : DialogFragment() {

    private var selectedItemText = DEFAULT_FOLDER_NAME

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // For smooth animation
        noteAdapter.notifyItemChanged(noteAdapter.getSelectedNotePosition())

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_fragment_move_to_folder)

        val container = dialog.findViewById<RelativeLayout>(R.id.move_to_folder_container)
        val textView = dialog.findViewById<TextView>(R.id.text_view_title)
        val button = dialog.findViewById<Button>(R.id.button)

        container.setBackgroundColor(MainActivity.backgroundColor)
        textView.setBackgroundColor(MainActivity.toolbarColor)
        button.setBackgroundColor(MainActivity.toolbarColor)

        textView.adjustDialogTitleTextSize(MainActivity.fontId)
        button.adjustDialogButtonTextSize(MainActivity.fontId)

        textView.typeface = MainActivity.font
        button.typeface = MainActivity.font

        val spinner = dialog.findViewById<Spinner>(R.id.spinner)
        val adapter = FolderSpinnerAdapter(activity as MainActivity, folderAdapter.folders)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = onItemSelectedListener

        dialog.findViewById<Button>(R.id.button).setOnClickListener {
            // Folder names are not duplicated.
            folderAdapter.moveNoteToFolder(noteAdapter.selectedNote!!,
                folderAdapter.getFolderByName(selectedItemText))
            (activity as MainActivity).showToast(getString(R.string.folder_moved_notification))
            (activity as MainActivity).refreshCurrentFolderItem()
            dialog.dismiss()
        }

        return dialog
    }

    private val onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            selectedItemText = (view as TextView).text.toString()
        }
    }
}
