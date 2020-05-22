package com.elliot.kim.kotlin.dimcatcamnote

import android.content.Context
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.elliot.kim.kotlin.dimcatcamnote.databinding.CardViewNavigationDrawerBinding
import com.elliot.kim.kotlin.dimcatcamnote.databinding.CardViewNavigationDrawerBinding.bind

class FolderAdapter(private val context: Context?, private val folderManager: FolderManager):
    RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    private val folders = folderManager.folders

    enum class MenuItemId(val id: Int) {
        OPEN(1000),
        LOCK(1001),
        REMOVE(1002)
    }

    class ViewHolder(context: Context?, v: View, folderManager: FolderManager, folderAdapter: FolderAdapter)
        : RecyclerView.ViewHolder(v), View.OnCreateContextMenuListener {
        val binding: CardViewNavigationDrawerBinding = bind(v)

        init { binding.cardView.setOnCreateContextMenuListener(this) }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            if (menu != null) {
                menu.add(Menu.NONE, MenuItemId.OPEN.id, 1, "폴더 열기")
                    .setOnMenuItemClickListener(menuItemClickListener)
                menu.add(Menu.NONE, MenuItemId.LOCK.id, 2, "폴더 잠금")
                    .setOnMenuItemClickListener(menuItemClickListener)
                menu.add(Menu.NONE, MenuItemId.REMOVE.id, 3, "폴더 제거")
                    .setOnMenuItemClickListener(menuItemClickListener)
            }
        }

        private val menuItemClickListener = MenuItem.OnMenuItemClickListener {
            val folder = folderManager.folders[adapterPosition]
            when (it.itemId) {
                MenuItemId.OPEN.id -> { (context as MainActivity)._setCurrentFolderName(folder.name) }
                MenuItemId.LOCK.id -> {}
                MenuItemId.REMOVE.id -> {
                    folderManager.removeFolder(folder)
                    folderAdapter.notifyItemRemoved(adapterPosition)
                }
            }

            true
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view_navigation_drawer,
            parent, false)
        return ViewHolder(context, v, folderManager, this)
    }

    override fun getItemCount(): Int {
        return if(folders.isNullOrEmpty()) 0 else folders.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder: Folder = folders[position]
        val name = folder.name

        holder.binding.textView.text = name
        holder.binding.cardView.setOnClickListener {
            (context as MainActivity).showToast("HELLO")
        }
        holder.binding.cardView.setOnLongClickListener { false }
    }
}