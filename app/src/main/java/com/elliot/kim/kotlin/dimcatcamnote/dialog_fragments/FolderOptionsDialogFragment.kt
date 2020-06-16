package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.adapters.FolderAdapter
import com.elliot.kim.kotlin.dimcatcamnote.adapters.NoteAdapter
import com.elliot.kim.kotlin.dimcatcamnote.data.Folder

class FolderOptionsDialogFragment(private val folderAdapter: FolderAdapter,
                                  private val noteAdapter: NoteAdapter
) : DialogFragment() {

    private lateinit var activity: MainActivity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity = requireActivity() as MainActivity

        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_fragment_folder_options)

        val container = dialog.findViewById<RelativeLayout>(R.id.move_to_folder_container)
        val textView = dialog.findViewById<TextView>(R.id.text_view_title)
        val button = dialog.findViewById<Button>(R.id.button)

        container.setBackgroundColor(MainActivity.backgroundColor)
        textView.setBackgroundColor(MainActivity.toolbarColor)
        button.setBackgroundColor(MainActivity.toolbarColor)

        val spinner = dialog.findViewById<Spinner>(R.id.spinner)
        setSpinner(spinner, folderAdapter.folders)
        spinner.prompt = "폴더 목록"


        dialog.findViewById<Button>(R.id.button).setOnClickListener {
            val folder = folderAdapter.getFolderByName(spinner.selectedItem as String)
            folderAdapter.moveNoteToFolder(noteAdapter.selectedNote, folder)
            activity.showToast(getString(R.string.folder_moved_notification))
            noteAdapter.notifyDataSetChanged()
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