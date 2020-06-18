package com.elliot.kim.kotlin.dimcatcamnote.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.adjustSpinnerItemTextSize
import com.elliot.kim.kotlin.dimcatcamnote.data.Folder

class FolderSpinnerAdapter(private val activity: MainActivity,
                           private val folders: MutableList<Folder>): BaseAdapter() {
    private lateinit var inflater: LayoutInflater

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val itemView = inflater.inflate(R.layout.spinner_item_view, null)
        val textView: TextView = itemView.findViewById(R.id.text_view_spinner)

        textView.text = folders[position].name
        textView.adjustSpinnerItemTextSize(MainActivity.fontId)
        textView.typeface = MainActivity.font

        return itemView
    }

    // Not used.
    override fun getItem(position: Int): Any {
        return 0
    }

    // Not used.
    override fun getItemId(position: Int): Long {
        return 0L
    }

    override fun getCount(): Int = folders.count()
}