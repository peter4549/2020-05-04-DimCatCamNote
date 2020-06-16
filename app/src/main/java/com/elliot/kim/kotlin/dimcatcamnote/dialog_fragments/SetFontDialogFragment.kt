package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.RadioGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity

class SetFontDialogFragment(private val toolbar: Toolbar?) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_fragment_set_font) // 이거 디자인..

        val preferences = requireContext().getSharedPreferences(
            PREFERENCES_FONT,
            Context.MODE_PRIVATE
        )
        val editor = preferences.edit()

        val checkedRadioButtonId =
            preferences.getInt(KEY_SET_FONT_CHECKED_RADIO_BUTTON_ID, defaultCheckedRadioButtonId)

        val radioGroup = dialog.findViewById<RadioGroup>(R.id.radio_group)
        radioGroup.check(checkedRadioButtonId)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.radio_button_nanum_myeongjo -> {
                    MainActivity.fontStyleId = R.style.FontNanumMyeongjo
                    MainActivity.fontId = R.font.nanum_myeongjo_font_family
                }
                R.id.radio_button_nanum_pen -> {
                    MainActivity.fontStyleId = R.style.FontNanumPen
                    MainActivity.fontId = R.font.nanum_pen_font_family
                }
                R.id.radio_button_reko -> {
                    MainActivity.fontStyleId = R.style.FontReko
                    MainActivity.fontId = R.font.reko_font_family
                }
            }
            MainActivity.font = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                resources.getFont(MainActivity.fontId)
            else ResourcesCompat.getFont(requireContext(), MainActivity.fontId)

            editor.putInt(KEY_FONT_ID, MainActivity.fontId)
            editor.putInt(KEY_FONT_STYLE_ID, MainActivity.fontStyleId)
            editor.putInt(KEY_SET_FONT_CHECKED_RADIO_BUTTON_ID, radioGroup.checkedRadioButtonId)
            editor.apply()

            // fragment_configure toolbar
            toolbar!!.setTitleTextAppearance(context, MainActivity.fontStyleId)
            toolbar.invalidate()

            // Note
            (requireActivity() as MainActivity).getNoteAdapter().notifyDataSetChanged()

            // activity_main
            (requireActivity() as MainActivity).setFont()
        }

        return dialog
    }

    companion object {
        const val defaultCheckedRadioButtonId = R.id.radio_button_nanum_pen
    }



}