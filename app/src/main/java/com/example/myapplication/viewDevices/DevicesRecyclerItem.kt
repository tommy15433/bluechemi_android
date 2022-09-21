package com.example.myapplication.viewDevices

/**
 * @param UID Unique ID to identify selected device. Use MAC address of the device
 *
 * */
class DevicesRecyclerItem (
    val UID: String,
    var Name: String,
    var Connection: Connection,
    var State: DevicesStateEnum,
    var Sensitivity: Int,
    var Brightness: Int,
    var Battery: Int,
        ){

    override fun toString(): String {
        return "$UID $Name $Connection $State"

    }
}