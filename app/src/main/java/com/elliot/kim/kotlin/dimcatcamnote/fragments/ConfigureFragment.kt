package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.elliot.kim.kotlin.dimcatcamnote.CurrentFragment

import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity
import com.elliot.kim.kotlin.dimcatcamnote.databinding.FragmentConfigureBinding
import com.elliot.kim.kotlin.dimcatcamnote.dialog_fragments.DialogFragments

class ConfigureFragment : Fragment() {

    private lateinit var binding: FragmentConfigureBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_configure, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentConfigureBinding.bind(view)
        binding.toolbar.title = "환경설정"
        binding.toolbar.setBackgroundColor(MainActivity.toolbarColor)

        (activity as MainActivity).setSupportActionBar(binding.toolbar)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        binding.setThemeColorContainer.setOnClickListener {
            (activity as MainActivity).showDialogFragment(DialogFragments.SET_THEME_COLOR, binding.toolbar)
        }

        binding.setNoteColorContainer.setOnClickListener {
            (activity as MainActivity).showDialogFragment(DialogFragments.SET_NOTE_COLOR)
        }

        binding.setFontContainer.setOnClickListener {
            (activity as MainActivity).showDialogFragment(DialogFragments.SET_FONT, binding.toolbar)
        }
    }

    override fun onResume() {
        super.onResume()
        MainActivity.currentFragment = CurrentFragment.CONFIGURE_FRAGMENT
    }

    override fun onStop() {
        super.onStop()
        MainActivity.currentFragment = null
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {

        val animation = AnimationUtils.loadAnimation(activity, nextAnim)

        animation!!.setAnimationListener( object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                (activity as MainActivity).closeDrawer()
            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })

        return animation
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> (activity as MainActivity).onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
