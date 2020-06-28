package com.elliot.kim.kotlin.dimcatcamnote

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import com.elliot.kim.kotlin.dimcatcamnote.activities.EditActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.SingleNoteConfigureActivity


class NoteAppWidgetProvider: AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val colorPreferences = context.getSharedPreferences(PREFERENCES_SET_COLOR,
            Context.MODE_PRIVATE)
        val opacityPreferences =
            context.getSharedPreferences(PREFERENCES_OPACITY, Context.MODE_PRIVATE)
        val preferences =
            context.getSharedPreferences(APP_WIDGET_PREFERENCES, Context.MODE_PRIVATE)
        val noAttachmentMessage = context.getString(R.string.no_attachment_message)

        // The color resource ID must be converted to a color value by the getColor method
        // before being used.
        // The value stored in the preference is a color value
        // that has already been converted in SetNoteColorDialogFragment.
        val appWidgetTitleColor = colorPreferences.getInt(
            KEY_COLOR_APP_WIDGET_TITLE,
            context.getColor(R.color.defaultColorAppWidgetTitle))
        val appWidgetBackgroundColor = colorPreferences.getInt(
            KEY_COLOR_APP_WIDGET_BACKGROUND,
            context.getColor(R.color.defaultColorAppWidgetBackground))
        val opacity = opacityPreferences.getString(KEY_OPACITY, DEFAULT_HEX_OPACITY.toString())
        val argbChannelTitleColor =
            String.format("#${opacity}%06X", 0xFFFFFF and appWidgetTitleColor)
        val argbChannelBackgroundColor =
            String.format("#${opacity}%06X", 0xFFFFFF and appWidgetBackgroundColor)

        // Perform this loop procedure for each App Widget that belongs to this provider
        appWidgetIds.forEach { appWidgetId ->
            val noteExist = preferences.getBoolean(KEY_APP_WIDGET_NOTE_EXIST + appWidgetId, false)
            if (noteExist) {
                // Run when a note is attached.
                val noteId =
                    preferences.getInt(
                        KEY_APP_WIDGET_NOTE_ID + appWidgetId,
                        DEFAULT_VALUE_NOTE_ID
                    )
                val title = preferences.getString(
                    KEY_APP_WIDGET_NOTE_TITLE + appWidgetId, noAttachmentMessage
                )
                val content = preferences.getString(
                    KEY_APP_WIDGET_NOTE_CONTENT + appWidgetId, noAttachmentMessage
                )
                val uri = preferences.getString(KEY_APP_WIDGET_NOTE_URI + appWidgetId, "")
                val creationTime =
                    preferences.getLong(KEY_APP_WIDGET_NOTE_CREATION_TIME + appWidgetId, 0L)
                val alarmTime =
                    preferences.getLong(KEY_APP_WIDGET_NOTE_ALARM_TIME + appWidgetId, 0L)
                val editTime =
                    preferences.getLong(KEY_APP_WIDGET_NOTE_EDIT_TIME + appWidgetId, 0L)
                val isDone =
                    preferences.getBoolean(KEY_APP_WIDGET_NOTE_IS_DONE + appWidgetId, false)
                val isLocked =
                    preferences.getBoolean(KEY_APP_WIDGET_NOTE_IS_LOCKED + appWidgetId, false)
                val password =
                    preferences.getString(KEY_APP_WIDGET_NOTE_PASSWORD + appWidgetId, "")

                // Create an Intent to launch EditActivity.
                val intent = Intent(context, EditActivity::class.java)
                intent.action = ACTION_APP_WIDGET_ATTACHED + noteId
                val pendingIntent: PendingIntent = intent.let {
                    PendingIntent.getActivity(context, 0, it, 0)
                }

                val noteConfigureIntent = Intent(context, SingleNoteConfigureActivity::class.java)
                // App widget ID is sent to SingleNoteConfigureActivity through the action.
                noteConfigureIntent.action = ACTION_APP_WIDGET_ATTACHED + appWidgetId
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
                    setCharSequence(R.id.text_view_title, "setText", title)
                    setCharSequence(R.id.text_view_content, "setText", content)

                    if (isDone) setViewVisibility(R.id.image_view_done, View.VISIBLE)
                    else setViewVisibility(R.id.image_view_done, View.GONE)

                    if (isLocked) setViewVisibility(R.id.image_view_lock, View.VISIBLE)
                    else setViewVisibility(R.id.image_view_lock, View.GONE)

                    if (alarmTime == 0L) {
                        setViewVisibility(R.id.image_view_alarm, View.GONE)
                        setViewVisibility(R.id.text_view_alarm_time, View.GONE)
                    } else {
                        setViewVisibility(R.id.image_view_alarm, View.VISIBLE)
                        setViewVisibility(R.id.text_view_alarm_time, View.VISIBLE)
                        setCharSequence(R.id.text_view_alarm_time, "setText",
                            " " + MainActivity.longTimeToString(alarmTime, PATTERN_UP_TO_MINUTES))
                    }

                    if (editTime == 0L) {
                        setViewVisibility(R.id.text_view_creation_time, View.VISIBLE)
                        setViewVisibility(R.id.text_view_edit_time, View.GONE)
                        setCharSequence(R.id.text_view_creation_time, "setText",
                            " " + MainActivity.longTimeToString(creationTime, PATTERN_UP_TO_MINUTES))
                    } else {
                        setViewVisibility(R.id.text_view_edit_time, View.VISIBLE)
                        setViewVisibility(R.id.text_view_creation_time, View.GONE)
                        setCharSequence(R.id.text_view_edit_time, "setText",
                            " " + MainActivity.longTimeToString(editTime, PATTERN_UP_TO_MINUTES))
                    }

                    setInt(R.id.text_view_alarm_time, "setBackgroundColor", Color.parseColor(argbChannelBackgroundColor))
                    setInt(R.id.text_view_creation_time, "setBackgroundColor", Color.parseColor(argbChannelBackgroundColor))
                    setInt(R.id.text_view_edit_time, "setBackgroundColor", Color.parseColor(argbChannelBackgroundColor))

                    if (uri == "") setViewVisibility(R.id.image_view_photo, View.GONE)
                    else setViewVisibility(R.id.image_view_photo, View.VISIBLE)
                }

                // Notify appWidgetManager of app widget updates.
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } else {
                val intent = Intent(context, SingleNoteConfigureActivity::class.java)
                // App Widget ID is sent to SingleNoteConfigureActivity through the action.
                intent.action = ACTION_APP_WIDGET_ATTACHED + appWidgetId

                val pendingIntent = intent.let {
                    PendingIntent.getActivity(context, 0, it, 0)
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
                    setInt(R.id.title_container, "setBackgroundColor", Color.parseColor(argbChannelTitleColor))
                    setInt(R.id.content_container, "setBackgroundColor", Color.parseColor(argbChannelBackgroundColor))
                    setOnClickPendingIntent(R.id.text_view_content, pendingIntent)
                    setOnClickPendingIntent(R.id.image_button_change, pendingIntent)
                    setCharSequence(R.id.text_view_title, "setText", noAttachmentMessage)
                    setCharSequence(R.id.text_view_content, "setText", noAttachmentMessage)
                }

                // Notify appWidgetManager of app widget updates.
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
            super.onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
}