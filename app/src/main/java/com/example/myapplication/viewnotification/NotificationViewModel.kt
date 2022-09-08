package com.example.myapplication.viewnotification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotificationViewModel : ViewModel() {
    private val mList: ArrayList<NotificationItem> = arrayListOf()
    private val mUnreadMessages: MutableLiveData<ArrayList<NotificationItem>> = MutableLiveData()
    val unreadMessages: LiveData<ArrayList<NotificationItem>>
        get() = mUnreadMessages

    val unreadMessageCount: Int
        get() = mList.count()

    private fun update(){
        mUnreadMessages.value = mList
    }
    fun removeAt(idx: Int){
        mList.removeAt(idx)
        update()
    }
    fun add(item: NotificationItem){
        mList.add(item)
        update()
    }
    fun removeAll(){
        mList.clear()
        update()
    }

}