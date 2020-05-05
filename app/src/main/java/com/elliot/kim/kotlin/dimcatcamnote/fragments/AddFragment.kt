package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.elliot.kim.kotlin.dimcatcamnote.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.Note
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.databinding.FragmentAddBinding

class AddFragment : Fragment() {

    private lateinit var binding: FragmentAddBinding
    private lateinit var content: String
    private lateinit var title: String

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
    }

    override fun onResume() {
        super.onResume()

        MainActivity.isFragment = true
        MainActivity.isAddFragment = true

        binding.editTextTitle.text = null
        binding.editTextContent.text = null
    }

    override fun onStop() {
        super.onStop()

        MainActivity.isFragment = false
        MainActivity.isAddFragment = false

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

    private fun isEmpty() = title == "" && content == ""

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
        if (title == "") title = if (content.length > 16) content.substring(0, 16)
        else content

        val note = Note(
            title,
            MainActivity.getCurrentTime(),
            null
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
                SAVE -> { save()
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

    companion object {
        const val BACK_PRESSED = 0
        const val SAVE = 1
    }
}