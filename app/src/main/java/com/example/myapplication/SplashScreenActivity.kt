package com.example.myapplication

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AlertDialog
import com.example.myapplication.comm.Ble
import com.example.myapplication.weatherApi.ForecastParser

class SplashScreenActivity : AppCompatActivity() {
    val TAG = "SplashScreen"

    val minDelayMs: Long = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()

        // permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (Build.VERSION.SDK_INT >= 31){
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.BLUETOOTH,
                        android.Manifest.permission.BLUETOOTH_CONNECT,
                        android.Manifest.permission.BLUETOOTH_SCAN,
                        android.Manifest.permission.BLUETOOTH_ADVERTISE),
                    2
                )
            }else{
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.BLUETOOTH),
                    2
                )
            }
        } else{
            // no need for permissions if android version lower than M
            // they give permission by force if needed
            Handler().postDelayed({
                val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }, minDelayMs)
        }

        Ble.initInstance(applicationContext)
        Locations.initInstance(applicationContext)
        Locations.instance?.startRequest()
        ForecastParser.parse(37.4791657, 127.1414918)

        if (!Ble.instance.isEnabled){
            AlertDialog.Builder(this)
                .setTitle("Need Bluetooth")
                .setMessage("Enable Blutoooth")
                .setPositiveButton("exit"){_, _ ->
                    finish()
                }.show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            2 -> {
                if (grantResults.size > 0)
                {
                    grantResults.forEach {
                        if (it != PackageManager.PERMISSION_GRANTED){
                            finish()
                        }
                    }
                    Handler().postDelayed({
                        val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }, minDelayMs)
                }
                else{
                    finish()
                }
            }
        }
    }
}