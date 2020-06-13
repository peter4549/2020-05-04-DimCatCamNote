package com.elliot.kim.kotlin.dimcatcamnote.adapters

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
import android.view.animation.AnimationUtils
import android.widget.Filter
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.APP_WIDGET_PREFERENCES
import com.elliot.kim.kotlin.dimcatcamnote.activities.EditActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.SingleNoteConfigureActivity
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.databinding.CardViewBinding
import com.elliot.kim.kotlin.dimcatcamnote.databinding.CardViewBinding.bind
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.ConfirmPasswordDialogFragment
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

    var isFirstBinding = true

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

        if (isFirstBinding) recyclerView.scheduleLayoutAnimation()

        val note: Note = notesFiltered[position]
        val title: String = note.title
        val creationTime: Long = note.creationTime
        val uri: String? = note.uri
        val content: String = note.content
        val editTime: Long? = note.editTime ?: creationTime
        val alarmTime: Long? = note.alarmTime

        val time: String = if (editTime == creationTime)
            "${context?.getString(R.string.creation_time)}: ${MainActivity.longTimeToString(creationTime,
                PATTERN_UP_TO_SECONDS
            )}"
        else
            "${context?.getString(R.string.edit_time)}: ${MainActivity.longTimeToString(editTime,
                PATTERN_UP_TO_SECONDS
            )}"

        holder.binding.colorContainer.setBackgroundColor(MainActivity.noteColor)

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
            val alarmTimeText =
                "${context?.getString(R.string.alarm_time)}: ${MainActivity.longTimeToString(alarmTime,
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
            if (isFirstBinding) isFirstBinding = false
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
        ConfirmPasswordDialogFragment(this).show((context as MainActivity).fragmentManager,
        "")
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
                else if (searchWord.isNotEmpty() && currentFolderId == DEFAULT_FOLDER_ID){
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
                    val noteListFiltering: MutableList<Note> = ArrayList()
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

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(
                constraint: CharSequence,
                results: FilterResults
            ) {
                notesFiltered = results.values as MutableList<Note>
                notifyDataSetChanged()
            }
        }
    }

    fun insert(note: Note) {
        if (isFirstBinding) isFirstBinding = false

        notes.add(0, note)
        notifyItemInserted(0)
        (context as MainActivity).recyclerViewScrollToTop()

        context.showCurrentFolderItems((context).currentFolder, false)

        /** The code below controls the scrolling speed.
        val linearSmoothScroller: LinearSmoothScroller =
            object : LinearSmoothScroller(recyclerView.context) {
                override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                    return 256f / displayMetrics.densityDpi
                }
            }

        linearSmoothScroller.targetPosition = 0
        recyclerView.layoutManager!!.startSmoothScroll(linearSmoothScroller)
         */
    }

    fun update(note: Note) {
        notifyItemChanged(getPosition(note))
    }

    fun delete(note: Note) {
        if (isFirstBinding) isFirstBinding = false

        val position: Int = notesFiltered.indexOf(note)
        notesFiltered.removeAt(position)
        notes.remove(note)
        notifyItemRemoved(position)
        note.isDeleted = true
        (context as MainActivity).cancelAlarm(note, isDelete = true, isByUser = true)
        if (note.uri != null) context.deleteFileFromUri(note.uri!!)
    }

    fun removePhoto(note: Note) {
        if (note.uri != null) (context as MainActivity).deleteFileFromUri(note.uri!!)
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

        RemoteViews(activity.packageName,
            R.layout.app_widget
        ).apply {
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

        val resultIntent = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        note.appWidgetIds += appWidgetId
        activity.viewModel.update(note)
        activity.setResult(AppCompatActivity.RESULT_OK, resultIntent)
        activity.finish()
    }

    fun getAlarmedNotes() = notes.filter { it.alarmTime != null }

    fun setSelectedNoteByCreationTime(creationTime: Long) {
        selectedNote = notes.filter { it.creationTime == creationTime }[0]
    }
}