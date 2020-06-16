package com.elliot.kim.kotlin.dimcatcamnote.adapters

import android.annotation.SuppressLint
import android.content.Context

import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.elliot.kim.kotlin.dimcatcamnote.DEFAULT_FOLDER_NAME
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.data.Folder
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.databinding.CardViewNavigationDrawerBinding
import com.elliot.kim.kotlin.dimcatcamnote.databinding.CardViewNavigationDrawerBinding.bind
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.ConfirmPasswordDialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.SetPasswordDialogFragment
import java.util.*

class FolderAdapter(private val context: Context?):
    RecyclerView.Adapter<FolderAdapter.ViewHolder>() {
    private val tag = "FolderAdapter"

    var folders: MutableList<Folder>
    var selectedFolder: Folder? = null
    var lastId = 0

    init {
        folders = loadFolders()
        if (folders.isNotEmpty()) lastId = folders.last().id
    }

    enum class MenuItemId(val id: Int) {
        OPEN(1000),
        LOCK(1001),
        REMOVE(1002)
    }

    inner class ViewHolder(context: Context?, v: View)
        : RecyclerView.ViewHolder(v), View.OnCreateContextMenuListener {
        var binding: CardViewNavigationDrawerBinding = bind(v)

        init { binding.cardView.setOnCreateContextMenuListener(this) }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            if (menu != null) {
                menu.add(Menu.NONE, MenuItemId.OPEN.id, 1, "폴더 열기")
                    .setOnMenuItemClickListener(menuItemClickListener)

                val menuItemTitle = if (folders[adapterPosition].isLocked) "잠금 해제"
                else "폴더 잠금"
                menu.add(Menu.NONE, MenuItemId.LOCK.id, 2, menuItemTitle)
                    .setOnMenuItemClickListener(menuItemClickListener)

                if (!folders[adapterPosition].isLocked &&
                    folders[adapterPosition].id != DEFAULT_FOLDER_ID)
                    menu.add(Menu.NONE, MenuItemId.REMOVE.id, 3, "폴더 제거")
                        .setOnMenuItemClickListener(menuItemClickListener)
            }
        }

        private val menuItemClickListener = MenuItem.OnMenuItemClickListener {
            val folder = folders[adapterPosition]
            when (it.itemId) {
                MenuItemId.OPEN.id -> {
                    if (folder.isLocked)
                        confirmPassword()
                    else (context as MainActivity).showCurrentFolderItems(folder)
                }
                MenuItemId.LOCK.id -> {
                    if (folder.isLocked) unlock()
                    else lock()
                }
                MenuItemId.REMOVE.id -> {
                    removeFolder(folder)
                    notifyItemRemoved(adapterPosition)
                }
            }

            true
        }
    }

    private fun confirmPassword() {
        ConfirmPasswordDialogFragment(this)
            .show((context as MainActivity).fragmentManager, tag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(
            R.layout.card_view_navigation_drawer,
            parent, false)
        return ViewHolder(context, v)
    }

    override fun getItemCount(): Int {
        return if(folders.isNullOrEmpty()) 0 else folders.size
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.folderCardViewContainer.setBackgroundColor(MainActivity.backgroundColor)

        val folder: Folder = folders[position]

        holder.binding.textView.text = folder.name

        if (folder.isLocked) {
            holder.binding.imageViewThumbnail.visibility = View.VISIBLE
            holder.binding.imageViewThumbnail
                .setImageDrawable((context as MainActivity).getDrawable(R.drawable.ic_lock_outline_black_24dp))
        } else holder.binding.imageViewThumbnail.visibility = View.GONE


        holder.binding.cardView.setOnTouchListener { _: View, _: MotionEvent ->
            selectedFolder = folder
            false
        }

        holder.binding.cardView.setOnClickListener {
            if (folder.isLocked) confirmPassword()
            else {
                (context as MainActivity).showCurrentFolderItems(folder)
                context.onBackPressed()
            }
        }

        holder.binding.cardView.setOnLongClickListener { false }
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

        var count = 0
        var id = 0
        var name = ""
        var isLocked = false
        var password = ""

        Arrays.sort(keySet)

        // Add default folder.
        folders.add(
            Folder(
                DEFAULT_FOLDER_ID,
                DEFAULT_FOLDER_NAME
            )
        )

        for (i in 0 until entriesSize) {
            val key = keySet[i].toString()

            when (count) {
                0 -> id = preferences.getInt(key, -1)
                1 -> name = preferences.getString(key, "")!!
                2 -> isLocked = preferences.getBoolean(key, false)
                3 -> password = preferences.getString(key, "")!!
            }

            if (++count >= 4) {
                count = 0
                val folder =
                    Folder(id, name)
                folder.isLocked = isLocked
                if (folder.isLocked) folder.password = password
                folders.add(folder)
            }
        }

        return folders
    }

    private fun saveFolder(folder: Folder) {
        val preferences = (context as MainActivity).getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt("${folder.id}0", folder.id)
        editor.putString("${folder.id}1", folder.name)
        editor.putBoolean("${folder.id}2", folder.isLocked)
        editor.putString("${folder.id}3", folder.password)

        editor.apply()
    }

    fun addFolder(name: String): Boolean {
        return if (folders.add(
                Folder(
                    ++lastId,
                    name
                )
            )) {
            notifyItemInserted(folders.size - 1)
            saveFolder(folders.last())
            (context as MainActivity).showToast("폴더가 생성되었습니다.")
            true
        } else false
    }

    fun removeFolder(folder: Folder): Boolean {
        val preferences = (context as MainActivity).getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
        val editor = preferences.edit()
        for (i in 0..3)
            editor.remove("${folder.id}$i")
        editor.apply()

        return folders.remove(folder)
    }

    fun moveNoteToFolder(note: Note?, folder: Folder) {
        note!!.folderId = folder.id
        (context as MainActivity).viewModel.update(note)
    }

    fun getFolderById(id: Int): Folder = folders.filter { it.id == id }[0]
    fun getFolderByName(name: String): Folder = folders.filter { it.name == name }[0]

    companion object {
        const val PREFERENCES_NAME = "folder_preferences"
        const val DEFAULT_FOLDER_ID = 0
    }

    fun lock() {
        SetPasswordDialogFragment(this)
            .show((context as MainActivity).fragmentManager, tag)
    }

    fun unlock() {
        ConfirmPasswordDialogFragment(this, true)
            .show((context as MainActivity).fragmentManager, tag)
    }

    fun update(folder: Folder) {
        saveFolder(folder)
        notifyItemChanged(getPositionByFolder(folder))
    }

    fun isLockedFolder(id: Int) = folders.filter { it.id == id }[0].isLocked

    private fun getPositionByFolder(folder: Folder): Int = folders.indexOf(folder)
}