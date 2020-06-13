package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager

import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.adapters.AlarmedNoteAdapter
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.databinding.FragmentAlarmedNoteSelectionBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class AlarmedNoteSelectionFragment(private val alarmedNotes: ArrayList<Note>,
                                   private val selectedDateText: String,
                                   private val selectedImageView: ImageView) : Fragment() {

    private lateinit var activity: MainActivity
    private lateinit var binding: FragmentAlarmedNoteSelectionBinding
    private lateinit var alarmedNoteAdapter: AlarmedNoteAdapter
    private var selectedDate = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity = requireActivity() as MainActivity
        this.alarmedNoteAdapter = AlarmedNoteAdapter(activity, alarmedNotes)
        activity.alarmedNoteAdapter = this.alarmedNoteAdapter
        return inflater.inflate(R.layout.fragment_alarmed_note_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAlarmedNoteSelectionBinding.bind(view)
        binding.textViewDate.text = selectedDateText

        val buttonText = selectedDateText.replace("[^0-9]".toRegex(), "")
        val year = buttonText.substring(0, 4).toInt()
        val month = buttonText.substring(4, 6).toInt()
        val dayOfMonth = buttonText.substring(6).toInt()

        selectedDate = convertDateAndHourIntToLong(year, month, dayOfMonth)

        binding.addContainer.setOnClickListener {
            startWriteFragment()
        }

        binding.recyclerView.apply {
            setHasFixedSize(true)
            adapter = alarmedNoteAdapter
            layoutManager = LinearLayoutManager(context)
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
                R.anim.anim_slide_down_pop_enter,
                R.anim.anim_slide_down_pop_exit
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
