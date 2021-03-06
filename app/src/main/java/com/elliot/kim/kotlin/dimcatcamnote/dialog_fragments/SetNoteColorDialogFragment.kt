package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity

class SetNoteColorDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val preferences = requireContext().getSharedPreferences(
            PREFERENCES_SET_COLOR,
            Context.MODE_PRIVATE
        )
        val editor = preferences.edit()

        val dialog = Dialog(context as MainActivity)
        dialog.setContentView(R.layout.dialog_fragment_set_color)

        val titleTextView = dialog.findViewById<TextView>(R.id.text_view)
        titleTextView.text = "노트 색상 설정"
        titleTextView.setBackgroundColor(MainActivity.toolbarColor)
        titleTextView.adjustDialogTitleTextSize(MainActivity.fontId, -2f)
        titleTextView.typeface = MainActivity.font

        val checkedRadioButtonId =
            preferences.getInt(KEY_SET_NOTE_COLOR_CHECKED_RADIO_BUTTON_ID, R.id.radio_button_set_in_yellow)

        val radioGroup = dialog.findViewById<RadioGroup>(R.id.radio_group)
        radioGroup.getChildAt(0).visibility = View.VISIBLE  // White
        radioGroup.check(checkedRadioButtonId)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.radio_button_set_in_white -> {
                    MainActivity.noteColor = (context as MainActivity).getColor(android.R.color.white)
                }
                R.id.radio_button_set_in_red -> {
                    MainActivity.noteColor = (context as MainActivity).getColor(R.color.colorNoteRed)
                }
                R.id.radio_button_set_in_pink -> {
                    MainActivity.noteColor = (context as MainActivity).getColor(R.color.colorNotePink)
                }
                R.id.radio_button_set_in_purple -> {
                    MainActivity.noteColor = (context as MainActivity).getColor(R.color.colorNotePurple)
                }
                R.id.radio_button_set_in_deep_purple -> {
                    MainActivity.noteColor = (context as MainActivity).getColor(R.color.colorNoteDeepPurple)
                }
                R.id.radio_button_set_in_indigo -> {
                    MainActivity.noteColor = (context as MainActivity).getColor(R.color.colorNoteIndigo)
                }
                R.id.radio_button_set_in_blue -> {
                    MainActivity.noteColor = (context as MainActivity).getColor(R.color.colorNoteBlue)
                }
                R.id.radio_button_set_in_light_blue -> {
                    MainActivity.noteColor = (context as MainActivity).getColor(R.color.colorNoteLightBlue)
                }
                R.id.radio_button_set_in_cyan -> {
                    MainActivity.noteColor = (context as MainActivity).getColor(R.color.colorNoteCyan)
                }
                R.id.radio_button_set_in_teal -> {
                    MainActivity.noteColor = (context as MainActivity).getColor(R.color.colorNoteTeal)
                }
                R.id.radio_button_set_in_green -> {
                    MainActivity.noteColor = (context as MainActivity).getColor(R.color.colorNoteGreen)
                }
                R.id.radio_button_set_in_light_green -> {
                    MainActivity.noteColor = (context as MainActivity).getColor(R.color.colorNoteLightGreen)
                }
                R.id.radio_button_set_in_lime -> {
                    MainActivity.noteColor = (context as MainActivity).getColor(R.color.colorNoteLime)
                }
                R.id.radio_button_set_in_yellow -> {
                    MainActivity.noteColor = (context as MainActivity).getColor(R.color.colorNoteYellow)
                }
                R.id.radio_button_set_in_amber -> {
                    MainActivity.noteColor = (context as MainActivity).getColor(R.color.colorNoteAmber)
                }
                R.id.radio_button_set_in_orange -> {
                    MainActivity.noteColor = (context as MainActivity).getColor(R.color.colorNoteOrange)
                }
                R.id.radio_button_set_in_deep_orange -> {
                    MainActivity.noteColor = (context as MainActivity).getColor(R.color.colorNoteDeepOrange)
                }
            }

            editor.putInt(KEY_COLOR_NOTE, MainActivity.noteColor)
            editor.putInt(KEY_SET_NOTE_COLOR_CHECKED_RADIO_BUTTON_ID, radioGroup.checkedRadioButtonId)
            editor.apply()

            (activity as MainActivity).getNoteAdapter().notifyDataSetChanged()
        }

        return dialog
    }
}