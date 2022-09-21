package com.example.myapplication.viewDevices

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.myapplication.AppSettings.Settings
import com.example.myapplication.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_UID = "uid"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentDeviceSetting.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentDeviceSetting : Fragment() {

    lateinit var editTextName: EditText
    lateinit var seekbarSensitivity: SeekBar
    lateinit var seekbarBrightness: SeekBar
    lateinit var batteryImage: ImageView

    val model: DeviceSettingViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_device_setting, container, false)

        editTextName = view.findViewById(R.id.edittext_device_changename)
        seekbarSensitivity = view.findViewById(R.id.seekbar_sensitivity)
        seekbarBrightness = view.findViewById(R.id.seekbar_brightness)
        batteryImage = view.findViewById(R.id.imageView_battery)

        seekbarSensitivity.max = Settings.Sensitivity.points - 1
        model.sensitivityUi.value?.let {
            if (it <= seekbarSensitivity.max){
                seekbarSensitivity.progress = it
            }else{
                seekbarSensitivity.progress = 0
            }
        } ?: run {
            seekbarSensitivity.progress = 0
        }

        seekbarBrightness.max = Settings.LedBrightness.points - 1
        model.brightnessUi.value?.let {
            if (it <= seekbarBrightness.max){
                seekbarBrightness.progress = it
            }else{
                seekbarBrightness.progress = 0
            }
        } ?: run{
            seekbarBrightness.progress = 0
        }
        registerListeners()

        return view
    }

    fun registerListeners(){

        seekbarBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                model.updateBrightnessUi(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
        seekbarSensitivity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                model.updateSensitivityUi(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}