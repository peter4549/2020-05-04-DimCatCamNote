package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity

class SetInlayColorDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = Dialog(context as MainActivity)
        dialog.setContentView(R.layout.dialog_fragment_set_color)

        val preferences = requireContext().getSharedPreferences(
            PREFERENCES_SET_COLOR,
            Context.MODE_PRIVATE
        )
        val editor = preferences.edit()

        val checkedRadioButtonId =
            preferences.getInt(KEY_SET_INLAY_COLOR_CHECKED_RADIO_BUTTON_ID, R.id.radio_button_set_in_yellow)

        val radioGroup = dialog.findViewById<RadioGroup>(R.id.radio_group)
        radioGroup.check(checkedRadioButtonId)

        dialog.findViewById<RadioGroup>(R.id.radio_group).setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.radio_button_set_in_red ->
                    MainActivity.inlayColor = (context as MainActivity).getColor(R.color.backgroundColorRed)
                R.id.radio_button_set_in_pink ->
                    MainActivity.inlayColor = (context as MainActivity).getColor(R.color.backgroundColorPink)
                R.id.radio_button_set_in_purple ->
                    MainActivity.inlayColor = (context as MainActivity).getColor(R.color.backgroundColorPurple)
                R.id.radio_button_set_in_deep_purple ->
                    MainActivity.inlayColor = (context as MainActivity).getColor(R.color.backgroundColorDeepPurple)
                R.id.radio_button_set_in_indigo ->
                    MainActivity.inlayColor = (context as MainActivity).getColor(R.color.backgroundColorIndigo)
                R.id.radio_button_set_in_blue ->
                    MainActivity.inlayColor = (context as MainActivity).getColor(R.color.backgroundColorBlue)
                R.id.radio_button_set_in_light_blue ->
                    MainActivity.inlayColor = (context as MainActivity).getColor(R.color.backgroundColorLightBlue)
                R.id.radio_button_set_in_cyan ->
                    MainActivity.inlayColor = (context as MainActivity).getColor(R.color.backgroundColorCyan)
                R.id.radio_button_set_in_teal ->
                    MainActivity.inlayColor = (context as MainActivity).getColor(R.color.backgroundColorTeal)
                R.id.radio_button_set_in_green ->
                    MainActivity.inlayColor = (context as MainActivity).getColor(R.color.backgroundColorGreen)
                R.id.radio_button_set_in_light_green ->
                    MainActivity.inlayColor = (context as MainActivity).getColor(R.color.backgroundColorLightGreen)
                R.id.radio_button_set_in_lime ->
                    MainActivity.inlayColor = (context as MainActivity).getColor(R.color.backgroundColorLime)
                R.id.radio_button_set_in_yellow ->
                    MainActivity.inlayColor = (context as MainActivity).getColor(R.color.backgroundColorYellow)
                R.id.radio_button_set_in_amber ->
                    MainActivity.inlayColor = (context as MainActivity).getColor(R.color.backgroundColorAmber)
                R.id.radio_button_set_in_orange ->
                    MainActivity.inlayColor = (context as MainActivity).getColor(R.color.backgroundColorOrange)
                R.id.radio_button_set_in_deep_orange ->
                    MainActivity.inlayColor = (context as MainActivity).getColor(R.color.backgroundColorDeepOrange)
            }
            editor.putInt(KEY_COLOR_INLAY, MainActivity.backgroundColor)
            editor.putInt(KEY_SET_INLAY_COLOR_CHECKED_RADIO_BUTTON_ID, radioGroup.checkedRadioButtonId)
            editor.apply()
        }

        return dialog
    }
}