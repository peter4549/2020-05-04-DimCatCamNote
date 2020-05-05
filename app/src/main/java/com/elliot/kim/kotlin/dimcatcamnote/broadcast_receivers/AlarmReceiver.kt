package com.elliot.kim.kotlin.dimcatcamnote.broadcast_receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.elliot.kim.kotlin.dimcatcamnote.fragments.AlarmFragment
import com.elliot.kim.kotlin.dimcatcamnote.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.services.AlarmIntentService

const val CHANNEL_ID = "default"
const val CHANNEL_NAME = "elliot_cam_note_channel"

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val builder = NotificationCompat.Builder(context!!, "default")
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intentNotification = Intent(context, MainActivity::class.java)
        val serviceIntent = Intent(context, AlarmIntentService::class.java)

        val id = intent!!.getIntExtra(AlarmFragment.KEY_ID, 0)
        val title = intent.getStringExtra(AlarmFragment.KEY_TITLE)
        val content = intent.getStringExtra(AlarmFragment.KEY_CONTENT)

        intentNotification.action = "ALARM_ACTION"
        intentNotification.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        intentNotification.putExtra("ID", id)

        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intentNotification,
            PendingIntent.FLAG_ONE_SHOT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)

            channel.description = "Notification channel for the Cat Note"
            manager.createNotificationChannel(channel)
            builder.setSmallIcon(R.drawable.time_8c9eff_240)
        } else builder.setSmallIcon(R.mipmap.time_8c9eff_240)

        builder.setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(title)
            .setContentText(content)
            .setContentInfo("Content of note set as notification")
            .setContentIntent(pendingIntent)

        manager.notify(id, builder.build())

        serviceIntent.putExtra("ID", id)
        context.startService(serviceIntent)
    }
}