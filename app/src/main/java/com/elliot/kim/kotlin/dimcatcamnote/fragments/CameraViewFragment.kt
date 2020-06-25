package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import com.elliot.kim.kotlin.dimcatcamnote.CurrentFragment
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.SingleNoteConfigureActivity
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
            }
        })
    }

    override fun onResume() {
        super.onResume()
        uri = null
        if (activity is MainActivity)
            (activity as MainActivity).setCurrentFragment(CurrentFragment.CAMERA_FRAGMENT)
        if (activity is SingleNoteConfigureActivity)
            (activity as SingleNoteConfigureActivity).setCurrentFragment(CurrentFragment.CAMERA_FRAGMENT)
    }

    fun reopenCamera() {
        cameraView.close()
        cameraView.open()
    }

    override fun onFileReady(file: File?) {
        if (file == null)
            return

        val savedUri = Uri.fromFile(file)
        uri = savedUri

        if (activity is MainActivity) {
            (activity as MainActivity).writeFragment.uri = uri.toString()
            (activity as MainActivity).deleteFileFromUri(previousUri)
            previousUri = null
            (activity as MainActivity).onBackPressed()
        } else if (activity is SingleNoteConfigureActivity) {
            (activity as SingleNoteConfigureActivity).writeFragment.uri = uri.toString()
            (activity as SingleNoteConfigureActivity).deleteFileFromUri(previousUri)
            previousUri = null
            (activity as SingleNoteConfigureActivity).onBackPressed()
        }



        // Implicit broadcasts will be ignored for devices running API level >= 24
        // so if you only target API level 24+ you can remove this statement

        /*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            requireActivity().sendBroadcast(
                Intent(Camera.ACTION_NEW_PICTURE, savedUri)
            )
        }
         */

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

    override fun onDestroyView() {
        if (activity is MainActivity) {
            val message = (activity as MainActivity).writeFragment.handler.obtainMessage()
            message.what = WriteFragment.SHOW_BOTTOM_NAVIGATION_VIEW
            (activity as MainActivity).writeFragment.handler.sendMessage(message)
            (activity as MainActivity).setCurrentFragment(CurrentFragment.WRITE_FRAGMENT)
        } else if (activity is SingleNoteConfigureActivity) {
            val message = (activity as SingleNoteConfigureActivity).writeFragment.handler.obtainMessage()
            message.what = WriteFragment.SHOW_BOTTOM_NAVIGATION_VIEW
            (activity as SingleNoteConfigureActivity).writeFragment.handler.sendMessage(message)
            (activity as SingleNoteConfigureActivity).setCurrentFragment(CurrentFragment.WRITE_FRAGMENT)
        }

        super.onDestroyView()
    }

    companion object {
        private const val TAG = "CAMERA_VIEW_FRAGMENT"
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"

        /** Helper function used to create a timestamped file */
        private fun createFile(baseFolder: File, format: String, extension: String) =
            File(baseFolder, SimpleDateFormat(format, Locale.getDefault())
                .format(System.currentTimeMillis()) + extension)
    }
}