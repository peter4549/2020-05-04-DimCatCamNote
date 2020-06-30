package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.elliot.kim.kotlin.dimcatcamnote.*

import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.adapters.AlarmedNoteAdapter
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.databinding.FragmentAlarmedNoteSelectionBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest
import kotlin.collections.ArrayList

class AlarmedNoteSelectionFragment : Fragment() {

    private lateinit var alarmedNotes: ArrayList<Note>
    private lateinit var selectedImageView: ImageView
    private var currentDate: Long = 0L

    private lateinit var activity: MainActivity
    private lateinit var binding: FragmentAlarmedNoteSelectionBinding
    lateinit var alarmedNoteAdapter: AlarmedNoteAdapter
    private var selectedDate = 0L

    fun setData(alarmedNotes: ArrayList<Note>, currentDate: Long, imageView: ImageView) {
        this.alarmedNotes = alarmedNotes
        this.currentDate = currentDate
        this.selectedImageView = imageView
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity = requireActivity() as MainActivity
        this.alarmedNoteAdapter = AlarmedNoteAdapter(activity, alarmedNotes)
        // Synchronize deletion with reference to alarmedNotAdapter
        activity.alarmedNoteAdapter = this.alarmedNoteAdapter
        return inflater.inflate(R.layout.fragment_alarmed_note_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAlarmedNoteSelectionBinding.bind(view)
        binding.textViewDate.text =  MainActivity.longTimeToString(currentDate, PATTERN_YYYY_MM_dd)

        binding.textViewDate.setBackgroundColor(MainActivity.backgroundColor)
        binding.textViewAdd.setBackgroundColor(MainActivity.toolbarColor)

        binding.textViewDate.adjustDialogTitleTextSize(MainActivity.fontId)
        binding.textViewAdd.adjustDialogItemTextSize(MainActivity.fontId)
        binding.textViewDate.typeface = MainActivity.font
        binding.textViewAdd.typeface = MainActivity.font

        val year = SimpleDateFormat("yyyy",
            Locale.getDefault()).format(currentDate).toInt()
        val month = SimpleDateFormat("MM",
            Locale.getDefault()).format(currentDate).toInt()
        val dayOfMonth = SimpleDateFormat("dd",
            Locale.getDefault()).format(currentDate).toInt()

        selectedDate = convertDateAndHourIntToLong(year, month, dayOfMonth)

        binding.addContainer.setOnClickListener {
            startWriteFragment()
        }

        binding.recyclerView.apply {
            setHasFixedSize(true)
            adapter = alarmedNoteAdapter
            // Replaced from LinearLayoutManager to LinearLayoutManagerWrapper
            layoutManager = LayoutManagerWrapper(context, 1)
        }
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {

        val animation = AnimationUtils.loadAnimation(activity, nextAnim)

        animation!!.setAnimationListener( object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {

            }

            override fun onAnimationStart(animation: Animation?) {
                updateCalendarView(selectedImageView, alarmedNotes.isEmpty())
            }
        })

        return animation
    }

    private fun startWriteFragment() {
        activity.writeFragment.dateSelectedInCalender = selectedDate
        activity.writeFragment.isFromAlarmedNoteSelectionFragment = true
        activity.writeFragment.setAlarmedNoteAdapter(alarmedNoteAdapter)

        activity.fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.anim_slide_in_left_enter,
                R.anim.anim_slide_in_left_exit,
                R.anim.anim_slide_out_right_enter,
                R.anim.anim_slide_out_right_exit
            )
            .addToBackStack(null)
            .replace(R.id.alarmed_note_selection_container, activity.writeFragment).commit()
    }

    private fun convertDateAndHourIntToLong(year: Int, month: Int, date: Int, hour: Int = 8): Long {
        val simpleDateFormat = SimpleDateFormat(
            "yyyy-MM-dd-hh", Locale.getDefault())

        return simpleDateFormat.parse(String.format("%d-%d-%d-%d", year, month, date, hour))?.time!!
    }

    private fun updateCalendarView(imageView: ImageView, isEmpty: Boolean) {
        if (isEmpty)
            imageView.visibility = View.INVISIBLE
        else
            imageView.visibility = View.VISIBLE
    }
}
