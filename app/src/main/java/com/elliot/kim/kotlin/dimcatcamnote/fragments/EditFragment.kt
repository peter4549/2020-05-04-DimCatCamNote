package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View.OnTouchListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.view.forEachIndexed
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.EditActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.databinding.FragmentEditBinding
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.AddToCalendarDialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.DialogFragments
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.MoreOptionsDialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.SetPasswordDialogFragment
import com.elliot.kim.kotlin.dimcatcamnote.view_model.MainViewModel

class EditFragment(private val activity: MainActivity) : Fragment() {

    private lateinit var binding: FragmentEditBinding
    private lateinit var modeIcon: MenuItem
    private lateinit var note: Note
    private lateinit var originContent: String
    private lateinit var viewModel: MainViewModel
    private var isEditMode = false
    private var originAlarmTime: Long? = null
    private var shortAnimationDuration = 0
    private var uri: String? = null
    var isFromAlarmedNoteSelectionFragment = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = activity.viewModel
        setOriginalData()
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    private fun setOriginalData() {
        originAlarmTime = note.alarmTime
        originContent = note.content
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
    }

    fun setNote(note: Note) {
        this.note = note
        originAlarmTime = note.alarmTime
        originContent = note.content
        uri = note.uri
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditBinding.bind(view)

        setViewDesign()

        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        // Apply the design.
        binding.textViewTime.adjustDialogItemTextSize(MainActivity.fontId, true)
        binding.editTextContent.adjustDialogInputTextSize(MainActivity.fontId, 4f)

        binding.toolbar.setTitleTextAppearance(requireContext(), MainActivity.fontStyleId)
        binding.textViewTime.typeface = MainActivity.font
        binding.editTextContent.typeface = MainActivity.font

        showImage()

        binding.editTextContent.setOnTouchListener(object : OnTouchListener {
            private val gestureDetector = GestureDetector(activity,
                object : SimpleOnGestureListener() {
                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        if (!isEditMode) {
                            activity.showToast("더블 탭하여 노트를 편집하세요.")
                        }

                        return super.onSingleTapUp(e)
                    }

                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        if (!isEditMode) {
                            binding.editTextContent.highlightColor = activity.getColor(android.R.color.transparent)
                        }

                        return super.onDoubleTap(e)
                    }

                    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
                        if (e!!.action == MotionEvent.ACTION_UP && !isEditMode) {
                            getFocus()
                            val cursorPosition = binding.editTextContent.selectionEnd
                            binding.editTextContent.setSelection(cursorPosition, cursorPosition)
                            binding.editTextContent.highlightColor = activity.getColor(R.color.colorAccent)
                        }

                        return super.onDoubleTapEvent(e)
                    }
                })

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                return gestureDetector.onTouchEvent(event)
            }
        })

        binding.imageView.setOnClickListener {
            startPhotoFragment()
        }

        binding.editTextContent.viewTreeObserver.addOnGlobalLayoutListener {
            if (keyboardShown(binding.editTextContent.rootView) && isEditMode)
                crossFadeImageView(false)
            else if (binding.imageView.visibility != View.VISIBLE)
                showImage()
        }
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {

        val animation = AnimationUtils.loadAnimation(activity, nextAnim)

        animation!!.setAnimationListener( object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                MainActivity.hideKeyboard(context, view)
            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })

        return animation
    }

    override fun onResume() {
        super.onResume()
        setNoteText(note)
        activity.setCurrentFragment(CurrentFragment.EDIT_FRAGMENT)
    }

    override fun onPause() {
        note.content = binding.editTextContent.text.toString()
        super.onPause()
    }

    override fun onDestroyView() {
        isEditMode = false
        binding.editTextContent.clearFocus()
        binding.editTextContent.isFocusable = false
        binding.editTextContent.isFocusableInTouchMode = false

        if (isFromAlarmedNoteSelectionFragment) {
            activity.setCurrentFragment(CurrentFragment.CALENDAR_FRAGMENT)
            isFromAlarmedNoteSelectionFragment = false
        }
        else {
            activity.setCurrentFragment(null)
            activity.showFloatingActionButton()
        }

        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_edit, menu)
        modeIcon = menu.findItem(R.id.menu_mode_icon)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val menuDone = menu.findItem(R.id.menu_done)
        val menuAlarm = menu.findItem(R.id.menu_alarm)
        val menuChangeAlarm = menu.findItem(R.id.menu_change_alarm)
        val menuLock = menu.findItem(R.id.menu_lock)

        if (note.isDone) menuDone.title = "완료해제"
        else menuDone.title = "완료체크"

        if (note.alarmTime == null) {
            menuAlarm.title = "알림설정"
            menuChangeAlarm.isVisible = false
        } else {
            menuAlarm.title = "알림해제"
            menuChangeAlarm.isVisible = true
        }

        if (note.isLocked)
            menuLock.title = "잠금해제"
        else
            menuLock.title = "잠금설정"

        // Apply font
        menu.forEachIndexed { index, _ ->
            // Except mode menu
            if (index != 0) {
                val menuItem = menu.getItem(index)
                val spanString = SpannableString(menuItem.title.toString())
                spanString.setSpan(
                    TextAppearanceSpan(context, MainActivity.fontStyleId),
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish(BACK_PRESSED)
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
                        Toast.makeText(context, "변경사항이 없습니다.", Toast.LENGTH_SHORT).show()

                    if (keyboardShown(binding.editTextContent.rootView))
                        MainActivity.hideKeyboard(activity, binding.editTextContent)

                    isEditMode = !isEditMode
                } else
                    getFocus()
            }
            R.id.menu_alarm -> {
                if (note.alarmTime == null)
                    startAlarmFragment(note)
                else {
                    activity.cancelAlarm(note, isDelete = false, isByUser = true)
                    setTimeText(note)
                }
            }
            R.id.menu_change_alarm -> startAlarmFragment(note)
            R.id.menu_share -> MainActivity.share(activity, note)
            R.id.menu_done -> {
                if (note.isDone)
                    Toast.makeText(requireContext(), "완료처리를 해제하셨습니다.", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(requireContext(), "완료처리 되었습니다.", Toast.LENGTH_SHORT).show()
                note.isDone = !note.isDone
                viewModel.update(note)
            }
            R.id.menu_lock -> {
                if (note.isLocked) unlock()
                else lock()
            }
            R.id.menu_delete -> {
                activity.closeOptionsMenu()
                activity.showDialogFragment(DialogFragments.CONFIRM_DELETE)
            }
            R.id.menu_add_to_status_bar -> addToStatusBar(note)
            R.id.menu_add_to_calendar -> AddToCalendarDialogFragment(note)
                .show((requireActivity() as MainActivity).fragmentManager, tag)
            R.id.menu_move_to_folder -> (requireActivity() as MainActivity)
                .showDialogFragment(DialogFragments.MOVE_TO_FOLDER)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun lock() {
        SetPasswordDialogFragment(activity.getNoteAdapter()).show(activity.fragmentManager, tag)
    }

    private fun unlock() {
        note.isLocked = false
        note.password = ""
        activity.viewModel.update(note)
        activity.showToast("잠금이 해제되었습니다.")
    }

    private fun setNoteText(note: Note) {
        binding.toolbar.title = note.title
        binding.editTextContent.text.clear()
        binding.editTextContent.setText(note.content)
        setTimeText(note)
    }

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

        binding.textViewTime.text = timeText
    }

    private fun setViewDesign() {
        binding.toolbar.setBackgroundColor(MainActivity.toolbarColor)
        binding.textViewTime.setBackgroundColor(MainActivity.inlayColor)
        binding.editTextContent.setBackgroundColor(MainActivity.inlayColor)
    }

    private fun startAlarmFragment(note: Note) {
        MainActivity.hideKeyboard(binding.editTextContent.context, binding.editTextContent)
        activity.alarmFragment.isFromEditFragment = true
        activity.alarmFragment.setNote(note)
        activity.fragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_up, R.anim.slide_down, R.anim.slide_down)
            .replace(R.id.edit_note_container,
                activity.alarmFragment).commit()
    }

    private fun startPhotoFragment() {
        MainActivity.hideKeyboard(binding.editTextContent.context, binding.editTextContent)
        val photoFragment = PhotoFragment()
        photoFragment.setParameters(this, note.uri!!)
        activity.fragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(R.anim.anim_slide_in_left_enter,
                R.anim.anim_slide_in_left_exit,
                R.anim.anim_slide_out_right_enter,
                R.anim.anim_slide_out_right_exit)
            .replace(R.id.edit_note_container, photoFragment).commit()
    }

    private fun addToStatusBar(note: Note) {
        val builder = NotificationCompat.Builder(requireContext(),
            MoreOptionsDialogFragment.CHANNEL_ID
        )
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationIntent = Intent(context, EditActivity::class.java)
        val id = note.id
        val title = note.title
        val content = note.content

        notificationIntent.action = ACTION_ALARM_NOTIFICATION_CLICKED + id
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        notificationIntent.putExtra(KEY_NOTE_ID, id)

        val pendingIntent = PendingIntent.getActivity(
            context,
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

        (requireActivity() as MainActivity).showToast("상태바에 등록되었습니다.")
    }

    private fun showCheckMessage() {
        val builder = AlertDialog.Builder(requireContext())
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

        val alertTitleId = resources.getIdentifier("alertTitle", "id", requireContext().packageName)
        val messageTextView = dialog.findViewById<TextView>(android.R.id.message)!!
        val okButton = dialog.findViewById<Button>(android.R.id.button1)!!
        val cancelButton = dialog.findViewById<Button>(android.R.id.button2)!!
        val keepWritingButton = dialog.findViewById<Button>(android.R.id.button3)!!

        if (alertTitleId > 0) {
            val titleTextView = dialog.findViewById<TextView>(alertTitleId)!!

            titleTextView.setTextColor(MainActivity.toolbarColor)
            okButton.setTextColor(MainActivity.toolbarColor)
            cancelButton.setTextColor(MainActivity.toolbarColor)
            keepWritingButton.setTextColor(MainActivity.toolbarColor)

            titleTextView.adjustDialogTitleTextSize(MainActivity.fontId)
            messageTextView.adjustDialogItemTextSize(MainActivity.fontId)
            okButton.adjustDialogButtonTextSize(MainActivity.fontId)
            cancelButton.adjustDialogButtonTextSize(MainActivity.fontId)
            keepWritingButton.adjustDialogButtonTextSize(MainActivity.fontId)

            titleTextView.typeface = MainActivity.font
            messageTextView.typeface = MainActivity.font
            okButton.typeface = MainActivity.font
            cancelButton.typeface = MainActivity.font
            keepWritingButton.typeface = MainActivity.font
        }
    }

    fun finish(action: Int) {
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
        activity.viewModel.update(note)

        Toast.makeText(context, "노트가 수정되었습니다.", Toast.LENGTH_SHORT).show()
        activity.backPressed()
    }

    private fun finishWithoutSaving() {
        Toast.makeText(context, "변경된 내용이 없습니다.", Toast.LENGTH_SHORT).show()
        activity.backPressed()
    }

    private fun getFocus() {
        isEditMode = true
        modeIcon.setIcon(R.drawable.ic_done_white_24dp)
        binding.editTextContent.isFocusable = true
        binding.editTextContent.isFocusableInTouchMode = true
        binding.editTextContent.requestFocus()
        activity.showToast("노트를 수정하세요.")

        if (!keyboardShown(binding.editTextContent.rootView)) {
            MainActivity.showKeyboard(binding.editTextContent.context, binding.editTextContent)
        }
    }

    private fun isContentChanged() = originContent != binding.editTextContent.text.toString()

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

    private fun keyboardShown(rootView: View): Boolean {
        val softKeyboardHeight = 100
        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)

        val metrics = rootView.resources.displayMetrics
        val heightDiff: Int = rootView.bottom - rect.bottom

        return heightDiff > softKeyboardHeight * metrics.density
    }

    private fun showImage() {
        if(uri == null) return
        else crossFadeImageView(true)
    }

    private val requestListener: RequestListener<Drawable> = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
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
    }
}
