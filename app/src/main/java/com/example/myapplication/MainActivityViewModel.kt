package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.ViewDevices.DevicesRecyclerItem
import com.example.myapplication.BlueChemi.BiteLog

class MainActivityViewModel : ViewModel() {

    private val connectedDevices = ArrayList<DevicesRecyclerItem>()
    private val connectedDevicesLiveData = MutableLiveData<List<DevicesRecyclerItem>>()

    private val curValue = ArrayList<BiteLog>()
    private val curValueLiveData = MutableLiveData<List<BiteLog>>()

    val value : LiveData<List<BiteLog>>
        get() = curValueLiveData

    val devices: LiveData<List<DevicesRecyclerItem>>
        get() = connectedDevicesLiveData


    init {

    }

    fun addDevice(item: DevicesRecyclerItem){
        connectedDevices.add(item)
        connectedDevicesLiveData.value = connectedDevices
    }

    fun deleteDevice(){
        TODO("not implemented")
    }

    fun add(biteLog: BiteLog){
        curValue.add(biteLog)
        curValueLiveData.value = curValue
    }

    fun delete(idx : Int){
        curValue.removeAt(idx)
        curValueLiveData.value = curValue
    }
}