package com.elliot.kim.kotlin.dimcatcamnote

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
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

    var selectedNote: Note? = null

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

    @SuppressLint("ClickableViewAccessibility")
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

        holder.binding.textViewTitle.text = title
        holder.binding.textViewTime.text = time
        holder.binding.textViewContent.text = content

        if (uri == null) {
            holder.binding.imageViewThumbnail.visibility = View.GONE
        } else {
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
                "${context?.getString(R.string.alarm_time)}: ${MainActivity.timeToString(alarmTime)}"
            holder.binding.textViewAlarmTime.visibility = View.VISIBLE
            holder.binding.textViewAlarmTime.text = alarmTimeText
            holder.binding.imageViewAlarm.visibility = View.VISIBLE
        }

        holder.binding.cardView.setOnTouchListener { _, _ ->
            selectedNote = note
            false
        }

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
                val currentFolderName = (context as MainActivity).currentFolderName
                val searchWord = constraint.toString()
                if (searchWord.isEmpty() && currentFolderName == null)  notesFiltered = notes
                else if (searchWord.isNotEmpty() && currentFolderName == null){
                    val noteListFiltering: MutableList<Note> =
                        ArrayList()
                    for (note in notes) {
                        if (note.title.toLowerCase(Locale.ROOT)
                                .contains(searchWord.toLowerCase(Locale.ROOT))) {
                            noteListFiltering.add(note)
                        }
                    }
                    notesFiltered = noteListFiltering
                } else if (searchWord.isEmpty() && currentFolderName != null){
                    val noteListFiltering: MutableList<Note> =
                        ArrayList()
                    for (note in notes) {
                        if (note.folderName == currentFolderName) {
                            noteListFiltering.add(note)
                        }
                    }
                    notesFiltered = noteListFiltering
                } else {
                    val noteListFiltering: MutableList<Note> =
                        ArrayList()
                    for (note in notes) {
                        if (note.title.toLowerCase(Locale.ROOT)
                                .contains(searchWord.toLowerCase(Locale.ROOT)) &&
                            (note.folderName == currentFolderName)) {
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
        if (note.uri != null) context.deleteFileFromUri(note.uri!!)
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

    fun getNoteById(id: Int): Note {
        return notesFiltered.filter{ it.id == id }[0]
    }
}