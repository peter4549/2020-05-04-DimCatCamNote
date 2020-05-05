package com.elliot.kim.kotlin.dimcatcamnote.broadcast_receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.elliot.kim.kotlin.dimcatcamnote.fragments.AlarmFragment
import java.util.*

class DeviceBootReceiver : BroadcastReceiver()  {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intentToAlarmReceiver = Intent(context, AlarmReceiver::class.java)
            val preferences = context.getSharedPreferences(
                "alarm_preferences",
                Context.MODE_PRIVATE
            )

            val entries = preferences.all
            val entriesSize = entries.size
            val intKeySet =
                Arrays.stream(entries.keys.toTypedArray())
                    .mapToInt { s: String -> s.toInt() }.toArray()

            val calendar: Calendar = GregorianCalendar()

            var count = 0
            var id = 0
            var title: String? = ""
            var content: String? = ""

            Arrays.sort(intKeySet)

            for (i in 0 until entriesSize) {
                val key = intKeySet[i].toString()
                when (count) {
                    0 -> id = preferences.getInt(key, 0)
                    1 -> calendar.timeInMillis = preferences.getLong(key, 0)
                    2 -> title = preferences.getString(key, "")
                    3 -> content = preferences.getString(key, "")
                }

                ++count
                if (count == 4) {
                    count = 0

                    intentToAlarmReceiver.putExtra(AlarmFragment.KEY_ID, id)
                    intentToAlarmReceiver.putExtra(AlarmFragment.KEY_TITLE, title)
                    intentToAlarmReceiver.putExtra(AlarmFragment.KEY_CONTENT, content)

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        id,
                        intentToAlarmReceiver,
                        PendingIntent.FLAG_ONE_SHOT
                    )

                    alarmManager[AlarmManager.RTC_WAKEUP, calendar.timeInMillis] = pendingIntent
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                }
            }
        }
    }
}