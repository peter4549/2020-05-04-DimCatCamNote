package com.elliot.kim.kotlin.dimcatcamnote

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.elliot.kim.kotlin.dimcatcamnote.databinding.CardViewBinding
import com.elliot.kim.kotlin.dimcatcamnote.databinding.CardViewBinding.bind

class NoteAdapter(private val context: Context?, private val notes: MutableList<Note>) :
    RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    private var notesFiltered: MutableList<Note> = notes

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val binding: CardViewBinding = bind(v)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view, parent, false)

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note: Note = notesFiltered[position]

        val title: String = note.title
        val creationTime: Long = note.creationTime
        val uri: String? = note.uri  // 호출시점에 널 체크, 널이면 기본화면 표시
        val content: String = note.content
        val editTime: Long? = note.editTime ?: creationTime
        val alarmTime: Long? = note.alarmTime // 호출 시점에 널 체크. 널이면 텍스트 gone, 있으면표시

        val time: String = if (editTime != null)
            "${context?.getString(R.string.creation_time)}: ${MainActivity.timeToString(creationTime)}"
        else
            "${context?.getString(R.string.edit_time)}: ${MainActivity.timeToString(editTime)}"

        if (uri != null) {
            Glide.with(holder.binding.imageViewThumbnail.context)
                .load(Uri.parse(uri))
                .transform(CircleCrop())
                .into(holder.binding.imageViewThumbnail)
        } else {
            //Glide.clear(holder.binding.imageViewThumbnail)
        }

        holder.binding.textViewTitle.text = title
        holder.binding.textViewTime.text = time
        holder.binding.textViewContent.text = content

        holder.binding.cardView.setOnClickListener {
            (context as MainActivity).startEditFragment(note)
        }
    }

    override fun getItemCount(): Int {
        return if(notesFiltered.isNullOrEmpty()) 0 else notesFiltered.size
    }

    fun insert(camNote: Note) {
        notes.add(camNote)
        notifyItemInserted(notes.size - 1)
    }

    fun update(note: Note) {
        notifyItemChanged(getPosition(note))
    }

    fun delete(camNote: Note) {
        val position: Int = notesFiltered.indexOf(camNote)
        notesFiltered.removeAt(position)
        notes.remove(camNote)
        notifyItemRemoved(position)
    }

    fun getPosition(note: Note?): Int = notesFiltered.indexOf(note)

}