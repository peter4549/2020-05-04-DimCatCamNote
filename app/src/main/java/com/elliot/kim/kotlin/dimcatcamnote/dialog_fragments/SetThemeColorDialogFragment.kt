package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.RadioGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.*

import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity

class SetThemeColorDialogFragment(private val toolbar: Toolbar?) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val preferences = requireContext().getSharedPreferences(
            PREFERENCES_SET_COLOR,
            Context.MODE_PRIVATE
        )
        val editor = preferences.edit()

        val dialog = Dialog(context as MainActivity)
        dialog.setContentView(R.layout.dialog_fragment_set_color)

        val radioGroup = dialog.findViewById<RadioGroup>(R.id.radio_group)
        val checkedRadioButtonId =
            preferences.getInt(KEY_SET_THEME_COLOR_CHECKED_RADIO_BUTTON_ID, R.id.radio_button_set_in_purple)
        radioGroup.check(checkedRadioButtonId)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.radio_button_set_in_red -> {
                    MainActivity.toolbarColor =
                        (context as MainActivity).getColor(R.color.colorToolbarRed)
                    MainActivity.backgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorRed)
                }
                R.id.radio_button_set_in_pink ->{
                    MainActivity.toolbarColor =
                        (context as MainActivity).getColor(R.color.colorToolbarPink)
                    MainActivity.backgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorPink)
                }
                R.id.radio_button_set_in_purple -> {
                    MainActivity.toolbarColor =
                        (context as MainActivity).getColor(R.color.colorToolbarPurple)
                    MainActivity.backgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorPurple)
                }
                R.id.radio_button_set_in_deep_purple -> {
                    MainActivity.toolbarColor =
                        (context as MainActivity).getColor(R.color.colorToolbarDeepPurple)
                    MainActivity.backgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorDeepPurple)
                }
                R.id.radio_button_set_in_indigo -> {
                    MainActivity.toolbarColor =
                        (context as MainActivity).getColor(R.color.colorToolbarIndigo)
                    MainActivity.backgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorIndigo)
                }
                R.id.radio_button_set_in_blue -> {
                    MainActivity.toolbarColor =
                        (context as MainActivity).getColor(R.color.colorToolbarBlue)
                    MainActivity.backgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorBlue)
                }
                R.id.radio_button_set_in_light_blue -> {
                    MainActivity.toolbarColor =
                        (context as MainActivity).getColor(R.color.colorToolbarLightBlue)
                    MainActivity.backgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorLightBlue)
                }
                R.id.radio_button_set_in_cyan -> {
                    MainActivity.toolbarColor =
                        (context as MainActivity).getColor(R.color.colorToolbarCyan)
                    MainActivity.backgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorCyan)
                }
                R.id.radio_button_set_in_teal -> {
                    MainActivity.toolbarColor =
                        (context as MainActivity).getColor(R.color.colorToolbarTeal)
                    MainActivity.backgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorTeal)
                }
                R.id.radio_button_set_in_green -> {
                    MainActivity.toolbarColor =
                        (context as MainActivity).getColor(R.color.colorToolbarGreen)
                    MainActivity.backgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorGreen)
                }
                R.id.radio_button_set_in_light_green -> {
                    MainActivity.toolbarColor =
                        (context as MainActivity).getColor(R.color.colorToolbarLightGreen)
                    MainActivity.backgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorLightGreen)
                }
                R.id.radio_button_set_in_lime -> {
                    MainActivity.toolbarColor =
                        (context as MainActivity).getColor(R.color.colorToolbarLime)
                    MainActivity.backgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorLime)
                }
                R.id.radio_button_set_in_yellow -> {
                    MainActivity.toolbarColor =
                        (context as MainActivity).getColor(R.color.colorToolbarYellow)
                    MainActivity.backgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorYellow)
                }
                R.id.radio_button_set_in_amber -> {
                    MainActivity.toolbarColor =
                        (context as MainActivity).getColor(R.color.colorToolbarAmber)
                    MainActivity.backgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorAmber)
                }
                R.id.radio_button_set_in_orange -> {
                    MainActivity.toolbarColor =
                        (context as MainActivity).getColor(R.color.colorToolbarOrange)
                    MainActivity.backgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorOrange)
                }
                R.id.radio_button_set_in_deep_orange -> {
                    MainActivity.toolbarColor =
                        (context as MainActivity).getColor(R.color.colorToolbarDeepOrange)
                    MainActivity.backgroundColor =
                        (context as MainActivity).getColor(R.color.backgroundColorDeepOrange)
                }
            }

            toolbar!!.setBackgroundColor(MainActivity.toolbarColor)
            (activity as MainActivity).setViewColor()
            (activity as MainActivity).setNavigationDrawerColor()

            editor.putInt(KEY_COLOR_TOOLBAR, MainActivity.toolbarColor)
            editor.putInt(KEY_COLOR_BACKGROUND, MainActivity.backgroundColor)
            editor.putInt(KEY_SET_THEME_COLOR_CHECKED_RADIO_BUTTON_ID, radioGroup.checkedRadioButtonId)
            editor.apply()
        }

        return dialog
    }
}
