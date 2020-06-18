package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.EditActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.adapters.FolderAdapter
import com.elliot.kim.kotlin.dimcatcamnote.adapters.NoteAdapter

class ConfirmPasswordDialogFragment(private val adapter: Any,
                                    private var activity: AppCompatActivity,
                                    private val forUnlocking: Boolean = false)
    : DialogFragment() {

    private lateinit var editText: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = object : Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                if (requireActivity() is EditActivity)
                    activity.finish()
                else super.onBackPressed()
            }
        }

        dialog.setContentView(R.layout.dialog_fragment_set_password)

        if (activity is EditActivity)
            dialog.setCanceledOnTouchOutside(false)

        val setPasswordContainer = dialog.findViewById<RelativeLayout>(R.id.set_password_container)
        val textView = dialog.findViewById<TextView>(R.id.text_view_title)
        editText = dialog.findViewById(R.id.edit_text_password)
        val button = dialog.findViewById<Button>(R.id.button_enter_password)

        // Apply design
        if (activity is MainActivity) {
            setPasswordContainer.setBackgroundColor(MainActivity.backgroundColor)
            textView.setBackgroundColor(MainActivity.toolbarColor)
            button.setBackgroundColor(MainActivity.toolbarColor)

            textView.adjustDialogTitleTextSize(MainActivity.fontId)
            editText.adjustDialogInputTextSize(MainActivity.fontId)
            button.adjustDialogButtonTextSize(MainActivity.fontId)

            textView.typeface = MainActivity.font
            editText.typeface = MainActivity.font
            button.typeface = MainActivity.font
        } else {
            val preferences =
                activity.getSharedPreferences(PREFERENCES_SET_COLOR, Context.MODE_PRIVATE)
            setPasswordContainer.setBackgroundColor(preferences.getInt(KEY_COLOR_BACKGROUND,
                activity.getColor(R.color.defaultColorBackground)))
            textView.setBackgroundColor(preferences.getInt(KEY_COLOR_TOOLBAR,
                activity.getColor(R.color.defaultColorToolbar)))
            button.setBackgroundColor(preferences.getInt(KEY_COLOR_TOOLBAR,
                activity.getColor(R.color.defaultColorToolbar)))

            val fontPreferences =
                activity.getSharedPreferences(PREFERENCES_FONT, Context.MODE_PRIVATE)
            val fontId = fontPreferences.getInt(KEY_FONT_ID, DEFAULT_FOLDER_ID)

            val font = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                resources.getFont(fontId)
            else ResourcesCompat.getFont(requireContext(), fontId)

            textView.adjustDialogTitleTextSize(fontId)
            editText.adjustDialogInputTextSize(fontId)
            button.adjustDialogButtonTextSize(fontId)

            textView.typeface = font
            editText.typeface = font
            button.typeface = font
        }

        button.setOnClickListener {
            val password = editText.text.toString()
            if (password.isBlank()) showToast(getString(R.string.password_request))
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
                        (activity as MainActivity).showCurrentFolderItems(adapter.selectedFolder!!)

                        // Close the navigation drawer.
                        (activity as MainActivity).onBackPressed()
                    }

                    dialog.dismiss()
                } else {
                    showToast(getString(R.string.password_mismatch))
                    editText.text = null
                }
            }
            is NoteAdapter -> {
                if (adapter.selectedNote!!.password == password) {
                    if (activity is EditActivity) {
                        sendConfirmationToEditActivity()
                    } else {
                        // MainActivity
                        (activity as MainActivity).startEditFragment()
                    }
                    dialog.dismiss()
                }
                else {
                    showToast(getString(R.string.password_mismatch))
                    editText.text = null
                }
            }
            else -> throw RuntimeException()
        }
    }

    private fun unlockFolder() {
        (adapter as FolderAdapter).selectedFolder!!.isLocked = false
        adapter.selectedFolder!!.password = ""
        adapter.update(adapter.selectedFolder!!)
        showToast(getString(R.string.folder_unlock_notification))
    }

    private fun sendConfirmationToEditActivity() {
        val intent = Intent(EditActivity.ACTION_PASSWORD_CONFIRMED)
        intent.putExtra(KEY_PASSWORD_CONFIRMED, true)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    private fun showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(requireContext(), text, duration).show()
    }
}
