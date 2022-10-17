package com.example.myapplication.viewDevices

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.myapplication.AppSettings.Settings
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentDeviceSettingBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Exception

class FragmentDeviceSetting : Fragment() {

    companion object{
        val ARG_SENSE = "sensitivity"
        val ARG_BRIGHTNESS = "brightness"
        val ARG_USERNAME = "username"
    }

    val TAG = "Device Setting"

    lateinit var mBinding: FragmentDeviceSettingBinding

    val model: DeviceSettingViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //val view = inflater.inflate(R.layout.fragment_device_setting, container, false)
        mBinding = FragmentDeviceSettingBinding.inflate(inflater, container, false)
        mBinding.lifecycleOwner = viewLifecycleOwner
        mBinding.viewmodel = model

        mBinding.seekbarBrightness.max = Settings.LedBrightness.max
        mBinding.seekbarSensitivity.max = Settings.Sensitivity.max

        arguments?.let {
            try{
                val b = it.getInt(ARG_BRIGHTNESS, Settings.LedBrightness.def)
                val s = it.getInt(ARG_SENSE, Settings.Sensitivity.def)
                val n = it.getString(ARG_USERNAME, getString(R.string.db_username_default))

                val uib = Settings.LedBrightness.int2point(b)
                val uis = Settings.Sensitivity.int2point(s)
                model.sensitivityProgress.set(uis)
                model.brightnessProgress.set(uib)

                model.usernameString.set(n)
            } catch (e: Exception){
                model.brightnessProgress.set(Settings.LedBrightness.int2point(Settings.LedBrightness.def))
                model.sensitivityProgress.set(Settings.Sensitivity.int2point(Settings.Sensitivity.def))
                model.usernameString.set(getString(R.string.db_username_default))
            }
        }?: run{
            model.brightnessProgress.set(Settings.LedBrightness.int2point(Settings.LedBrightness.def))
            model.sensitivityProgress.set(Settings.Sensitivity.int2point(Settings.Sensitivity.def))
            model.usernameString.set(getString(R.string.db_username_default))
        }

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}