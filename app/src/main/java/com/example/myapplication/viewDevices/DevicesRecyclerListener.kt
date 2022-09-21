package com.example.myapplication.viewDevices

interface DevicesRecyclerListener {
    fun onDeviceChangeConnection(uid: String)
    fun onDeviceTogglePlay(uid: String)
    fun onDeviceChangeName(uid: String, name: String)
    fun onDeviceSetting(uid: String)
    fun onDeviceLightBulb(uid: String)
}