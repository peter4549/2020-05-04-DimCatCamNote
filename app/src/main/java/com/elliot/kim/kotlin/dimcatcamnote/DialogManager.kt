package com.elliot.kim.kotlin.dimcatcamnote

import android.R.layout.simple_spinner_item
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog

import com.elliot.kim.kotlin.dimcatcamnote.R


class DialogManager(private val context: Context) {

    private var activity = (context as MainActivity)
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var folderManager: FolderManager
    private lateinit var noteAdapter: NoteAdapter

    fun setFolderManager(folderManager: FolderManager) { this.folderManager = folderManager }
    fun setNoteAdapter(noteAdapter: NoteAdapter) { this.noteAdapter = noteAdapter }
    fun setFolderAdapter(folderAdapter: FolderAdapter) { this.folderAdapter = folderAdapter }

    fun showDialog(dialog: DialogType) {
        when (dialog) {
            DialogType.ADD_FOLDER -> {
                showAddFolderDialog()
            }
            DialogType.FOLDER_OPTIONS -> {
                showFolderOptionsDialog()
            }
            DialogType.SORT -> {
                showSortDialog()
            }
        }
    }

    private fun showAddFolderDialog() {
        val builder = AlertDialog.Builder(activity)

        builder.setTitle("폴더 생성")
        builder.setMessage("폴더이름을 입력해주세요.")

        val editText = EditText(activity)
        builder.setView(editText)

        builder.setPositiveButton("확인") { _: DialogInterface?, _: Int ->
            if (editText.text.toString() == "") {
                activity.showToast("폴더이름을 입력해주세요.")
            } else {
                folderManager.addFolder(editText.text.toString())
                folderAdapter.notifyItemInserted(folderManager.folders.size - 1)
            }
        }

        builder.setNegativeButton("취소") { _: DialogInterface?, _: Int -> }

        builder.create()
        builder.show()
    }

    private fun showFolderOptionsDialog() {
        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_folder_options)
        val spinner = dialog.findViewById<Spinner>(R.id.spinner)
        setSpinner(spinner, folderManager.folders)
        dialog.findViewById<Button>(R.id.button_move).setOnClickListener {

        }
        dialog.findViewById<Button>(R.id.button_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setSpinner(spinner: Spinner, folders: MutableList<Folder>) {
        val adapter = ArrayAdapter<CharSequence>(
            context,
            simple_spinner_item
        )

        for (folder in folders) adapter.add(folder.name)

        spinner.adapter = adapter


    }

    private fun showSortDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.sort_by)
            .setItems(
                activity.resources.getStringArray(R.array.sort_by)) { _: DialogInterface?,
                                                                      which: Int ->
                noteAdapter.sort(which) // 옵션 기억하도록.
                noteAdapter.notifyDataSetChanged()
            }

        builder.create()
        builder.show()
    }

    companion object {
        enum class DialogType {
            ADD_FOLDER,
            FOLDER_OPTIONS,
            SORT
        }
    }
}