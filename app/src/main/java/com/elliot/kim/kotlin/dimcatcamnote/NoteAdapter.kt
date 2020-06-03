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
import com.elliot.kim.kotlin.dimcatcamnote.activities.APP_WIDGET_PREFERENCES
import com.elliot.kim.kotlin.dimcatcamnote.activities.EditActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.SingleNoteConfigureActivity
import com.elliot.kim.kotlin.dimcatcamnote.databinding.CardViewBinding
import com.elliot.kim.kotlin.dimcatcamnote.databinding.CardViewBinding.bind
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.PasswordConfirmationDialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.item_touch_helper.ItemMovedListener
import java.util.*
import kotlin.collections.ArrayList


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
            "${context?.getString(R.string.creation_time)}: ${MainActivity.longTimeToString(creationTime, PATTERN_UP_TO_SECONDS)}"
        else
            "${context?.getString(R.string.edit_time)}: ${MainActivity.longTimeToString(editTime, PATTERN_UP_TO_SECONDS)}"

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
                "${context?.getString(R.string.alarm_time)}: ${MainActivity.longTimeToString(alarmTime, PATTERN_UP_TO_SECONDS)}"
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
                else (context as MainActivity).startEditFragment()
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

        note.isDeleted = true
        (context as MainActivity).cancelAlarm(note, true)
        if (note.uri != null) (context as MainActivity).deleteFileFromUri(note.uri!!)
    }

    fun removePhoto(note: Note) {
        if (note.uri != null) (context as MainActivity).deleteFileFromUri(note.uri!!)
    }

    fun delete(position: Int) {
        val removedNote = notesFiltered.removeAt(position)
        notes.remove(removedNote)
        notifyItemRemoved(position)
        if (removedNote.uri != null) (context as MainActivity).deleteFileFromUri(removedNote.uri!!)
    }

    fun sort(sortingCriteria: Int): Long {
        Collections.sort(notes,
            Comparator { o1: Note, o2: Note ->
                when (sortingCriteria) {
                    SortingCriteria.CREATION_TIME.index-> return@Comparator (o2.creationTime - o1.creationTime).toInt()
                    SortingCriteria.EDIT_TIME.index -> return@Comparator ((o2.editTime ?: 0L) - (o1.editTime ?: 0L)).toInt()
                    SortingCriteria.NAME.index -> return@Comparator o1.title.compareTo(o2.title)
                    else -> return@Comparator 0
                }
            }
        )
        recyclerView.scheduleLayoutAnimation()
        notifyDataSetChanged()

        val preferences =
            context!!.getSharedPreferences(PREFERENCES_SORTING_CRITERIA, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt(KEY_SORTING_CRITERIA, sortingCriteria)
        editor.apply()

        return 0L
    }

    private fun getPosition(note: Note?): Int = notesFiltered.indexOf(note)

    fun getNoteByPosition(position: Int): Note = notesFiltered[position]

    fun getNoteById(id: Int): Note {
        return notes.filter{ it.id == id }[0]
    }

    private fun confirmConfiguration(activity: SingleNoteConfigureActivity, appWidgetId: Int) {

        val note = selectedNote!!
        val appWidgetManager = AppWidgetManager.getInstance(activity)

        val intent = Intent(activity, EditActivity::class.java)
        intent.action = ACTION_APP_WIDGET_ATTACHED + note.id
        val pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0)

        RemoteViews(activity.packageName, R.layout.app_widget).apply {
            setOnClickPendingIntent(R.id.text_view_content, pendingIntent)
            setCharSequence(R.id.text_view_title, "setText", note.title)
            setCharSequence(R.id.text_view_content, "setText", note.content)

            if (note.isDone) setViewVisibility(R.id.image_view_done, View.VISIBLE)
            else setViewVisibility(R.id.image_view_done, View.GONE)

            if (note.isLocked) setViewVisibility(R.id.image_view_lock, View.VISIBLE)
            else setViewVisibility(R.id.image_view_lock, View.GONE)

            if (note.alarmTime != null) setViewVisibility(R.id.image_view_alarm, View.VISIBLE)
            else setViewVisibility(R.id.image_view_alarm, View.GONE)

        }.also {
            appWidgetManager.updateAppWidget(appWidgetId, it)
        }

        val preferences = context!!.getSharedPreferences(APP_WIDGET_PREFERENCES, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt(KEY_APP_WIDGET_NOTE_ID + appWidgetId, note.id)
        editor.putString(KEY_APP_WIDGET_NOTE_TITLE + appWidgetId, note.title)
        editor.putString(KEY_APP_WIDGET_NOTE_CONTENT + appWidgetId, note.content)
        editor.putString(KEY_APP_WIDGET_NOTE_URI + appWidgetId, note.uri ?: "")
        editor.putLong(KEY_APP_WIDGET_NOTE_CREATION_TIME + appWidgetId, note.creationTime)
        editor.putLong(KEY_APP_WIDGET_NOTE_EDIT_TIME + appWidgetId, note.editTime ?: 0L)
        editor.putLong(KEY_APP_WIDGET_NOTE_ALARM_TIME + appWidgetId, note.alarmTime ?: 0L)
        editor.putBoolean(KEY_APP_WIDGET_NOTE_IS_DONE + appWidgetId, note.isDone)
        editor.putBoolean(KEY_APP_WIDGET_NOTE_IS_LOCKED + appWidgetId, note.isLocked)
        editor.putString(KEY_APP_WIDGET_NOTE_PASSWORD + appWidgetId, note.password ?: "")
        editor.apply()

        val resultIntent = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        note.appWidgetIds += appWidgetId
        //(note.appWidgetIds as MutableList<Int>).add(appWidgetId)
        activity.viewModel.update(note, false)
        activity.setResult(AppCompatActivity.RESULT_OK, resultIntent)
        activity.finish()
    }
}