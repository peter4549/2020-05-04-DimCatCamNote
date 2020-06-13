package com.elliot.kim.kotlin.dimcatcamnote.services

import android.app.IntentService
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.room.Room
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.database.AppDatabase

class AlarmIntentService : IntentService("AlarmIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        val id = intent?.getIntExtra(
            KEY_NOTE_ID,
            DEFAULT_VALUE_NOTE_ID
        ) ?: DEFAULT_VALUE_NOTE_ID

        if (MainActivity.isAppRunning) notifyIsAppRunning(id)
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
        removeAlarmPreferences(id)
    }

    private fun notifyIsAppRunning(id: Int) {
        val intent = Intent(MainActivity.ACTION_IS_APP_RUNNING)
        intent.putExtra(KEY_NOTE_ID, id)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun removeAlarmPreferences(id: Int) {
        val sharedPreferences = getSharedPreferences(
            "alarm_information",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.remove(id.toString() + "0")
        editor.remove(id.toString() + "1")
        editor.remove(id.toString() + "2")
        editor.remove(id.toString() + "3")
        editor.apply()
    }
}
