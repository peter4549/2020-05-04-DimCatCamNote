package com.elliot.kim.kotlin.dimcatcamnote.fragments

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.elliot.kim.kotlin.dimcatcamnote.CurrentFragment
import com.elliot.kim.kotlin.dimcatcamnote.R
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity


class VersionAndLicenseFragment : Fragment() {

    private lateinit var configureFragment: ConfigureFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get application version
        var version = "버전 정보를 읽어오지 못했습니다."
        try {
            val pInfo: PackageInfo =
                requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            version = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        val view = inflater.inflate(R.layout.fragment_licences, container, false)
        view.findViewById<TextView>(R.id.text_view_version).text = version

        return view
    }


    fun setConfigureFragment(configureFragment: ConfigureFragment) {
        this.configureFragment = configureFragment
    }

    override fun onResume() {
        super.onResume()
        MainActivity.currentFragment = CurrentFragment.LICENSE_FRAGMENT
    }

    override fun onDestroyView() {
        configureFragment.setCurrentFragment()
        super.onDestroyView()
    }

    interface OnFragmentFinished {
        fun setCurrentFragment()
    }
}