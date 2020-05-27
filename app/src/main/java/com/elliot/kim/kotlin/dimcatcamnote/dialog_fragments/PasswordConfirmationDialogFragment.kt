package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.*

class PasswordConfirmationDialogFragment(private val adapter: Any,
                                         private val forUnlocking: Boolean = false)
    : DialogFragment() {

    private lateinit var activity: MainActivity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity = requireActivity() as MainActivity

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_set_password)

        val editText = dialog.findViewById<EditText>(R.id.edit_text_password)
        dialog.findViewById<Button>(R.id.button_enter_password).setOnClickListener {
            val password = editText.text.toString()
            if (password.isBlank()) activity.showToast("비밀번호를 입력해주세요.")
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
                } else {
                    activity.showToast("비밀번호가 일치하지 않습니다.")
                }
            }
            is NoteAdapter -> {
                if (adapter.selectedNote!!.password == password)
                    activity.startEditFragment(adapter.selectedNote!!)
                else
                    activity.showToast("비밀번호가 일치하지 않습니다.")
            }
            else -> throw RuntimeException()
        }

        dialog.dismiss()
    }

    private fun unlockFolder() {
        (adapter as FolderAdapter).selectedFolder!!.isLocked = false
        adapter.selectedFolder!!.password = ""
        adapter.update(adapter.selectedFolder!!)
        activity.showToast("폴더 잠금이 해제되었습니다.")
    }
}