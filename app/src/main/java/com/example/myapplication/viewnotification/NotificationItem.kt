package com.example.myapplication.viewnotification

data class NotificationItem(
    val devUuid: String,
    val date: String,
    val time: String,
    val address: String,
    val weather: String,
    val waveHeight: String,
    var message: String = "blank") {

    override fun toString(): String {
        return "${date} ${time} ${address} ${weather} ${waveHeight} ${message}"
    }

}