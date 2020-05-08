package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.DialogInterface
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.elliot.kim.kotlin.dimcatcamnote.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.Note
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.databinding.FragmentAddBinding

class AddFragment : Fragment() {

    private lateinit var binding: FragmentAddBinding
    private lateinit var content: String
    private lateinit var title: String

    private var uri: String? = null

    private var shortAnimationDuration = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_add, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAddBinding.bind(view)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolBar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.title = "새 노트"

        setHasOptionsMenu(true)
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

        binding.editTextContent.viewTreeObserver.addOnGlobalLayoutListener {
            if(keyboardShown(binding.editTextContent.rootView))
                crossFade(false)
            else
                showImage()
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

    override fun onResume() {
        super.onResume()

        MainActivity.isFragment = true
        MainActivity.isAddFragment = true

        if (isFromCameraFragment) showImage()
        else binding.imageView.visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()

        if (isFromCameraFragment) isFromCameraFragment = false
        else MainActivity.isFragment = false

        MainActivity.isAddFragment = false

        (activity as MainActivity).hideKeyboard()
        (activity as MainActivity).showFloatingActionButton()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_add, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish(BACK_PRESSED)
            R.id.save -> finish(
                SAVE
            )
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setTitleContent() {
        title = binding.editTextTitle.text.toString()
        content = binding.editTextContent.text.toString()
    }

    private fun showImage() {
        uri = getUri()
        if(uri == "")
            return
        else {
            crossFade(true)
            Glide.with(this)
                .load(Uri.parse(uri))
                .into(binding.imageView)
        }
    }

    private fun getUri(): String? {
        return if (arguments != null) arguments?.getString(CameraFragment.KEY_URI)
        else ""
    }

    private fun isEmpty() = title == "" && content == "" && uri == null

    private fun showCheckMessage() {
        val builder = context?.let { AlertDialog.Builder(it) }
        builder?.setTitle("노트 저장")
        builder?.setMessage("지금까지 작성한 내용을 저장하시겠습니까?")
        builder?.setPositiveButton("저장") { _: DialogInterface?, _: Int ->
            save()
            activity?.supportFragmentManager?.popBackStack()
        }
        builder?.setNeutralButton("계속쓰기") { _: DialogInterface?, _: Int -> }
        builder?.setNegativeButton("아니요") { _: DialogInterface?, _: Int ->
            finishWithoutSaving()
        }
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
                SAVE -> {
                    save()
                    activity?.supportFragmentManager?.popBackStack()
                }
                else -> showCheckMessage()
            }
        }
    }

    private fun finishWithoutSaving() {
        Toast.makeText(context, "저장되지 않았습니다.", Toast.LENGTH_SHORT).show()
        activity?.supportFragmentManager?.popBackStack()
    }

    private fun crossFade(fadeIn: Boolean) {
        if (fadeIn) {
            binding.imageView.apply {
                alpha = 0f
                visibility = View.VISIBLE

                animate()
                    .alpha(1f)
                    .setDuration(shortAnimationDuration.toLong())
                    .setListener(null)
            }
        } else {
            binding.imageView.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.imageView.visibility = View.GONE
                    }
                })
        }
    }

    companion object {
        const val BACK_PRESSED = 0
        const val SAVE = 1

        var isFromCameraFragment = false
    }
}