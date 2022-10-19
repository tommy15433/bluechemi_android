package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.example.myapplication.AppSettings.Settings
import com.example.myapplication.comm.Ble
import com.example.myapplication.weatherApi.ForecastParser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class SplashScreenActivity : AppCompatActivity() {
    val TAG = "SplashScreen"

    val minDelayMs: Long = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()

        // update application uuid only once
        val spref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        val appUuid = spref.getString(getString(R.string.app_uuid), "")
        if(appUuid.isNullOrEmpty()){
            Settings.APP_UUID = UUID.randomUUID().toString()
            with(spref.edit()){
                putString(getString(R.string.app_uuid), Settings.APP_UUID)
                apply()
            }
        }else{
            Settings.APP_UUID = appUuid
        }


        Firebase.firestore.collection(getString(R.string.db_root))
            .document(Settings.APP_UUID)
            .collection(getString(R.string.db_doc_setting))
            .get(Source.CACHE)
            .addOnSuccessListener {
                it.documents.forEach {
                    val brightness =
                        it.data?.getOrDefault(getString(R.string.db_brightness), 128) ?: 128
                    val sensitivity =
                        it.data?.getOrDefault(getString(R.string.db_sensitivity), 128) ?: 128
                    val name = it.data?.getOrDefault(
                        getString(R.string.db_username),
                        getString(R.string.db_username_default)
                    ) ?: getString(R.string.db_username_default)
                    val uuid = it.id
                    Settings.deviceHashMap.put(
                        uuid,
                        hashMapOf(
                            getString(R.string.db_username) to name,
                            getString(R.string.db_brightness) to brightness,
                            getString(R.string.db_sensitivity) to sensitivity
                        )
                    )
                }
            }
            .addOnFailureListener {  }

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