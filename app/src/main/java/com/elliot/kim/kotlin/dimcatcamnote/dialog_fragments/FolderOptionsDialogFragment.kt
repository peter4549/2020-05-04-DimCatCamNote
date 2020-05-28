package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.*

class FolderOptionsDialogFragment(private val folderAdapter: FolderAdapter,
                                  private val noteAdapter: NoteAdapter) : DialogFragment() {

    private lateinit var activity: MainActivity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity = requireActivity() as MainActivity

        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_folder_options)

        val spinner = dialog.findViewById<Spinner>(R.id.spinner)
        setSpinner(spinner, folderAdapter.folders)

        dialog.findViewById<Button>(R.id.button_move).setOnClickListener {
            val folder = folderAdapter.getFolderByName(spinner.selectedItem as String)
            folderAdapter.moveNoteToFolder(noteAdapter.selectedNote, folder)
            activity.showToast("폴더로 이동하였습니다.")
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.button_cancel).setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }

    private fun setSpinner(spinner: Spinner, folders: MutableList<Folder>) {

        val adapter = ArrayAdapter<CharSequence>(
            activity,
            android.R.layout.simple_spinner_item
        )

        for (folder in folders) adapter.add(folder.name)

        spinner.adapter = adapter
    }
}