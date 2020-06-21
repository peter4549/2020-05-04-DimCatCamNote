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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.databinding.CardViewAlarmedNoteBinding
import com.elliot.kim.kotlin.dimcatcamnote.databinding.CardViewBinding
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.ConfirmPasswordDialogFragment

class AlarmedNoteAdapter(private val activity: MainActivity,
                         private val notes: ArrayList<Note>):
    RecyclerView.Adapter<AlarmedNoteAdapter.ViewHolder>() {

    private lateinit var recyclerView: RecyclerView
    private val tag = "AlarmedNoteAdapter"
    private var currentTime = 0L
    var selectedNote: Note? = null

    inner class ViewHolder(context: Context?, v: View)
        : RecyclerView.ViewHolder(v){
        var binding: CardViewBinding = bind(v)!!
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmedNoteAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(
            R.layout.card_view,
            parent, false)
        return ViewHolder(activity, v)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val note: Note = notes[position]
        val title: String = note.title
        val creationTime: Long = note.creationTime
        val uri: String? = note.uri
        val content: String = note.content
        val editTime: Long? = note.editTime ?: creationTime
        val alarmTime: Long? = note.alarmTime

        val time: String = if (editTime != null)
            "${activity.getString(R.string.creation_time)}: ${MainActivity.longTimeToString(creationTime,
                PATTERN_UP_TO_SECONDS
            )}"
        else
            "${activity.getString(R.string.edit_time)}: ${MainActivity.longTimeToString(editTime,
                PATTERN_UP_TO_SECONDS
            )}"

        holder.binding.colorContainer.setBackgroundColor(MainActivity.noteColor)

        holder.binding.textViewTitle.adjustNoteTextSize(MainActivity.fontId, NoteItem.TITLE)
        holder.binding.textViewTime.adjustNoteTextSize(MainActivity.fontId, NoteItem.TIME)
        holder.binding.textViewAlarmTime.adjustNoteTextSize(MainActivity.fontId, NoteItem.TIME)
        holder.binding.textViewContent.adjustNoteTextSize(MainActivity.fontId, NoteItem.CONTENT)

        holder.binding.textViewTitle.typeface = MainActivity.font
        holder.binding.textViewTime.typeface = MainActivity.font
        holder.binding.textViewAlarmTime.typeface = MainActivity.font
        holder.binding.textViewContent.typeface = MainActivity.font

        holder.binding.textViewTitle.text = title
        holder.binding.textViewTime.text = time
        holder.binding.textViewContent.text = content

        if (uri == null) holder.binding.imageViewThumbnail.visibility = View.GONE
        else {
            holder.binding.imageViewThumbnail.visibility = View.VISIBLE
            Glide.with(holder.binding.imageViewThumbnail.context)
                .load(Uri.parse(uri))
                .error(R.drawable.ic_sentiment_dissatisfied_grey_24dp)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
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
            //holder.binding.imageViewLogo.setImageResource(R.drawable.ic_cat_footprint)
            holder.binding.imageViewDone.visibility = View.VISIBLE
        } else {
            holder.binding.textViewTitle.paintFlags = 0
            holder.binding.textViewTime.paintFlags = 0
            holder.binding.textViewContent.paintFlags = 0
            //holder.binding.imageViewLogo.setImageResource(R.drawable.ic_cat_card_view_00)
            holder.binding.imageViewDone.visibility = View.INVISIBLE
        }

        if (note.alarmTime == null) {
            holder.binding.textViewAlarmTime.visibility = View.GONE
            holder.binding.imageViewAlarm.visibility = View.GONE
        } else {
            holder.binding.imageViewAlarm.visibility = View.VISIBLE

            currentTime = MainActivity.getCurrentTime()
            var text = activity.getString(R.string.alarm_time)
            holder.binding.imageViewAlarm
                .setImageDrawable(activity.getDrawable(R.drawable.ic_alarm_on_white_24dp))

            if (note.alarmTime!! < currentTime) {
                text = "캘린더 등록시간"
                holder.binding.imageViewAlarm
                    .setImageDrawable(activity.getDrawable(R.drawable.ic_today_white_24dp))
            }

            val alarmTimeText =
                "$text: ${MainActivity.longTimeToString(alarmTime,
                    PATTERN_UP_TO_MINUTES
                )}"
            holder.binding.textViewAlarmTime.visibility = View.VISIBLE
            holder.binding.textViewAlarmTime.text = alarmTimeText
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
            activity.getNoteAdapter().setSelectedNoteByCreationTime(note.creationTime) // 초이스를 안하고 대입을 해버리니 새로운 존재가 생긴듯.
            false
        }

        holder.binding.cardView.setOnClickListener {
            if (note.isLocked)
                confirmPassword()
            else startEditFragment(selectedNote!!)
        }
    }

    private fun confirmPassword() {
        ConfirmPasswordDialogFragment(this, activity).show(activity.fragmentManager,
            "")
    }

    override fun getItemCount(): Int {
        return if(notes.isNullOrEmpty()) 0 else notes.size
    }


    private fun startEditFragment(note: Note) {
        activity.editFragment.setNote(note)
        activity.editFragment.isFromAlarmedNoteSelectionFragment = true
        activity.fragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(R.anim.anim_slide_in_left_enter,
                R.anim.anim_slide_in_left_exit,
                R.anim.anim_slide_down_pop_enter,
                R.anim.anim_slide_down_pop_exit)
            .replace(R.id.calendar_container, activity.editFragment).commit()
    }

    fun insert(note: Note) {
        notes.add(0, note)
        notifyItemInserted(0)
        recyclerView.smoothScrollToPosition(0)
    }

    fun delete(note: Note) {
        val position: Int = notes.indexOf(note)
        notes.remove(note)
        notifyItemRemoved(position)
        activity.cancelAlarm(note, true)
        if (note.uri != null) activity.deleteFileFromUri(note.uri!!)
    }

    fun getSelectedNoteByCreationTime(creationTime: Long) = notes.filter { it.creationTime == creationTime }[0]
}