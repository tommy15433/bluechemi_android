package com.bluechemi.application.viewDevices

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bluechemi.application.utils.*
import com.bluechemi.application.AppSettings.Settings
import com.bluechemi.application.comm.Ble
import com.bluechemi.application.BlueChemi.BlueChemiParameters

@SuppressLint("MissingPermission")
class DevicesViewModel: ViewModel() {

    val TAG = "DevicesViewModel"

    private val devicesLiveData = MutableLiveData<List<DevicesRecyclerItem>>()
    private val mDevices = ArrayList<DevicesRecyclerItem>()

    val devices: LiveData<List<DevicesRecyclerItem>>
        get() = devicesLiveData

    init {
//        Ble.instance.scannedDevice.observeForever(Observer {
//            addNewDevice(it.device.address, it.device.name)
//        })
//
//        Ble.instance.connectedDevice.observeForever(Observer {
//
//            val gatt = it
//
//            mDevices.find { it.UID == gatt.device.address }?.Connection = Connection.CONNECTED
//            updateLiveData()
//        })
//
//        Ble.instance.lastMessage.observeForever(Observer {
//
//            Log.i(TAG, it);
//        })
    }
    private fun updateLiveData(){
        devicesLiveData.value = mDevices
    }

    fun addTestDevice(){
        mDevices.add(DevicesRecyclerItem(
            getRandomString(10),
            "NAME",
            Connection.DISCONNECTED,
            DevicesStateEnum.STOPPED,
            Settings.Sensitivity.min,
            Settings.LedBrightness.min,
            100
        ))

        updateLiveData()
    }
    fun addNewDevice(uid: String, name: String, sensitivity: Int, brightness: Int){
        mDevices.add(DevicesRecyclerItem(
            uid,
            name,
            Connection.DISCONNECTED,
            DevicesStateEnum.STOPPED,
            sensitivity,
            brightness,
            100
        ))
        updateLiveData()
    }

    fun removeDevice(uid: String) {

        var toRemove: DevicesRecyclerItem? = null

        mDevices.forEach {
            if (it.UID == uid){
                toRemove = it
                return@forEach
            }
        }

        toRemove?.let{
            mDevices.remove(it)
        }
        updateLiveData()
    }
    fun addNewDevice(item: DevicesRecyclerItem){
        // todo ble search needed

        mDevices.add(item)
        updateLiveData()
    }
    fun playDevice(uid: String){

        mDevices.find { it.UID == uid }?.State = DevicesStateEnum.PLAYING
        updateLiveData()
    }
    var cnt: Int = 0;
    fun test(uid: String){
        Ble.instance.write(uid, BlueChemiParameters.CUSTOM_LED_UUID, cnt++)
    }
    fun pauseDevice(uid: String){

        mDevices.find { it.UID == uid }?.State = DevicesStateEnum.STOPPED
        updateLiveData()
    }
    fun togglePlay(uid: String){
        if (mDevices.find { it.UID == uid }?.State == DevicesStateEnum.PLAYING){
            Ble.instance.write(uid, BlueChemiParameters.CUSTOM_STOP_UUID, 1)
            Ble.instance.enableNotification(uid, BlueChemiParameters.CUSTOM_BAT_UUID, false);
            Ble.instance.enableNotification(uid, BlueChemiParameters.CUSTOM_BITE_UUID, false);


        }else{
            Ble.instance.write(uid, BlueChemiParameters.CUSTOM_START_UUID, 1)
            Ble.instance.enableNotification(uid, BlueChemiParameters.CUSTOM_BAT_UUID, true);
            Ble.instance.enableNotification(uid, BlueChemiParameters.CUSTOM_BITE_UUID, true);

        }
    }
    fun devicePing(uid: String){
        Ble.instance.write(uid, BlueChemiParameters.CUSTOM_PING_UUID, 1)
    }
    fun toggleConnection(uid: String){
        Ble.instance.toggleConnection(uid)
    }
    fun batteryChanged(uid: String, value: Int){

        mDevices.find { it.UID == uid }?.Battery = value
        updateLiveData()
    }

    fun deviceConnected(uid: String){
        mDevices.find { it.UID == uid }?.let{
            it.Connection = Connection.CONNECTED

            // for bluechemi v02 connect disconnect error
            Ble.instance.write(uid, BlueChemiParameters.CUSTOM_CUSTOM_UUID, 0);
            Ble.instance.write(uid, BlueChemiParameters.CUSTOM_SENSE_UUID, it.Sensitivity)
            Ble.instance.write(uid, BlueChemiParameters.CUSTOM_LED_UUID, it.Brightness)


            updateLiveData()

        }

    }
    fun deviceDisconnected(uid: String){
        mDevices.find { it.UID == uid }?.Connection = Connection.DISCONNECTED
        updateLiveData()
    }
    fun devicePlayed(uid: String){
        mDevices.find { it.UID == uid }?.State = DevicesStateEnum.PLAYING
        updateLiveData()
    }
    fun devicePaused(uid: String){
        mDevices.find { it.UID == uid }?.State = DevicesStateEnum.STOPPED
        updateLiveData()
    }

    fun setDeviceName(uid: String, name: String){

        mDevices.find { it.UID == uid }?.Name = name
        updateLiveData()
    }

    fun getDevice(uid: String): DevicesRecyclerItem?{
        mDevices.find { it.UID == uid }?.let {
            return it
        }?: run{
            return null
        }
    }

}