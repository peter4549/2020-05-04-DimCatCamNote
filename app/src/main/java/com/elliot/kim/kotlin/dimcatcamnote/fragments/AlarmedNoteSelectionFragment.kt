package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager

import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.adapters.AlarmedNoteAdapter
import com.elliot.kim.kotlin.dimcatcamnote.data.Note
import com.elliot.kim.kotlin.dimcatcamnote.databinding.FragmentAlarmedNoteSelectionBinding

/**
 * A simple [Fragment] subclass.
 */
class AlarmedNoteSelectionFragment(val alarmedNotes: ArrayList<Note>) : Fragment() {

    private lateinit var activity: MainActivity
    private lateinit var binding: FragmentAlarmedNoteSelectionBinding
    private lateinit var alarmedNoteAdapter: AlarmedNoteAdapter

    fun setNoteAdapter() {

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        activity = requireActivity() as MainActivity
        alarmedNoteAdapter = AlarmedNoteAdapter(activity, alarmedNotes)
        return inflater.inflate(R.layout.fragment_alarmed_note_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (alarmedNotes.isNotEmpty())
            Log.d("SHow passed note", alarmedNotes[0].title)

        binding = FragmentAlarmedNoteSelectionBinding.bind(view)

        binding.recyclerView.apply {
            setHasFixedSize(true)
            adapter = alarmedNoteAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

}
