package com.elliot.kim.kotlin.dimcatcamnote

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews

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
            // Create an Intent to launch ExampleActivity
            val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
                .let { intent ->
                    PendingIntent.getActivity(context, 0, intent, 0)
                }

            val text = preferences.getString(KEY_NOTE_TITLE + appWidgetId, "Fale")


            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            val views: RemoteViews = RemoteViews(
                context.packageName,
                R.layout.app_widget
            ).apply {
                setOnClickPendingIntent(R.id.text_view_content, pendingIntent)
                setCharSequence(R.id.text_view_content, "setText", text)
            }

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views)

            super.onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
}