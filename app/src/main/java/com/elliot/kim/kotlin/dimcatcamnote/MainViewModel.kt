package com.elliot.kim.kotlin.dimcatcamnote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.room.Room
import kotlinx.coroutines.*

class MainViewModel(application: Application): AndroidViewModel(application) {

    private val database = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "dim_cat_notes"
    ).fallbackToDestructiveMigration().build()

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    var camNotes = getAll()

    var targetNote: Note? = null

    fun getAll(): LiveData<MutableList<Note>> = database.dao().getAll()

    fun insert(note: Note) {
        scope.launch {
            database.dao().insert(note)
        }
    }

    fun update(note: Note) {
        scope.launch {
            targetNote = note
            database.dao().update(note)
        }
    }

    fun delete(note: Note) {
        scope.launch {
            targetNote = note
            database.dao().delete(note)
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}