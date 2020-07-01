package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity

class SetTextColorDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val preferences = requireContext().getSharedPreferences(
            PREFERENCES_SET_COLOR,
            Context.MODE_PRIVATE
        )
        val editor = preferences.edit()

        val dialog = Dialog(context as MainActivity)
        dialog.setContentView(R.layout.dialog_fragment_set_text_color)

        val titleTextView = dialog.findViewById<TextView>(R.id.text_view)
        titleTextView.text = "폰트 색상 설정"
        titleTextView.setBackgroundColor(MainActivity.toolbarColor)
        titleTextView.adjustDialogTitleTextSize(MainActivity.fontId, -2f)
        titleTextView.typeface = MainActivity.font

        val checkedRadioButtonId =
            preferences.getInt(KEY_SET_FONT_COLOR_CHECKED_RADIO_BUTTON_ID, R.id.radio_button_set_in_black)

        val radioGroup = dialog.findViewById<RadioGroup>(R.id.radio_group)
        radioGroup.check(checkedRadioButtonId)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.radio_button_set_in_black -> {
                    MainActivity.fontColor = (context as MainActivity).getColor(R.color.colorTextBlack)
                }
                R.id.radio_button_set_in_red -> {
                    MainActivity.fontColor = (context as MainActivity).getColor(R.color.colorTextRed)
                }
                R.id.radio_button_set_in_pink -> {
                    MainActivity.fontColor = (context as MainActivity).getColor(R.color.colorTextPink)
                }
                R.id.radio_button_set_in_purple -> {
                    MainActivity.fontColor = (context as MainActivity).getColor(R.color.colorTextPurple)
                }
                R.id.radio_button_set_in_deep_purple -> {
                    MainActivity.fontColor = (context as MainActivity).getColor(R.color.colorTextDeepPurple)
                }
                R.id.radio_button_set_in_indigo -> {
                    MainActivity.fontColor = (context as MainActivity).getColor(R.color.colorTextIndigo)
                }
                R.id.radio_button_set_in_blue -> {
                    MainActivity.fontColor = (context as MainActivity).getColor(R.color.colorTextBlue)
                }
                R.id.radio_button_set_in_light_blue -> {
                    MainActivity.fontColor = (context as MainActivity).getColor(R.color.colorTextLightBlue)
                }
                R.id.radio_button_set_in_cyan -> {
                    MainActivity.fontColor = (context as MainActivity).getColor(R.color.colorTextCyan)
                }
                R.id.radio_button_set_in_teal -> {
                    MainActivity.fontColor = (context as MainActivity).getColor(R.color.colorTextTeal)
                }
                R.id.radio_button_set_in_green -> {
                    MainActivity.fontColor = (context as MainActivity).getColor(R.color.colorTextGreen)
                }
                R.id.radio_button_set_in_light_green -> {
                    MainActivity.fontColor = (context as MainActivity).getColor(R.color.colorTextLightGreen)
                }
                R.id.radio_button_set_in_lime -> {
                    MainActivity.fontColor = (context as MainActivity).getColor(R.color.colorTextLime)
                }
                R.id.radio_button_set_in_yellow -> {
                    MainActivity.fontColor = (context as MainActivity).getColor(R.color.colorTextYellow)
                }
                R.id.radio_button_set_in_amber -> {
                    MainActivity.fontColor = (context as MainActivity).getColor(R.color.colorTextAmber)
                }
                R.id.radio_button_set_in_orange -> {
                    MainActivity.fontColor = (context as MainActivity).getColor(R.color.colorTextOrange)
                }
                R.id.radio_button_set_in_deep_orange -> {
                    MainActivity.fontColor = (context as MainActivity).getColor(R.color.colorTextDeepOrange)
                }
                R.id.radio_button_set_in_brown -> {
                    MainActivity.fontColor = (context as MainActivity).getColor(R.color.colorTextBrown)
                }
                R.id.radio_button_set_in_grey -> {
                    MainActivity.fontColor = (context as MainActivity).getColor(R.color.colorTextGrey)
                }
                R.id.radio_button_set_in_blue_grey -> {
                    MainActivity.fontColor = (context as MainActivity).getColor(R.color.colorTextBlueGrey)
                }
            }

            editor.putInt(KEY_COLOR_FONT, MainActivity.fontColor)
            editor.putInt(KEY_SET_FONT_COLOR_CHECKED_RADIO_BUTTON_ID, radioGroup.checkedRadioButtonId)
            editor.apply()

            (activity as MainActivity).getNoteAdapter().setFontColor(MainActivity.fontColor)
            (activity as MainActivity).getNoteAdapter().notifyDataSetChanged()
        }

        return dialog
    }
}