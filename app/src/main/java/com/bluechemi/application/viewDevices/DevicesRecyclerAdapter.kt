package com.bluechemi.application.viewDevices

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bluechemi.application.R

class DevicesRecyclerAdapter(
    val listener: DevicesRecyclerListener
    ):RecyclerView.Adapter<DevicesRecyclerAdapter.ViewHolder>() {

    val devices : MutableList<DevicesRecyclerItem> = mutableListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewDeviceName: TextView
        val textViewDeviceConnection: TextView
        val layoutDevice: LinearLayout
        val buttonPlayPause: ImageButton
        val buttonSetting: ImageButton
        val buttonLightbulb: ImageButton
        val imageViewBattery: ImageView

        val layoutOptions: View

        val textviewStringMeter: TextView

        var UID: String = ""
            set(value) {
                if (field == ""){
                    field = value
                }
            }


        init {
            textViewDeviceName = itemView.findViewById(R.id.textview_device_name)
            textViewDeviceConnection = itemView.findViewById(R.id.textview_device_connection)
            layoutDevice = itemView.findViewById(R.id.layout_device)
            buttonPlayPause = itemView.findViewById(R.id.button_device_playpause)
            buttonSetting = itemView.findViewById(R.id.button_device_setting)
            buttonLightbulb = itemView.findViewById(R.id.button_device_lightbulb)
            imageViewBattery = itemView.findViewById(R.id.imageView_battery)

            layoutOptions = itemView.findViewById(R.id.layout_device_options)

            textviewStringMeter = itemView.findViewById(R.id.textview_stringmeter)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_device_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.UID = devices[position].UID
        holder.textViewDeviceName.text = devices[position].Name
        holder.textViewDeviceConnection.text = devices[position].Connection.toString()

        if (devices[position].Connection == Connection.CONNECTED){
            holder.layoutOptions.visibility = View.VISIBLE
            holder.buttonSetting.isClickable = false
            holder.buttonPlayPause.isClickable = false
        }else{
            holder.layoutOptions.visibility = View.GONE
            holder.buttonSetting.isClickable = true
            holder.buttonPlayPause.isClickable = true
        }

        if (devices[position].State == DevicesStateEnum.PLAYING){
            holder.buttonPlayPause.setImageResource(R.drawable.ic_pause)
        }else{
            holder.buttonPlayPause.setImageResource(R.drawable.ic_play)
        }

        if (devices[position].Battery >= 75){
            holder.imageViewBattery.setImageResource(R.drawable.ic_battery_full)
        }else if(devices[position].Battery >= 50){
            holder.imageViewBattery.setImageResource(R.drawable.ic_battery_2block)
        }else if(devices[position].Battery >= 25){
            holder.imageViewBattery.setImageResource(R.drawable.ic_battery_1block)
        }else{
            holder.imageViewBattery.setImageResource(R.drawable.ic_battery_min)
        }

        holder.textviewStringMeter.text = (devices[position].StringMeter.toDouble() / 100.0).toString()

        holder.layoutDevice.setOnClickListener {

            listener.onDeviceChangeConnection(devices[position].UID)
        }
        holder.buttonPlayPause.setOnClickListener{

            listener.onDeviceTogglePlay(holder.UID)
        }
        holder.buttonSetting.setOnClickListener {

            listener.onDeviceSetting(holder.UID)
        }
        holder.buttonLightbulb.setOnClickListener {

            listener.onDeviceLightBulb(holder.UID)
        }
    }

    override fun getItemCount(): Int {
        return devices.count()
    }

    fun setDevices(items: List<DevicesRecyclerItem>){
        devices.clear()
        items.forEach {
            devices.add(it)
        }

        notifyDataSetChanged()
    }

}