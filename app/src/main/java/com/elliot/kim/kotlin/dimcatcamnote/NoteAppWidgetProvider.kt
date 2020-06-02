package com.elliot.kim.kotlin.dimcatcamnote

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.elliot.kim.kotlin.dimcatcamnote.activities.APP_WIDGET_PREFERENCES
import com.elliot.kim.kotlin.dimcatcamnote.activities.EditActivity
import java.lang.Exception

class NoteAppWidgetProvider: AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val preferences =
            context.getSharedPreferences(APP_WIDGET_PREFERENCES, Context.MODE_PRIVATE)

        // Perform this loop procedure for each App Widget that belongs to this provider
        appWidgetIds.forEach { appWidgetId ->
            val id = preferences.getInt(KEY_APP_WIDGET_NOTE_ID + appWidgetId, DEFAULT_VALUE_NOTE_ID)
            //if (id == DEFAULT_VALUE_NOTE_ID) throw Exception("Unexpected id.")
            val title = preferences.getString(KEY_APP_WIDGET_NOTE_TITLE + appWidgetId,
                "노트가 존재하지 않습니다.")
            val content = preferences.getString(KEY_APP_WIDGET_NOTE_CONTENT + appWidgetId,
                "노트가 존재하지 않습니다.")
            val uri = preferences.getString(KEY_APP_WIDGET_NOTE_URI + appWidgetId, "")
            val creationTime = preferences.getLong(KEY_APP_WIDGET_NOTE_CREATION_TIME + appWidgetId, 0L)
            val alarmTime = preferences.getLong(KEY_APP_WIDGET_NOTE_ALARM_TIME + appWidgetId, 0L)
            val editTime = preferences.getLong(KEY_APP_WIDGET_NOTE_EDIT_TIME + appWidgetId, 0L)
            val isDone = preferences.getBoolean(KEY_APP_WIDGET_NOTE_IS_DONE + appWidgetId, false)
            val isLocked = preferences.getBoolean(KEY_APP_WIDGET_NOTE_IS_LOCKED + appWidgetId, false)
            val password = preferences.getString(KEY_APP_WIDGET_NOTE_PASSWORD + appWidgetId, "")

            // Create an Intent to launch EditActivity.
            val intent = Intent(context, EditActivity::class.java)
            intent.action = ACTION_APP_WIDGET_ATTACHED + id
            val pendingIntent: PendingIntent = intent.let {
                    PendingIntent.getActivity(context, 0, it, 0)
                }

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            val views: RemoteViews = RemoteViews(
                context.packageName,
                R.layout.app_widget
            ).apply {
                setOnClickPendingIntent(R.id.text_view_content, pendingIntent)
                setCharSequence(R.id.text_view_title, "setText", title)
                setCharSequence(R.id.text_view_content, "setText", content)

                if (isDone) setViewVisibility(R.id.image_view_done, View.VISIBLE)
                else setViewVisibility(R.id.image_view_done, View.GONE)

                if (isLocked) setViewVisibility(R.id.image_view_lock, View.VISIBLE)
                else setViewVisibility(R.id.image_view_lock, View.GONE)

                if (alarmTime != 0L) setViewVisibility(R.id.image_view_alarm, View.VISIBLE)
                else setViewVisibility(R.id.image_view_alarm, View.GONE)
            }

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views)

            //super.onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
}