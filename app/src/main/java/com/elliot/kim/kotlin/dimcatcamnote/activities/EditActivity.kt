package com.elliot.kim.kotlin.dimcatcamnote.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.forEachIndexed
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.adapters.NoteAdapter
import com.elliot.kim.kotlin.dimcatcamnote.broadcast_receivers.AlarmReceiver
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.databinding.ActivityEditBinding
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.AddToCalendarDialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.ConfirmPasswordDialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.MoreOptionsDialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.SetPasswordDialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.fragments.AlarmFragment
import com.elliot.kim.kotlin.dimcatcamnote.fragments.PhotoFragment
import com.elliot.kim.kotlin.dimcatcamnote.view_model.MainViewModel
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

// 만약 알림 설정된 노트의 EditActivity 가 실행 중에 알림이 울리고, 그 다음 EditActivity 의 수정을 완료하는 경우
// 알림이 재등록되며 캘린더 등록 시간으로 빠져버린다.
// 허나 그럴 일이 얼마나 있을지 모르니. 우선은 보류하였다.
// 방법은 리시버를 등록하는 것이 정답인듯.
// 여기서 종료 동작에 설정하면 더 복잡할 듯

class EditActivity: AppCompatActivity() {

    lateinit var fragmentManager: FragmentManager
    lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityEditBinding
    private lateinit var modeIcon: MenuItem
    private lateinit var note: Note
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var originContent: String
    private val alarmFragment = AlarmFragment(this)
    private val tag = "EditActivity"
    private var isEditMode = false
    private var initialized = false
    private var originAlarmTime: Long? = null
    private var dataLoadingComplete = false
    private var passwordConfirmationResult = false
    private var shortAnimationDuration = 0
    var currentFragment: CurrentFragment? = null

    private val receiver: BroadcastReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.action == ACTION_PASSWORD_CONFIRMED) {
                passwordConfirmationResult = intent.getBooleanExtra(KEY_PASSWORD_CONFIRMED, false)
                if(passwordConfirmationResult) {
                    crossFadeOutLockView()
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
            IntentFilter(ACTION_PASSWORD_CONFIRMED)
        )

        fragmentManager = supportFragmentManager
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit)
        textViewTime = binding.textViewTime
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initDesignOptions()
        applyDesign()

        val intent = intent!!  // Get intent
        var id = 0
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

        val action = intent.action
        if (action!!.startsWith(ACTION_APP_WIDGET_ATTACHED))
            id = action.substring(ACTION_APP_WIDGET_ATTACHED.length).toInt()
        else if (action.startsWith(ACTION_ALARM_NOTIFICATION_CLICKED))
            id = action.substring(ACTION_ALARM_NOTIFICATION_CLICKED.length).toInt()

        val viewModelFactory =
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        viewModel.setContext(this)

        viewModel.getAll().observe(this, androidx.lifecycle.Observer { notes ->
            if (!initialized) {
                noteAdapter =
                    NoteAdapter(
                        this,
                        notes
                    )
                note = noteAdapter.getNoteById(id)
                noteAdapter.selectedNote = note

                // Password confirmation
                if (note.isLocked) {
                        ConfirmPasswordDialogFragment(noteAdapter, this)
                            .show(fragmentManager, tag)
                } else {
                    crossFadeOutLockView()
                }

                binding.editTextContent.setText(note.content)
                originAlarmTime = note.alarmTime
                originContent = note.content
                setText(note)
                showImage()

                if (note.uri == null) {
                    binding.imageView.visibility = View.GONE
                } else {
                    binding.editTextContent.viewTreeObserver.addOnGlobalLayoutListener {
                        if (keyboardShown(binding.editTextContent.rootView) && isEditMode)
                            crossFadeImageView(false)
                        else if (binding.imageView.visibility != View.VISIBLE)
                                showImage()
                    }
                }

                dataLoadingComplete = true
                initialized = true
            }
        })

        binding.imageView.setOnClickListener {
            startPhotoFragment()
        }

        binding.editTextContent.setOnTouchListener(object : View.OnTouchListener {
            private val gestureDetector = GestureDetector(applicationContext,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        if (!isEditMode)
                            showToast("더블 탭하여 노트를 편집하세요.")

                        return super.onSingleTapUp(e)
                    }

                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        if (!isEditMode) {
                            binding.editTextContent.highlightColor = getColor(android.R.color.transparent)
                        }

                        return super.onDoubleTap(e)
                    }

                    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
                        if (e!!.action == MotionEvent.ACTION_UP && !isEditMode) {
                            getFocus()
                            val cursorPosition = binding.editTextContent.selectionEnd
                            binding.editTextContent.setSelection(cursorPosition, cursorPosition)
                            binding.editTextContent.highlightColor = getColor(R.color.colorAccent)
                        }

                        return super.onDoubleTapEvent(e)
                    }
                })

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                return gestureDetector.onTouchEvent(event)
            }
        })

        // Ad
        MobileAds.initialize(this)
        binding.adView.loadAd(AdRequest.Builder().build())
        val adListener = object : AdListener() {
            override fun onAdImpression() {
                super.onAdImpression()
            }

            override fun onAdLeftApplication() {
                super.onAdLeftApplication()
            }

            override fun onAdClicked() {
                super.onAdClicked()
            }

            override fun onAdFailedToLoad(p0: Int) {
                super.onAdFailedToLoad(p0)
            }

            override fun onAdClosed() {
                super.onAdClosed()
            }

            override fun onAdOpened() {
                super.onAdOpened()
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
            }
        }

        binding.adView.adListener = adListener
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    override fun onBackPressed() {
        if (currentFragment == null)
            finish(BACK_PRESSED)
        else
            super.onBackPressed()
    }

    private fun keyboardShown(rootView: View): Boolean {
        val softKeyboardHeight = 100
        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)

        val metrics = rootView.resources.displayMetrics
        val heightDiff: Int = rootView.bottom - rect.bottom

        return heightDiff > softKeyboardHeight * metrics.density
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)

        modeIcon = menu.findItem(R.id.menu_mode_icon)

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val menuDone = menu.findItem(R.id.menu_done)
        val menuAlarm = menu.findItem(R.id.menu_alarm)
        val menuChangeAlarm = menu.findItem(R.id.menu_change_alarm)
        val menuMoveToFolder = menu.findItem(R.id.menu_move_to_folder)
        val menuLock = menu.findItem(R.id.menu_lock)

        // EditActivity does not offer the menu below
        menuMoveToFolder.isVisible = false

        if (dataLoadingComplete) {
            binding.toolbar.title = note.title
            if (note.isDone) menuDone.title = "완료해제" else menuDone.title = "완료체크"

            if (note.alarmTime == null) {
                menuAlarm.title = "알림설정"
                menuChangeAlarm.isVisible = false
            } else {
                menuAlarm.title = "알림해제"
                menuChangeAlarm.isVisible = true
            }

            if (note.isLocked) menuLock.title = "잠금해제" else menuLock.title = "잠금설정"

            binding.toolbar.invalidate()

            // Apply font
            menu.forEachIndexed { index, _ ->
                // Except mode menu
                if (index != 0) {
                    val menuItem = menu.getItem(index)
                    val spanString = SpannableString(menuItem.title.toString())
                    spanString.setSpan(
                        TextAppearanceSpan(this, fontStyleId),
                        0,
                        spanString.length,
                        0
                    )

                    spanString.setSpan(
                        ForegroundColorSpan(Color.BLACK),
                        0,
                        spanString.length,
                        0
                    )

                    menuItem.title = (spanString)
                }
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                MainActivity.hideKeyboard(this, binding.editTextContent)
                finish()
            }
            R.id.menu_mode_icon -> {
                if (isEditMode) {
                    item.setIcon(R.drawable.ic_edit_white_24dp)

                    binding.editTextContent.clearFocus()
                    binding.editTextContent.isFocusable = false
                    binding.editTextContent.isFocusableInTouchMode = false

                    if (isContentChanged())
                        finishWithSaving()
                    else
                        Toast.makeText(this, "변경사항이 없습니다.", Toast.LENGTH_SHORT).show()

                    if (keyboardShown(binding.editTextContent.rootView))
                        MainActivity.hideKeyboard(this, binding.editTextContent)
                    // binding.adView.visibility = View.VISIBLE

                    isEditMode = !isEditMode
                } else
                    getFocus()
            }
            R.id.menu_alarm -> {
                if (note.alarmTime == null)
                    startAlarmFragment(note)
                else {
                    cancelAlarm(note)
                    // Not required for EditActivity
                    // EditFragment.setTimeText(note)
                }
            }
            R.id.menu_change_alarm -> startAlarmFragment(note)
            R.id.menu_share -> MainActivity.share(this, note)
            R.id.menu_done -> {
                note.isDone = !note.isDone
                viewModel.update(note)
            }
            R.id.menu_lock -> {
                if (note.isLocked) unlock()
                else lock()
            }
            R.id.menu_delete -> {
                closeOptionsMenu()
                viewModel.delete(note)

                showToast("노트가 삭제되었습니다.")
                finish()
            }
            R.id.menu_add_to_status_bar -> addToStatusBar(note)
            R.id.menu_add_to_calendar -> AddToCalendarDialogFragment(note)
                .show(fragmentManager, tag)

        }
        return super.onOptionsItemSelected(item)
    }

    private fun initDesignOptions() {
        val defaultToolbarColor = getColor(R.color.defaultColorToolbar)
        val defaultBackgroundColor = getColor(R.color.defaultColorBackground)
        val defaultInlayColor = getColor(R.color.defaultColorInlay)

        val colorPreferences = this.getSharedPreferences(
            PREFERENCES_SET_COLOR,
            Context.MODE_PRIVATE
        )

        toolbarColor = colorPreferences.getInt(KEY_COLOR_TOOLBAR, defaultToolbarColor)
        backgroundColor = colorPreferences.getInt(KEY_COLOR_BACKGROUND, defaultBackgroundColor)
        inlayColor = colorPreferences.getInt(KEY_COLOR_INLAY, defaultInlayColor)
        fontColor = colorPreferences.getInt(KEY_COLOR_FONT, getColor(R.color.defaultTextColor))

        val fontPreferences = getSharedPreferences(PREFERENCES_FONT,
            Context.MODE_PRIVATE)
        fontId = fontPreferences.getInt(KEY_FONT_ID, R.font.nanum_gothic_font_family)
        fontStyleId = fontPreferences.getInt(KEY_FONT_STYLE_ID, R.style.FontNanumGothic)
        font = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            resources.getFont(fontId)
        else ResourcesCompat.getFont(this, fontId)
    }

    private fun lock() {
        SetPasswordDialogFragment(noteAdapter).show(fragmentManager, tag)
    }

    private fun unlock() {
        note.isLocked = false
        note.password = ""
        viewModel.update(note)
        showToast("잠금이 해제되었습니다.")
    }

    private fun setText(note: Note) {
        binding.toolbar.title = note.title
        binding.editTextContent.text.clear()
        binding.editTextContent.setText(note.content)
        setTimeText(note)
    }

    private fun startAlarmFragment(note: Note) {
        MainActivity.hideKeyboard(binding.editTextContent.context, binding.editTextContent)
        alarmFragment.isFromEditFragment = false
        alarmFragment.setNote(note)
        fragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_up, R.anim.slide_down, R.anim.slide_down)
            .replace(R.id.edit_note_container,
                alarmFragment).commit()
    }

    private fun startPhotoFragment() {
        MainActivity.hideKeyboard(binding.editTextContent.context, binding.editTextContent)
        val photoFragment = PhotoFragment()
        photoFragment.setParameters(this, note.uri!!)
        fragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(R.anim.anim_slide_in_left_enter,
                R.anim.anim_slide_in_left_exit,
                R.anim.anim_slide_out_right_enter,
                R.anim.anim_slide_out_right_exit)
            .replace(R.id.edit_note_container, photoFragment).commit()
    }

    private fun showCheckMessage() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("노트 수정")
        builder.setMessage("지금까지 편집한 내용을 저장하시겠습니까?")
        builder.setPositiveButton("저장") { _: DialogInterface?, _: Int ->
            finish(SAVE)
        }.setNeutralButton("계속쓰기") { _: DialogInterface?, _: Int -> }
        builder.setNegativeButton("아니요") { _: DialogInterface?, _: Int ->
            finishWithoutSaving()
        }
        builder.create()

        val dialog = builder.show()!!

        val alertTitleId = resources.getIdentifier("alertTitle", "id", packageName)
        val messageTextView = dialog.findViewById<TextView>(android.R.id.message)!!
        val okButton = dialog.findViewById<Button>(android.R.id.button1)!!
        val cancelButton = dialog.findViewById<Button>(android.R.id.button2)!!
        val keepWritingButton = dialog.findViewById<Button>(android.R.id.button3)!!

        if (alertTitleId > 0) {
            val titleTextView = dialog.findViewById<TextView>(alertTitleId)!!

            titleTextView.setTextColor(toolbarColor)
            okButton.setTextColor(toolbarColor)
            cancelButton.setTextColor(toolbarColor)
            keepWritingButton.setTextColor(toolbarColor)

            titleTextView.adjustDialogTitleTextSize(fontId)
            messageTextView.adjustDialogItemTextSize(fontId)
            okButton.adjustDialogButtonTextSize(fontId)
            cancelButton.adjustDialogButtonTextSize(fontId)
            keepWritingButton.adjustDialogButtonTextSize(fontId)

            titleTextView.typeface = font
            messageTextView.typeface = font
            okButton.typeface = font
            cancelButton.typeface = font
            keepWritingButton.typeface = font
        }
    }

    private fun addToStatusBar(note: Note) {
        val builder = NotificationCompat.Builder(this,
            MoreOptionsDialogFragment.CHANNEL_ID
        )
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationIntent = Intent(this, EditActivity::class.java)
        val id = note.id
        val title = note.title
        val content = note.content

        notificationIntent.action = ACTION_ALARM_NOTIFICATION_CLICKED + id
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        notificationIntent.putExtra(KEY_NOTE_ID, id)

        val pendingIntent = PendingIntent.getActivity(
            this,
            id,
            notificationIntent,
            PendingIntent.FLAG_ONE_SHOT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(MoreOptionsDialogFragment.CHANNEL_ID, MoreOptionsDialogFragment.CHANNEL_NAME, importance)

            builder.setSmallIcon(R.drawable.ic_cat_00_orange_32dp)
            channel.description = MoreOptionsDialogFragment.CHANNEL_DESCRIPTION
            notificationManager.createNotificationChannel(channel)
        } else builder.setSmallIcon(R.mipmap.ic_cat_00_orange_128px)

        builder.setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(title)
            .setContentText(content)
            .setContentInfo(MoreOptionsDialogFragment.CONTENT_INFO)
            .setContentIntent(pendingIntent)
        notificationManager.notify(id, builder.build())

        showToast("상태바에 등록되었습니다.")
    }

    private fun finish(action: Int) {
        if (isContentChanged()) {
            when (action) {
                SAVE -> finishWithSaving()
                BACK_PRESSED -> showCheckMessage()
            }
        }
        else finishWithoutSaving()
    }

    private fun finishWithSaving() {
        note.editTime =  MainActivity.getCurrentTime()
        note.content = binding.editTextContent.text.toString()
        viewModel.update(note)

        showToast("노트가 수정되었습니다.")
        finish()
    }

    private fun finishWithoutSaving() {
        showToast("변경된 내용이 없습니다.")
        finish()
    }

    private fun getFocus() {
        isEditMode = true
        modeIcon.setIcon(R.drawable.ic_done_white_24dp)
        binding.editTextContent.isFocusable = true
        binding.editTextContent.isFocusableInTouchMode = true
        binding.editTextContent.requestFocus()
        showToast("노트를 수정하세요.")

        // binding.adView.visibility = View.GONE

        if (!keyboardShown(binding.editTextContent.rootView)) {
            MainActivity.showKeyboard(binding.editTextContent.context, binding.editTextContent)
        }
    }

    private fun isContentChanged() = originContent != binding.editTextContent.text.toString()

    private fun cancelAlarm(note: Note) {
        val id: Int = note.id

        val alarmManager =
            getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            id,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        alarmManager.cancel(pendingIntent)
        removeAlarmPreferences(id)

        note.alarmTime = null
        viewModel.update(note)

        Toast.makeText(this, "알림이 해제되었습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun removeAlarmPreferences(number: Int) {
        val sharedPreferences = getSharedPreferences(
            "alarm_information",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.remove(number.toString() + "0")
        editor.remove(number.toString() + "1")
        editor.remove(number.toString() + "2")
        editor.remove(number.toString() + "3")
        editor.apply()
    }

    private fun showToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, text, duration).show()
    }

    private fun showImage() {
        if(note.uri == null) return
        else crossFadeImageView(true)
    }

    private fun crossFadeImageView(fadeIn: Boolean) {
        if (fadeIn) {
            binding.imageView.apply {
                alpha = 0F
                visibility = View.VISIBLE

                animate()
                    .alpha(1F)
                    .setDuration(shortAnimationDuration.toLong())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator) {
                            Glide.with(binding.imageView.context)
                                .load(Uri.parse(note.uri))
                                .error(R.drawable.ic_sentiment_dissatisfied_grey_24dp)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .listener(requestListener)
                                .into(binding.imageView)
                        }
                    })
            }
        } else {
            binding.imageView.animate()
                .alpha(0F)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.imageView.visibility = View.GONE
                    }
                })
        }
    }

    private fun applyDesign() {
        // Set color
        binding.toolbar.setBackgroundColor(toolbarColor)
        binding.textViewTime.setBackgroundColor(inlayColor)
        binding.editTextContent.setBackgroundColor(inlayColor)
        binding.viewLock.setBackgroundColor(backgroundColor)

        binding.textViewTime.setTextColor(fontColor)
        binding.editTextContent.setTextColor(fontColor)

        // Set font
        binding.textViewTime.adjustDialogItemTextSize(fontId, true)
        binding.editTextContent.adjustDialogInputTextSize(fontId, 4f)

        binding.toolbar.setTitleTextAppearance(this, fontStyleId)
        binding.textViewTime.typeface = font
        binding.editTextContent.typeface = font
    }

    private fun crossFadeOutLockView() {
        binding.viewLock.animate()
            .alpha(0F)
            .setDuration(shortAnimationDuration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.viewLock.visibility = View.GONE
                }
            })
    }

    private val requestListener: RequestListener<Drawable> = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            binding.imageView.setOnClickListener {
                showToast("이미지를 찾을 수 없습니다.")
                note.uri = null
                viewModel.update(note)
                it.visibility = View.GONE
            }
            binding.imageView.isClickable = false
            binding.imageView.isFocusable = false

            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            binding.imageView.isClickable = true
            binding.imageView.isFocusable = true

            return false
        }
    }


    companion object {
        const val SAVE = 0
        const val BACK_PRESSED = 1

        var font: Typeface? = null
        var fontId = 0
        var fontColor = 0
        var fontStyleId = 0
        var toolbarColor = 0
        var backgroundColor = 0
        var inlayColor = 0

        const val ACTION_PASSWORD_CONFIRMED = "action_password_confirmed"

        lateinit var textViewTime: TextView

        fun setTimeText(note: Note) {
            var timeText = "최초 작성일: " + MainActivity.longTimeToString(
                note.creationTime, PATTERN_UP_TO_SECONDS
            )

            if (note.editTime != null) timeText += "\n최근 수정일: ${MainActivity.longTimeToString(
                note.editTime, PATTERN_UP_TO_SECONDS
            )}"

            if (note.alarmTime != null) timeText += "\n알림 시간: ${MainActivity.longTimeToString(
                note.alarmTime, PATTERN_UP_TO_SECONDS
            )}"

            textViewTime.text = timeText
        }
    }
}