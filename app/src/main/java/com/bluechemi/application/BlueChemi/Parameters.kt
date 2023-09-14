package com.bluechemi.application.BlueChemi

fun make(uuid16: String): String{
        return "0000$uuid16-0000-1000-8000-00805f9b34fb"
}

object BlueChemiParameters {

        val DEVICE_NAME = "FishingBobber_Demo"

        val SERVICE_UUID = "000000ee-0000-1000-8000-00805f9b34fb"

        val CUSTOM_SERVICE_UUID = make("00ff")
        val CUSTOM_BITE_UUID = make("ff01")
        val CUSTOM_LED_UUID = make("ff02")
        val CUSTOM_SENSE_UUID = make("ff03")
        val CUSTOM_PING_UUID = make("ff04")
        val CUSTOM_START_UUID = make("ff05")
        val CUSTOM_STOP_UUID = make("ff06")
        val CUSTOM_BAT_UUID = make("ff07")
        val CUSTOM_CUSTOM_UUID = make("ff08")
        val CUSTOM_STRINGMETER_UUID = make("ff09")
}
