package com.bluechemi.application.viewnotification

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
    fun remove(item: NotificationItem){
        val iter = mList.iterator()
        while (iter.hasNext()){
            val tmp = iter.next()
            if (tmp.compareTo(item) == 0){
                mList.remove(tmp)
                update()
                return
            }
        }
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