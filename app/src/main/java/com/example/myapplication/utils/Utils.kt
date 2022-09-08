package com.example.myapplication.utils

import com.example.myapplication.ViewDiary.DiaryItem
import com.example.myapplication.viewnotification.NotificationItem
import com.google.android.gms.location.LocationResult
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*

fun getRandomString(length: Int) : String {
    val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..length)
        .map { charset.random() }
        .joinToString("")
}

fun toInt32(bytes: ByteArray, index: Int): Int {
    if (bytes == null){
        return 0
    }
    require(bytes.size == 4) { "length must be 4, got: ${bytes.size}" }
    return ByteBuffer.wrap(bytes, index, 4).order(ByteOrder.LITTLE_ENDIAN).int
}
fun toByteArray(value: Int): ByteArray{
    val tmp: ByteArray = ByteArray(4)
    tmp[0] = value.shr(24).toByte()
    tmp[0] = value.shr(16).toByte()
    tmp[0] = value.shr(0).toByte()
    tmp[0] = value.shr(0).toByte()

    return tmp
}

fun getCurrentTime(): String{
    val sdf = SimpleDateFormat("HH시 mm분 ss초", Locale.getDefault())
    return sdf.format(Date())

}
fun getCurrentDate(): String{
    val sdf = SimpleDateFormat("yyyy년MM월dd일", Locale.getDefault())
    return sdf.format(Date())
}

fun compareNotiDiary(noti: NotificationItem, diary: DiaryItem): Boolean{
    if (
        noti.date == diary.date &&
        noti.address == diary.address){

            return true
    }

    return false
}