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
    private lateinit var activity: MainActivity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity = requireActivity() as MainActivity

        val dialog = Dialog(activity)
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
        if (editText.text.isBlank()) activity.showToast(PASSWORD_REQUEST_MESSAGE)
        else {
            firstEnteredPassword = editText.text.toString()
            editText.text = null
            textView.text = PASSWORD_CONFIRMATION_MESSAGE
            isPasswordEntered = true
        }
    }

    private fun reconfirmPassword(dialog: Dialog, editText: EditText) {
        val password = editText.text.toString()
        if (password == firstEnteredPassword) {
            isPasswordEntered = false
            setPassword(password)
            dialog.dismiss()
        } else activity.showToast(PASSWORD_MISMATCH_MESSAGE)
    }

    private fun setPassword(password: String) {
        when (adapter) {
            is FolderAdapter -> setFolderPassword(password)
            is NoteAdapter -> setNotePassword(password)
            else -> throw RuntimeException()
        }

        activity.showToast(PASSWORD_SETTING_NOTIFICATION_MESSAGE)
    }

    private fun setFolderPassword(password: String) {
        (adapter as FolderAdapter).selectedFolder?.isLocked = true
        adapter.selectedFolder?.password = password
        adapter.update(adapter.selectedFolder!!)
    }

    private fun setNotePassword(password: String) {
        (adapter as NoteAdapter).selectedNote?.isLocked = true
        adapter.selectedNote?.password = password
        activity.viewModel.update(adapter.selectedNote!!)
    }
}