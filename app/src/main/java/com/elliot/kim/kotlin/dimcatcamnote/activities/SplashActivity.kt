package com.elliot.kim.kotlin.dimcatcamnote.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, MainActivity::class.java)
        // 여기서 로드 작업..
        // 데이터 어떻게 던지는가...
        // 어레이.. 어떻게 던지지.
        //
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())

        finish()
    }
}