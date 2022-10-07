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

        model.UID?.let{ mac ->
            val db = Firebase.firestore
            db.collection(mac)
                .document("Settings")
                .get()
                .addOnSuccessListener {
                    it.data?.let{
                        try{
                            val brightness = (it.getOrDefault("brightness", Settings.LedBrightness.def) as Long).toInt()
                            val sensitivity = (it.getOrDefault("sensitivity", Settings.Sensitivity.def) as Long).toInt()

                            model.brightnessProgress.set(Settings.LedBrightness.int2point(brightness))
                            model.sensitivityProgress.set(Settings.Sensitivity.int2point(sensitivity))
                        }catch (e: Exception){
                            Toast.makeText(context, "failed to read settings from server: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

                }.addOnFailureListener {
                    Log.i("device setting", "exception: ${it.message}")
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
        }
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        Log.i("device setting", "onDestroyView")

        val settingData = hashMapOf(
            "sensitivity" to model.sensitivityValue.get(),
            "brightness" to model.brightnessValue.get()
        )

        val db = Firebase.firestore

        model.UID?.let {
            db.collection(it)
                .document("Settings")
                .set(settingData)
                .addOnSuccessListener {
                    Log.i("device setting", "db updated")
                }
                .addOnFailureListener{
                    Log.i("device setting", "db update failed: ${it.message}")
                }
        }

        super.onDestroyView()

    }

}