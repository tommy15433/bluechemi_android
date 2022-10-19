package com.example.myapplication.viewnotification

interface NotificationRecyclerListener {
    fun onNotiSubmit(item: NotificationItem)
    fun onNotiDelete(item: NotificationItem)
}