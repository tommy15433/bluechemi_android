package com.bluechemi.application.ViewDiary

import java.io.Serializable

class DiaryItem(
    val date: String,
    val address: String
) : Serializable{
    private val mLogs: MutableList<DiaryLog> = mutableListOf()

    val diaryLogs: List<DiaryLog>
        get() = mLogs

    val count: Int
        get() = mLogs.count()

    val diarySubject: DiarySubject
        get() = DiarySubject(date, address, mLogs.count())

    fun addLog(log: DiaryLog){
        mLogs.add(log)
    }
    fun delete(log: DiaryLog){
        mLogs.remove(log)
    }
    fun removeAt(idx: Int){
        if (mLogs.count() > idx){
            mLogs.removeAt(idx)
        }
    }
}

class DiarySubject(
    val date: String,
    val address: String,
    var bitecount: Int
) : Serializable{
}

class DiaryLog(
    val time: String,
    val weather: String,
    val waveheight: String,
    val message: String
) : Serializable{}