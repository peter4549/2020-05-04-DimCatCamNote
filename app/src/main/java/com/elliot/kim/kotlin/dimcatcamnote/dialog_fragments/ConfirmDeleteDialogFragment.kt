package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity

class ConfirmDeleteDialogFragment : DialogFragment() {

    private lateinit var activity: MainActivity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity = requireActivity() as MainActivity

        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_fragment_confirm_delete)

        val editText = dialog.findViewById<EditText>(R.id.edit_text)

        val textViewTitle = dialog.findViewById<TextView>(R.id.text_view_title)
        textViewTitle.setBackgroundColor(themeColor)
        // 어캐하는 지 확인... 다듬기는 .. 시바 언제하지..
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            textViewTitle.typeface = resources.getFont(R.font.reko)
        else textViewTitle.typeface = ResourcesCompat.getFont(activity, R.font.reko)


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