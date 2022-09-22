package com.example.myapplication.BlueChemi

object BlueChemiIntentFilters {
    private fun make(filter: String): String = PREFIX + filter
    private val PREFIX = "com.example.myapplication."

    val ACTION_BRIGHTNESS_CHANGED = make("ACTION_BRIGHTNESS_CHANGED")
    val ACTION_BATTERY_CHANGED = make("ACTION_BATTERY_CHANGED")
    val ACTION_SENSITIVITY_CHANGED = make("ACTION_SENSITIVITY_CHANGED")
    val ACTION_BITE_DETECTED = make("ACTION_BITE_DETECTED")
    val ACTION_PLAYSTOP_CHANGED = make("ACTION_PLAYSTOP_CHANGED")
    val ACTION_PLAY = make("ACTION_PLAY")
    val ACTION_STOP = make("ACTION_STOP")

    val ACTION_SCAN_DEVICE_ADDED = make("ACTION_SCAN_DEVICE_ADDED")
    val ACTION_SCAN_DEVICE_REMOVED = make("ACTION_SCAN_DIVICE_REMOVED")
    val ACTION_GATT_CONNECTED = make("ACTION_GATT_CONNECTED")
    val ACTION_GATT_DISCONENCTED = make("ACTION_GATT_DISCONNECTED")
    val ACTION_CHAR_CHANGED = make("ACTION_CHAR_CHANGED")


    val EXTRA_ADDRESS = "address"
    val EXTRA_STRING = "string"
    val EXTRA_VALUE = "value"
}