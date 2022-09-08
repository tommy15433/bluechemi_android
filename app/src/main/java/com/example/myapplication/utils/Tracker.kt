package com.example.myapplication.utils

import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.LocationResult
import java.util.*
import kotlin.coroutines.cancellation.CancellationException

object Tracker {

    interface listener{
        fun onTraveledFar(distance: Double)
    }

    private var mListener: listener? = null
    fun setListener(listener: listener){
        mListener = listener
    }

    private var mSize = 10
    private val mCheckSeconds = 60

    private var list: MutableList<db> = mutableListOf()

    fun add(location: LocationResult){

        location.lastLocation?.let {
            if (it.longitude != null && it.latitude != null){
                list.add(db(xy(it.latitude, it.longitude), Calendar.getInstance().timeInMillis) )

                checkDistance()
            }
        }

    }

    fun checkDistance(){
        if (list.size > 1){
            val start: xy = list.get(0).location
            val end: xy = list.get(1).location

            val vec = xy.vector(start, end)
            Log.i("Tracker Distanse", vec.toString())

            mListener?.let {
                it.onTraveledFar(vec)
            }
            list.removeAt(0)
        }

    }

    internal class db(val location: xy, val timeMs: Long)
    internal class xy(val x: Double, val y: Double){
        companion object{
            fun vector(p1: xy, p2: xy) = Math.sqrt(Math.pow(Math.abs(p1.x - p2.x), 2.0) + Math.pow(Math.abs(p1.y - p2.y), 2.0))
        }

        override fun toString(): String {
            return "x:${x}, y:${y}"
        }
    }
}