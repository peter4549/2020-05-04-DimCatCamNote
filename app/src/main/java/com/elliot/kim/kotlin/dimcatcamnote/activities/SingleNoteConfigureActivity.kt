package com.elliot.kim.kotlin.dimcatcamnote.activities

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.adapters.NoteAdapter
import com.elliot.kim.kotlin.dimcatcamnote.databinding.ActivitySingleNoteConfigureBinding
import com.elliot.kim.kotlin.dimcatcamnote.view_model.MainViewModel

class SingleNoteConfigureActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingleNoteConfigureBinding
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_single_note_configure
        )

        binding.container.setOnClickListener {
            finish()
        }

        val configureIntent = intent
        val extras: Bundle? = configureIntent.extras

        var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
        val action = intent.action

        if (action!!.startsWith(ACTION_APP_WIDGET_ATTACHED))
            appWidgetId = action.substring(ACTION_APP_WIDGET_ATTACHED.length).toInt()
        else if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        val resultIntent = Intent()
        resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_CANCELED, resultIntent)

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) finish()

        initDesignOptions()

        val viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        viewModel.setContext(this)
        viewModel.getAll().observe(this, androidx.lifecycle.Observer { notes ->
            binding.recyclerView.apply {
                setHasFixedSize(true)
                adapter =
                    NoteAdapter(
                        this@SingleNoteConfigureActivity, notes,
                        true, appWidgetId
                    )
                // Replaced from LinearLayoutManager to LinearLayoutManagerWrapper
                layoutManager = LinearLayoutManagerWrapper(context)
            }
        })

        binding.addNoteContainer.setOnClickListener {
            startWriteFragment()
        }
    }

    private fun initDesignOptions() {
        val colorPreferences =
            getSharedPreferences(PREFERENCES_SET_COLOR, Context.MODE_PRIVATE)
        val fontPreferences =
            getSharedPreferences(PREFERENCES_FONT, Context.MODE_PRIVATE)

        noteColor = colorPreferences.getInt(KEY_COLOR_NOTE, getColor(R.color.defaultColorNote))
        fontId = fontPreferences.getInt(KEY_FONT_ID, R.font.nanum_gothic_font_family)
        font = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            resources.getFont(fontId)
        else ResourcesCompat.getFont(this, fontId)
    }

    private fun startWriteFragment() {
        // 여 부터.
    }

    companion object {
        var noteColor = 0
        var font: Typeface? = null
        var fontId = 0
    }
}