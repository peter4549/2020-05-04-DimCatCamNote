package com.elliot.kim.kotlin.dimcatcamnote.services

import android.app.ActivityManager
import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.room.Room
import com.elliot.kim.kotlin.dimcatcamnote.AppDatabase
import com.elliot.kim.kotlin.dimcatcamnote.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.Note
import com.elliot.kim.kotlin.dimcatcamnote.NoteDao
import com.elliot.kim.kotlin.dimcatcamnote.broadcast_receivers.AlarmReceiver
import com.elliot.kim.kotlin.dimcatcamnote.fragments.AlarmFragment

class AlarmIntentService : IntentService("AlarmIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        val id = intent?.getIntExtra(
            AlarmFragment.KEY_ID_EXTRA,
            AlarmFragment.DEFAULT_VALUE_EXTRA
        ) ?: AlarmFragment.DEFAULT_VALUE_EXTRA

        if (isAppRunning(this)) notifyIsAppRunning(id)
        else {
            if (id != AlarmFragment.DEFAULT_VALUE_EXTRA) {
                val database: AppDatabase = Room.databaseBuilder(
                    application, AppDatabase::class.java,
                    MainActivity.DATABASE_NAME
                ).fallbackToDestructiveMigration().build()
                val dao = database.dao()
                val note = dao.findNoteById(id)
                note.alarmTime = null
                dao.update(note)
            }
        }
    }

    private fun isAppRunning(context: Context?): Boolean {
        val activityManager = context!!
            .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val packageName = context.packageName
        val processInfoList: List<ActivityManager.RunningAppProcessInfo>
                = activityManager.runningAppProcesses
        for (processInfo in processInfoList)
            if (processInfo.processName == packageName) return true
        return false
    }

    private fun notifyIsAppRunning(id: Int) {
        val intent = Intent(MainActivity.ACTION_IS_APP_RUNNING)
        intent.putExtra(AlarmFragment.KEY_ID_EXTRA, id)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}
