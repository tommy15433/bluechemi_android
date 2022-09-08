package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.awaitCancellation
import java.lang.Exception
import java.util.concurrent.Executor
import kotlin.math.log

class Locations(val context: Context) {

    interface Listener {
        fun onLocationUpdated(location: LocationResult)
    }

    var mListener: Listener? = null
    fun setListener(listener: Listener){
        mListener = listener
    }

    companion object{
        private var mInstance: Locations? = null
        val instance: Locations?
            get() = mInstance

        var interval: Long = 10000
        var fastestInterval: Long = 5000


        fun initInstance(context: Context){
            mInstance = Locations(context)
        }
    }

    private var mLocationResult: LocationResult? = null
    val locationResult: LocationResult?
        get() = mLocationResult

    private var mAddressResult: Address? = null
    val addressResult: Address?
        get() = mAddressResult

    val locationCallback: LocationCallback = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            Log.i("Locations", "location updated")

            mLocationResult = p0
            mLocationResult?.also {
                mListener?.onLocationUpdated(it)
            }

        }
    }


    private val fusedLocations: FusedLocationProviderClient by lazy{
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun startRequest(){
        val locationRequest: LocationRequest? = LocationRequest.create()?.also {
            it.interval = interval
            it.fastestInterval = fastestInterval
            it.priority = Priority.PRIORITY_HIGH_ACCURACY
        }
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("Locations", "location permission required")
            return
        }

        locationRequest?.let {
            fusedLocations.requestLocationUpdates(it, locationCallback, Looper.getMainLooper())
        }

    }
    fun stopRequest(){
        fusedLocations.removeLocationUpdates(locationCallback)
    }

    fun changeInterval(intv: Long){
        stopRequest()
        interval = intv
        fastestInterval = intv/2
        startRequest()

    }


    fun updateAddress(){
        object : Thread() {
            override fun run() {

                Log.i("Geothread", "run")

                val geocoder: Geocoder = Geocoder(context)

                mLocationResult?.let {
                    try {
                        val aList = geocoder.getFromLocation(it.lastLocation?.latitude ?: 0.0, it.lastLocation?.longitude ?: 0.0, 1)
                        aList.get(0)?.let {
                            mAddressResult = it
                            Log.i("geothread", it.toString())
                        }
                    }catch (E: Exception){
                        Log.i("geothread", E.message?: "null")
                    }

                }
            }
        }.start()
    }
}