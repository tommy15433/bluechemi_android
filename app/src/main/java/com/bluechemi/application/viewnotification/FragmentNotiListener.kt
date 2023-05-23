package com.bluechemi.application.viewnotification

interface FragmentNotiListener {
    fun onSubmit(item: NotificationItem)
    fun onRemove(item: NotificationItem)
    fun onStartCapture(item: NotificationItem)
}