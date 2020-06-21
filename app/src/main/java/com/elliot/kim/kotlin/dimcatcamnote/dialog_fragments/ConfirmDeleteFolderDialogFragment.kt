package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.adapters.FolderAdapter

class ConfirmDeleteFolderDialogFragment(private val folderAdapter: FolderAdapter) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_fragment_confirm_delete_folder)

        val container = dialog.findViewById<RelativeLayout>(R.id.dialog_fragment_container)
        val textViewTitle = dialog.findViewById<TextView>(R.id.text_view_title)
        textViewTitle.text = folderAdapter.selectedFolder!!.name
        val textViewMessage = dialog.findViewById<TextView>(R.id.text_view_message)
        val buttonRemain = dialog.findViewById<Button>(R.id.button_remain)
        val buttonDelete = dialog.findViewById<Button>(R.id.button_delete)

        container.setBackgroundColor(MainActivity.backgroundColor)
        textViewTitle.setBackgroundColor(MainActivity.toolbarColor)
        buttonRemain.setBackgroundColor(MainActivity.toolbarColor)
        buttonDelete.setBackgroundColor(MainActivity.toolbarColor)

        textViewTitle.adjustDialogTitleTextSize(MainActivity.fontId)
        textViewMessage.adjustDialogItemTextSize(MainActivity.fontId)
        buttonRemain.adjustDialogButtonTextSize(MainActivity.fontId)
        buttonDelete.adjustDialogButtonTextSize(MainActivity.fontId)

        textViewTitle.typeface = MainActivity.font
        textViewMessage.typeface = MainActivity.font
        buttonRemain.typeface = MainActivity.font
        buttonDelete.typeface = MainActivity.font

        dialog.findViewById<Button>(R.id.button_remain).setOnClickListener {
            folderAdapter.removeSelectedFolder()
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.button_delete).setOnClickListener {
            folderAdapter.removeSelectedFolder()
            dialog.dismiss()
        }

        return dialog
    }
}