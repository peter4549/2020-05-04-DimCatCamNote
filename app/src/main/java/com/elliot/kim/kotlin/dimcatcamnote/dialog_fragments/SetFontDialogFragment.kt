package com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.widget.RadioGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity

class SetFontDialogFragment(private val toolbar: Toolbar?) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // 폰트 설정
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_fragment_set_font) // 이거 디자인..
        dialog.findViewById<RadioGroup>(R.id.radio_group).setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.radio_button_nanum_myeongjo -> {
                    MainActivity.fontId = R.style.FontNanumMyeongjo
                    MainActivity.font = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                          resources.getFont(R.font.nanum_myeongjo_font_family)
                    else ResourcesCompat.getFont(requireContext(), R.font.nanum_myeongjo_font_family)
                }
                R.id.radio_button_nanum_pen -> {
                    MainActivity.fontId = R.style.FontNanumPen
                    MainActivity.font = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        resources.getFont(R.font.nanum_pen_font_family)
                    else ResourcesCompat.getFont(requireContext(), R.font.nanum_pen_font_family)
                }
                R.id.radio_button_reko -> {
                    MainActivity.fontId = R.style.FontReko
                    MainActivity.font = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        resources.getFont(R.font.nanum_pen_font_family)
                    else ResourcesCompat.getFont(requireContext(), R.font.nanum_pen_font_family)
                }
            }
            toolbar!!.setTitleTextAppearance(context, MainActivity.fontId)
            toolbar.invalidate()

        }

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            textViewTitle.typeface = resources.getFont(R.font.reko)
        else textViewTitle.typeface = ResourcesCompat.getFont(activity, R.font.reko)

         */

        return dialog
    }



}