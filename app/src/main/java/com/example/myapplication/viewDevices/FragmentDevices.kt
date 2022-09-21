package com.example.myapplication.viewDevices

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapplication.BlueChemi.BlueChemiIntentFilters
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.comm.Ble
import com.example.myapplication.firebase.Db
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class FragmentDevices : Fragment() {

    val TAG = "FragmentDevices"

    // implementation "androidx.fragment:fragment-ktx:1.5.0"
    val model: DevicesViewModel by activityViewModels()

    lateinit var recyclerView: RecyclerView
    lateinit var recyclerAdapter: DevicesRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(TAG, object{}.javaClass.enclosingMethod.name)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        Log.i(TAG, object{}.javaClass.enclosingMethod.name)

        val view = inflater.inflate(R.layout.fragment_devices, container, false)

        view.findViewById<SwipeRefreshLayout>(R.id.swiprefreshlayout_recycler_devices).apply {
            setOnRefreshListener {
                Ble.instance.removeUnconnected()
                Ble.instance.startScan()
                Handler().postDelayed(
                    {
                        Ble.instance.stopScan()
                        isRefreshing = false
                    }, 2000
                )
            }
        }

        recyclerView = view.findViewById(com.example.myapplication.R.id.recycler_devices)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerAdapter = DevicesRecyclerAdapter(object : DevicesRecyclerListener{

            override fun onDeviceChangeConnection(uid: String) {
                Log.i(TAG, "adapter onDeviceChangeConnection invoked\r\n"+"UID: " + uid + " name: " + "name")
                model.toggleConnection(uid)
            }

            override fun onDeviceTogglePlay(uid: String) {
                Log.i(TAG, "adapter onDeviceTogglePlay invoked\r\n"+"UID: " + uid + " name: " + "name")
                model.togglePlay(uid)
            }

            override fun onDeviceChangeName(uid: String, name: String) {
                Log.i(TAG, "adapter on device change invoked\r\n"+"UID: " + uid + " name: " + "name")
                model.setDeviceName(uid, name)

            }
            override fun onDeviceSetting(uid: String) {
                (activity as MainActivity).showDeviceSetting(uid)
            }

            override fun onDeviceLightBulb(uid: String) {
                Log.i(TAG, "adapter on device lightbulb invoked\r\n"+"UID: " + uid + " name: " + "name")
                model.devicePing(uid)
            }

        })
        recyclerView.adapter = recyclerAdapter


        // since recycler is added to the above view instance, need to return that same instance.
        //return inflater.inflate(com.example.myapplication.R.layout.fragment_devices, container, false)
        return view
    }


    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i(TAG, object{}.javaClass.enclosingMethod.name)

        model.devices.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                Log.i(TAG, "model devices changed. scanned dev count = " + it.count().toString())
                it.forEach {
                    Log.i(TAG, it.toString())
                }
                recyclerAdapter.setDevices(it)
            }
        })
    }

    override fun onPause() {
        Log.i(TAG, object{}.javaClass.enclosingMethod.name)
        super.onPause()
    }

    override fun onDestroy() {
        Log.i(TAG, object{}.javaClass.enclosingMethod.name)
        super.onDestroy()
    }

    override fun onStop() {
        Log.i(TAG, object{}.javaClass.enclosingMethod.name)
        super.onStop()
    }

    override fun onResume() {
        Log.i(TAG, object{}.javaClass.enclosingMethod.name)
        super.onResume()
    }

}