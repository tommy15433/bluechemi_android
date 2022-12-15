package com.bluechemi.application

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import java.lang.Exception

class Locations(val context: Context) {

    interface Listener {
        fun onLocationUpdated(location: LocationResult)
        fun onAddressUpdated(result: LocationsCallback)
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

        val INTERVAL: Long = 10000
        val FASTESTINTERVAL: Long = 5000
        val PRIORITY: Int = Priority.PRIORITY_HIGH_ACCURACY

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

    fun getCurrentAddress(context: Context){
        fusedLocations.getCurrentLocation(PRIORITY, object : CancellationToken(){
            override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken {
                // cancel
                return CancellationTokenSource().token
            }

            override fun isCancellationRequested(): Boolean {
                // canceled
                return false
            }
        })
            .addOnSuccessListener {
                Log.i("Locations", "on get current location success")
                object : Thread(){
                    override fun run() {
                        val geocoder: Geocoder = Geocoder(context)
                        val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 10)

                        Log.i("GPS","lat:${it.latitude} lon:${it.longitude}")
                        addresses.forEach {
                            Log.i("Addresses", it.getAddressLine(0).toString())
                        }
                        addresses.get(0)?.let{ addr ->
                        mListener?.onAddressUpdated(LocationsCallback(
                            addr.getAddressLine(0).toString(),
                            it.latitude,
                            it.longitude
                            ))
                        }
                    }
                }.start()
            }
            .addOnFailureListener {
                Log.i("Locations", "on get current location failed")
                Toast.makeText(context, "failed to request location", Toast.LENGTH_SHORT).show()
            }

    }
}
