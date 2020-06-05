package com.elliot.kim.kotlin.dimcatcamnote.view_model

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.APP_WIDGET_PREFERENCES
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
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
                }

                val intent = Intent(context, NoteAppWidgetProvider::class.java)
                intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val ids = AppWidgetManager.getInstance(getApplication())
                    .getAppWidgetIds(ComponentName(getApplication(),
                        NoteAppWidgetProvider::class.java))
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                context.sendBroadcast(intent)
            }
        }
        scope.launch {
            targetNote = note
            database.dao().update(note)
        }
    }

    fun delete(note: Note) {
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