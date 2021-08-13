package com.yunwoon.targetproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val window = window
        window.setFlags( // full screen setting
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val handler = Handler()
        handler.postDelayed( {
            val intent = Intent( this, MainActivity::class.java)
            startActivity(intent) },
            3500) // 3.5초 후 종료
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}