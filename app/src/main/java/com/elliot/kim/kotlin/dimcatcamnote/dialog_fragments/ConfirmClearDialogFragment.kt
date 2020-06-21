package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.adjustDialogButtonTextSize
import com.elliot.kim.kotlin.dimcatcamnote.adjustDialogItemTextSize
import com.elliot.kim.kotlin.dimcatcamnote.adjustDialogTitleTextSize

class ConfirmClearDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_fragment_confirm_clear)

        val container = dialog.findViewById<RelativeLayout>(R.id.dialog_fragment_container)
        val textViewTitle = dialog.findViewById<TextView>(R.id.text_view_title)
        val textViewMessage = dialog.findViewById<TextView>(R.id.text_view_message)
        val buttonCancel = dialog.findViewById<Button>(R.id.button_cancel)
        val buttonOk = dialog.findViewById<Button>(R.id.button_ok)

        container.setBackgroundColor(MainActivity.backgroundColor)
        textViewTitle.setBackgroundColor(MainActivity.toolbarColor)
        buttonCancel.setBackgroundColor(MainActivity.toolbarColor)
        buttonOk.setBackgroundColor(MainActivity.toolbarColor)

        textViewTitle.adjustDialogTitleTextSize(MainActivity.fontId)
        textViewMessage.adjustDialogItemTextSize(MainActivity.fontId)
        buttonCancel.adjustDialogButtonTextSize(MainActivity.fontId)
        buttonOk.adjustDialogButtonTextSize(MainActivity.fontId)

        textViewTitle.typeface = MainActivity.font
        textViewMessage.typeface = MainActivity.font
        buttonCancel.typeface = MainActivity.font
        buttonOk.typeface = MainActivity.font

        dialog.findViewById<Button>(R.id.button_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.button_ok).setOnClickListener {
            (requireActivity() as MainActivity).clear()
            dialog.dismiss()
        }

        return dialog
    }
}