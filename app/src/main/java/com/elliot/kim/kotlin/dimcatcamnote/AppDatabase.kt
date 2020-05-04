package com.elliot.kim.kotlin.dimcatcamnote

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Note::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): NoteDao
}