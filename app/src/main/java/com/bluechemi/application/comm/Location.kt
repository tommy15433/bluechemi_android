package com.bluechemi.application.comm

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

class Location(val context: Activity) {

    var Latitude = 0.0
    var Longitude = 0.0

    private val REQUESTCODE = 2
    private val manager : LocationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    fun requestPermission(){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED    ){

            ActivityCompat.requestPermissions(
                context,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                REQUESTCODE)
        }
    }
    fun initialize(){
        requestPermission()

        val gpsLocationListener : LocationListener = object : LocationListener{
            override fun onLocationChanged(location: Location) {
                Latitude = location.latitude
                Longitude = location.longitude
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun startLocationUpdate(){

    }
}
