package com.elliot.kim.kotlin.dimcatcamnote.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil.bind
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.elliot.kim.kotlin.dimcatcamnote.PATTERN_UP_TO_SECONDS
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.databinding.CardViewAlarmedNoteBinding
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.PasswordConfirmationDialogFragment

class AlarmedNoteAdapter(private val activity: MainActivity,
                         private val notes: ArrayList<Note>):
    RecyclerView.Adapter<AlarmedNoteAdapter.ViewHolder>() {

    private val tag = "AlarmedNoteAdapter"

    var selectedNote: Note? = null

    init {
        // 표시의 문제인건지 전달의 문제인건지 체크. 전달의 문제로 밝혀짐. 시바.
        //notes += Note("aaaa", 0L, null)
    }

    inner class ViewHolder(context: Context?, v: View)
        : RecyclerView.ViewHolder(v){
        var binding: CardViewAlarmedNoteBinding = bind(v)!!

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmedNoteAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(
            R.layout.card_view_alarmed_note,
            parent, false)
        return ViewHolder(activity, v)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (notes.isNotEmpty()) {
            Log.d("NOT EMPTYYY", notes[0].title)
        }

        val note: Note = notes[position]
        val title: String = note.title
        val creationTime: Long = note.creationTime
        val uri: String? = note.uri
        val content: String = note.content
        val editTime: Long? = note.editTime ?: creationTime
        val alarmTime: Long? = note.alarmTime

        val time: String = if (editTime != null)
            "${activity?.getString(R.string.creation_time)}: ${MainActivity.longTimeToString(creationTime,
                PATTERN_UP_TO_SECONDS
            )}"
        else
            "${activity?.getString(R.string.edit_time)}: ${MainActivity.longTimeToString(editTime,
                PATTERN_UP_TO_SECONDS
            )}"

        holder.binding.textViewTitle.text = title
        holder.binding.textViewTime.text = time
        holder.binding.textViewContent.text = content

        if (uri == null) holder.binding.imageViewThumbnail.visibility = View.GONE
        else {
            holder.binding.imageViewThumbnail.visibility = View.VISIBLE
            Glide.with(holder.binding.imageViewThumbnail.context)
                .load(Uri.parse(uri))
                .transform(CircleCrop())
                .into(holder.binding.imageViewThumbnail)
        }

        if (note.isDone) {
            holder.binding.textViewTitle.paintFlags = holder.binding.textViewTitle.paintFlags or
                    Paint.STRIKE_THRU_TEXT_FLAG
            holder.binding.textViewTime.paintFlags = holder.binding.textViewTime.paintFlags or
                    Paint.STRIKE_THRU_TEXT_FLAG
            holder.binding.textViewContent.paintFlags = holder.binding.textViewContent.paintFlags or
                    Paint.STRIKE_THRU_TEXT_FLAG
            holder.binding.imageViewLogo.setImageResource(R.drawable.ic_cat_footprint)
            holder.binding.imageViewDone.visibility = View.VISIBLE
        } else {
            holder.binding.textViewTitle.paintFlags = 0
            holder.binding.textViewTime.paintFlags = 0
            holder.binding.textViewContent.paintFlags = 0
            holder.binding.imageViewLogo.setImageResource(R.drawable.ic_cat_card_view_00)
            holder.binding.imageViewDone.visibility = View.INVISIBLE
        }

        if (note.alarmTime == null) {
            holder.binding.textViewAlarmTime.visibility = View.GONE
            holder.binding.imageViewAlarm.visibility = View.GONE
        } else {
            val alarmTimeText =
                "${activity?.getString(R.string.alarm_time)}: ${MainActivity.longTimeToString(alarmTime,
                    PATTERN_UP_TO_SECONDS
                )}"
            holder.binding.textViewAlarmTime.visibility = View.VISIBLE
            holder.binding.textViewAlarmTime.text = alarmTimeText
            holder.binding.imageViewAlarm.visibility = View.VISIBLE
        }

        if (note.isLocked) {
            holder.binding.imageViewLock.visibility = View.VISIBLE
            holder.binding.textViewContent.visibility = View.INVISIBLE
        } else {
            holder.binding.imageViewLock.visibility = View.GONE
            holder.binding.textViewContent.visibility = View.VISIBLE
        }

        holder.binding.cardView.setOnTouchListener { _, _ ->
            selectedNote = note
            false
        }

        // If the NoteAdapter is instantiated in the SingleNoteConfigureActivity,
        // it handles click events differently than instantiated in the MainActivity.

        holder.binding.cardView.setOnClickListener {
            if (note.isLocked)
                confirmPassword()
            else startEditFragment(selectedNote!!)
        }
    }

    private fun confirmPassword() {
        PasswordConfirmationDialogFragment(this).show(activity.fragmentManager,
            "")
    }

    override fun getItemCount(): Int {
        return if(notes.isNullOrEmpty()) 0 else notes.size
    }


    private fun startEditFragment(note: Note) {
        activity.editFragment.setNote(note)
        activity.fragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(R.anim.anim_slide_in_left_enter,
                R.anim.anim_slide_in_left_exit,
                R.anim.anim_slide_down_pop_enter,
                R.anim.anim_slide_down_pop_exit)
            .replace(R.id.calendar_container, activity.editFragment).commit()
    }
}