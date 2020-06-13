package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.adapters.FolderAdapter
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.R

class AddFolderDialogFragment(private val folderAdapter: FolderAdapter) : DialogFragment() {

    private lateinit var activity: MainActivity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity = requireActivity() as MainActivity

        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_fragment_add_folder)

        val textViewTitle = dialog.findViewById<TextView>(R.id.text_view_title)
        val editText = dialog.findViewById<EditText>(R.id.edit_text)
        val button = dialog.findViewById<Button>(R.id.button)


        // 어캐하는 지 확인... 다듬기는 .. 시바 언제하지..
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            textViewTitle.typeface = resources.getFont(R.font.reko)
            editText.typeface = resources.getFont(R.font.reko)
            button.typeface = resources.getFont(R.font.reko)
        }
        else {
            textViewTitle.typeface = ResourcesCompat.getFont(activity, R.font.reko)
            editText.typeface = ResourcesCompat.getFont(activity, R.font.reko)
            button.typeface = ResourcesCompat.getFont(activity, R.font.reko)
        }

        // Set color
        textViewTitle.setBackgroundColor(MainActivity.toolbarColor)
        editText.setBackgroundColor(MainActivity.backgroundColor)
        button.setBackgroundColor(MainActivity.toolbarColor)

        button.setOnClickListener {
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