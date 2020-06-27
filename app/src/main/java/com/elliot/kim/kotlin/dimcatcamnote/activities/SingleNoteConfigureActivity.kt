package com.elliot.kim.kotlin.dimcatcamnote.activities

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.adapters.NoteAdapter
import com.elliot.kim.kotlin.dimcatcamnote.databinding.ActivityAppWidgetConfigureBinding
import com.elliot.kim.kotlin.dimcatcamnote.fragments.CameraViewFragment
import com.elliot.kim.kotlin.dimcatcamnote.fragments.WriteFragment
import com.elliot.kim.kotlin.dimcatcamnote.view_model.MainViewModel
import java.io.File

class SingleNoteConfigureActivity : AppCompatActivity() {

    lateinit var cameraFragment: CameraViewFragment
    lateinit var writeFragment: WriteFragment

    lateinit var fragmentManager: FragmentManager
    lateinit var noteAdapter: NoteAdapter
    lateinit var viewModel: MainViewModel

    private lateinit var binding: ActivityAppWidgetConfigureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_app_widget_configure
        )

        binding.frameLayout.setOnClickListener {
            finish()
        }

        cameraFragment = CameraViewFragment()
        writeFragment = WriteFragment()

        fragmentManager = supportFragmentManager

        val configureIntent = intent
        val extras: Bundle? = configureIntent.extras
        val action = intent.action
        var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

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
            noteAdapter = NoteAdapter(
                this@SingleNoteConfigureActivity, notes,
                true, appWidgetId
            )

            binding.recyclerView.apply {
                setHasFixedSize(true)
                adapter = noteAdapter
                // Replaced from LinearLayoutManager to LinearLayoutManagerWrapper
                layoutManager = LayoutManagerWrapper(context, 1)
            }

            noteAdapter.sort(SortingCriteria.EDIT_TIME.index)
        })

        binding.addNoteContainer.setOnClickListener {
            startFragment(writeFragment)
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
        else
            ResourcesCompat.getFont(this, fontId)
    }

    private fun startFragment(fragment: Fragment) {
        fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.anim_slide_in_left_enter,
                R.anim.anim_slide_in_left_exit,
                R.anim.anim_slide_out_right_enter,
                R.anim.anim_slide_out_right_exit
            )
            .addToBackStack(null)
            .replace(R.id.frame_layout, fragment).commit()
    }

    fun recyclerViewScrollToTop() {
        binding.recyclerView.smoothScrollToPosition(0)
    }

    fun setCurrentFragment(fragment: CurrentFragment?) {
        MainActivity.currentFragment = fragment
    }

    fun deleteFileFromUri(stringUri: String?): Boolean {
        if (stringUri == null) return false

        val uri = Uri.parse(stringUri)
        val file = File(uri.path!!)
        if (file.delete()) {
            refreshGallery(file)
            return true
        }
        else {
            if (file.canonicalFile.delete()) {
                refreshGallery(file)
                return true
            }
            else applicationContext.deleteFile(file.name)
            return if (file.exists()) false
            else {
                refreshGallery(file)
                true
            }
        }
    }

    fun deleteFileFromUri(uri: Uri?): Boolean {
        if (uri == null) return false

        val file = File(uri.path!!)
        if (file.delete()) {
            refreshGallery(file)
            return true
        }
        else {
            if (file.canonicalFile.delete()) {
                refreshGallery(file)
                return true
            }
            else applicationContext.deleteFile(file.name)
            return if (file.exists()) false
            else {
                refreshGallery(file)
                true
            }
        }
    }

    private fun refreshGallery(file: File) {
        MediaScannerConnection.scanFile(this, arrayOf(file.toString()), null
        ) { path, uri ->
            Log.i("ExternalStorage", "Scanned $path:")
            Log.i("ExternalStorage", "uri=$uri");}
    }

    override fun onBackPressed() {
        if (MainActivity.currentFragment == null)
            finish()
        else {
            when (MainActivity.currentFragment) {
                CurrentFragment.ALARM_FRAGMENT -> super.onBackPressed()
                CurrentFragment.CALENDAR_FRAGMENT -> super.onBackPressed()
                CurrentFragment.CAMERA_FRAGMENT -> super.onBackPressed()
                CurrentFragment.CONFIGURE_FRAGMENT -> super.onBackPressed()
                CurrentFragment.EDIT_FRAGMENT -> super.onBackPressed()
                CurrentFragment.PHOTO_FRAGMENT -> super.onBackPressed()
                CurrentFragment.WRITE_FRAGMENT -> writeFragment.finish(WriteFragment.BACK_PRESSED)
            }
        }
    }

    fun backPressed() {
        super.onBackPressed()
    }

    companion object {
        var noteColor = 0
        var font: Typeface? = null
        var fontId = 0
    }
}