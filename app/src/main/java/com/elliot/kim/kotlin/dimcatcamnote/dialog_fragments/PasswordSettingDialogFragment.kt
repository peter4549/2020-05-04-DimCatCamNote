package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.*
import kotlin.RuntimeException

class PasswordSettingDialogFragment(private val adapter: Any) : DialogFragment() {

    private var firstEnteredPassword: String? = null
    private var isPasswordEntered = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_set_password)

        val editText = dialog.findViewById<EditText>(R.id.edit_text_password)
        val textView = dialog.findViewById<TextView>(R.id.text_view_set_password)

        dialog.findViewById<Button>(R.id.button_enter_password).setOnClickListener {
            if (isPasswordEntered) reconfirmPassword(dialog, editText)
            else getEnteredPassword(editText, textView)
        }

        return dialog
    }

    private fun getEnteredPassword(editText: EditText, textView: TextView) {
        if (editText.text.isBlank()) (activity as MainActivity).showToast("비밀번호를 입력해주세요.")
        else {
            firstEnteredPassword = editText.text.toString()
            editText.setText("")
            textView.text = "비밀번호를 확인해주세요."
            isPasswordEntered = true
        }
    }

    private fun reconfirmPassword(dialog: Dialog, editText: EditText) {
        val password = editText.text.toString()
        if (password == firstEnteredPassword) {
            isPasswordEntered = false
            setPassword(password)
            dialog.dismiss()
        } else {
            (activity as MainActivity).showToast("비밀번호가 일치하지 않습니다.\n다시 확인해주세요.")
        }
    }

    private fun setPassword(password: String) {
        when (adapter) {
            is FolderAdapter -> setFolderPassword(password)
            is NoteAdapter -> setNotePassword(password)
            else -> throw RuntimeException()
        }

        (activity as MainActivity).showToast("비밀번호가 설정되었습니다.")
    }

    private fun setFolderPassword(password: String) {
        (adapter as FolderAdapter).selectedFolder?.isLocked = true
        adapter.selectedFolder?.password = password
        adapter.update(adapter.selectedFolder!!)
    }

    private fun setNotePassword(password: String) {
        (adapter as NoteAdapter).selectedNote?.isLocked = true
        adapter.selectedNote?.password = password
        (activity as MainActivity).viewModel.update(adapter.selectedNote!!)
    }
}