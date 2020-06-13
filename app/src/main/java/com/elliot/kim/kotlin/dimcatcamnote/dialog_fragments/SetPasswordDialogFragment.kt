package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.EditActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.adapters.FolderAdapter
import com.elliot.kim.kotlin.dimcatcamnote.adapters.NoteAdapter
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import java.lang.Exception
import kotlin.RuntimeException

class SetPasswordDialogFragment(private val adapter: Any) : DialogFragment() {

    private var firstEnteredPassword: String? = null
    private var isPasswordEntered = false
    private lateinit var activity: Activity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity = when(requireActivity()) {
            is MainActivity -> requireActivity() as MainActivity
            is EditActivity -> requireActivity() as EditActivity
            else -> throw Exception()
        }

        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_fragment_set_password)

        val editText = dialog.findViewById<EditText>(R.id.edit_text_password)
        val textView = dialog.findViewById<TextView>(R.id.text_view_title)
        val button = dialog.findViewById<Button>(R.id.button_enter_password)

        textView.setBackgroundColor(MainActivity.toolbarColor)
        editText.setBackgroundColor(MainActivity.backgroundColor)
        button.setBackgroundColor(MainActivity.toolbarColor)

        button.setOnClickListener {
            if (isPasswordEntered) reconfirmPassword(dialog, editText)
            else getEnteredPassword(editText, textView)
        }

        return dialog
    }

    private fun getEnteredPassword(editText: EditText, textView: TextView) {
        if (editText.text.isBlank()) showToast(activity, getString(R.string.password_request))
        else {
            firstEnteredPassword = editText.text.toString()
            editText.text = null
            editText.hint = getString(R.string.re_request_password)
            textView.text = getString(R.string.password_confirmation)
            isPasswordEntered = true
        }
    }

    private fun reconfirmPassword(dialog: Dialog, editText: EditText) {
        val password = editText.text.toString()
        if (password == firstEnteredPassword) {
            isPasswordEntered = false
            setPassword(password)
            dialog.dismiss()
        } else showToast(activity, getString(R.string.password_mismatch))
    }

    private fun setPassword(password: String) {
        when (adapter) {
            is FolderAdapter -> setFolderPassword(password)
            is NoteAdapter -> setNotePassword(password)
            else -> throw RuntimeException()
        }

        showToast(activity, getString(R.string.password_set_notification))
    }

    private fun setFolderPassword(password: String) {
        (adapter as FolderAdapter).selectedFolder?.isLocked = true
        adapter.selectedFolder?.password = password
        adapter.update(adapter.selectedFolder!!)
    }

    private fun setNotePassword(password: String) {
        (adapter as NoteAdapter).selectedNote?.isLocked = true
        adapter.selectedNote?.password = password
        update(activity, adapter.selectedNote!!)
    }

    private fun update(activity: Activity, note: Note) {
        when(activity) {
            is EditActivity -> activity.viewModel.update(note)
            is MainActivity -> activity.viewModel.update(note)
            else -> throw Exception("Invalid function call.")
        }
    }

    private fun showToast(context: Context, text: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, text, duration).show()
    }
}