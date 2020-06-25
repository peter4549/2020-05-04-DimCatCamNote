package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.elliot.kim.kotlin.dimcatcamnote.CurrentFragment
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.EditActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.activities.SingleNoteConfigureActivity
import com.github.chrisbanes.photoview.PhotoView

class PhotoFragment : Fragment() {

    private lateinit var photoView: PhotoView
    private lateinit var fragment: Any
    private lateinit var uri: String

    fun setParameters(fragment: Any, uri: String) {
        this.fragment = fragment
        this.uri = uri
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_photo, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoView = view.findViewById(R.id.photo_view)
        Glide.with(photoView.context)
            .load(Uri.parse(uri))
            .error(R.drawable.ic_sentiment_dissatisfied_grey_24dp)
            .transition(DrawableTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(photoView)
    }

    override fun onResume() {
        super.onResume()
        if (activity is MainActivity)
            (activity as MainActivity).setCurrentFragment(CurrentFragment.PHOTO_FRAGMENT)
        else if (activity is SingleNoteConfigureActivity)
            (activity as SingleNoteConfigureActivity).setCurrentFragment(CurrentFragment.PHOTO_FRAGMENT)
    }

    override fun onDestroyView() {
        when (fragment) {
            is EditFragment -> (activity as MainActivity).setCurrentFragment(CurrentFragment.EDIT_FRAGMENT)
            is WriteFragment -> {
                val message = (fragment as WriteFragment).handler.obtainMessage()
                message.what = WriteFragment.SHOW_BOTTOM_NAVIGATION_VIEW
                (fragment as WriteFragment).handler.sendMessage(message)
                if (activity is MainActivity)
                    (activity as MainActivity).setCurrentFragment(CurrentFragment.WRITE_FRAGMENT)
                else if (activity is SingleNoteConfigureActivity)
                    (activity as SingleNoteConfigureActivity)
                        .setCurrentFragment(CurrentFragment.WRITE_FRAGMENT)
            }
            is EditActivity -> {
                // PhotoFragment can also be executed from a EditActivity.
                // Blank
            }
        }
        super.onDestroyView()
    }
}
