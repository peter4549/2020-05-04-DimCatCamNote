package com.elliot.kim.kotlin.dimcatcamnote.adapters

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.SingleNoteConfigureActivity
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.databinding.CardViewBinding
import com.elliot.kim.kotlin.dimcatcamnote.databinding.CardViewBinding.bind
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.ConfirmPasswordDialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.fragments.ConfigureFragment
import com.elliot.kim.kotlin.dimcatcamnote.item_touch_helper.ItemMovedListener
import java.util.*
import kotlin.collections.ArrayList


class NoteAdapter(private val context: Context?, private val notes: MutableList<Note>,
                  private val isAppWidgetConfigure: Boolean = false,
                  private val appWidgetId: Int? = null) :
    RecyclerView.Adapter<NoteAdapter.ViewHolder>(),
    ItemMovedListener {

    private val tag = "NoteAdapter"
    private var currentTime = 0L
    private var notesFiltered: MutableList<Note> = notes
    private lateinit var recyclerView: RecyclerView
    var selectedNote: Note? = null
    var sortingCriteria = SortingCriteria.EDIT_TIME.index
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
                PATTERN_UP_TO_MINUTES
            )}"
        else
            "${context?.getString(R.string.edit_time)}: ${MainActivity.longTimeToString(editTime,
                PATTERN_UP_TO_MINUTES
            )}"

        // Apply design
        if (context is MainActivity) {
            holder.binding.colorContainer.setBackgroundColor(MainActivity.noteColor)

            holder.binding.textViewTitle.adjustNoteTextSize(MainActivity.fontId, NoteItem.TITLE)
            holder.binding.textViewTime.adjustNoteTextSize(MainActivity.fontId, NoteItem.TIME)
            holder.binding.textViewAlarmTime.adjustNoteTextSize(MainActivity.fontId, NoteItem.TIME)
            holder.binding.textViewContent.adjustNoteTextSize(MainActivity.fontId, NoteItem.CONTENT)

            holder.binding.textViewTitle.typeface = MainActivity.font
            holder.binding.textViewTime.typeface = MainActivity.font
            holder.binding.textViewAlarmTime.typeface = MainActivity.font
            holder.binding.textViewContent.typeface = MainActivity.font
        } else if (context is SingleNoteConfigureActivity) {
            holder.binding.colorContainer.setBackgroundColor(SingleNoteConfigureActivity.noteColor)

            holder.binding.textViewTitle.adjustNoteTextSize(SingleNoteConfigureActivity.fontId, NoteItem.TITLE)
            holder.binding.textViewTime.adjustNoteTextSize(SingleNoteConfigureActivity.fontId, NoteItem.TIME)
            holder.binding.textViewAlarmTime.adjustNoteTextSize(SingleNoteConfigureActivity.fontId, NoteItem.TIME)
            holder.binding.textViewContent.adjustNoteTextSize(SingleNoteConfigureActivity.fontId, NoteItem.CONTENT)

            holder.binding.textViewTitle.typeface = SingleNoteConfigureActivity.font
            holder.binding.textViewTime.typeface = SingleNoteConfigureActivity.font
            holder.binding.textViewAlarmTime.typeface = SingleNoteConfigureActivity.font
            holder.binding.textViewContent.typeface = SingleNoteConfigureActivity.font
        }

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
            var text = context?.getString(R.string.alarm_time)
            if (note.alarmTime!! < currentTime) {
                text = "캘린더 등록시간"
                holder.binding.imageViewAlarm
                    .setImageDrawable(context!!.getDrawable(R.drawable.ic_today_white_24dp))
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
                    ConfirmPasswordDialogFragment(this, (context as MainActivity)).show(context
                        .fragmentManager, tag)
                else (context as MainActivity).startEditFragment()
            }
        }
    }

    override fun getItemCount(): Int {
        return if(notesFiltered.isNullOrEmpty()) 0 else notesFiltered.size
    }

    fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val currentFolderId = (context as MainActivity).currentFolder.id
                val searchWord = constraint.toString()
                val lockedFolders = context.getFolderAdapter().getLockedFolderIds()
                if (searchWord.isEmpty() && currentFolderId == DEFAULT_FOLDER_ID) {
                    // Show all notes.
                    val noteListFiltering: MutableList<Note> = ArrayList()
                    for (note in notes) {
                        // Exclude note in the locked folder.
                        if (note.folderId == DEFAULT_FOLDER_ID)
                            noteListFiltering.add(note)
                        else if (note.folderId !in lockedFolders)
                            noteListFiltering.add(note)
                    }

                    notesFiltered =  noteListFiltering
                }
                else if (searchWord.isNotEmpty() && currentFolderId == DEFAULT_FOLDER_ID){
                    // Show notes that contain search word in the title.
                    val noteListFiltering: MutableList<Note> = ArrayList()
                    for (note in notes) {
                        if (note.title.toLowerCase(Locale.ROOT)
                                .contains(searchWord.toLowerCase(Locale.ROOT))) {
                            // Exclude note in the locked folder.
                            if (!context.getFolderAdapter().isLockedFolder(note.folderId))
                                noteListFiltering.add(note)
                        }
                    }
                    notesFiltered = noteListFiltering
                } else if (searchWord.isEmpty() && currentFolderId != DEFAULT_FOLDER_ID){
                    // Show notes contained in a specific folder.
                    val noteListFiltering: MutableList<Note> = ArrayList()
                    for (note in notes) {
                        if (note.folderId == currentFolderId) {
                            noteListFiltering.add(note)
                        }
                    }
                    notesFiltered = noteListFiltering
                } else {
                    // Shows notes that are included in a specific folder
                    // and contain search word in the title.
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
                if (results.values != null)
                    notesFiltered = results.values as MutableList<Note>

                Collections.sort(notesFiltered,
                    Comparator { o1: Note, o2: Note ->
                        when (sortingCriteria) {
                            SortingCriteria.CREATION_TIME.index->
                                return@Comparator (o2.creationTime - o1.creationTime).toInt()
                            SortingCriteria.EDIT_TIME.index ->
                                return@Comparator ((o2.editTime ?: o2.creationTime) - (o1.editTime ?: o1.creationTime)).toInt()
                            SortingCriteria.NAME.index -> return@Comparator o1.title.compareTo(o2.title)
                            else -> return@Comparator 0
                        }
                    }
                )

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

    fun clear() {
        val notesIterator = notes.iterator()
        while(notesIterator.hasNext()) {
            val note = notesIterator.next()
            (context as MainActivity).cancelAlarm(note, isDelete = true, isByUser = true)
            if (note.uri != null) context.deleteFileFromUri(note.uri!!)
            notesIterator.remove()
        }

        val filteredNotesIterator = notesFiltered.iterator()
        while(filteredNotesIterator.hasNext()) {
            val note = filteredNotesIterator.next()
            filteredNotesIterator.remove()
        }

        if (notes.count() == 0) {
            val message = (context as MainActivity).configureFragment.progressDialogHandler.obtainMessage()
            message.what = ConfigureFragment.STOP_PROGRESS_DIALOG
            context.configureFragment.progressDialogHandler.sendMessage(message)
            context.showToast("모든 노트가 삭제되었습니다.")
            notifyDataSetChanged()
        }
    }

    // 이미지 새로 찍었을 때 기존 이미지 제거에 사용가할듯.
    fun removePhoto(note: Note) {
        if (note.uri != null) (context as MainActivity).deleteFileFromUri(note.uri!!)
    }

    fun sort(sortingCriteria: Int): Long {
        this.sortingCriteria = sortingCriteria
        Collections.sort(notesFiltered,
            Comparator { o1: Note, o2: Note ->
                when (sortingCriteria) {
                    SortingCriteria.CREATION_TIME.index->
                        return@Comparator (o2.creationTime - o1.creationTime).toInt()
                    SortingCriteria.EDIT_TIME.index ->
                        return@Comparator ((o2.editTime ?: o2.creationTime) - (o1.editTime ?: o1.creationTime)).toInt()
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

    fun getSelectedNotePosition(): Int = notesFiltered.indexOf(selectedNote)

    fun getNoteByPosition(position: Int): Note = notesFiltered[position]

    fun getNoteById(id: Int): Note {
        return notes.filter{ it.id == id }[0]
    }

    fun getAllNotes() = notes

    // For smooth display when the folder is changed.
    fun removeFromNotesFiltered(note: Note) {
        val position: Int = notesFiltered.indexOf(note)
        notesFiltered.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun confirmConfiguration(activity: SingleNoteConfigureActivity, appWidgetId: Int) {

        val note = selectedNote!!

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