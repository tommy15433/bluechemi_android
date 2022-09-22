package com.example.myapplication.comm

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.myapplication.BlueChemi.BlueChemiIntentFilters
import com.example.myapplication.BlueChemi.BlueChemiParameters
import com.example.myapplication.utils.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

// Access with Ble.instance after initailize
// Creating new class object may cause memory leak
class Ble(val context: Context) {

    object LocationPermissionException: Exception("Location Permission Required")
    object InitializeInstanceException: Exception("Call Ble.initInstance(context) first")

    companion object{
        lateinit var instance: Ble
        var isInitialized: Boolean = false

        fun initInstance(
            context: Context
        ): Ble{
            if (isInitialized == false){
                isInitialized = true
                instance = Ble(context)
            }
            return instance
        }
    }

    private val mScanResults: ArrayList<ScanResult> = ArrayList()
    private val mConnections: ArrayList<BleDevice> = ArrayList()

    val bluetoothManager: BluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    val bluetoothAdapter: BluetoothAdapter by lazy{
        bluetoothManager.adapter
    }
    val bluetoothScanner: BluetoothLeScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private var isScanning = false

    init{
        if (!isInitialized){
            throw InitializeInstanceException
        }
    }

    val isEnabled: Boolean
        get() =
            try{
                bluetoothAdapter.isEnabled
            }catch (e: Exception){
                false
            }

    val isPermissionsGranted: Boolean
        get() = (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED)
                &&
                (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) ==
            PackageManager.PERMISSION_GRANTED)

    val isScanPermissionGranted: Boolean
        get() = (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) ==
                PackageManager.PERMISSION_GRANTED)

    private fun uuid2char(uuid: UUID): String{
        when(uuid.toString()){
            BlueChemiParameters.CUSTOM_BITE_UUID -> return BlueChemiIntentFilters.ACTION_BITE_DETECTED
            BlueChemiParameters.CUSTOM_SENSE_UUID -> return BlueChemiIntentFilters.ACTION_SENSITIVITY_CHANGED
            BlueChemiParameters.CUSTOM_LED_UUID -> return BlueChemiIntentFilters.ACTION_BRIGHTNESS_CHANGED
            BlueChemiParameters.CUSTOM_START_UUID -> return BlueChemiIntentFilters.ACTION_PLAY
            BlueChemiParameters.CUSTOM_STOP_UUID -> return BlueChemiIntentFilters.ACTION_STOP
            else -> return ""
        }
    }
    private fun sendBroadcast(_action: String, _mac: String){
        val intent = Intent().apply {
            action = _action
            putExtra(BlueChemiIntentFilters.EXTRA_ADDRESS, _mac)
        }

        context.sendBroadcast(intent)
    }
    private fun sendBroadcast(_action: String, _mac: String, _value: String?){
        val intent = Intent().apply {
            action = _action
            putExtra(BlueChemiIntentFilters.EXTRA_ADDRESS, _mac)
            _value?.let { putExtra(BlueChemiIntentFilters.EXTRA_STRING, it) }
        }

        context.sendBroadcast(intent)
    }
    private fun sendBroadcast(_action: String, _mac: String, _value: Int?){
        val intent = Intent().apply {
            action = _action
            putExtra(BlueChemiIntentFilters.EXTRA_ADDRESS, _mac)
            _value?.let { putExtra(BlueChemiIntentFilters.EXTRA_VALUE, it) }
        }

        context.sendBroadcast(intent)
    }

    val scanCallback = object : ScanCallback(){
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let { scanResult ->
                val exists = mConnections.indexOfFirst { it.uid == result.device.address }
                if (exists == -1){
                    mConnections.add(BleDevice().apply {
                        scan = scanResult
                    })

                    sendBroadcast(BlueChemiIntentFilters.ACTION_SCAN_DEVICE_ADDED, result.device.address, result.device.name)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
        }
    }

    @SuppressLint("MissingPermission")
    val gattCallBack = object : BluetoothGattCallback(){

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            when (newState){
                BluetoothGatt.STATE_CONNECTED ->{
                    Log.i("BLE", "STATE_CONNECTED")
                    gatt?.discoverServices()
                }
                BluetoothGatt.STATE_CONNECTING ->{
                    Log.i("BLE", "STATE_CONNECTING")
                }
                BluetoothGatt.STATE_DISCONNECTED ->{
                    Log.i("BLE", "STATE_DISCONNECTED")
                    gatt?.let {
                        mConnections.find { it.uid == gatt.device.address.toString() }?.let { dev ->
                            dev.status = BleDevice.Status.disconnected
                        }
                        //it.close()
                        sendBroadcast(BlueChemiIntentFilters.ACTION_GATT_DISCONENCTED, it.device.address)
                    }
                }
                BluetoothGatt.STATE_DISCONNECTING ->{
                    Log.i("BLE", "STATE_DISCONNECTING")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            val curDev: BleDevice? = mConnections.find {
                gatt?.device?.address == it.uid
            }

            gatt?.services?.forEach{ service ->
                curDev?.let{
                    it.services.add(service)
                }

                service.characteristics.forEach{ characteristic ->
                    curDev?.let{ it.characteristics.add(characteristic) }

                    if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0 ||
                        characteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE > 0){

                        gatt?.setCharacteristicNotification(characteristic, true)
                    }
                }
            }
            curDev?.let{ dev ->
                dev?.uid?.let{ uid ->
                    sendBroadcast(BlueChemiIntentFilters.ACTION_GATT_CONNECTED, uid)
                    dev.status = BleDevice.Status.connected
                }
            }
        }

        override fun onServiceChanged(gatt: BluetoothGatt) {
            super.onServiceChanged(gatt)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)

            val action: String = characteristic?.uuid?.let { uuid2char(it) } ?: "null"
            val mac = gatt?.device?.address.toString()
            val value = characteristic?.value?.let { toInt32(it, 0) }

            sendBroadcast(action, mac, value)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)

            val action: String = characteristic?.uuid?.let { uuid2char(it) } ?: "null"
            val mac = gatt?.device?.address.toString()
            val value = characteristic?.value?.let { toInt32(it, 0) }

            sendBroadcast(action, mac, value)

        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)

            val action: String = characteristic?.uuid?.let { uuid2char(it) } ?: "null"
            val mac = gatt?.device?.address.toString()
            val value = characteristic?.value?.let { toInt32(it, 0) }

            sendBroadcast(action, mac, value)
        }

    }
    @SuppressLint("MissingPermission")
    fun startScan() {

        Log.i("Ble", "startScan")

        if (isScanning == false) {

            val filter: ScanFilter = ScanFilter.Builder().setDeviceName("Blue Chemi").build()
            val setting: ScanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
            bluetoothScanner.startScan(arrayListOf(filter), setting, scanCallback)
            isScanning = true
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan(){

        Log.i("Ble", "stopScan")

        if (isScanning == true){

            bluetoothScanner.stopScan(scanCallback)
            isScanning = false
        }
    }
    fun removeUnconnected(){

        Log.i("Ble", "removeUnconnected")

        val connected  = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)
        val iterator = mConnections.iterator()
        while (iterator.hasNext()){
            val cur = iterator.next()
            if (cur.status == BleDevice.Status.disconnected){
                mConnections.remove(cur)
                cur.uid?.let{
                    sendBroadcast(BlueChemiIntentFilters.ACTION_SCAN_DEVICE_REMOVED, it, "")
                }
            }
        }

        // todo: write code that manages mConnections or make mConnections List as a class to handle connected state
    }

    fun connect(mac: String){
        mConnections.find { it.uid == mac }?.let { bleDev ->
            if (bleDev.status != BleDevice.Status.disconnected){
                return;
            }else{
                bleDev.status = BleDevice.Status.connecting
                if (bleDev.gatt != null){
                    val connection = bleDev.gatt?.connect()
                }else{
                    bleDev.gatt = bleDev.scan?.device?.connectGatt(context, false, gattCallBack)
                }
            }
        }
    }
    fun disconnect(mac: String){
        mConnections.find { it.uid == mac }?.let { bleDev ->
            if (bleDev.status != BleDevice.Status.connected){
                return;

            }else{
                bleDev.status = BleDevice.Status.disconnecting
                bleDev.gatt?.disconnect()
                //bleDev.gatt?.close()
            }
        }
    }
    @SuppressLint("MissingPermission")
    fun toggleConnection(mac: String){
        mConnections.find { it.uid == mac }?.let { bleDev ->
            if (bleDev.status == BleDevice.Status.connected){

                Log.i("Ble", "disconnect called")
                disconnect(mac)
            }else if (bleDev.status == BleDevice.Status.disconnected){
                Log.i("Ble", "connect called")
                connect(mac)

            }else{
                Log.i("Ble", "toggle connection busy")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun write(devUid: String, charUuid: String, value: Int){

        Log.i("Ble write", "$devUid\t$charUuid\t$value")

        mConnections
            .find { it.uid == devUid }
            ?.let{ curConnection ->
                curConnection.characteristics
                    ?.find { it.uuid.toString() == charUuid }
                    ?.let{
                        it.value = toByteArray(value)
                        curConnection.gatt?.writeCharacteristic(it)
                    }
            }

    }

    @SuppressLint("MissingPermission")
    fun read(devUid: String, charUuid: String){
        Log.i("Ble read", "$devUid\t$charUuid")

        mConnections
            .find { it.uid == devUid }
            ?.let{ curConnection ->
                curConnection.characteristics
                    ?.find { it.uuid.toString() == charUuid }
                    ?.let{
                        curConnection.gatt?.readCharacteristic(it)
                    }
            }
    }

}