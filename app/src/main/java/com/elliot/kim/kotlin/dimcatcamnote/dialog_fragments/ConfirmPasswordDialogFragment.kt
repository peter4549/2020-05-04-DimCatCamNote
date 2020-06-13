package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.adapters.FolderAdapter
import com.elliot.kim.kotlin.dimcatcamnote.adapters.NoteAdapter

class ConfirmPasswordDialogFragment(private val adapter: Any,
                                    private val forUnlocking: Boolean = false)
    : DialogFragment() {

    private lateinit var activity: MainActivity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity = requireActivity() as MainActivity

        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_fragment_set_password)

        val textView = dialog.findViewById<TextView>(R.id.text_view_title)
        val editText = dialog.findViewById<EditText>(R.id.edit_text_password)
        val button = dialog.findViewById<Button>(R.id.button_enter_password)

        textView.setBackgroundColor(MainActivity.toolbarColor)
        editText.setBackgroundColor(MainActivity.backgroundColor)
        button.setBackgroundColor(MainActivity.toolbarColor)

        dialog.findViewById<Button>(R.id.button_enter_password).setOnClickListener {
            val password = editText.text.toString()
            if (password.isBlank()) activity.showToast(getString(R.string.password_request))
            else confirmPassword(dialog, password)
        }

        return dialog
    }

    private fun confirmPassword(dialog: Dialog, password: String) {
        when (adapter) {
            is FolderAdapter -> {
                if (adapter.selectedFolder!!.password == password) {
                    if (forUnlocking)
                        unlockFolder()
                    else {
                        activity.showCurrentFolderItems(adapter.selectedFolder!!)

                        // Close the navigation drawer.
                        activity.onBackPressed()
                    }
                } else activity.showToast(getString(R.string.password_mismatch))
            }
            is NoteAdapter -> {
                if (adapter.selectedNote!!.password == password)
                    activity.startEditFragment()
                else
                    activity.showToast(getString(R.string.password_mismatch))
            }
            else -> throw RuntimeException()
        }

        dialog.dismiss()
    }

    private fun unlockFolder() {
        (adapter as FolderAdapter).selectedFolder!!.isLocked = false
        adapter.selectedFolder!!.password = ""
        adapter.update(adapter.selectedFolder!!)
        activity.showToast(getString(R.string.folder_unlock_notification))
    }
}