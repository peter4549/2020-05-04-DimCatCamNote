package com.elliot.kim.kotlin.dimcatcamnote

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.elliot.kim.kotlin.dimcatcamnote.databinding.CardViewBinding
import com.elliot.kim.kotlin.dimcatcamnote.databinding.CardViewBinding.bind
import com.elliot.kim.kotlin.dimcatcamnote.item_touch_helper.ItemMovedListener
import java.util.*

class NoteAdapter(private val context: Context?, private val notes: MutableList<Note>) :
    RecyclerView.Adapter<NoteAdapter.ViewHolder>(),
    ItemMovedListener {

    private var notesFiltered: MutableList<Note> = notes

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val binding: CardViewBinding = bind(v)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view, parent, false)

        return ViewHolder(v)
    }

    override fun onItemMoved(from: Int, to: Int) {
        if (from == to) {
            return
        }

        val fromItem = notesFiltered.removeAt(from)
        notesFiltered.add(to, fromItem)
        notifyItemMoved(from, to)
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

    fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val searchWord = constraint.toString()
                if (searchWord.isEmpty()) {
                    notesFiltered = notes
                } else {
                    val noteListFiltering: MutableList<Note> =
                        ArrayList()
                    for (note in notes) {
                        if (note.title.toLowerCase().contains(searchWord.toLowerCase())) {
                            noteListFiltering.add(note)
                        }
                    }
                    notesFiltered = noteListFiltering
                }
                val filterResults = FilterResults()
                filterResults.values = notesFiltered
                return filterResults
            }

            override fun publishResults(
                constraint: CharSequence,
                results: FilterResults
            ) {
                notesFiltered = results.values as MutableList<Note>
                notifyDataSetChanged()
            }
        }
    }

    fun insert(camNote: Note) {
        notes.add(camNote)
        notifyItemInserted(notes.size - 1)
    }

    fun update(note: Note) {
        notifyItemChanged(getPosition(note))
    }

    fun delete(note: Note) {
        val position: Int = notesFiltered.indexOf(note)
        notesFiltered.removeAt(position)
        notes.remove(note)
        notifyItemRemoved(position)

        (context as MainActivity).cancelAlarm(note, true)
        if (note.uri != null) (context as MainActivity).deleteFileFromUri(note.uri!!)
    }

    fun delete(position: Int) {
        val removedNote = notesFiltered.removeAt(position)
        notes.remove(removedNote)
        notifyItemRemoved(position)
        if (removedNote.uri != null) (context as MainActivity).deleteFileFromUri(removedNote.uri!!)
    }

    fun sort(sortBy: Int): Long {
        Collections.sort(notes,
            Comparator<Note> { o1: Note, o2: Note ->
                when (sortBy) {
                    0 -> return@Comparator (o2.creationTime - o1.creationTime).toInt()
                    1 -> return@Comparator ((o2.editTime ?: 0L) - (o1.editTime ?: 0L)).toInt()
                    2 -> return@Comparator o1.title.compareTo(o2.title)
                    else -> return@Comparator 0
                }
            }
        )
        return 0L
    }

    fun getPosition(note: Note?): Int = notesFiltered.indexOf(note)

    fun getNoteByPosition(position: Int): Note = notesFiltered[position]

}