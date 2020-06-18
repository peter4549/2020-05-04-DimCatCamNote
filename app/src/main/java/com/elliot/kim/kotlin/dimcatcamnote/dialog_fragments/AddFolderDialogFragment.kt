package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.adapters.FolderAdapter
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity

class AddFolderDialogFragment(private val folderAdapter: FolderAdapter) : DialogFragment() {

    private lateinit var activity: MainActivity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity = requireActivity() as MainActivity

        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_fragment_add_folder)

        val addFolderContainer = dialog.findViewById<LinearLayout>(R.id.add_folder_container)
        val textView = dialog.findViewById<TextView>(R.id.text_view_title)
        val editText = dialog.findViewById<EditText>(R.id.edit_text)
        val button = dialog.findViewById<Button>(R.id.button)

        // Apply design.
        textView.typeface = MainActivity.font
        editText.typeface = MainActivity.font
        button.typeface = MainActivity.font

        textView.adjustDialogTitleTextSize(MainActivity.fontId)
        editText.adjustDialogInputTextSize(MainActivity.fontId)
        button.adjustDialogButtonTextSize(MainActivity.fontId)

        // Color
        addFolderContainer.setBackgroundColor(MainActivity.backgroundColor)
        textView.setBackgroundColor(MainActivity.toolbarColor)
        button.setBackgroundColor(MainActivity.toolbarColor)

        button.setOnClickListener {
            val folderName = editText.text.toString()
            when {
                folderName.isBlank() -> activity.showToast(getString(R.string.folder_name_request))
                folderName in folderAdapter.getAllFolderNames() ->
                    activity.showToast("중복된 폴더이름을 사용할 수 없습니다.")
                else -> {
                    folderAdapter.addFolder(folderName)
                    dialog.dismiss()
                }
            }
        }

        return dialog
    }
}