package com.example.myapplication.viewnotification

interface FragmentNotiListener {
    fun onSubmit(item: NotificationItem)
    fun onRemove(item: NotificationItem)
}