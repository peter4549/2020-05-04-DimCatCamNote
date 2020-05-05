package com.elliot.kim.kotlin.dimcatcamnote.services

import android.app.IntentService
import android.content.Intent
import androidx.room.Room
import com.elliot.kim.kotlin.dimcatcamnote.AppDatabase
import com.elliot.kim.kotlin.dimcatcamnote.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.Note
import com.elliot.kim.kotlin.dimcatcamnote.NoteDao

class AlarmIntentService : IntentService("AlarmIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        val id = intent?.getIntExtra("ID", -1) ?: -1
        if (id != -1) {
            val database: AppDatabase = Room.databaseBuilder(
                application, AppDatabase::class.java,
                MainActivity.DATABASE_NAME
            ).fallbackToDestructiveMigration().build()
            val dao: NoteDao = database.dao()
            val note: Note = dao.findNoteById(id)

            note.alarmTime = null
            dao.update(note)
        }
    }
}
