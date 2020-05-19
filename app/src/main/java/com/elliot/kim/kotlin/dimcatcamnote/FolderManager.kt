package com.elliot.kim.kotlin.dimcatcamnote

import android.content.Context
import android.content.DialogInterface
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import java.util.*

class FolderManager(private val context: Context) {

    var folders: MutableList<Pair<Int, String>>
    var lastId = 0
    var isAdded = false

    init {
        folders = loadFolders()
        if (folders.isNotEmpty()) lastId = folders.last().first
    }

    fun addFolder(name: String): Boolean {
        return if (folders.add(Pair(++lastId, name))) {
            (context as MainActivity).showToast("폴더가 생성되었습니다.")
            true
        } else false
    }

    fun removeFolder(id: Int, name: String): Boolean {
        return folders.remove(Pair(id, name))
    }

    private fun loadFolders(): MutableList<Pair<Int, String>> {
        val folders = mutableListOf<Pair<Int, String>>()
        val preferences = (context as MainActivity).getSharedPreferences(
            "folders_preferences",
            Context.MODE_PRIVATE)

        val entries = preferences.all
        val entriesSize = entries.size
        val keySet =
            Arrays.stream(entries.keys.toTypedArray())
                .mapToInt { s: String -> s.toInt() }.toArray()

        Arrays.sort(keySet)

        for (i in 0 until entriesSize) {
            val key = keySet[i]
            folders.add(Pair(key, preferences.getString(key.toString(), "")!!))
        }

        return folders
    }

    fun saveFolders() {
        val preferences = (context as MainActivity).getSharedPreferences(
            "folders_preferences",
            Context.MODE_PRIVATE)
        val editor = preferences.edit()

        for (folder in folders)
            editor.putString("${folder.first}", folder.second)

        editor.apply()
    }
}