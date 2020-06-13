package com.elliot.kim.kotlin.dimcatcamnote.view_model

import android.app.Application
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.APP_WIDGET_PREFERENCES
import com.elliot.kim.kotlin.dimcatcamnote.activities.EditActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.SingleNoteConfigureActivity
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class MainViewModel(application: Application): AndroidViewModel(application) {

    private val database = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        MainActivity.DATABASE_NAME
    ).build()

    private lateinit var context: Context
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    var targetNote: Note? = null

    fun setContext(context: Context) { this.context = context }

    fun getAll(): LiveData<MutableList<Note>> = database.dao().getAll()

    fun insert(note: Note) {
        scope.launch {
            targetNote = note
            database.dao().insert(note)
        }
    }

    fun update(note: Note, updateAppWidget: Boolean = true) {
        if (updateAppWidget) {
            if (note.appWidgetIds.isNotEmpty()) {
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
                    val views: RemoteViews = RemoteViews(
                        context.packageName,
                        R.layout.app_widget
                    ).apply {
                        setOnClickPendingIntent(R.id.text_view_content, pendingIntent)
                        setCharSequence(R.id.text_view_title, "setText", note.title)
                        setCharSequence(R.id.text_view_content, "setText", note.content)

                        if (note.isDone) setViewVisibility(R.id.image_view_done, View.VISIBLE)
                        else setViewVisibility(R.id.image_view_done, View.GONE)

                        if (note.isLocked) setViewVisibility(R.id.image_view_lock, View.VISIBLE)
                        else setViewVisibility(R.id.image_view_lock, View.GONE)

                        if (note.alarmTime == null || note.alarmTime == 0L) setViewVisibility(R.id.image_view_alarm, View.GONE)
                        else setViewVisibility(R.id.image_view_alarm, View.VISIBLE)
                    }
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    // Tell the AppWidgetManager to perform an update on the current app widget
                    appWidgetManager.updateAppWidget(it, views)
                }

                val intent = Intent(context, NoteAppWidgetProvider::class.java)
                intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val ids = AppWidgetManager.getInstance(getApplication())
                    .getAppWidgetIds(ComponentName(getApplication(),
                        NoteAppWidgetProvider::class.java))
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
               // context.sendBroadcast(intent) 리시버 안쓰고 업데이트 되나.. 췍.
                // 걍 여기서 호출해서 족치자.




            }
        }
        scope.launch {
            targetNote = note
            database.dao().update(note)
        }
    }

    fun delete(note: Note) {
        if (note.appWidgetIds.isNotEmpty()) {
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
                // App Widget ID is sent to SingleNoteConfigureActivity through the action.
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
                    setOnClickPendingIntent(R.id.text_view_content, pendingIntent)
                    setCharSequence(R.id.text_view_title, "setText", context.getString(R.string.no_attachment_message))
                    setCharSequence(R.id.text_view_content, "setText", context.getString(R.string.no_attachment_message))
                }

                // Tell the AppWidgetManager to perform an update on the current app widget
                val appWidgetManager = AppWidgetManager.getInstance(context)
                appWidgetManager.updateAppWidget(it, views)
            }

            val intent = Intent(context, NoteAppWidgetProvider::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(ComponentName(getApplication(),
                    NoteAppWidgetProvider::class.java))

            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
        scope.launch {
            targetNote = note
            database.dao().delete(note)
        }
    }

    fun delete(id: Int) {
        scope.launch {
            targetNote = database.dao().findNoteById(id)
            database.dao().delete(database.dao().findNoteById(id))
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}