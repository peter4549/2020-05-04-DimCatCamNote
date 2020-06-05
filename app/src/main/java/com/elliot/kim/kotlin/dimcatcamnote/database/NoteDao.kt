package com.elliot.kim.kotlin.dimcatcamnote.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.elliot.kim.kotlin.dimcatcamnote.data.Note

@Dao
interface NoteDao {

    @Query("SELECT * FROM note")
    fun getAll(): LiveData<MutableList<Note>>

    @Insert
    fun insert(note: Note)

    @Update
    fun update(note: Note)

    @Delete
    fun delete(note: Note)

    @Query("SELECT * FROM note WHERE id LIKE :id")
    fun findNoteById(id: Int): Note
}