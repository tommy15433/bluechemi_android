package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class activity_splashTest : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_test)

        supportActionBar?.hide()

        Locations.initInstance(applicationContext)
        Locations.instance?.startRequest()

        Handler().postDelayed({
            val intent = Intent(this@activity_splashTest, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 100)
    }
}