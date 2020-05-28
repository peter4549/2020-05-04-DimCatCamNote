package com.elliot.kim.kotlin.dimcatcamnote

import androidx.lifecycle.LiveData
import androidx.room.*

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