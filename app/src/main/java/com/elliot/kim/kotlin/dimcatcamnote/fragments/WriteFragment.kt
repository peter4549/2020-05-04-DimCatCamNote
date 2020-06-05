package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.elliot.kim.kotlin.dimcatcamnote.CurrentFragment
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity.Companion.CAMERA_PERMISSIONS_REQUEST_CODE
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity.Companion.RECORD_AUDIO_PERMISSIONS_REQUEST_CODE
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.PATTERN_YYYY_MM_dd
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity.Companion.getCurrentTime
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity.Companion.longTimeToString
import com.elliot.kim.kotlin.dimcatcamnote.databinding.FragmentWriteBinding
import kotlinx.android.synthetic.main.fragment_write.view.*

class WriteFragment : Fragment() {

    lateinit var handler: Handler
    private lateinit var activity: MainActivity
    private lateinit var audioManager: AudioManager
    private lateinit var binding: FragmentWriteBinding
    private lateinit var intent: Intent
    private lateinit var recognizer: SpeechRecognizer
    private lateinit var title: String
    private lateinit var content: String
    private var newNote: Note? = null
    private var originalVolume = 0
    private var previousPartialText = ""
    private var shortAnimationDuration = 0
    private var isFinish = false
    private var isFirstOnResults = true
    private var isRecognizingSpeech = false
    var uri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = requireActivity() as MainActivity
        audioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

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
        clearText()
        activity.setCurrentFragment(CurrentFragment.WRITE_FRAGMENT)
        isFinish = false// 스탑에 있으면 안될듯.
        originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWriteBinding.bind(view)

        activity.setSupportActionBar(binding.toolBar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        binding.toolBar.title = TITLE_TOOLBAR
        binding.imageView.visibility = View.GONE
        binding.imageView.setOnClickListener { startPhotoFragment() }

        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.camera -> {
                    if (isRecognizingSpeech) finishSpeechRecognition()
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
                R.id.menu_location -> {
                    /*
                    if (MainActivity.hasLocationPermissions(requireContext()))

                    else
                        requestPermissions(MainActivity.LOCATION_PERMISSIONS_REQUESTED,
                            LOCATION_PERMISSIONS_REQUEST_CODE)

                     */
                }
            }
            return@setOnNavigationItemSelectedListener true
        }

        binding.buttonCompleteSpeechRecognition.setOnClickListener { finishSpeechRecognition() }
        binding.editTextContent.viewTreeObserver.addOnGlobalLayoutListener {
            if (keyboardShown(binding.editTextContent.rootView)) crossFadeImageView(false)
            else showImage()
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

        animation!!.setAnimationListener( object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                MainActivity.hideKeyboard(context, view)


                if(isFinish) {
                    val asyncSaveTask = AsyncSaveTask(activity) {
                        save()
                    }
                    asyncSaveTask.execute()
                }
            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })

        return animation
    }

    override fun onStop() {
        super.onStop()
        uri = null

        if (this::recognizer.isInitialized) { recognizer.destroy() }

        activity.setCurrentFragment(null)
        activity.showFloatingActionButton()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        menu.clear()
        inflater.inflate(R.menu.menu_write, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // MainActivity.hideKeyboard(context, view)

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
                    Toast.makeText(
                        context,
                        "CAM in WRITE permission request granted",
                        Toast.LENGTH_LONG
                    ).show()
                    startCameraFragment()
                } else {
                    Toast.makeText(
                        context,
                        "카메라 권한을 승인하셔야 카메라 기능을 사용하실 수 있습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            RECORD_AUDIO_PERMISSIONS_REQUEST_CODE -> {
                if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                    Toast.makeText(
                        context,
                        "AUDIO in WRITE permission request granted",
                        Toast.LENGTH_LONG
                    ).show()
                    startCameraFragment()
                } else {
                    Toast.makeText(
                        context,
                        "오디오 권한을 승인하셔야 오디오 기능을 사용하실 수 있습니다.",
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
        if(uri == null) return
        else {
            crossFadeImageView(true)
            Glide.with(binding.imageView.context)
                .load(Uri.parse(uri))
                .into(binding.imageView)
        }
    }

    private fun startCameraFragment() {
        binding.bottomNavigationView.apply {
            translationY = 0F

            animate().translationY(bottomNavigationView.height.toFloat())
                .setDuration(128.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.bottomNavigationView.visibility = View.GONE

                        activity.fragmentManager.beginTransaction()
                            .addToBackStack(null)
                            .setCustomAnimations(R.anim.anim_camera_fragment_enter,
                                R.anim.anim_camera_fragment_exit,
                                R.anim.anim_camera_fragment_pop_enter,
                                R.anim.anim_camera_fragment_pop_exit)
                            .replace(R.id.write_fragment_container, activity.cameraFragment).commit()
                    }
                })
        }
    }

    private fun startPhotoFragment() {
        binding.bottomNavigationView.apply {
            translationY = 0F

            animate().translationY(bottomNavigationView.height.toFloat())
                .setDuration(128.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.bottomNavigationView.visibility = View.GONE
                        activity.fragmentManager.beginTransaction()
                            .addToBackStack(null)
                            .setCustomAnimations(R.anim.slide_up, R.anim.slide_up, R.anim.slide_down, R.anim.slide_down)
                            .replace(R.id.write_fragment_container, PhotoFragment(this, uri!!)).commit()
                    }
                })
        }

    }

    private fun startSpeechRecognition() {
        recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer.setRecognitionListener(recognitionListener)

        isFirstOnResults = true
        binding.linearLayoutSpeechRecognition.apply {
            alpha = 0F
            visibility = View.VISIBLE

            animate()
                .alpha(1F)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        isRecognizingSpeech = true
                    }
                })
        }

        recognizer.startListening(intent)
        setEditTextDisable()
    }

    private fun finishSpeechRecognition() {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
        binding.linearLayoutSpeechRecognition.animate()
            .alpha(0F)
            .setDuration(shortAnimationDuration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.linearLayoutSpeechRecognition.visibility = View.GONE
                    recognizer.stopListening()
                    recognizer.cancel()
                    recognizer.destroy()

                    isRecognizingSpeech = false
                }
            })
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
        builder?.create()
        builder?.show()
    }

    private fun showPictureChangeMessage() {
        val builder = context?.let { AlertDialog.Builder(it) }
        builder?.setTitle("사진 변경")
        builder?.setMessage("새로운 사진을 찍으시겠습니까?")
        builder?.setPositiveButton("네") { _: DialogInterface?, _: Int ->
            startCameraFragment()
        }
        builder?.setNegativeButton("아니요") { _: DialogInterface?, _: Int -> }
        builder?.create()
        builder?.show()
    }

    private fun save() {
        Log.d("dddd", "NOTE???")
        activity.viewModel.insert(newNote!!)
    }


    fun finish(save: Int) {
        if (isRecognizingSpeech) finishSpeechRecognition()
        else {
            setTitleContent()
            if (isEmpty()) finishWithoutSaving()
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
        newNote = createNote()
        isFinish = true
        //activity.getNoteAdapter().insert(newNote!!)
        activity.backPressed()
    }

    private fun finishWithoutSaving() {
        Toast.makeText(context, "저장되지 않았습니다.", Toast.LENGTH_SHORT).show()
        activity.backPressed()
    }

    private fun clearText() {
        binding.editTextTitle.text = null
        binding.editTextContent.text = null
    }

    private fun crossFadeImageView(fadeIn: Boolean) {
        if (fadeIn) {
            binding.imageView.apply {
                alpha = 0F
                visibility = View.VISIBLE

                animate()
                    .alpha(1F)
                    .setDuration(shortAnimationDuration.toLong())
                    .setListener(null)
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
                    binding.editTextContent.append(text.subSequence(previousPartialText.length,
                        text.length))
                    previousPartialText = text
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

                    if (isRecognizingSpeech) {
                        recognizer.stopListening()
                        recognizer.startListening(intent)
                    }
                }
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                    Log.e(SPEECH_RECOGNIZER_ERROR_TAG, "Recognizer Busy")
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                    Log.e(SPEECH_RECOGNIZER_ERROR_TAG, "Speech Timeout")
                    finishSpeechRecognition()
                    (requireActivity() as MainActivity).showToast("시간이 초과되었습니다.")
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
                    binding.editTextContent.append(text
                        .substring(previousPartialText.length, text.length))

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
            if (title.isBlank()) title = if (content.length > 12) content.substring(0, 12)
            else content
        }

        val note = Note(title, getCurrentTime(), uri)
        note.content = content
        note.folderId = activity.currentFolder.id

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


    class AsyncSaveTask(val activity: MainActivity, val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            handler()
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
        }
    }

}