package com.elliot.kim.kotlin.dimcatcamnote

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.elliot.kim.kotlin.dimcatcamnote.databinding.CardViewBinding
import com.elliot.kim.kotlin.dimcatcamnote.databinding.CardViewBinding.bind
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.PasswordConfirmationDialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.item_touch_helper.ItemMovedListener
import java.lang.Exception
import java.util.*


class NoteAdapter(private val context: Context?, private val notes: MutableList<Note>,
                  private val isAppWidgetConfigure: Boolean = false,
                  private val appWidgetId: Int? = null) :
    RecyclerView.Adapter<NoteAdapter.ViewHolder>(),
    ItemMovedListener {

    private var notesFiltered: MutableList<Note> = notes
    private lateinit var recyclerView: RecyclerView
    var selectedNote: Note? = null

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val binding: CardViewBinding = bind(v)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view, parent, false)

        return ViewHolder(v)
    }

    override fun onItemMoved(from: Int, to: Int) {

        if (from == to) return

        val fromItem = notesFiltered.removeAt(from)
        notesFiltered.add(to, fromItem)
        notifyItemMoved(from, to)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val note: Note = notesFiltered[position]
        val title: String = note.title
        val creationTime: Long = note.creationTime
        val uri: String? = note.uri
        val content: String = note.content
        val editTime: Long? = note.editTime ?: creationTime
        val alarmTime: Long? = note.alarmTime

        val time: String = if (editTime != null)
            "${context?.getString(R.string.creation_time)}: ${MainActivity.timeToString(creationTime)}"
        else
            "${context?.getString(R.string.edit_time)}: ${MainActivity.timeToString(editTime)}"

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
                "${context?.getString(R.string.alarm_time)}: ${MainActivity.timeToString(alarmTime)}"
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
            if (isAppWidgetConfigure) {
                // Instantiated from the SingleNoteConfigureActivity.

                if (appWidgetId == null) throw Exception("appWidgetId does not exist.")
                else confirmConfiguration(context as SingleNoteConfigureActivity, appWidgetId)
            } else {
                // Instantiated from the MainActivity.

                if (note.isLocked)
                    confirmPassword()
                else (context as MainActivity).startEditFragment(note)
            }
        }
    }

    private fun confirmPassword() {
        PasswordConfirmationDialogFragment(this).show((context as MainActivity).fragmentManager,
        "")
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun getItemCount(): Int {
        return if(notesFiltered.isNullOrEmpty()) 0 else notesFiltered.size
    }

    fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val currentFolderId = (context as MainActivity).currentFolder.id
                val searchWord = constraint.toString()
                if (searchWord.isEmpty() && currentFolderId == DEFAULT_FOLDER_ID)  notesFiltered = notes
                else if (searchWord.isNotEmpty() && currentFolderId == 0){
                    val noteListFiltering: MutableList<Note> =
                        ArrayList()
                    for (note in notes) {
                        if (note.title.toLowerCase(Locale.ROOT)
                                .contains(searchWord.toLowerCase(Locale.ROOT))) {
                            noteListFiltering.add(note)
                        }
                    }
                    notesFiltered = noteListFiltering
                } else if (searchWord.isEmpty() && currentFolderId != DEFAULT_FOLDER_ID){
                    val noteListFiltering: MutableList<Note> =
                        ArrayList()
                    for (note in notes) {
                        if (note.folderId == currentFolderId) {
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
                            (note.folderId == currentFolderId)) {
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
        (context as MainActivity).showCurrentFolderItems((context).currentFolder)
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

    fun sort(sortBy: SortBy): Long {
        Collections.sort(notes,
            Comparator { o1: Note, o2: Note ->
                when (sortBy) {
                    SortBy.CREATION_TIME -> return@Comparator (o2.creationTime - o1.creationTime).toInt()
                    SortBy.EDIT_TIME -> return@Comparator ((o2.editTime ?: 0L) - (o1.editTime ?: 0L)).toInt()
                    SortBy.NAME -> return@Comparator o1.title.compareTo(o2.title)
                    else -> return@Comparator 0
                }
            }
        )
        recyclerView.scheduleLayoutAnimation()
        notifyDataSetChanged()

        return 0L
    }

    private fun getPosition(note: Note?): Int = notesFiltered.indexOf(note)

    fun getNoteByPosition(position: Int): Note = notesFiltered[position]

    fun getNoteById(id: Int): Note {
        return notesFiltered.filter{ it.id == id }[0]
    }

    private fun confirmConfiguration(activity: SingleNoteConfigureActivity, appWidgetId: Int) {

        val appWidgetManager = AppWidgetManager.getInstance(activity)

        //Intent 에딧 프래그먼트 액티비티로 바꾸고 넣을 것.
        // pending intent 등 사용

        val intent = Intent(activity, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0)

        //val views = RemoteViews(activity.packageName, R.layout.widget)
        //views.setOnClickPendingIntent()
        //views.setCharSequence(R.id.text_view_title, "setText", selectedNote!!.title)
        //views.setCharSequence(R.id.text_view_content, "setText", selectedNote!!.content)

        RemoteViews(activity.packageName, R.layout.app_widget).apply {
            setOnClickPendingIntent(R.id.text_view_content, pendingIntent)
            setCharSequence(R.id.text_view_title, "setText", selectedNote!!.title)
            setCharSequence(R.id.text_view_content, "setText", selectedNote!!.content)
        }.also {
            appWidgetManager.updateAppWidget(appWidgetId, it)
        }

        val preferences = context!!.getSharedPreferences(APP_WIDGET_PREFERENCES, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(KEY_NOTE_TITLE + appWidgetId, selectedNote!!.title)
        editor.apply()

        val resultIntent = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        activity.setResult(AppCompatActivity.RESULT_OK, resultIntent)
        activity.finish()

    }
}