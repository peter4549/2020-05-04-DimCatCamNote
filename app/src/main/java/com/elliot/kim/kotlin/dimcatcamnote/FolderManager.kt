package com.elliot.kim.kotlin.dimcatcamnote

import android.content.Context
import android.util.Log
import java.util.*

class FolderManager(private val context: Context) {

    var folders: MutableList<Folder>
    var lastId = 0
    var isAdded = false

    init {
        folders = loadFolders()
        if (folders.isNotEmpty()) lastId = folders.last().id
        else {
            for (folder in folders) Log.d("HERE", folder.name)
        }
    }

    fun addFolder(name: String): Boolean {
        return if (folders.add(Folder(++lastId, name))) {
            (context as MainActivity).showToast("폴더가 생성되었습니다.")
            saveFolder(folders.last())
            true
        } else false
    }

    fun removeFolder(folder: Folder): Boolean {
        val preferences = (context as MainActivity).getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
        val editor = preferences.edit()
        editor.remove("${folder.id}")
        editor.apply()

        return folders.remove(folder)
    }

    private fun loadFolders(): MutableList<Folder> {
        val folders = mutableListOf<Folder>()
        val preferences = (context as MainActivity).getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE)

        val entries = preferences.all
        val entriesSize = entries.size
        val keySet =
            Arrays.stream(entries.keys.toTypedArray())
                .mapToInt { s: String -> s.toInt() }.toArray()

        Arrays.sort(keySet)

        for (i in 0 until entriesSize) {
            val key = keySet[i]
            folders.add(Folder(key, preferences.getString(key.toString(), "")!!))
        }

        return folders
    }

    private fun saveFolder(folder: Folder) {
        val preferences = (context as MainActivity).getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("${folder.id}", folder.name)
        editor.apply()
    }

    fun moveNoteToFolder(note: Note, folder: Folder) {
        note.folderId = folder.id
        (context as MainActivity).viewModel.update(note)
    }

    companion object {
        const val PREFERENCES_NAME = "folder_preferences"
    }
}