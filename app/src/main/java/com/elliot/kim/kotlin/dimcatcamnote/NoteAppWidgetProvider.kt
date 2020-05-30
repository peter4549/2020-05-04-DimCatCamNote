package com.elliot.kim.kotlin.dimcatcamnote

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.elliot.kim.kotlin.dimcatcamnote.activities.APP_WIDGET_PREFERENCES
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.SingleNoteConfigureActivity

// 뷰모델이 업데이트 된 상태에서 알려주는 것..
// 프리퍼런스로 값 전달하는 게 맞는듯. 인텐트를 리시브하는건 아닌듯. 가능은 하겟으나.. 쓰는데 이유가 있겟지.
// 일괄호출 콜백쓰는게 더 편하긴 할듯

class NoteAppWidgetProvider: AppWidgetProvider() {

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
    }

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
            //val intent = Intent() 잠시 보류..
            val pendingIntent: PendingIntent = Intent(context, SingleNoteConfigureActivity::class.java)
                .let { intent ->
                    PendingIntent.getActivity(context, 0, intent, 0)
                }

            val title = preferences.getString(KEY_APP_WIDGET_NOTE_TITLE + appWidgetId,
                "노트가 존재하지 않습니다.")
            val content = preferences.getString(KEY_APP_WIDGET_NOTE_CONTENT + appWidgetId,
                "노트가 존재하지 않습니다.")


            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            val views: RemoteViews = RemoteViews(
                context.packageName,
                R.layout.app_widget
            ).apply {
                setOnClickPendingIntent(R.id.text_view_content, pendingIntent) // 얘 말고, 버튼 클릭시.
                setCharSequence(R.id.text_view_title, "setText", title)
                setCharSequence(R.id.text_view_content, "setText", content)
            }

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views)

            super.onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
}