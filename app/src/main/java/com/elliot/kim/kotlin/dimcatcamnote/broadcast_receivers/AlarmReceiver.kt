package com.elliot.kim.kotlin.dimcatcamnote.broadcast_receivers

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.elliot.kim.kotlin.dimcatcamnote.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.fragments.AlarmFragment
import com.elliot.kim.kotlin.dimcatcamnote.services.AlarmIntentService


class AlarmReceiver : BroadcastReceiver() {

    private lateinit var activity: MainActivity

    fun setActivity(activity: MainActivity) {
        this.activity = MainActivity()
        this.activity = activity
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val builder = NotificationCompat.Builder(context!!, CHANNEL_ID)
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationIntent = Intent(context, MainActivity::class.java)
        val serviceIntent = Intent(context, AlarmIntentService::class.java)
        val id = intent!!.getIntExtra(AlarmFragment.KEY_ID_EXTRA,
            AlarmFragment.DEFAULT_VALUE_EXTRA)
        if (id == AlarmFragment.DEFAULT_VALUE_EXTRA) return
        val title = intent.getStringExtra(AlarmFragment.KEY_TITLE_EXTRA)
        val content = intent.getStringExtra(AlarmFragment.KEY_CONTENT_EXTRA)

        notificationIntent.action = ACTION_ALARM
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        notificationIntent.putExtra(AlarmFragment.KEY_ID_EXTRA, id)

        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            notificationIntent,
            PendingIntent.FLAG_ONE_SHOT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)

            builder.setSmallIcon(R.drawable.ic_notification)
            channel.description = CHANNEL_DESCRIPTION
            manager.createNotificationChannel(channel)
        } else builder.setSmallIcon(R.mipmap.ic_notification)

        builder.setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(title)
            .setContentText(content)
            .setContentInfo(CONTENT_INFO)
            .setContentIntent(pendingIntent)
        manager.notify(id, builder.build())

        serviceIntent.putExtra(AlarmFragment.KEY_ID_EXTRA, id)
        context.startService(serviceIntent)

        /*
        if (isAppRunning(context)) {
            val note = activity.getNoteById(id)
            activity.cancelAlarm(note, false)
        } else {
            serviceIntent.putExtra(AlarmFragment.KEY_ID_EXTRA, id)
            context.startService(serviceIntent)
        }

         */
    }

    companion object {
        private const val ACTION_ALARM = "action_alarm"
        private const val CHANNEL_ID = "default"
        private const val CHANNEL_NAME = "com_duke_elliot_kim_kotlin_cat_note" // 이름 차후 수정 아래도.
        private const val CHANNEL_DESCRIPTION = "dim_cat_note_channel"
        private const val CONTENT_INFO = "note_info"
    }
}