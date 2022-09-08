package com.example.myapplication.ViewDiary

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.viewnotification.NotificationItem
import com.example.myapplication.utils.compareNotiDiary

class DiarySubjectViewModel: ViewModel() {

    private var mItems: ArrayList<DiaryItem> = arrayListOf()
    private val mItemsLive: MutableLiveData<ArrayList<DiaryItem>> = MutableLiveData()

    val diaries
        get() = mItemsLive


    fun addNoti(notiItem: NotificationItem){

        Log.i("DiarySubjectViewModel", "addNoti")

        mItems.forEach{ diaryItem ->
            if (compareNotiDiary(notiItem, diaryItem)){
                diaryItem.addLog(DiaryLog(
                    notiItem.time,
                    notiItem.weather,
                    notiItem.waveHeight,
                    notiItem.message
                ))

                return
            }
        }

        // when not found
        mItems.add(DiaryItem(notiItem.date, notiItem.address))
        mItems.last().addLog(DiaryLog(
            notiItem.time,
            notiItem.weather,
            notiItem.waveHeight,
            notiItem.message))

        mItemsLive.value = mItems
    }
}