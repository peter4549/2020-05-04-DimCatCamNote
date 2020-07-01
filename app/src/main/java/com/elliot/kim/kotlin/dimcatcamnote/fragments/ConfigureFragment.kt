package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.RemoteViews
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.EditActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.SingleNoteConfigureActivity
import com.elliot.kim.kotlin.dimcatcamnote.databinding.FragmentConfigureBinding
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.DialogFragments


class ConfigureFragment : Fragment(), VersionAndLicenseFragment.OnFragmentFinished {

    private lateinit var binding: FragmentConfigureBinding
    private var hexStringOpacity = "80"  // 50%
    private var seekBarProgress = DEFAULT_SEEK_BAR_PROGRESS
    lateinit var progressDialogHandler: Handler
    private var startLicenseFragment = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_configure, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentConfigureBinding.bind(view)

        (activity as MainActivity).setSupportActionBar(binding.toolbar)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        binding.toolbar.setTitleTextAppearance(requireContext(), MainActivity.fontStyleId)
        binding.toolbar.title = "환경설정"
        binding.toolbar.setBackgroundColor(MainActivity.toolbarColor)

        initOpacitySeekBar()

        binding.textViewSetThemeColor.setOnClickListener {
            (activity as MainActivity).showDialogFragment(DialogFragments.SET_THEME_COLOR, binding.toolbar)
        }

        binding.textViewSetNoteColor.setOnClickListener {
            (activity as MainActivity).showDialogFragment(DialogFragments.SET_NOTE_COLOR)
        }

        binding.textViewSetFont.setOnClickListener {
            (activity as MainActivity).showDialogFragment(DialogFragments.SET_FONT, binding.toolbar)
        }

        binding.textViewSetFontColor.setOnClickListener {
            (activity as MainActivity).showDialogFragment(DialogFragments.SET_FONT_COLOR)
        }

        binding.textViewClear.setOnClickListener {
            (activity as MainActivity).showDialogFragment(DialogFragments.CONFIRM_CLEAR)
        }

        binding.textViewSetInlayColor.setOnClickListener {
            (activity as MainActivity).showDialogFragment(DialogFragments.SET_INLAY_COLOR)
        }

        binding.textViewSetAppWidgetColor.setOnClickListener {
            (activity as MainActivity).showDialogFragment(DialogFragments.SET_APP_WIDGET_COLOR)
        }

        binding.textViewVersionLicense.setOnClickListener {
            startLicenseFragment = true
            val versionAndLicenseFragment = VersionAndLicenseFragment()
            versionAndLicenseFragment.setConfigureFragment(this)
            (activity as MainActivity).startFragment(versionAndLicenseFragment, R.id.fragment_configure_container)
        }

        @Suppress("DEPRECATION")
        binding.seekBar.thumb.setColorFilter(requireContext().getColor(R.color.colorStatusIcon), PorterDuff.Mode.SRC_ATOP)
        binding.seekBar.progressTintList = ColorStateList.valueOf(requireContext().getColor(R.color.colorStatusIcon))
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                var temp = progress
                if (temp < 10)
                    temp = 10
                val text = "${temp}%"
                binding.textViewOpacity.text = text
                seekBarProgress = temp
                hexStringOpacity = percentageToHexString(temp)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val preferences =
                    requireContext().getSharedPreferences(PREFERENCES_OPACITY, Context.MODE_PRIVATE)
                val editor = preferences.edit()
                editor.putInt(KEY_OPACITY_SEEK_BAR_PROGRESS, seekBarProgress)
                editor.apply()
            }

        })

        progressDialogHandler = Handler {
            when(it.what) {
                START_PROGRESS_DIALOG -> {
                    binding.progressContainer.visibility = View.VISIBLE
                }
                STOP_PROGRESS_DIALOG -> {
                    binding.progressContainer.visibility = View.GONE
                }
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        MainActivity.currentFragment = CurrentFragment.CONFIGURE_FRAGMENT
    }

    override fun onStop() {
        super.onStop()

        // Apply app widget opacity.
        applyAppWidgetDesignSettings()

        val preferences = requireContext()
            .getSharedPreferences(PREFERENCES_OPACITY, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(KEY_OPACITY, hexStringOpacity)
        editor.apply()

        MainActivity.currentFragment = null
        if (!startLicenseFragment)
            (requireActivity() as MainActivity).showFloatingActionButton()
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {

        val animation = AnimationUtils.loadAnimation(activity, nextAnim)

        animation!!.setAnimationListener( object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                (activity as MainActivity).closeDrawer()
            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })

        return animation
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> (activity as MainActivity).onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    private fun initOpacitySeekBar() {
        val preferences = requireContext()
            .getSharedPreferences(PREFERENCES_OPACITY, Context.MODE_PRIVATE)
        seekBarProgress = preferences.getInt(KEY_OPACITY_SEEK_BAR_PROGRESS, DEFAULT_SEEK_BAR_PROGRESS)
        binding.seekBar.progress = seekBarProgress
        binding.textViewOpacity.text = "$seekBarProgress%"
    }

    private fun percentageToHexString(percentage: Int): String {
        val hex = (percentage * 2.55).toInt()
        return Integer.toHexString(hex)
    }

    private fun applyAppWidgetDesignSettings() {
        val preferences =
            requireContext().getSharedPreferences(APP_WIDGET_PREFERENCES, Context.MODE_PRIVATE)
        val noAttachmentMessage = requireActivity().getString(R.string.no_attachment_message)

        val argbChannelTitleColor =
            String.format("#${hexStringOpacity}%06X", 0xFFFFFF and MainActivity.appWidgetTitleColor)
        val argbChannelBackgroundColor =
            String.format("#${hexStringOpacity}%06X", 0xFFFFFF and MainActivity.appWidgetBackgroundColor)

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds: IntArray =
            appWidgetManager.getAppWidgetIds(ComponentName(requireContext(), NoteAppWidgetProvider::class.java))
        appWidgetIds.forEach { appWidgetId ->
            val noteExist = preferences.getBoolean(KEY_APP_WIDGET_NOTE_EXIST + appWidgetId, false)
            if (noteExist) {
                // Run when a note is attached.
                val noteId =
                    preferences.getInt(
                        KEY_APP_WIDGET_NOTE_ID + appWidgetId,
                        DEFAULT_VALUE_NOTE_ID
                    )
                val title = preferences.getString(
                    KEY_APP_WIDGET_NOTE_TITLE + appWidgetId, noAttachmentMessage
                )

                /*
                val content = preferences.getString(
                    KEY_APP_WIDGET_NOTE_CONTENT + appWidgetId, noAttachmentMessage
                )
                 */

                // val uri = preferences.getString(KEY_APP_WIDGET_NOTE_URI + appWidgetId, "")
                val creationTime =
                    preferences.getLong(KEY_APP_WIDGET_NOTE_CREATION_TIME + appWidgetId, 0L)
                val alarmTime =
                    preferences.getLong(KEY_APP_WIDGET_NOTE_ALARM_TIME + appWidgetId, 0L)
                val editTime =
                    preferences.getLong(KEY_APP_WIDGET_NOTE_EDIT_TIME + appWidgetId, 0L)
                val isDone =
                    preferences.getBoolean(KEY_APP_WIDGET_NOTE_IS_DONE + appWidgetId, false)
                val isLocked =
                    preferences.getBoolean(KEY_APP_WIDGET_NOTE_IS_LOCKED + appWidgetId, false)

                /*
                val password =
                    preferences.getString(KEY_APP_WIDGET_NOTE_PASSWORD + appWidgetId, "")
                 */

                // Create an Intent to launch EditActivity.
                val intent = Intent(context, EditActivity::class.java)
                intent.action = ACTION_APP_WIDGET_ATTACHED + noteId
                val pendingIntent: PendingIntent = intent.let {
                    PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_UPDATE_CURRENT)
                }

                val views: RemoteViews = RemoteViews(
                    requireContext().packageName,
                    R.layout.app_widget
                ).apply {
                    setInt(R.id.title_container, "setBackgroundColor",
                        Color.parseColor(argbChannelTitleColor))
                    setInt(R.id.text_view_content, "setBackgroundColor",
                        Color.parseColor(argbChannelBackgroundColor))

                    setOnClickPendingIntent(R.id.text_view_content, pendingIntent)
                    setCharSequence(R.id.text_view_title, "setText", title)

                    if (isDone) setViewVisibility(R.id.image_view_done, View.VISIBLE)
                    else setViewVisibility(R.id.image_view_done, View.GONE)

                    if (isLocked) setViewVisibility(R.id.image_view_lock, View.VISIBLE)
                    else setViewVisibility(R.id.image_view_lock, View.GONE)

                    if (alarmTime == 0L) {
                        setViewVisibility(R.id.image_view_alarm, View.GONE)
                        setViewVisibility(R.id.text_view_alarm_time, View.GONE)
                    } else {
                        setViewVisibility(R.id.image_view_alarm, View.VISIBLE)
                        setViewVisibility(R.id.text_view_alarm_time, View.VISIBLE)
                        setCharSequence(
                            R.id.text_view_alarm_time, "setText",
                            " " + MainActivity.longTimeToString(alarmTime, PATTERN_UP_TO_MINUTES)
                        )
                    }

                    if (editTime == 0L) {
                        setViewVisibility(R.id.text_view_creation_time, View.VISIBLE)
                        setViewVisibility(R.id.text_view_edit_time, View.GONE)
                        setCharSequence(
                            R.id.text_view_creation_time, "setText",
                            " " + MainActivity.longTimeToString(creationTime, PATTERN_UP_TO_MINUTES)
                        )
                    } else {
                        setViewVisibility(R.id.text_view_edit_time, View.VISIBLE)
                        setViewVisibility(R.id.text_view_creation_time, View.GONE)
                        setCharSequence(
                            R.id.text_view_edit_time, "setText",
                            " " + MainActivity.longTimeToString(editTime, PATTERN_UP_TO_MINUTES)
                        )
                    }

                    setInt(R.id.text_view_alarm_time, "setBackgroundColor",
                        Color.parseColor(argbChannelBackgroundColor))
                    setInt(R.id.text_view_creation_time, "setBackgroundColor",
                        Color.parseColor(argbChannelBackgroundColor))
                    setInt(R.id.text_view_edit_time, "setBackgroundColor",
                        Color.parseColor(argbChannelBackgroundColor))
                }

                // Notify appWidgetManager of app widget updates.
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } else {
                val intent = Intent(context, SingleNoteConfigureActivity::class.java)
                // App widget ID is sent to SingleNoteConfigureActivity through the action.
                intent.action = ACTION_APP_WIDGET_ATTACHED + appWidgetId

                val pendingIntent = intent.let {
                    PendingIntent.getActivity(context, 0, intent, 0)
                }

                val views: RemoteViews = RemoteViews(
                    requireActivity().packageName,
                    R.layout.app_widget
                ).apply {
                    setViewVisibility(R.id.image_view_done, View.GONE)
                    setViewVisibility(R.id.image_view_lock, View.GONE)
                    setViewVisibility(R.id.image_view_alarm, View.GONE)
                    setViewVisibility(R.id.text_view_creation_time, View.GONE)
                    setViewVisibility(R.id.text_view_edit_time, View.GONE)
                    setViewVisibility(R.id.text_view_alarm_time, View.GONE)
                    setViewVisibility(R.id.image_view_photo, View.GONE)
                    setInt(R.id.title_container, "setBackgroundColor", Color.parseColor(argbChannelTitleColor))
                    setInt(R.id.text_view_content, "setBackgroundColor", Color.parseColor(argbChannelBackgroundColor))
                    setOnClickPendingIntent(R.id.text_view_content, pendingIntent)
                    setOnClickPendingIntent(R.id.image_button_change, pendingIntent)
                    setCharSequence(R.id.text_view_title, "setText",
                        requireContext().getString(R.string.no_attachment_message))
                    setCharSequence(R.id.text_view_content, "setText",
                        requireContext().getString(R.string.no_attachment_message))
                }

                // Notify appWidgetManager of app widget updates.
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    companion object {
        const val STOP_PROGRESS_DIALOG = 0
        const val START_PROGRESS_DIALOG = 1
    }

    override fun setCurrentFragment() {
        MainActivity.currentFragment = CurrentFragment.CONFIGURE_FRAGMENT
        startLicenseFragment = false
    }
}



