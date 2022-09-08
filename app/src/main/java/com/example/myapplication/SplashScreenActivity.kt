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
        else{
            if (!Ble.instance.isPermissionsGranted){
                AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("Allow Access Fine Location Permission?")
                    .setPositiveButton("yes"){_, _ ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(
                                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                                2)
                        }else{
                            // if below API22 or Android 5.1
                            // device permits automatically so no need to check
                        }
                    }.show()
            }
            else{
                Handler().postDelayed({
                    val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }, minDelayMs)
            }
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
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Handler().postDelayed({
                        val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }, minDelayMs)
                }else{
                    finish()
                }
            }
        }
    }
}