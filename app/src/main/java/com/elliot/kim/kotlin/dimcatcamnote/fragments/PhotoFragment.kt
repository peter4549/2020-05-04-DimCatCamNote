package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.elliot.kim.kotlin.dimcatcamnote.CurrentFragment
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.EditActivity
import com.github.chrisbanes.photoview.PhotoView

class PhotoFragment(private val fragment: Any, private val uri: String) : Fragment() {

    private lateinit var photoView: PhotoView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_photo, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoView = view.findViewById(R.id.photo_view)
        // val resource = uri.let { File(it) } ?: R.drawable.check_mark
        Glide.with(photoView.context).load(Uri.parse(uri)).into(photoView)
    }

    override fun onResume() {
        super.onResume()
        if (activity is MainActivity)
            (activity as MainActivity).setCurrentFragment(CurrentFragment.PHOTO_FRAGMENT)
    }

    override fun onStop() {
        when (fragment) {
            is EditFragment -> (activity as MainActivity).setCurrentFragment(CurrentFragment.EDIT_FRAGMENT)
            is WriteFragment -> {
                val message =
                    (activity as MainActivity).writeFragment.handler.obtainMessage()
                (activity as MainActivity).writeFragment.handler.sendMessage(message)
                (activity as MainActivity).setCurrentFragment(CurrentFragment.WRITE_FRAGMENT)
            }
            is EditActivity -> {
                // PhotoFragment can also be executed from a EditActivity.
                // Blank
            }
        }

        super.onStop()
    }
}