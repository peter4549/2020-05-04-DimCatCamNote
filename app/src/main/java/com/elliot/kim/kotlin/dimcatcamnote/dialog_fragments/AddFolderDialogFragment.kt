package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.FolderAdapter
import com.elliot.kim.kotlin.dimcatcamnote.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.R

class AddFolderDialogFragment(private val folderAdapter: FolderAdapter) : DialogFragment() {

    private lateinit var activity: MainActivity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity = requireActivity() as MainActivity

        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_fragment_add_folder)

        val editText = dialog.findViewById<EditText>(R.id.edit_text)

        dialog.findViewById<Button>(R.id.button).setOnClickListener {

            val folderName = editText.text.toString()
            if (folderName.isBlank())
                activity.showToast(getString(R.string.folder_name_request))
            else {
                folderAdapter.addFolder(folderName)
                dialog.dismiss()
            }

        }

        return dialog

    }

}