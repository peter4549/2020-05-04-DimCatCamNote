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
import com.elliot.kim.kotlin.dimcatcamnote.R
import java.io.File


/** Fragment used for each individual page showing a photo inside of [GalleryFragment] */
class PhotoFragment internal constructor() : Fragment() {

    private lateinit var imageView: ImageView
    // 이후 전달하는 방식으로 변환할 것.
    var uri: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_photo, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageView = view.findViewById<ImageView>(R.id.image_view)
        val resource = uri?.let { File(it) } ?: R.drawable.check_mark
        Glide.with(imageView.context).load(Uri.parse(uri)).into(imageView)
    }

    // navigation 패러다임으로 전환 시 아래 내용 onViewCreated에서 처리하도록 할 것.
    override fun onResume() {
        super.onResume()



    }

    companion object {
        private const val FILE_NAME_KEY = "file_name"

        fun create(image: File) = PhotoFragment().apply {
            arguments = Bundle().apply {
                putString(FILE_NAME_KEY, image.absolutePath)
            }
        }
    }
}