package com.elliot.kim.kotlin.dimcatcamnote.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.elliot.kim.kotlin.dimcatcamnote.data.Note

@Database(entities = [Note::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): NoteDao
}