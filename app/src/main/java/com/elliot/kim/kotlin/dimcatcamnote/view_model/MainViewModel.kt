package com.elliot.kim.kotlin.dimcatcamnote.view_model

import android.app.Application
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.EditActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.SingleNoteConfigureActivity
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.database.AppDatabase
import kotlinx.coroutines.*

class MainViewModel(application: Application): AndroidViewModel(application) {

    private val database = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        MainActivity.DATABASE_NAME
    ).build()

    private lateinit var context: Context
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    var itemCount = -1
    var targetNote: Note? = null

    fun setContext(context: Context) { this.context = context }

    fun getAll(): LiveData<MutableList<Note>> = database.dao().getAll()

    fun insert(note: Note) {
        scope.launch {
            targetNote = note
            database.dao().insert(note)
            itemCount = getItemCount()
        }
    }

    fun update(note: Note, updateAppWidget: Boolean = true) {
        scope.launch {
            targetNote = note
            database.dao().update(note)
            itemCount = getItemCount()
        }

        if (updateAppWidget) {
            if (note.appWidgetIds.isNotEmpty()) {
                val colorPreferences = context.getSharedPreferences(PREFERENCES_SET_COLOR,
                    Context.MODE_PRIVATE)
                val opacityPreferences =
                    context.getSharedPreferences(PREFERENCES_OPACITY, Context.MODE_PRIVATE)

                // The color resource ID must be converted to a color value by the getColor method
                // before being used.
                // The value stored in the preference is a color value
                // that has already been converted in SetNoteColorDialogFragment.
                val appWidgetTitleColor = colorPreferences.getInt(KEY_COLOR_APP_WIDGET_TITLE,
                    context.getColor(R.color.defaultColorAppWidgetTitle))
                val appWidgetBackgroundColor = colorPreferences.getInt(KEY_COLOR_APP_WIDGET_BACKGROUND,
                    context.getColor(R.color.defaultColorAppWidgetBackground))
                val opacity = opacityPreferences.getString(KEY_OPACITY, DEFAULT_HEX_OPACITY.toString())
                val argbChannelTitleColor =
                    String.format("#${opacity}%06X", 0xFFFFFF and appWidgetTitleColor)
                val argbChannelBackgroundColor =
                    String.format("#${opacity}%06X", 0xFFFFFF and appWidgetBackgroundColor)

                note.appWidgetIds.forEach {
                    val preferences =
                        context.getSharedPreferences(APP_WIDGET_PREFERENCES, Context.MODE_PRIVATE)
                    val editor = preferences.edit()
                    editor.putBoolean(KEY_APP_WIDGET_NOTE_EXIST + it, true)
                    editor.putInt(KEY_APP_WIDGET_NOTE_ID + it, note.id)
                    editor.putString(KEY_APP_WIDGET_NOTE_TITLE + it, note.title)
                    editor.putString(KEY_APP_WIDGET_NOTE_CONTENT + it, note.content)
                    editor.putString(KEY_APP_WIDGET_NOTE_URI + it, note.uri ?: "")
                    editor.putLong(KEY_APP_WIDGET_NOTE_CREATION_TIME + it, note.creationTime)
                    editor.putLong(KEY_APP_WIDGET_NOTE_EDIT_TIME + it, note.editTime ?: 0L)
                    editor.putLong(KEY_APP_WIDGET_NOTE_ALARM_TIME + it, note.alarmTime ?: 0L)
                    editor.putBoolean(KEY_APP_WIDGET_NOTE_IS_DONE + it, note.isDone)
                    editor.putBoolean(KEY_APP_WIDGET_NOTE_IS_LOCKED + it, note.isLocked)
                    editor.putString(KEY_APP_WIDGET_NOTE_PASSWORD + it, note.password ?: "")
                    editor.apply()

                    val intent = Intent(context, EditActivity::class.java)
                    intent.action = ACTION_APP_WIDGET_ATTACHED + note.id
                    val pendingIntent: PendingIntent = intent.let {
                        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    }

                    val noteConfigureIntent = Intent(context, SingleNoteConfigureActivity::class.java)
                    // App widget ID is sent to SingleNoteConfigureActivity through the action.
                    noteConfigureIntent.action = ACTION_APP_WIDGET_ATTACHED + it

                    val noteConfigurePendingIntent = noteConfigureIntent.let {
                        PendingIntent.getActivity(context, 0, noteConfigureIntent, 0)
                    }

                    val views: RemoteViews = RemoteViews(
                        context.packageName,
                        R.layout.app_widget
                    ).apply {
                        setInt(R.id.title_container, "setBackgroundColor", Color.parseColor(argbChannelTitleColor))
                        setInt(R.id.text_view_content, "setBackgroundColor",
                            Color.parseColor(argbChannelBackgroundColor))
                        setOnClickPendingIntent(R.id.text_view_content, pendingIntent)
                        setOnClickPendingIntent(R.id.image_button_change, noteConfigurePendingIntent)
                        setCharSequence(R.id.text_view_title, "setText", note.title)
                        setCharSequence(R.id.text_view_content, "setText", note.content)

                        if (note.isDone) setViewVisibility(R.id.image_view_done, View.VISIBLE)
                        else setViewVisibility(R.id.image_view_done, View.GONE)

                        if (note.isLocked) setViewVisibility(R.id.image_view_lock, View.VISIBLE)
                        else setViewVisibility(R.id.image_view_lock, View.GONE)

                        if (note.alarmTime == null) {
                            setViewVisibility(R.id.image_view_alarm, View.GONE)
                            setViewVisibility(R.id.text_view_alarm_time, View.GONE)
                        } else {
                            setViewVisibility(R.id.image_view_alarm, View.VISIBLE)
                            setViewVisibility(R.id.text_view_alarm_time, View.VISIBLE)
                            setCharSequence(R.id.text_view_alarm_time, "setText",
                                " " + MainActivity.longTimeToString(note.alarmTime, PATTERN_UP_TO_MINUTES))
                        }

                        if (note.editTime == null) {
                            setViewVisibility(R.id.text_view_creation_time, View.VISIBLE)
                            setViewVisibility(R.id.text_view_edit_time, View.GONE)
                            setCharSequence(R.id.text_view_creation_time, "setText",
                                " " + MainActivity.longTimeToString(note.creationTime, PATTERN_UP_TO_MINUTES))
                        } else {
                            setViewVisibility(R.id.text_view_edit_time, View.VISIBLE)
                            setViewVisibility(R.id.text_view_creation_time, View.GONE)
                            setCharSequence(R.id.text_view_edit_time, "setText",
                                " " + MainActivity.longTimeToString(note.editTime, PATTERN_UP_TO_MINUTES))
                        }

                        setInt(R.id.text_view_alarm_time, "setBackgroundColor", Color.parseColor(argbChannelBackgroundColor))
                        setInt(R.id.text_view_creation_time, "setBackgroundColor", Color.parseColor(argbChannelBackgroundColor))
                        setInt(R.id.text_view_edit_time, "setBackgroundColor", Color.parseColor(argbChannelBackgroundColor))

                        if (note.uri == null) setViewVisibility(R.id.image_view_photo, View.GONE)
                        else setViewVisibility(R.id.image_view_photo, View.VISIBLE)
                    }

                    // Notify appWidgetManager of app widget updates.
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    appWidgetManager.updateAppWidget(it, views)
                }

                val intent = Intent(context, NoteAppWidgetProvider::class.java)
                intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val ids = AppWidgetManager.getInstance(getApplication())
                    .getAppWidgetIds(ComponentName(getApplication(),
                        NoteAppWidgetProvider::class.java))
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
        }
    }

    fun delete(note: Note, allowDelete: Boolean = true) {
        MainActivity.allowAdapterDelete = allowDelete
        scope.launch {
            if (note.appWidgetIds.isNotEmpty()) {
                val colorPreferences = context.getSharedPreferences(
                    PREFERENCES_SET_COLOR,
                    Context.MODE_PRIVATE
                )
                val opacityPreferences =
                    context.getSharedPreferences(PREFERENCES_OPACITY, Context.MODE_PRIVATE)

                // The color resource ID must be converted to a color value by the getColor method
                // before being used.
                // The value stored in the preference is a color value
                // that has already been converted in SetNoteColorDialogFragment.
                val appWidgetTitleColor = colorPreferences.getInt(
                    KEY_COLOR_NOTE,
                    context.getColor(R.color.defaultColorNote)
                )
                val appWidgetBackgroundColor = colorPreferences.getInt(
                    KEY_COLOR_APP_WIDGET_BACKGROUND,
                    context.getColor(R.color.defaultColorAppWidgetBackground)
                )
                val opacity =
                    opacityPreferences.getString(KEY_OPACITY, DEFAULT_HEX_OPACITY.toString())
                val argbChannelTitleColor =
                    String.format("#${opacity}%06X", 0xFFFFFF and appWidgetTitleColor)
                val argbChannelBackgroundColor =
                    String.format("#${opacity}%06X", 0xFFFFFF and appWidgetBackgroundColor)

                note.appWidgetIds.forEach {
                    val preferences =
                        context.getSharedPreferences(APP_WIDGET_PREFERENCES, Context.MODE_PRIVATE)
                    val editor = preferences.edit()
                    editor.remove(KEY_APP_WIDGET_NOTE_ID + it)
                    editor.remove(KEY_APP_WIDGET_NOTE_EXIST + it)
                    editor.remove(KEY_APP_WIDGET_NOTE_TITLE + it)
                    editor.remove(KEY_APP_WIDGET_NOTE_CONTENT + it)
                    editor.remove(KEY_APP_WIDGET_NOTE_URI + it)
                    editor.remove(KEY_APP_WIDGET_NOTE_CREATION_TIME + it)
                    editor.remove(KEY_APP_WIDGET_NOTE_EDIT_TIME + it)
                    editor.remove(KEY_APP_WIDGET_NOTE_ALARM_TIME + it)
                    editor.remove(KEY_APP_WIDGET_NOTE_IS_DONE + it)
                    editor.remove(KEY_APP_WIDGET_NOTE_IS_LOCKED + it)
                    editor.remove(KEY_APP_WIDGET_NOTE_PASSWORD + it)
                    editor.apply()

                    val intent = Intent(context, SingleNoteConfigureActivity::class.java)
                    // App widget ID is sent to SingleNoteConfigureActivity through the action.
                    intent.action = ACTION_APP_WIDGET_ATTACHED + it

                    val pendingIntent = intent.let {
                        PendingIntent.getActivity(context, 0, intent, 0)
                    }

                    // Get the layout for the App Widget and attach an on-click listener
                    // to the button
                    val views: RemoteViews = RemoteViews(
                        context.packageName,
                        R.layout.app_widget
                    ).apply {
                        setViewVisibility(R.id.image_view_done, View.GONE)
                        setViewVisibility(R.id.image_view_lock, View.GONE)
                        setViewVisibility(R.id.image_view_alarm, View.GONE)
                        setViewVisibility(R.id.text_view_creation_time, View.GONE)
                        setViewVisibility(R.id.text_view_edit_time, View.GONE)
                        setViewVisibility(R.id.text_view_alarm_time, View.GONE)
                        setViewVisibility(R.id.image_view_photo, View.GONE)
                        setInt(
                            R.id.title_container,
                            "setBackgroundColor",
                            Color.parseColor(argbChannelTitleColor)
                        )
                        setInt(
                            R.id.text_view_content,
                            "setBackgroundColor",
                            Color.parseColor(argbChannelBackgroundColor)
                        )
                        setOnClickPendingIntent(R.id.text_view_content, pendingIntent)
                        setOnClickPendingIntent(R.id.image_button_change, pendingIntent)
                        setCharSequence(
                            R.id.text_view_title,
                            "setText",
                            context.getString(R.string.no_attachment_message)
                        )
                        setCharSequence(
                            R.id.text_view_content,
                            "setText",
                            context.getString(R.string.no_attachment_message)
                        )
                    }

                    // Notify appWidgetManager of app widget updates.
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    appWidgetManager.updateAppWidget(it, views)
                }

                val intent = Intent(context, NoteAppWidgetProvider::class.java)
                intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val ids = AppWidgetManager.getInstance(getApplication())
                    .getAppWidgetIds(
                        ComponentName(
                            getApplication(),
                            NoteAppWidgetProvider::class.java
                        )
                    )

                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                context.sendBroadcast(intent)
            }

            scope.launch {
                targetNote = note
                database.dao().delete(note)
                itemCount = getItemCount()
            }
        }
    }

    private suspend fun getItemCount(): Int = scope.async {
        return@async database.dao().getItemCount()
    }.await()

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}