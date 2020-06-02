package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment

import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity

class ThemeOptionsDialogFragment : DialogFragment() {

    private lateinit var activity: MainActivity

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity = requireActivity() as MainActivity

        // 쫌 잘못됫는데,, 아래 내용은 setting color dialog가 되야 하고
        // 본 다이얼로그는 폰트, 칼러 즉, 아래 옵션을 띄워주는 다이얼로그로 변경해야함. 뭐 이런식으로 이건됫고.
        // 바로 변경하면서 보여주는게 어떻게 적용되었는지 보여줄것.
        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.dialog_fragment_color_selection)
        dialog.findViewById<RadioGroup>(R.id.radio_group).setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.radio_button_set_in_red ->
                    DialogFragmentManager.themeColor = activity.getColor(R.color.colorAmberfff8e1)
                R.id.radio_button_set_in_blue ->
                    DialogFragmentManager.themeColor = activity.getColor(android.R.color.holo_blue_bright)
            }
        }

        return dialog
    }
}
