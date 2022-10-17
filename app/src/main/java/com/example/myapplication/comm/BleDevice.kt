package com.example.myapplication.comm

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanResult
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("MissingPermission")
class BleDevice {
    enum class Status{
        connecting, connected, disconnecting, disconnected
    }
    var status: Status = Status.disconnected

    var scan: ScanResult? = null
    var gatt: BluetoothGatt? = null
    val services: ArrayList<BluetoothGattService> = arrayListOf()
    val characteristics: ArrayList<BluetoothGattCharacteristic> = arrayListOf()

    val uid: String?
        get() = scan?.device?.address

    val name: String?
        get() = scan?.device?.name

}

enum class operation {
    connect, disconnect, read, write
}