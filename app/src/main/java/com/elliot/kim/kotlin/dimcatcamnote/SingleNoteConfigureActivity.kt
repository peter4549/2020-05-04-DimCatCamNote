package com.elliot.kim.kotlin.dimcatcamnote

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.elliot.kim.kotlin.dimcatcamnote.databinding.ActivitySingleNoteConfigureBinding

class SingleNoteConfigureActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingleNoteConfigureBinding
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_single_note_configure)

        val configureIntent = intent
        val extras: Bundle? = configureIntent.extras

        if (extras != null)
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) finish()

        val resultIntent = Intent()
        resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_CANCELED, resultIntent)

        val database: AppDatabase = Room.databaseBuilder(
            application, AppDatabase::class.java,
            MainActivity.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
        val dao = database.dao()

        val notes = dao.getAll().value!! // 받지를 못하는디?? 시바 그냥 쉐어드로??

        binding.recyclerView.apply {
            setHasFixedSize(true)
            adapter = NoteAdapter(this@SingleNoteConfigureActivity, notes)
            layoutManager = LinearLayoutManager(context)
        }

        /*
        val note = dao.findNoteById(id)
        note.alarmTime = null
        dao.update(note)

         */

    }

    fun confirmConfiguration(v: View) {
        val appWidgetManager = AppWidgetManager.getInstance(this)

        //Intent 에딧 프래그먼트 액티비티로 바꾸고 넣을 것.
        // pending intent 등 사용

        val views = RemoteViews(this.packageName, R.layout.widget)
        //views.setOnClickPendingIntent()
        views.setCharSequence(R.id.text_view_title, "setText", "JJJJ")

        val resultIntent = Intent()
        resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultIntent)
        finish()

    }

}