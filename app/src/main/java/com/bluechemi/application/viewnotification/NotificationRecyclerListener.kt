package com.bluechemi.application.viewnotification

interface NotificationRecyclerListener {
    fun onNotiSubmit(item: NotificationItem)
    fun onNotiDelete(item: NotificationItem)
}