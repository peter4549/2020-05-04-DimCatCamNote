package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.elliot.kim.kotlin.dimcatcamnote.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.MainActivity.Companion.CAMERA_PERMISSIONS_REQUEST_CODE
import com.elliot.kim.kotlin.dimcatcamnote.MainActivity.Companion.RECORD_AUDIO_PERMISSIONS_REQUEST_CODE
import com.elliot.kim.kotlin.dimcatcamnote.Note
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.databinding.FragmentWriteBinding

class WriteFragment : Fragment() {
    private lateinit var binding: FragmentWriteBinding
    lateinit var handler: Handler
    private var shortAnimationDuration = 0

    private lateinit var title: String
    private lateinit var content: String
    var uri: String? = null

    private lateinit var intent: Intent
    private lateinit var recognizer: SpeechRecognizer
    private var previousPartialText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, requireActivity().packageName)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the magic word")
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 100)
        recognizer = SpeechRecognizer.createSpeechRecognizer(context)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_write, container, false)

    override fun onResume() {
        super.onResume()

        clear()
        (activity as MainActivity).setCurrentFragment(MainActivity.CurrentFragment.WRITE_FRAGMENT)
    }

    override fun onStop() {
        super.onStop()

        uri = null

        (activity as MainActivity).setCurrentFragment(null)
        (activity as MainActivity).showFloatingActionButton()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentWriteBinding.bind(view)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolBar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.title = "새 노트"
        setHasOptionsMenu(true)

        binding.imageView.visibility = View.GONE
        binding.imageView.setOnClickListener { startPhotoFragment() }

        binding.editTextContent.viewTreeObserver.addOnGlobalLayoutListener {
            if (keyboardShown(binding.editTextContent.rootView)) crossFade(false)
            else showImage()
        }

        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.camera -> {
                    if(uri == null) {
                        if (MainActivity.hasCameraPermissions(requireContext()))
                            startCameraFragment()
                        else
                            requestPermissions(MainActivity.CAMERA_PERMISSIONS_REQUIRED,
                                CAMERA_PERMISSIONS_REQUEST_CODE)
                    }
                    else showPictureChangeMessage()
                }
                R.id.mic -> {
                    if (MainActivity.hasCameraPermissions(requireContext()))
                        startSpeechRecognition()
                    else
                        requestPermissions(MainActivity.RECORD_AUDIO_PERMISSIONS_REQUESTED,
                            RECORD_AUDIO_PERMISSIONS_REQUEST_CODE)

                    it.isChecked = false
                }
            }
            return@setOnNavigationItemSelectedListener true
        }

        handler = Handler {
            when(it.what) {
                SHOW_BOTTOM_NAVIGATION_VIEW ->
                    binding.bottomNavigationView.visibility = View.VISIBLE
            }
            true
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        menu.clear()
        inflater.inflate(R.menu.menu_write, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        MainActivity.hideKeyboard(context, view)

        when (item.itemId) {
            android.R.id.home -> finish(BACK_PRESSED)
            R.id.save -> finish(SAVE)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setTitleContent() {
        title = binding.editTextTitle.text.toString()
        content = binding.editTextContent.text.toString()
    }

    private fun showImage() {
        if(uri == null) return
        else {
            crossFade(true)
            Glide.with(binding.imageView.context)
                .load(Uri.parse(uri))
                .into(binding.imageView)
        }
    }

    private fun startCameraFragment() {
        binding.bottomNavigationView.visibility = View.GONE

        (activity as MainActivity).fragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_up, R.anim.slide_down, R.anim.slide_down)
            .replace(R.id.add_container, (activity as MainActivity).cameraFragment).commit()
    }

    private fun startSpeechRecognition() {
        recognizer.setRecognitionListener(recognitionListener)
        recognizer.startListening(intent)
    }

    private fun startPhotoFragment() {
        (activity as MainActivity).photoFragment.uri = uri
        (activity as MainActivity).fragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(R.anim.slide_up, R.anim.slide_up, R.anim.slide_down, R.anim.slide_down)
            .replace(R.id.add_container, (activity as MainActivity).photoFragment).commit()
    }

    private fun isEmpty() = title == "" && content == "" && uri == null

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
        if (title == "" && content == "")
            title = MainActivity.getCurrentTime().toString()
        else {
            if (title == "") title = if (content.length > 12) content.substring(0, 12)
            else content
        }

        val note = Note(
            title,
            MainActivity.getCurrentTime(),
            uri
        )
        note.content = content

        (activity as MainActivity).viewModel.insert(note)

        Toast.makeText(context, "저장되었습니다.", Toast.LENGTH_SHORT).show()
    }

    fun finish(save: Int) {
        setTitleContent()
        if (isEmpty()) finishWithoutSaving()
        else {
            when (save) {
                SAVE -> finishWithSaving()
                BACK_PRESSED -> showCheckMessage()
            }
        }
    }

    private fun finishWithSaving() {
        save()
        (activity as MainActivity).backPressed()
    }

    private fun finishWithoutSaving() {
        Toast.makeText(context, "저장되지 않았습니다.", Toast.LENGTH_SHORT).show()
        (activity as MainActivity).backPressed()
    }

    private fun crossFade(fadeIn: Boolean) {
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

    private fun clear() {
        binding.editTextTitle.text = null
        binding.editTextContent.text = null
    }

    private val recognitionListener = object: RecognitionListener {


        override fun onReadyForSpeech(params: Bundle?) {
            previousPartialText = ""
        }

        override fun onRmsChanged(rmsdB: Float) {

        }

        override fun onBufferReceived(buffer: ByteArray?) {

        }

        override fun onPartialResults(partialResults: Bundle?) {

            val results: List<String>? =
                partialResults!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (results != null) {
                val text = results[0]
                if (text != previousPartialText) {
                    Log.d("PARTIAL", text)
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

        }

        override fun onResults(results: Bundle?) {

        }
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

    companion object {
        const val SHOW_BOTTOM_NAVIGATION_VIEW = 0
        const val BACK_PRESSED = 0
        const val SAVE = 1
    }
}