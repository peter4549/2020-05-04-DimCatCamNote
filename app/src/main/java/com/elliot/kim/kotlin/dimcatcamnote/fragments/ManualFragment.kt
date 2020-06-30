package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.elliot.kim.kotlin.dimcatcamnote.CurrentFragment
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity

class ManualFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manual, container, false)
        view.findViewById<TextView>(R.id.text_view_manual).movementMethod = ScrollingMovementMethod()
        return view
    }

    override fun onResume() {
        super.onResume()
        MainActivity.currentFragment = CurrentFragment.MANUAL_FRAGMENT
    }

    override fun onDestroyView() {
        MainActivity.currentFragment = null
        super.onDestroyView()
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {

        val animation = AnimationUtils.loadAnimation(activity, nextAnim)

        animation!!.setAnimationListener( object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                (requireActivity() as MainActivity).closeDrawer()
            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })

        return animation
    }
}