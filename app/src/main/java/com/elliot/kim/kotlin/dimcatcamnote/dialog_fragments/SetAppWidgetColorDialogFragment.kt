package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity

class SetAppWidgetColorDialogFragment : DialogFragment(){
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val preferences = requireContext().getSharedPreferences(
            PREFERENCES_SET_COLOR,
            Context.MODE_PRIVATE
        )
        val editor = preferences.edit()

        val dialog = Dialog(context as MainActivity)
        dialog.setContentView(R.layout.dialog_fragment_set_color)

        val checkedRadioButtonId =
            preferences.getInt(KEY_SET_APP_WIDGET_COLOR_CHECKED_RADIO_BUTTON_ID, R.id.radio_button_set_in_yellow)

        val radioGroup = dialog.findViewById<RadioGroup>(R.id.radio_group)
        radioGroup.check(checkedRadioButtonId)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.radio_button_set_in_red -> {
                    MainActivity.appWidgetTitleColor = (context as MainActivity).getColor(R.color.colorNoteRed)
                    MainActivity.appWidgetBackgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorRed)
                }
                R.id.radio_button_set_in_pink -> {
                    MainActivity.appWidgetTitleColor = (context as MainActivity).getColor(R.color.colorNotePink)
                    MainActivity.appWidgetBackgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorPink)
                }
                R.id.radio_button_set_in_purple -> {
                    MainActivity.appWidgetTitleColor = (context as MainActivity).getColor(R.color.colorNotePurple)
                    MainActivity.appWidgetBackgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorPurple)
                }
                R.id.radio_button_set_in_deep_purple -> {
                    MainActivity.appWidgetTitleColor = (context as MainActivity).getColor(R.color.colorNoteDeepPurple)
                    MainActivity.appWidgetBackgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorDeepPurple)
                }
                R.id.radio_button_set_in_indigo -> {
                    MainActivity.appWidgetTitleColor = (context as MainActivity).getColor(R.color.colorNoteIndigo)
                    MainActivity.appWidgetBackgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorIndigo)
                }
                R.id.radio_button_set_in_blue -> {
                    MainActivity.appWidgetTitleColor = (context as MainActivity).getColor(R.color.colorNoteBlue)
                    MainActivity.appWidgetBackgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorBlue)
                }
                R.id.radio_button_set_in_light_blue -> {
                    MainActivity.appWidgetTitleColor = (context as MainActivity).getColor(R.color.colorNoteLightBlue)
                    MainActivity.appWidgetBackgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorLightBlue)
                }
                R.id.radio_button_set_in_cyan -> {
                    MainActivity.appWidgetTitleColor = (context as MainActivity).getColor(R.color.colorNoteCyan)
                    MainActivity.appWidgetBackgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorCyan)
                }
                R.id.radio_button_set_in_teal -> {
                    MainActivity.appWidgetTitleColor = (context as MainActivity).getColor(R.color.colorNoteTeal)
                    MainActivity.appWidgetBackgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorTeal)
                }
                R.id.radio_button_set_in_green -> {
                    MainActivity.appWidgetTitleColor = (context as MainActivity).getColor(R.color.colorNoteGreen)
                    MainActivity.appWidgetBackgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorGreen)
                }
                R.id.radio_button_set_in_light_green -> {
                    MainActivity.appWidgetTitleColor = (context as MainActivity).getColor(R.color.colorNoteLightGreen)
                    MainActivity.appWidgetBackgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorLightGreen)
                }
                R.id.radio_button_set_in_lime -> {
                    MainActivity.appWidgetTitleColor = (context as MainActivity).getColor(R.color.colorNoteLime)
                    MainActivity.appWidgetBackgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorLime)
                }
                R.id.radio_button_set_in_yellow -> {
                    MainActivity.appWidgetTitleColor = (context as MainActivity).getColor(R.color.colorNoteYellow)
                    MainActivity.appWidgetBackgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorYellow)
                }
                R.id.radio_button_set_in_amber -> {
                    MainActivity.appWidgetTitleColor = (context as MainActivity).getColor(R.color.colorNoteAmber)
                    MainActivity.appWidgetBackgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorAmber)
                }
                R.id.radio_button_set_in_orange -> {
                    MainActivity.appWidgetTitleColor = (context as MainActivity).getColor(R.color.colorNoteOrange)
                    MainActivity.appWidgetBackgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorOrange)
                }
                R.id.radio_button_set_in_deep_orange -> {
                    MainActivity.appWidgetTitleColor = (context as MainActivity).getColor(R.color.colorNoteDeepOrange)
                    MainActivity.appWidgetBackgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorDeepOrange)
                }
            }
            editor.putInt(KEY_COLOR_APP_WIDGET_TITLE, MainActivity.appWidgetTitleColor)
            editor.putInt(KEY_COLOR_APP_WIDGET_BACKGROUND, MainActivity.appWidgetBackgroundColor)
            editor.putInt(KEY_SET_APP_WIDGET_COLOR_CHECKED_RADIO_BUTTON_ID, radioGroup.checkedRadioButtonId)
            editor.apply()

            (activity as MainActivity).getNoteAdapter().notifyDataSetChanged()
        }

        return dialog
    }
}