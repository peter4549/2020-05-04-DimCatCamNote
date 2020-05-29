package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.elliot.kim.kotlin.dimcatcamnote.CurrentFragment
import com.elliot.kim.kotlin.dimcatcamnote.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.R
import java.io.File

class PhotoFragment(private val fragment: Any, private val uri: String) : Fragment() {

    private lateinit var activity: MainActivity
    private lateinit var imageView: ImageView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_photo, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity = requireActivity() as MainActivity
        imageView = view.findViewById<ImageView>(R.id.image_view)
        // val resource = uri.let { File(it) } ?: R.drawable.check_mark
        Glide.with(imageView.context).load(Uri.parse(uri)).into(imageView)
    }

    override fun onResume() {
        super.onResume()

        activity.setCurrentFragment(CurrentFragment.PHOTO_FRAGMENT)
    }

    override fun onStop() {
        when (fragment) {
            is EditFragment -> activity.setCurrentFragment(CurrentFragment.EDIT_FRAGMENT)
            is WriteFragment -> {
                val message = activity.writeFragment.handler.obtainMessage()
                activity.writeFragment.handler.sendMessage(message)
                activity.setCurrentFragment(CurrentFragment.WRITE_FRAGMENT)
            }
        }

        super.onStop()
    }

    companion object {
        /*
        private const val FILE_NAME_KEY = "file_name"

        fun create(image: File) = PhotoFragment().apply {
            arguments = Bundle().apply {
                putString(FILE_NAME_KEY, image.absolutePath)
            }
        }

         */
    }
}