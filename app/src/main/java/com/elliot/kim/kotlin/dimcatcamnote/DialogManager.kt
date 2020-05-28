package com.elliot.kim.kotlin.dimcatcamnote

import android.R.layout.simple_spinner_item
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.provider.CalendarContract
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity

import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.dialogs.AddToCalendarDialog
import java.util.*



class DialogManagers(private val context: Context) {

    private lateinit var folderAdapter: FolderAdapter
    private lateinit var noteAdapter: NoteAdapter
    private var activity = (context as MainActivity)
    private var firstEnteredPassword = ""
    private var isPasswordEntered = false

    enum class Target {
        FOLDER,
        NOTE
    }

    // 얘들, 인터페이스로 만들어서 필요한 순간에만 참조하는 방식으로 수정하는게 더 맞는 방법으로 생각된다.
    fun setNoteAdapter(noteAdapter: NoteAdapter) { this.noteAdapter = noteAdapter }
    fun setFolderAdapter(folderAdapter: FolderAdapter) { this.folderAdapter = folderAdapter }

    // 노트를 전달받는 형태도 갠춘할거 같음.
    fun showDialog(dialog: DialogType, target: Target = Target.NOTE) {
        when (dialog) {
            DialogType.ADD_FOLDER -> "showAddFolderDialog()"
            DialogType.FOLDER_OPTIONS -> showFolderOptionsDialog()
            DialogType.MORE_OPTIONS -> showMoreOptionDialog()
            DialogType.REQUEST_PASSWORD -> ""
            DialogType.SET_PASSWORD -> showSetPasswordDialog(target)
            DialogType.SORT -> showSortDialog()
        }
    }

    private fun showFolderOptionsDialog() {
        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_folder_options)
        val spinner = dialog.findViewById<Spinner>(R.id.spinner)
        setSpinner(spinner, folderAdapter.folders)
        dialog.findViewById<Button>(R.id.button_move).setOnClickListener {
            val folder = folderAdapter.getFolderByName(spinner.selectedItem as String)
            folderAdapter.moveNoteToFolder(noteAdapter.selectedNote, folder)
            (context as MainActivity).showToast("${noteAdapter.selectedNote!!.title}--폴더로 이동하였습니다.")
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.button_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showMoreOptionDialog() {
        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_more_options)
        val lockLayout = dialog.findViewById<LinearLayout>(R.id.linear_layout_lock)
        lockLayout.setOnClickListener {
            showSetPasswordDialog(Target.NOTE)
            dialog.dismiss()
        }

        ///
        val itemAddToCalendar = dialog.findViewById<LinearLayout>(R.id.item_add_to_calendar)
        itemAddToCalendar.setOnClickListener {
            //val addToCalendarDialog = AddToCalendarDialog(activity, noteAdapter.selectedNote!!)
            AddToCalendarDialog(activity, noteAdapter.selectedNote!!).show() // 이런 형태로 받아오는 형태.
        }


        dialog.show()
    }

    // 너무 기능적인 부분에 관여하는 느낌이 없지않아 있네.
    // 콜백 써서 필요한 순간에만 어뎁터 등의 객체 참조하는 방식이 더 이상적일듯.
    private fun showSetPasswordDialog(target: Target) {
        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_set_password)

        val editText = dialog.findViewById<EditText>(R.id.edit_text_password)
        val textView = dialog.findViewById<TextView>(R.id.text_view_set_password)
        dialog.findViewById<Button>(R.id.button_enter_password).setOnClickListener {
            if (isPasswordEntered) reconfirmPassword(dialog, editText)
            else getEnteredPassword(editText, textView)
        }

        dialog.show()
    }

    private fun getEnteredPassword(editText: EditText, textView: TextView) {
        if (editText.text.isBlank()) activity.showToast("비밀번호를 입력해주세요.")
        else {
            firstEnteredPassword = editText.text.toString()
            editText.setText("")
            textView.text = "비밀번호를 확인해주세요."
            isPasswordEntered = true
        }
    }

    // 이름 애매하네.
    private fun reconfirmPassword(dialog: Dialog, editText: EditText) {
        val password = editText.text.toString()
        if (password == firstEnteredPassword) {
            isPasswordEntered = false
            setPassword(Target.NOTE, password)
            dialog.dismiss()
        } else {
            activity.showToast("비밀번호가 일치하지 않습니다.\n다시 확인해주세요.")
        }
    }

    private fun setPassword(target: Target, password: String) {
        if (target == Target.FOLDER) {

        } else if (target == Target.NOTE) {
            noteAdapter.selectedNote?.isLocked = true
            noteAdapter.selectedNote?.password = password
            activity.viewModel.update(noteAdapter.selectedNote!!)
        }
        activity.showToast("비밀번호가 설정되었습니다.")
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

    /*
    private fun showRequestPasswordDialog() {
        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_set_password)

        val editText = dialog.findViewById<EditText>(R.id.edit_text_password)
        dialog.findViewById<Button>(R.id.button_enter_password).setOnClickListener {
            val password = editText.text.toString()
            if (password.isBlank()) activity.showToast("비밀번호를 입력해주세요.")
            else confirmPassword(dialog, noteAdapter.selectedNote!!, password)
        }

        dialog.show()
    }

     */

    private fun confirmPassword(dialog: Dialog, note: Note, password: String) {
        if (note.password == password) activity.startEditFragment(note)
        else activity.showToast("비밀번호가 일치하지 않습니다.")

        dialog.dismiss()
    }

    companion object {
        enum class DialogType {
            ADD_FOLDER,
            FOLDER_OPTIONS,
            MORE_OPTIONS,
            REQUEST_PASSWORD,
            SET_PASSWORD,
            SORT
        }
    }
}