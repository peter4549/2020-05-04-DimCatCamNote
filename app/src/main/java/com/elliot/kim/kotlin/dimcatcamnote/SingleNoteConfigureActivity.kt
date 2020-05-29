package com.elliot.kim.kotlin.dimcatcamnote

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.elliot.kim.kotlin.dimcatcamnote.databinding.ActivitySingleNoteConfigureBinding

const val APP_WIDGET_PREFERENCES = "app_widget_preferences"

class SingleNoteConfigureActivity : AppCompatActivity() {

    lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivitySingleNoteConfigureBinding
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_single_note_configure)

        val viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        viewModel.getAll().observe(this, androidx.lifecycle.Observer { notes ->
            binding.recyclerView.apply {
                setHasFixedSize(true)
                adapter = NoteAdapter(this@SingleNoteConfigureActivity, notes,
                    true, appWidgetId)
                layoutManager = LinearLayoutManager(context)
            }
        })

        val configureIntent = intent
        val extras: Bundle? = configureIntent.extras

        if (extras != null)
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)

        val resultIntent = Intent()
        resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_CANCELED, resultIntent)

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) finish()


    }

    /*
    fun confirmConfiguration(v: View) {
        val appWidgetManager = AppWidgetManager.getInstance(this)

        //Intent 에딧 프래그먼트 액티비티로 바꾸고 넣을 것.
        // pending intent 등 사용

        val views = RemoteViews(this.packageName, R.layout.widget)
        //views.setOnClickPendingIntent()
        views.setCharSequence(R.id.text_view_title, "setText", "JJJJ")

        appWidgetManager.updateAppWidget(appWidgetId, views)

        val preferences = getSharedPreferences(APP_WIDGET_PREFERENCES, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(KEY_NOTE_TITLE + appWidgetId, "DDDD")
        editor.apply()

        val resultIntent = Intent()
        resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultIntent)
        finish()

    }

     */

}