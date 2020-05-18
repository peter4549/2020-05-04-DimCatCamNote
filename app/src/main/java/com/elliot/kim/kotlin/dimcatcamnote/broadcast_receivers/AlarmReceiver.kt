package com.elliot.kim.kotlin.dimcatcamnote.broadcast_receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.elliot.kim.kotlin.dimcatcamnote.fragments.AlarmFragment
import com.elliot.kim.kotlin.dimcatcamnote.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.services.AlarmIntentService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val builder = NotificationCompat.Builder(context!!, "default")
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationIntent = Intent(context, MainActivity::class.java)
        val serviceIntent = Intent(context, AlarmIntentService::class.java)

        val id = intent!!.getIntExtra(AlarmFragment.KEY_ID, -1)
        if (id == -1) {
            Log.e(TAG, "ID not found.")
            return
        }

        val title = intent.getStringExtra(AlarmFragment.KEY_TITLE)
        val content = intent.getStringExtra(AlarmFragment.KEY_CONTENT)

        notificationIntent.action = "ALARM_ACTION"
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        notificationIntent.putExtra("ID", id)

        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            notificationIntent,
            PendingIntent.FLAG_ONE_SHOT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            channel.description = "Notification channel for the Cat Note" // 차후 수정

            manager.createNotificationChannel(channel)
            builder.setSmallIcon(R.drawable.ic_notification)
        } else builder.setSmallIcon(R.mipmap.ic_notification)

        builder.setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(title)
            .setContentText(content)
            .setContentInfo("Content of note set as notification") // 차후 수정 or Not..
            .setContentIntent(pendingIntent)

        manager.notify(id, builder.build())

        serviceIntent.putExtra("ID", id)
        context.startService(serviceIntent)
    }

    companion object {
        private const val TAG = "AlarmReceiver"

        private const val CHANNEL_ID = "default"
        private const val CHANNEL_NAME = "com_duke_elliot_kim_kotlin_cat_note" // 이름 차후 수정
    }
}