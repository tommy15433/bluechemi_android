package com.bluechemi.application.comm

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.bluechemi.application.BlueChemi.BlueChemiIntentFilters
import com.bluechemi.application.BlueChemi.BlueChemiParameters
import com.bluechemi.application.utils.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

// Access with Ble.instance after initailize
// Creating new class object may cause memory leak
class Ble(val context: Context) {

    private var _isConnectionBusy: Boolean = false
    private var _isTxBusy: Boolean = false
    private var _isRxBusy: Boolean = false

    private val _isBusy: Boolean
        get(){
            return _isConnectionBusy || _isTxBusy || _isRxBusy
        }
    val TAG = "BLE"

    object LocationPermissionException: Exception("Location Permission Required")
    object InitializeInstanceException: Exception("Call Ble.initInstance(context) first")

    fun clearFlags(){
        try{
            _isConnectionBusy = false
            _isTxBusy = false
            _isRxBusy = false
        }catch (e:Exception){
            clearFlags()
        }
    }
    object handler: Runnable{

        private val RESETTIMER_INTV: Long = 10
        private val RESETTIMER_MAX = 200
        private var resetTimer = 0
        var queue:PriorityQueue<BleRunnableType> = PriorityQueue()

        fun add(action: BleRunnableType){
            queue.add(action)
            Log.i("BLE", "action added, cur count: ${queue.count()}")
        }
        override fun run() {
            while (true){
                try{
                    if (instance._isBusy){
                        Thread.sleep(RESETTIMER_INTV)
                        if (resetTimer++ > RESETTIMER_MAX){
                            instance.clearFlags()
                            resetTimer = 0
                            //todo: handle busy error
                            Log.i("BLE", "reset timer reset")
                            queue.clear()
                        }else{

                        }
                    }else{
                        resetTimer = 0
                        Thread.sleep(50)
                        val pop = queue.poll()
                        //Log.i("BLE", "queue popped ${pop.toString()}}")
                            pop?.let { it ->

                            when (it.op){
                                operation.connect -> {
                                    if (it.dev.gatt == null){
                                        instance._isConnectionBusy = true
                                        it.dev.gatt = it.dev.scan?.device?.connectGatt(instance.context, false, instance.gattCallBack)
                                    }else{

                                    }
                                }
                                operation.disconnect -> {
                                    if (it.dev.gatt != null){
                                        instance._isConnectionBusy = true
                                        it.dev.gatt?.disconnect()
                                    }else{

                                    }
                                }
                                operation.read -> {

                                    val chartoread = it.char
                                    if (chartoread != null){
                                        it.dev.characteristics.find { chars ->
                                            val cur = chars.uuid.toString()
                                            cur == chartoread}?.let { found ->
                                            instance._isRxBusy = true
                                            it.dev.gatt?.readCharacteristic(found)
                                        }
                                    }else{
                                    }
                                }
                                operation.write -> {
                                    instance._isTxBusy = true
                                    val chartowrite = it.char
                                    val valuetowrite = it.data
                                    if (chartowrite != null && valuetowrite != null){
                                        it.dev.characteristics.find { chars ->
                                            val cur = chars.uuid.toString()
                                            cur == chartowrite
                                        }?.let { found ->
                                            try{
                                                instance._isTxBusy = true
                                                found.value = toByteArray(valuetowrite)
                                                it.dev.gatt?.writeCharacteristic(found)
                                            }catch (e: Exception){
                                                Log.i("BLE", "error ${e.message}")
                                            }

                                        }
                                    }else{

                                    }
                                }
                            }
                        }
                    }
                }catch (e: Exception){
                    Log.i("BLE", e.message.toString())
                }

            }
        }
    }

    companion object{
        lateinit var instance: Ble
        var isInitialized: Boolean = false

        fun initInstance(
            context: Context
        ): Ble{
            if (isInitialized == false){
                isInitialized = true
                instance = Ble(context)
                Thread(handler).start()
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
            BlueChemiParameters.CUSTOM_BAT_UUID -> return BlueChemiIntentFilters.ACTION_BATTERY_CHANGED
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
                            dev.gatt?.close()
                            dev.gatt = null
                            clearFlags()
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
                    clearFlags()
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
            val value = characteristic?.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 0)
                ?.div(0x1000000)

            Log.i(TAG, "onCharacteristicChanged: ${action}, value: ${value.toString()}")

            sendBroadcast(action, mac, value)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)

            Log.i(TAG, "onCharRead: ${characteristic.toString()}, result: ${status.toString()}")

            val action: String = characteristic?.uuid?.let { uuid2char(it) } ?: "null"
            val mac = gatt?.device?.address.toString()
            val value = characteristic?.value?.let { toInt32(it, 0) }

            sendBroadcast(action, mac, value)
            clearFlags()

        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)


            Log.i(TAG, "onCharWrite: ${characteristic.toString()}, result: ${status.toString()}")

            val action: String = characteristic?.uuid?.let { uuid2char(it) } ?: "null"
            val mac = gatt?.device?.address.toString()
            val value = characteristic?.value?.let { toInt32(it, 0) }

            sendBroadcast(action, mac, value)

            clearFlags()
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
        try{
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
        }catch (e: Exception){
            // todo: write error handling code which throws error to server
        }

        
    }

    fun connect(mac: String){
        mConnections.find { it.uid == mac }?.let { bleDev ->
            handler.add(BleRunnableType(bleDev, operation.connect, null, null))
        }
    }
    fun disconnect(mac: String){
        mConnections.find { it.uid == mac }?.let { bleDev ->
            handler.add(BleRunnableType(bleDev, operation.disconnect, null, null))
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
            ?.let{
                handler.add(BleRunnableType(it, operation.write, charUuid, value))
            }

    }

    @SuppressLint("MissingPermission")
    fun read(devUid: String, charUuid: String){
        Log.i("Ble read", "$devUid\t$charUuid")

        mConnections
            .find { it.uid == devUid }
            ?.let{
                handler.add(BleRunnableType(it, operation.read, charUuid, null))
            }
    }

}