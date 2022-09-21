package com.example.myapplication.viewDevices

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.AppSettings.Settings
import com.example.myapplication.comm.Ble
import com.example.myapplication.BlueChemi.BlueChemiParameters

class DeviceSettingViewModel: ViewModel() {

    // UI Values (corresponds to Setting's point value)
    private val mSensitivityUi = MutableLiveData<Int>()
    private val mBrightnessUi = MutableLiveData<Int>()

    // Device values (corresponds to Setting's int value)
    private val mSensitivityDevice = MutableLiveData<Int>()
    private val mBrightnessDevice = MutableLiveData<Int>()

    // UID
    var UID: String? = null

    // observe below
    val sensitivityUi: LiveData<Int>
        get() = mSensitivityUi
    val sensitivityDevice: LiveData<Int>
        get() = mSensitivityDevice
    val brightnessUi: LiveData<Int>
        get() = mBrightnessUi
    val brightnessDevice: LiveData<Int>
        get() = mBrightnessDevice


    fun updateSensitivityUi(_uiValue: Int?){
        _uiValue?.let { _value ->
            UID?.let { _uid ->
                Ble.instance.write(_uid, BlueChemiParameters.CUSTOM_SENSE_UUID, Settings.Sensitivity.point2int(_value))
            }

        }

    }
    fun updateBrightnessUi(_uiValue: Int?){
        _uiValue?.let { _value ->
            UID?.let { _uid ->
                Ble.instance.write(_uid, BlueChemiParameters.CUSTOM_LED_UUID, Settings.LedBrightness.point2int(_value))
            }

        }
    }

    fun sensitivityUpdated(_devValue: Int?){
        _devValue?.let {
            mSensitivityDevice.value = it
            mSensitivityUi.value = Settings.Sensitivity.int2point(it)
        }
    }
    fun brightnessUpdated(_devValue: Int?){
        _devValue?.let {
            mBrightnessDevice.value = it
            mBrightnessUi.value = Settings.LedBrightness.int2point(it)
        }
    }
}