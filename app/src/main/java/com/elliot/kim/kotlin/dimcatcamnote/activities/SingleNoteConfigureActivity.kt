package com.elliot.kim.kotlin.dimcatcamnote.activities

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.databinding.ActivitySingleNoteConfigureBinding
import com.elliot.kim.kotlin.dimcatcamnote.view_model.MainViewModel

const val APP_WIDGET_PREFERENCES = "app_widget_preferences"

class SingleNoteConfigureActivity : AppCompatActivity() {

    lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivitySingleNoteConfigureBinding
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_single_note_configure
        )

        val configureIntent = intent
        val extras: Bundle? = configureIntent.extras

        if (extras != null)
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)

        val viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        viewModel.setContext(this)
        viewModel.getAll().observe(this, androidx.lifecycle.Observer { notes ->
            binding.recyclerView.apply {
                setHasFixedSize(true)
                adapter = NoteAdapter(
                    this@SingleNoteConfigureActivity, notes,
                    true, appWidgetId
                )
                layoutManager = LinearLayoutManager(context)
            }
        })

        val resultIntent = Intent()
        resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_CANCELED, resultIntent)

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) finish()
    }
}