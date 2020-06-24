package com.elliot.kim.kotlin.dimcatcamnote

import android.content.Intent
import android.hardware.Camera
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.fragments.WriteFragment
import com.otaliastudios.cameraview.*
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.gesture.Gesture
import com.otaliastudios.cameraview.gesture.GestureAction
import kotlinx.android.synthetic.main.fragment_camera_view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraViewFragment : Fragment(), FileCallback {

    private lateinit var cameraView: CameraView
    private lateinit var outputDirectory: File

    private var uri: Uri? = null
    private var previousUri: Uri? = null

    fun setPreviousUri(uri: String?) {
        previousUri = if (uri == null) null
        else Uri.parse(uri)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        outputDirectory = MainActivity.getOutputDirectory(requireContext())

        return inflater.inflate(R.layout.fragment_camera_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraView = camera_view
        cameraView.setLifecycleOwner(viewLifecycleOwner)
        cameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM)
        cameraView.mapGesture(Gesture.TAP, GestureAction.AUTO_FOCUS)
        cameraView.mapGesture(Gesture.LONG_TAP, GestureAction.TAKE_PICTURE)

        camera_capture_button.setOnClickListener {
            cameraView.takePicture()
        }

        camera_switch_button.setOnClickListener {
            cameraView.facing =
                if (cameraView.facing == Facing.BACK)
                    Facing.FRONT
                else
                    Facing.BACK
        }

        cameraView.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                val photoFile =
                    createFile(
                        outputDirectory,
                        FILENAME,
                        PHOTO_EXTENSION
                    )

                result.toFile(photoFile, this@CameraViewFragment)

                (activity as MainActivity).fragmentManager.popBackStack()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        uri = null
        (activity as MainActivity).setCurrentFragment(CurrentFragment.CAMERA_FRAGMENT)
    }

    override fun onDestroyView() {
        if (uri != null)
            (activity as MainActivity).writeFragment.uri = uri.toString()

        previousUri = null

        val message = (activity as MainActivity).writeFragment.handler.obtainMessage()
        message.what = WriteFragment.SHOW_BOTTOM_NAVIGATION_VIEW
        (activity as MainActivity).writeFragment.handler
            .sendMessage(message)
        (activity as MainActivity).setCurrentFragment(CurrentFragment.WRITE_FRAGMENT)

        super.onDestroyView()
    }

    override fun onFileReady(file: File?) {
        if (file == null)
            return

        val savedUri = Uri.fromFile(file)
        uri = savedUri

        // Implicit broadcasts will be ignored for devices running API level >= 24
        // so if you only target API level 24+ you can remove this statement
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            requireActivity().sendBroadcast(
                Intent(Camera.ACTION_NEW_PICTURE, savedUri)
            )
        }

        // If the folder selected is an external media directory, this is
        // unnecessary but otherwise other apps will not be able to access our
        // images unless we scan them using [MediaScannerConnection]
        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(savedUri.toFile().extension)
        MediaScannerConnection.scanFile(
            context,
            arrayOf(savedUri.toFile().absolutePath),
            arrayOf(mimeType)
        ) { _, uri ->
            Log.d(TAG, "Image capture scanned into media store: $uri")
        }
    }

    companion object {
        private const val TAG = "CAMERA_VIEW_FRAGMENT"
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        const val KEY_URI = "key_uri"
        const val KEY_ROOT_DIRECTORY = "key_root_directory"

        var isFromWriteFragment = false

        /** Helper function used to create a timestamped file */
        private fun createFile(baseFolder: File, format: String, extension: String) =
            File(baseFolder, SimpleDateFormat(format, Locale.getDefault())
                .format(System.currentTimeMillis()) + extension)
    }
}