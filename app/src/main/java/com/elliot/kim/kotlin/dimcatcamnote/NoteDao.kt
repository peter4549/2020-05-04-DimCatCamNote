package com.elliot.kim.kotlin.dimcatcamnote

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {
    @Query("SELECT * FROM note")
    fun getAll(): LiveData<MutableList<Note>>

    @Insert
    fun insert(camNote: Note)

    @Update
    fun update(camNote: Note)

    @Delete
    fun delete(camNote: Note)

    @Query("SELECT * FROM Note WHERE id LIKE :id")
    fun findNoteByNumber(id: Int): Note
}