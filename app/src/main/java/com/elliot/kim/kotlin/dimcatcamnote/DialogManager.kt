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

// 다이얼로그 들을 배열로 가진 형태로 변경할 것. 각 기능을 갖는 다이얼로그들은 개별적으로 작성하는 형태로. 굿.

class DialogManager(private val context: Context) {

    private lateinit var folderAdapter: FolderAdapter
    private lateinit var folderManager: FolderManager
    private lateinit var noteAdapter: NoteAdapter
    private var activity = (context as MainActivity)
    private var firstEnteredPassword = ""
    private var isPasswordEntered = false

    // 얘들, 인터페이스로 만들어서 필요한 순간에만 참조하는 방식으로 수정하는게 더 맞는 방법으로 생각된다.
    fun setFolderManager(folderManager: FolderManager) { this.folderManager = folderManager }
    fun setNoteAdapter(noteAdapter: NoteAdapter) { this.noteAdapter = noteAdapter }
    fun setFolderAdapter(folderAdapter: FolderAdapter) { this.folderAdapter = folderAdapter }

    // 노트를 전달받는 형태도 갠춘할거 같음.
    fun showDialog(dialog: DialogType) {
        when (dialog) {
            DialogType.ADD_FOLDER -> showAddFolderDialog()
            DialogType.FOLDER_OPTIONS -> showFolderOptionsDialog()
            DialogType.MORE_OPTIONS -> showMoreOptionDialog()
            DialogType.REQUEST_PASSWORD -> showRequestPasswordDialog()
            DialogType.SET_PASSWORD -> showSetPasswordDialog()
            DialogType.SORT -> showSortDialog()
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
            val folder = folderManager.getFolderByName(spinner.selectedItem as String)
            folderManager.moveNoteToFolder(noteAdapter.selectedNote, folder)
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
            showSetPasswordDialog()
            dialog.dismiss()
        }

        ///
        val itemAddToCalendar = dialog.findViewById<LinearLayout>(R.id.item_add_to_calendar)
        itemAddToCalendar.setOnClickListener {
            //val addToCalendarDialog = AddToCalendarDialog(activity, noteAdapter.selectedNote!!)
            AddToCalendarDialog(activity, noteAdapter.selectedNote!!).show()
        }


        dialog.show()
    }

    // 너무 기능적인 부분에 관여하는 느낌이 없지않아 있네.
    // 콜백 써서 필요한 순간에만 어뎁터 등의 객체 참조하는 방식이 더 이상적일듯.
    private fun showSetPasswordDialog() {
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
            noteAdapter.selectedNote?.isLocked = true
            noteAdapter.selectedNote?.password = password
            activity.viewModel.update(noteAdapter.selectedNote!!)
            activity.showToast("비밀번호가 설정되었습니다.")
            dialog.dismiss()
        } else {
            activity.showToast("비밀번호가 일치하지 않습니다.\n다시 확인해주세요.")
        }
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