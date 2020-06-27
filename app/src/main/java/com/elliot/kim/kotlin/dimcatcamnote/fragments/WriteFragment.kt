package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.graphics.drawable.AnimationDrawable
import android.media.AudioManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.elliot.kim.kotlin.dimcatcamnote.*
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity.Companion.CAMERA_PERMISSIONS_REQUEST_CODE
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity.Companion.RECORD_AUDIO_PERMISSIONS_REQUEST_CODE
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity.Companion.currentFragment
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity.Companion.getCurrentTime
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity.Companion.longTimeToString
import com.elliot.kim.kotlin.dimcatcamnote.activities.SingleNoteConfigureActivity
import com.elliot.kim.kotlin.dimcatcamnote.adapters.AlarmedNoteAdapter
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.databinding.FragmentWriteBinding
import kotlinx.android.synthetic.main.fragment_write.view.*

class WriteFragment : Fragment() {

    lateinit var handler: Handler
    private lateinit var audioManager: AudioManager
    private lateinit var binding: FragmentWriteBinding
    private lateinit var intent: Intent
    private lateinit var recognizer: SpeechRecognizer
    private lateinit var title: String
    private lateinit var content: String
    private lateinit var micAnimation: AnimationDrawable
    private var alarmedNoteAdapter: AlarmedNoteAdapter? = null
    private var newNote: Note? = null
    private var originalVolume = 0
    private var previousPartialText = ""
    private var shortAnimationDuration = 0
    private var isSaved = false
    private var isFirstOnResults = true
    private var isRecognizingSpeech = false
    private var existingUri: String? = null
    private var noMatchCount = 0
    var isFromAlarmedNoteSelectionFragment = false
    var dateSelectedInCalender = 0L
    var uri: String? = null

    fun setAlarmedNoteAdapter(alarmedNoteAdapter: AlarmedNoteAdapter) {
        this.alarmedNoteAdapter = alarmedNoteAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (context == null) onDestroy()
        audioManager = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        existingUri = uri
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        shortAnimationDuration -= 80

        intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, requireActivity().packageName)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, VALUE_LANGUAGE)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, VALUE_MAX_RESULTS)
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 600000)
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 600000)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_write, container, false)

    override fun onResume() {
        super.onResume()
        if (activity is MainActivity)
            (activity as MainActivity).setCurrentFragment(CurrentFragment.WRITE_FRAGMENT)
        else if (activity is SingleNoteConfigureActivity)
            (activity as SingleNoteConfigureActivity)
                .setCurrentFragment(CurrentFragment.WRITE_FRAGMENT)
        isSaved = false
        originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentWriteBinding.bind(view)
        binding.toolbar.title = TITLE_TOOLBAR

        if (activity is MainActivity) {
            (activity as MainActivity).setSupportActionBar(binding.toolbar)
            (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        } else if (activity is SingleNoteConfigureActivity) {
            (activity as SingleNoteConfigureActivity).setSupportActionBar(binding.toolbar)
            (activity as SingleNoteConfigureActivity)
                .supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        setHasOptionsMenu(true)

        // Apply mic icon animation
        binding.imageViewMic.apply {
            setBackgroundResource(R.drawable.mic_animation_list)
            micAnimation = background as AnimationDrawable
        }

        // Apply design
        binding.toolbar.setBackgroundColor(MainActivity.toolbarColor)
        binding.bottomNavigationView.setBackgroundColor(MainActivity.toolbarColor)
        binding.editTextTitle.setBackgroundColor(MainActivity.inlayColor)
        binding.editTextContent.setBackgroundColor(MainActivity.inlayColor)
        binding.speechRecognitionContainer.setBackgroundColor(MainActivity.toolbarColor)

        binding.toolbar.setTitleTextAppearance(activity, MainActivity.fontStyleId)
        binding.toolbar.invalidate()
        binding.editTextTitle.typeface = MainActivity.font
        binding.editTextContent.typeface = MainActivity.font
        binding.textViewSpeechRecognitionFinish.typeface = MainActivity.font

        binding.imageView.setOnClickListener { startPhotoFragment() }

        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.camera -> {
                    if (isRecognizingSpeech)
                        finishSpeechRecognition()
                    else {
                        if (uri == null) {
                            if (MainActivity.hasCameraPermissions(requireContext()))
                                startCameraFragment()
                            else
                                requestPermissions(
                                    MainActivity.CAMERA_PERMISSIONS_REQUIRED,
                                    CAMERA_PERMISSIONS_REQUEST_CODE
                                )
                        } else showPictureChangeMessage()
                    }
                }
                R.id.microphone -> {
                    if (MainActivity.hasCameraPermissions(requireContext()))
                        startSpeechRecognition()
                    else
                        requestPermissions(
                            MainActivity.RECORD_AUDIO_PERMISSIONS_REQUESTED,
                            RECORD_AUDIO_PERMISSIONS_REQUEST_CODE)
                }
            }
            return@setOnNavigationItemSelectedListener true
        }

        binding.textViewSpeechRecognitionFinish.setOnClickListener { finishSpeechRecognition() }
        binding.editTextContent.viewTreeObserver.addOnGlobalLayoutListener {
            if (keyboardShown(binding.editTextContent.rootView)) {
                crossFadeImageView(false)
                crossFadeBottomNavigationView(false)
            } else {
                // Flickering occurs without the following conditions
                if (existingUri != uri ||
                    (binding.editTextTitle.isFocused
                            || binding.editTextContent.isFocused)) {
                    showImage()
                    existingUri = uri
                }

                if ((binding.editTextTitle.isFocused
                            || binding.editTextContent.isFocused)
                    && currentFragment == CurrentFragment.WRITE_FRAGMENT)
                    crossFadeBottomNavigationView(true)
            }
        }

        handler = Handler {
            when(it.what) {
                SHOW_BOTTOM_NAVIGATION_VIEW -> {
                    binding.bottomNavigationView.apply {
                        translationY = binding.bottomNavigationView.height.toFloat()
                        visibility = View.VISIBLE

                        animate()
                            .translationY(0F)
                            .setDuration(shortAnimationDuration.toLong())
                            .setListener(null)
                    }
                }
            }
            true
        }
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val animation = AnimationUtils.loadAnimation(activity, nextAnim)

        animation!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                if (!enter) {
                    MainActivity.hideKeyboard(context, view)
                    if (isSaved) showToast(requireContext().getString(R.string.save_complete_message))
                }
            }

            override fun onAnimationStart(animation: Animation?) {
                if (enter) clearText()
            }
        })

        return animation
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        uri = null

        if (this::recognizer.isInitialized) { recognizer.destroy() }

        if (activity is MainActivity) {
            if (isFromAlarmedNoteSelectionFragment)
                (activity as MainActivity).setCurrentFragment(CurrentFragment.CALENDAR_FRAGMENT)
            else {
                (activity as MainActivity).setCurrentFragment(null)
                (activity as MainActivity).showFloatingActionButton()
            }
        } else if (activity is SingleNoteConfigureActivity)
            (activity as SingleNoteConfigureActivity).setCurrentFragment(null)

        this.alarmedNoteAdapter = null
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_write, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish(BACK_PRESSED)
            R.id.save -> finish(SAVE)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CAMERA_PERMISSIONS_REQUEST_CODE -> {
                if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                    startCameraFragment()
                } else {
                    Toast.makeText(
                        context,
                        "카메라 권한을 승인하셔야 카메라를 사용하실 수 있습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            RECORD_AUDIO_PERMISSIONS_REQUEST_CODE -> {
                if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                    startSpeechRecognition()
                } else {
                    Toast.makeText(
                        context,
                        "오디오 권한을 승인하셔야 음성 노트를 사용하실 수 있습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
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

    private fun setTitleContent() {
        title = binding.editTextTitle.text.toString()
        content = binding.editTextContent.text.toString()
    }

    private fun showImage() {
        if(uri == null)
            return
        else
            crossFadeImageView(true)
    }

    private fun startCameraFragment() {
        binding.editTextTitle.clearFocus()
        binding.editTextContent.clearFocus()
        binding.bottomNavigationView.apply {
            translationY = 0F

            animate().translationY(bottomNavigationView.height.toFloat())
                .setDuration(shortAnimationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.bottomNavigationView.visibility = View.GONE
                        if (activity is MainActivity) {
                            (activity as MainActivity).cameraFragment.setPreviousUri(uri)
                            (activity as MainActivity).fragmentManager.beginTransaction()
                                .addToBackStack(null)
                                .setCustomAnimations(
                                    R.anim.anim_camera_fragment_enter,
                                    R.anim.anim_camera_fragment_exit,
                                    R.anim.anim_camera_fragment_pop_enter,
                                    R.anim.anim_camera_fragment_pop_exit
                                )
                                .replace(R.id.write_fragment_container, (activity as MainActivity).cameraFragment)
                                .commit()
                        } else if (activity is SingleNoteConfigureActivity) {
                            (activity as SingleNoteConfigureActivity).cameraFragment.setPreviousUri(uri)
                            (activity as SingleNoteConfigureActivity).fragmentManager.beginTransaction()
                                .addToBackStack(null)
                                .setCustomAnimations(
                                    R.anim.anim_camera_fragment_enter,
                                    R.anim.anim_camera_fragment_exit,
                                    R.anim.anim_camera_fragment_pop_enter,
                                    R.anim.anim_camera_fragment_pop_exit
                                )
                                .replace(R.id.write_fragment_container,
                                    (activity as SingleNoteConfigureActivity).cameraFragment)
                                .commit()
                        }
                    }
                })
        }
    }

    private fun startPhotoFragment() {
        binding.editTextTitle.clearFocus()
        binding.editTextContent.clearFocus()
        binding.bottomNavigationView.apply {
            translationY = 0F

            animate().translationY(bottomNavigationView.height.toFloat())
                .setDuration(shortAnimationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.bottomNavigationView.visibility = View.GONE
                        val photoFragment = PhotoFragment()
                        photoFragment.setParameters(this@WriteFragment, uri!!)
                        activity!!.supportFragmentManager.beginTransaction()
                            .addToBackStack(null)
                            .setCustomAnimations(R.anim.slide_up, R.anim.slide_up, R.anim.slide_down, R.anim.slide_down)
                            .replace(R.id.write_fragment_container, photoFragment).commit()
                    }
                })
        }

    }

    private fun startSpeechRecognition() {
        recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer.setRecognitionListener(recognitionListener)

        isFirstOnResults = true
        binding.speechRecognitionContainer.apply {
            alpha = 0F
            visibility = View.VISIBLE

            animate()
                .alpha(1F)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        isRecognizingSpeech = true
                        micAnimation.start()
                    }
                })
        }

        recognizer.startListening(intent)
        setEditTextDisable()
    }

    private fun finishSpeechRecognition() {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
        binding.speechRecognitionContainer.animate()
            .alpha(0F)
            .setDuration(shortAnimationDuration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    micAnimation.stop()
                    binding.speechRecognitionContainer.visibility = View.GONE
                    recognizer.stopListening()
                    recognizer.cancel()
                    recognizer.destroy()

                    isRecognizingSpeech = false
                }
            })
        noMatchCount = 0

        setEditTextEnable()
    }

    private fun setEditTextEnable() {
        binding.editTextTitle.isEnabled = true
        binding.editTextContent.isEnabled = true
    }

    private fun setEditTextDisable() {
        binding.editTextTitle.isEnabled = false
        binding.editTextContent.isEnabled = false
    }

    private fun showCheckMessage() {
        val builder = context?.let { AlertDialog.Builder(it) }
        builder?.setTitle("노트 저장")
        builder?.setMessage("지금까지 작성한 내용을 저장하시겠습니까?")
        builder?.setPositiveButton("저장") { _: DialogInterface?, _: Int ->
            finishWithSaving()
        }
        builder?.setNeutralButton("계속쓰기") { _: DialogInterface?, _: Int -> }
        builder?.setNegativeButton("아니요") { _: DialogInterface?, _: Int ->
            finishWithoutSaving()
        }

        val dialog = builder?.show()!!

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

    private fun showPictureChangeMessage() {
        val builder = context?.let { AlertDialog.Builder(it) }
        builder?.setTitle("사진 변경")
        builder?.setMessage("새로운 사진을 찍으시겠습니까?")
        builder?.setPositiveButton("네") { _: DialogInterface?, _: Int ->
            startCameraFragment()
        }
        builder?.setNegativeButton("아니요") { _: DialogInterface?, _: Int -> }

        val dialog = builder?.show()!!

        val alertTitleId = resources.getIdentifier("alertTitle", "id", requireContext().packageName)
        val messageTextView = dialog.findViewById<TextView>(android.R.id.message)!!
        val okButton = dialog.findViewById<Button>(android.R.id.button1)!!
        val cancelButton = dialog.findViewById<Button>(android.R.id.button2)!!

        if (alertTitleId > 0) {
            val titleTextView = dialog.findViewById<TextView>(alertTitleId)!!

            titleTextView.setTextColor(MainActivity.toolbarColor)
            okButton.setTextColor(MainActivity.toolbarColor)
            cancelButton.setTextColor(MainActivity.toolbarColor)

            titleTextView.adjustDialogTitleTextSize(MainActivity.fontId)
            messageTextView.adjustDialogItemTextSize(MainActivity.fontId)
            okButton.adjustDialogButtonTextSize(MainActivity.fontId)
            cancelButton.adjustDialogButtonTextSize(MainActivity.fontId)

            titleTextView.typeface = MainActivity.font
            messageTextView.typeface = MainActivity.font
            okButton.typeface = MainActivity.font
            cancelButton.typeface = MainActivity.font
        }
    }

    override fun onStop() {
        binding.editTextTitle.clearFocus()
        binding.editTextContent.clearFocus()
        super.onStop()
    }

    private fun save() {
        if (isFromAlarmedNoteSelectionFragment) {
            alarmedNoteAdapter!!.insert(newNote!!)
            (activity as MainActivity).calendarFragment.updateCalendarAdapter(newNote!!)
        }

        if (activity is MainActivity)
            (activity as MainActivity).viewModel.insert(newNote!!)
        else if (activity is SingleNoteConfigureActivity) {
            (activity as SingleNoteConfigureActivity).viewModel.insert(newNote!!)
            (activity as SingleNoteConfigureActivity).noteAdapter.insert(newNote!!)
        }
    }

    fun finish(save: Int) {
        if (isRecognizingSpeech) finishSpeechRecognition()
        else {
            setTitleContent()
            if (isEmpty())
                finishWithoutSaving()
            else {
                when (save) {
                    SAVE -> finishWithSaving()
                    BACK_PRESSED -> showCheckMessage()
                }
            }
        }
    }

    private fun isEmpty() = title == "" && content == "" && uri == null

    private fun finishWithSaving() {
        isSaved = true
        newNote = createNote()
        val asyncSaveTask = AsyncSaveTask(requireActivity()) {
            save()
        }
        asyncSaveTask.execute()
    }

    private fun finishWithoutSaving() {
        Toast.makeText(context, "저장되지 않았습니다.", Toast.LENGTH_SHORT).show()
        if (activity is MainActivity) {
            (activity as MainActivity).deleteFileFromUri(uri)
            (activity as MainActivity).backPressed()
        } else if (activity is SingleNoteConfigureActivity) {
            (activity as SingleNoteConfigureActivity).deleteFileFromUri(uri)
            (activity as SingleNoteConfigureActivity).backPressed()
        }
    }

    private fun clearText() {
        binding.editTextTitle.text.clear()
        binding.editTextContent.text.clear()
    }

    private fun crossFadeBottomNavigationView(fadeIn: Boolean) {
        if (fadeIn) {
            binding.bottomNavigationView.apply {
                translationY = bottomNavigationView.height.toFloat()

                animate()
                    .translationY(0F)
                    .setDuration(shortAnimationDuration.toLong())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            visibility = View.VISIBLE
                        }
                    })
            }
        } else {
            binding.bottomNavigationView.apply {
                translationY = 0F

                animate()
                    .translationY(bottomNavigationView.height.toFloat())
                    .setDuration(shortAnimationDuration.toLong())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            visibility = View.GONE
                        }
                    })
            }
        }
    }

    private fun crossFadeImageView(fadeIn: Boolean) {
        if (fadeIn) {
            binding.imageView.apply {
                alpha = 0F

                animate()
                    .alpha(1F)
                    .setDuration(shortAnimationDuration.toLong())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            visibility = View.VISIBLE
                            Glide.with(binding.imageView.context)
                                .load(Uri.parse(uri))
                                .error(R.drawable.ic_sentiment_dissatisfied_grey_24dp)
                                .transition(DrawableTransitionOptions.withCrossFade())
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

    private val recognitionListener = object: RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            isFirstOnResults = true
            previousPartialText = ""
        }

        override fun onRmsChanged(rmsdB: Float) {

        }

        override fun onBufferReceived(buffer: ByteArray?) {

        }

        // Full speech recognition results are not obtained.
        override fun onPartialResults(partialResults: Bundle?) {
            val results: List<String>? =
                partialResults!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (results != null) {
                val text = results[0]
                if (text != previousPartialText && text.length > previousPartialText.length) {
                    try {
                        binding.editTextContent.append(
                            text.subSequence(
                                previousPartialText.length,
                                text.length
                            )
                        )
                        previousPartialText = text
                    } catch (e: StringIndexOutOfBoundsException) { }
                }
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {

        }

        override fun onBeginningOfSpeech() {

        }

        override fun onEndOfSpeech() {

        }

        override fun onError(error: Int) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
            when (error) {
                SpeechRecognizer.ERROR_AUDIO -> Log.e(SPEECH_RECOGNIZER_ERROR_TAG, "Audio")
                SpeechRecognizer.ERROR_CLIENT -> Log.e(SPEECH_RECOGNIZER_ERROR_TAG, "Client")
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                    Log.e(SPEECH_RECOGNIZER_ERROR_TAG, "Insufficient Permission")
                SpeechRecognizer.ERROR_NETWORK -> Log.e(SPEECH_RECOGNIZER_ERROR_TAG, "Network")
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                    Log.e(SPEECH_RECOGNIZER_ERROR_TAG, "Network Timeout")
                SpeechRecognizer.ERROR_NO_MATCH -> {
                    Log.e(SPEECH_RECOGNIZER_ERROR_TAG, "No Match")

                    if (noMatchCount >= 3) {
                        finishSpeechRecognition()
                        (activity as MainActivity).showToast("시간이 초과되었습니다.")
                    }
                    else if (isRecognizingSpeech) {
                        recognizer.stopListening()
                        recognizer.startListening(intent)
                        noMatchCount += 1
                    }

                }
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                    Log.e(SPEECH_RECOGNIZER_ERROR_TAG, "Recognizer Busy")
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                    Log.e(SPEECH_RECOGNIZER_ERROR_TAG, "Speech Timeout")
                    finishSpeechRecognition()
                    (activity as MainActivity).showToast("시간이 초과되었습니다.")
                }
            }
        }

        override fun onResults(results: Bundle?) {
            val recognitionResults: List<String>? =
                results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (recognitionResults != null) {
                if (isFirstOnResults) {
                    audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_MUTE,
                        0
                    )

                    val text = recognitionResults[0]
                    try {
                        binding.editTextContent.append(
                            text
                                .substring(previousPartialText.length, text.length)
                        )
                    } catch (e: StringIndexOutOfBoundsException) { }

                    binding.editTextContent.append(" ")
                    recognizer.stopListening()
                    recognizer.startListening(intent)

                    isFirstOnResults = false
                }
            }
        }
    }

    private fun createNote(): Note? {
        if (title.isBlank() && content.isBlank())
            title = longTimeToString(getCurrentTime(), PATTERN_YYYY_MM_dd)
        else {
            if (title.isBlank()) title = if (content.length > 16) content.substring(0, 16)
            else content
        }

        val note = Note(title, getCurrentTime(), uri)
        note.content = content
        if (activity is MainActivity)
            note.folderId = (activity as MainActivity).currentFolder.id
        else
            note.folderId = DEFAULT_FOLDER_ID

        if (isFromAlarmedNoteSelectionFragment)
            note.alarmTime = dateSelectedInCalender

        return note
    }

    companion object {
        const val SHOW_BOTTOM_NAVIGATION_VIEW = 0
        const val BACK_PRESSED = 0
        const val SAVE = 1

        private const val TITLE_TOOLBAR = "새 노트"
        private const val VALUE_LANGUAGE = "ko-KR"
        private const val VALUE_MAX_RESULTS = 64
        private const val SPEECH_RECOGNIZER_ERROR_TAG = "Speech Recognizer Error"
    }

    class AsyncSaveTask(val activity: Activity, val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            handler()
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            val handler = Handler()
            handler.postDelayed({
                if (activity is MainActivity)
                    activity.backPressed()
                else if (activity is SingleNoteConfigureActivity)
                    activity.backPressed()
            }, 120)
        }
    }
}