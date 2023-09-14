package com.bluechemi.application.viewDevices

import android.widget.SeekBar
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.bluechemi.application.AppSettings.Settings
import com.bluechemi.application.comm.Ble
import com.bluechemi.application.BlueChemi.BlueChemiParameters


class DeviceSettingViewModel: ViewModel() {

    var sensitivityProgress = ObservableField<Int>()
    var sensitivityValue = ObservableField<Int>()
    var sensitivityString = ObservableField<String>()
    var brightnessProgress = ObservableField<Int>()
    var brightnessValue = ObservableField<Int>()
    var brightnessString = ObservableField<String>()

    var usernameString = ObservableField<String>()
    var stringMeter = ObservableField<Int>()

    fun onProgressSenseChanged(sb: SeekBar?, progress: Int, fromuser: Boolean){
        val datavalue = Settings.Sensitivity.point2int(progress)
        sensitivityProgress.set(progress)
        sensitivityValue.set(datavalue)
        sensitivityString.set(datavalue.toString())
        if (!fromuser){
            updateSensitivityUi(datavalue)
        }
    }
    fun onProgressSenseStopTracking(sb: SeekBar?){
        updateSensitivityUi(sensitivityValue.get())
    }
    fun onProgressBrightnessChanged(sb: SeekBar?, progress: Int, fromuser: Boolean){

        val datavalue = Settings.LedBrightness.point2int(progress)
        brightnessProgress.set(progress)
        brightnessValue.set(datavalue)
        brightnessString.set(datavalue.toString())

        if (!fromuser){
            updateBrightnessUi(datavalue)
        }
    }
    fun onProgressBrightnessStopTracking(sb: SeekBar?){
        updateBrightnessUi(brightnessValue.get())
    }

    // UID
    var UID: String? = null


    fun updateSensitivityUi(_uiValue: Int?){
        _uiValue?.let { _value ->
            UID?.let { _uid ->
                //Ble.instance.write(_uid, BlueChemiParameters.CUSTOM_SENSE_UUID, Settings.Sensitivity.point2int(_value))
                Ble.instance.write(_uid, BlueChemiParameters.CUSTOM_SENSE_UUID, _value)
            }

        }

    }
    fun updateBrightnessUi(_uiValue: Int?){
        _uiValue?.let { _value ->
            UID?.let { _uid ->
                //Ble.instance.write(_uid, BlueChemiParameters.CUSTOM_LED_UUID, Settings.LedBrightness.point2int(_value))
                Ble.instance.write(_uid, BlueChemiParameters.CUSTOM_LED_UUID, _value)
            }

        }
    }
    fun sensitivityUpdated(_devValue: Int?){
        _devValue?.let {

        }
    }
    fun brightnessUpdated(_devValue: Int?){
        _devValue?.let {

        }
    }

    fun stringMeterUpdate(_value: Int?){
        _value?.let{
            stringMeter.set(it)
        }
    }
}