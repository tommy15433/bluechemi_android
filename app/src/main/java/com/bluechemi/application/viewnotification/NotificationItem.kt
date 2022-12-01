package com.bluechemi.application.viewnotification

import com.bluechemi.application.utils.getRandomString

data class NotificationItem (
    val devUuid: String,
    val date: String,
    val time: String,
    val address: String,
    val weather: String,
    val waveHeight: String,
    var message: String = "blank")
        :Comparable<NotificationItem>{

    override fun toString(): String {
        return "${date} ${time} ${address} ${weather} ${waveHeight} ${message}"
    }

    companion object{
        fun makeRand(): NotificationItem{
            return NotificationItem(
                getRandomString(10),
                getRandomString(10),
                getRandomString(10),
                getRandomString(10),
                getRandomString(10),
                getRandomString(10),
                getRandomString(10),
            )
        }
    }

    override fun compareTo(other: NotificationItem): Int {
        return compareValuesBy(this, other,
            { it.devUuid },
            { it.date },
            { it.time }
        )
    }


}