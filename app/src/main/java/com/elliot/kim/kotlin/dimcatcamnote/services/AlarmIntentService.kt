package com.elliot.kim.kotlin.dimcatcamnote.services

import android.app.AlarmManager
import android.app.IntentService
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.room.Room
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.EditActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.SingleNoteConfigureActivity
import com.elliot.kim.kotlin.dimcatcamnote.broadcast_receivers.AlarmReceiver
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.database.AppDatabase

class AlarmIntentService : IntentService("AlarmIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        val id = intent?.getIntExtra(
            KEY_NOTE_ID,
            DEFAULT_VALUE_NOTE_ID
        ) ?: DEFAULT_VALUE_NOTE_ID

        if (MainActivity.isAppRunning)
            notifyIsAppRunning(id)
        else
            if (id != DEFAULT_VALUE_NOTE_ID) updateDatabase(id)
    }

    private fun updateDatabase(id: Int) {
        val database: AppDatabase = Room.databaseBuilder(
            application, AppDatabase::class.java,
            MainActivity.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
        val dao = database.dao()
        val note = dao.findNoteById(id)
        note.alarmTime = null
        dao.update(note)
        updateAppWidget(note)
        removeAlarmPreferences(id)
    }

    private fun notifyIsAppRunning(id: Int) {
        val intent = Intent(MainActivity.ACTION_IS_APP_RUNNING)
        intent.putExtra(KEY_NOTE_ID, id)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun removeAlarmPreferences(id: Int) {
        val sharedPreferences = getSharedPreferences(
            PREFERENCES_NAME_ALARM,
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.remove(id.toString() + "0")
        editor.remove(id.toString() + "1")
        editor.remove(id.toString() + "2")
        editor.remove(id.toString() + "3")
        editor.apply()
    }

    private fun updateAppWidget(note: Note) {
        if (note.appWidgetIds.isNotEmpty()) {
            val colorPreferences = this.getSharedPreferences(PREFERENCES_SET_COLOR,
                Context.MODE_PRIVATE)
            val opacityPreferences =
                this.getSharedPreferences(PREFERENCES_OPACITY, Context.MODE_PRIVATE)

            // The color resource ID must be converted to a color value by the getColor method
            // before being used.
            // The value stored in the preference is a color value
            // that has already been converted in SetNoteColorDialogFragment.
            val appWidgetTitleColor = colorPreferences.getInt(KEY_COLOR_APP_WIDGET_TITLE,
                this.getColor(R.color.defaultColorAppWidgetTitle))
            val appWidgetBackgroundColor = colorPreferences.getInt(KEY_COLOR_APP_WIDGET_BACKGROUND,
                this.getColor(R.color.defaultColorAppWidgetBackground))
            val opacity = opacityPreferences.getString(KEY_OPACITY, DEFAULT_HEX_OPACITY.toString())
            val argbChannelTitleColor =
                String.format("#${opacity}%06X", 0xFFFFFF and appWidgetTitleColor)
            val argbChannelBackgroundColor =
                String.format("#${opacity}%06X", 0xFFFFFF and appWidgetBackgroundColor)

            note.appWidgetIds.forEach {
                val preferences =
                    this.getSharedPreferences(APP_WIDGET_PREFERENCES, Context.MODE_PRIVATE)
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

                val intent = Intent(this, EditActivity::class.java)
                intent.action = ACTION_APP_WIDGET_ATTACHED + note.id
                val pendingIntent: PendingIntent = intent.let {
                    PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                }

                val noteConfigureIntent = Intent(this, SingleNoteConfigureActivity::class.java)
                // App widget ID is sent to SingleNoteConfigureActivity through the action.
                noteConfigureIntent.action = ACTION_APP_WIDGET_ATTACHED + it

                val noteConfigurePendingIntent = noteConfigureIntent.let {
                    PendingIntent.getActivity(this, 0, noteConfigureIntent, 0)
                }

                val views: RemoteViews = RemoteViews(
                    this.packageName,
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
                val appWidgetManager = AppWidgetManager.getInstance(this)
                appWidgetManager.updateAppWidget(it, views)
            }

            val intent = Intent(this, NoteAppWidgetProvider::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(
                    ComponentName(application,
                        NoteAppWidgetProvider::class.java)
                )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
    }
}
