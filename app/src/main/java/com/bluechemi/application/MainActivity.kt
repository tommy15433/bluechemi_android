package com.bluechemi.application

import android.Manifest
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.forEach
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bluechemi.application.AppSettings.Settings
import com.bluechemi.application.BlueChemi.BlueChemiIntentFilters
import com.bluechemi.application.ViewDiary.DiaryFragment
import com.bluechemi.application.ViewDiary.DiarySubjectViewModel
import com.bluechemi.application.databinding.ActivityMainBinding
import com.bluechemi.application.firebase.Db
import com.bluechemi.application.viewnotification.FragmentNoti
import com.bluechemi.application.viewnotification.FragmentNotiListener
import com.bluechemi.application.viewnotification.NotificationItem
import com.bluechemi.application.viewnotification.NotificationViewModel
import com.bluechemi.application.utils.*
import com.bluechemi.application.viewCamera.FragmentCamera
import com.bluechemi.application.viewCamera.FragmentCameraListener
import com.bluechemi.application.viewDevices.*
import com.bluechemi.application.weatherApi.ForecastParser
import com.bluechemi.application.weatherApi.ForecastResponse
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.common.base.Stopwatch
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    val ACTIVITY_REQUEST_LOCATION_SERVICE = 1

    companion object{
        val ACTIVITY_REQUEST_LOCATION_SERVICE = 1
        val ACTIVITY_REQUEST_CAMERA = 10
        val ACTIVITY_REQUEST_CAMERA_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

        val BACK_ID_FRAG_CAMERA = "BACK_FROM_CAMERA"
        val BACK_ID_FRAG_SETTING = "BACK_FROM_SETTING"

    }
    val CHANNEL_ID = "입질알림"

    lateinit var bottomNavigationView: BottomNavigationView

    lateinit var binding: ActivityMainBinding

    val model by lazy {
        ViewModelProvider(this).get(DevicesViewModel::class.java)
    }
    val notiModel by lazy{
        ViewModelProvider(this).get(NotificationViewModel::class.java)
    }
    val devSettingModel by lazy{
        ViewModelProvider(this).get(DeviceSettingViewModel::class.java)
    }
    val diarySubjectModel by lazy{
        ViewModelProvider(this).get(DiarySubjectViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main, null)
        Locations.instance?.getCurrentAddress(this)


        // set initial view to FragmentA
        supportFragmentManager.beginTransaction().add(R.id.linearlayout_fragment, FragmentDevices())
            .commit()

        Db.ParseDiariesCache(this, Settings.APP_UUID)
            .addOnSuccessListener {
                Db.QueryDiaries(this, it).forEach {
                    diarySubjectModel.addNoti(it)
                }
            }.addOnFailureListener {

            }

        initBottomNavigation()
        initLocation()
        initWeatherApi()
        registerBroadcast(this)

        notiChannelCreate()
    }

    private fun initLocation() {

        binding.address = "위치를 업데이트 해주세요"
        binding.sky = ""
        binding.rainprop = ""
        binding.waveheight = ""

        Tracker.setListener(object : Tracker.listener{
            override fun onTraveledFar(distance: Double) {


            }
        })

        Locations.instance?.setListener(object : Locations.Listener{
            override fun onLocationUpdated(location: LocationResult) {
                // update address
                // update weather
                Tracker.add(location)
                Locations.instance?.updateAddress()
                location.lastLocation?.let {
                    ForecastParser.parse(it.latitude, it.longitude)

                }

            }

            override fun onAddressUpdated(result: LocationsCallback) {
                binding.address = result.address

                ForecastParser.parse(result.latitude, result.longitude)

            }
        })
    }

    private fun initWeatherApi(){
        ForecastParser.setListener(object: ForecastParser.Listener{
            override fun onForecastUpdated(result: ForecastResponse) {

                Log.i(TAG, "날씨 parse ")

                val sky = result.parseCategory(ForecastResponse.CATEGORY.SKY)?.getInfo()?: "없음"
                val wave = result.parseCategory(ForecastResponse.CATEGORY.WAVE_HEIGHT)?.getInfo()?: "없음"
                val rainprop = result.parseCategory(ForecastResponse.CATEGORY.RAIN_PROC)?.getInfo()?: "없음"
                val temperature = result.parseCategory(ForecastResponse.CATEGORY.TEMP_LASTHOUR)?.getInfo()?: "없음"
                val rainfall = result.parseCategory(ForecastResponse.CATEGORY.RAIN_PERHOUR)?.getInfo()?: "없음"
                val snowfall = result.parseCategory(ForecastResponse.CATEGORY.SNOW_PERHOUR)?.getInfo()?: "없음"

                binding.sky = sky
                binding.waveheight = wave
                binding.rainprop = rainprop
                binding.temperature = temperature
                binding.rainFall = rainfall
                binding.snowFall = snowfall
            }

        })
    }

    fun initBottomNavigation() {

        bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavi)

        bottomNavigationView.setOnItemSelectedListener {
            displayFragment(it.itemId)

            return@setOnItemSelectedListener true
        }
    }


    fun showDeviceSetting(dev: DevicesRecyclerItem) {

        devSettingModel.UID = dev.UID

        bottomNavigationView.menu.forEach {
            it.isEnabled = false
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.linearlayout_fragment, FragmentDeviceSetting().apply {
                arguments = Bundle().apply {
                    putString(FragmentDeviceSetting.ARG_USERNAME, dev.Name)
                    putInt(FragmentDeviceSetting.ARG_SENSE, dev.Sensitivity)
                    putInt(FragmentDeviceSetting.ARG_BRIGHTNESS, dev.Brightness)
                }
            })
            .addToBackStack("preFragDev")
            .commitAllowingStateLoss()
    }

    override fun onBackPressed() {

        if (supportFragmentManager.backStackEntryCount > 0) {
            bottomNavigationView.menu.forEach {
                it.isEnabled = true

            }
            when(supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name)
            {
                "preFragDev" -> {

                }
                BACK_ID_FRAG_CAMERA -> {

                    supportFragmentManager.popBackStack()

                }
                BACK_ID_FRAG_SETTING -> {

                }

            }
            // LIFO
            if (supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name == "preFragDev") {
                devSettingModel.UID?.let { uid ->
                    val name = devSettingModel.usernameString.get()
                        ?: getString(R.string.db_username_default)
                    val sense = devSettingModel.sensitivityValue.get() ?: Settings.Sensitivity.def
                    val bright = devSettingModel.brightnessValue.get() ?: Settings.LedBrightness.def

                    val settingData: HashMap<String, Any> = hashMapOf(
                        getString(R.string.db_sensitivity) to sense,
                        getString(R.string.db_brightness) to bright,
                        getString(R.string.db_username) to name
                    )
                    Settings.deviceHashMap.get(uid)?.putAll(settingData)
                    Db.UpdateDeviceSettings(this, Settings.APP_UUID, uid, settingData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "server updated", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(
                                this,
                                "server upate failed: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    model.getDevice(uid)?.let {
                        it.Brightness = bright ?: Settings.LedBrightness.def
                        it.Sensitivity = sense ?: Settings.Sensitivity.def
                        it.Name = name ?: getString(R.string.db_username_default)
                    }
                }

                supportFragmentManager.popBackStack()
            }
        } else {
            //super.onBackPressed()
            moveTaskToBack(true)
        }
    }


    private fun registerBroadcast(c: Context){
        val mtag = "broadcast"

        val filters: ArrayList<String> = arrayListOf(
            BlueChemiIntentFilters.ACTION_SCAN_DEVICE_ADDED,
            BlueChemiIntentFilters.ACTION_SCAN_DEVICE_REMOVED,
            BlueChemiIntentFilters.ACTION_GATT_CONNECTED,
            BlueChemiIntentFilters.ACTION_GATT_DISCONENCTED,
            BlueChemiIntentFilters.ACTION_PLAYSTOP_CHANGED,
            BlueChemiIntentFilters.ACTION_BRIGHTNESS_CHANGED,
            BlueChemiIntentFilters.ACTION_SENSITIVITY_CHANGED,
            BlueChemiIntentFilters.ACTION_BITE_DETECTED,
            BlueChemiIntentFilters.ACTION_PLAY,
            BlueChemiIntentFilters.ACTION_STOP,
            BlueChemiIntentFilters.ACTION_BATTERY_CHANGED,
            BlueChemiIntentFilters.ACTION_STRINGMETER)

        val broadcastReceiver: BroadcastReceiver = object: BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                val uid: String = intent?.getStringExtra(BlueChemiIntentFilters.EXTRA_ADDRESS)?: "null"
                val valstr: String = intent?.getStringExtra(BlueChemiIntentFilters.EXTRA_STRING)?: "null"
                val valint: Int? = intent?.getIntExtra(BlueChemiIntentFilters.EXTRA_VALUE, Int.MIN_VALUE)

                Log.i(mtag, "action: " + intent?.action + " uid: " + uid + " valstr: " + valstr + " valint" + valint.toString())

                if (uid == "null"){
                    return
                }

                when (intent?.action){
                    BlueChemiIntentFilters.ACTION_BATTERY_CHANGED -> {
                        // todo: write battery change
                        valint?.let {
                            Log.i(TAG, "battery level changed: ${it.toString()}")
                            model.batteryChanged(uid, it)
                        }

                    }
                    BlueChemiIntentFilters.ACTION_SCAN_DEVICE_ADDED -> {

                        val devmap = Settings.deviceHashMap.get(uid)
                        devmap?.let {
                            val brightness = it.get(getString(R.string.db_brightness))
                            val sensitivity = it.get(getString(R.string.db_sensitivity))
                            val name = it.get(getString(R.string.db_username))
                            model.addNewDevice(uid, name.toString(), sensitivity.toString().toInt(), brightness.toString().toInt())
                        }?: run{
                            model.addNewDevice(uid, getString(R.string.db_username_default), Settings.Sensitivity.def, Settings.LedBrightness.def)
                        }
                    }
                    BlueChemiIntentFilters.ACTION_SCAN_DEVICE_REMOVED -> {
                        model.removeDevice(uid)
                    }
                    BlueChemiIntentFilters.ACTION_GATT_CONNECTED ->{
                        model.deviceConnected(uid)
                    }
                    BlueChemiIntentFilters.ACTION_GATT_DISCONENCTED ->{
                        model.deviceDisconnected(uid)
                    }
                    BlueChemiIntentFilters.ACTION_PLAYSTOP_CHANGED -> {
                        if (valint == 1){
                            model.devicePlayed(uid)
                        }else{
                            model.devicePaused(uid)
                        }
                    }
                    BlueChemiIntentFilters.ACTION_PLAY -> {
                        model.devicePlayed(uid)
                    }
                    BlueChemiIntentFilters.ACTION_STOP -> {
                        model.devicePaused(uid)
                    }
                    BlueChemiIntentFilters.ACTION_BRIGHTNESS_CHANGED -> {
                        devSettingModel.brightnessUpdated(valint)
                    }
                    BlueChemiIntentFilters.ACTION_SENSITIVITY_CHANGED -> {
                        devSettingModel.sensitivityUpdated(valint)
                    }
                    BlueChemiIntentFilters.ACTION_BITE_DETECTED -> {

                        val id = uid
                        val date = getCurrentDate()
                        val time = getCurrentTime()
                        val storagePath = makeStoragePath(Settings.APP_UUID, date, time)
                        val noti: NotificationItem = NotificationItem(
                            id,
                            date,
                            time,
                            binding.address?:"없음",
                            "${binding.sky} ${binding.temperature} ${binding.snowFall} ${binding.rainFall}",
                            binding.waveheight?:"없음",
                            "메모를 작성하세요",
                            resources.getDrawable(R.drawable.ic_empty_camera_svg),
                            storagePath
                        )

                        notiModel.add(noti)
                        notiDisplay(notiModel.unreadMessageCount)

                    }
                    BlueChemiIntentFilters.ACTION_STRINGMETER -> {
                        valint?.let{
                        if (ElapseTimer.hasStarted() == true){
                                if (ElapseTimer.hasElapsed(250)){
                                    model.setStringMeter(uid, it)
                                    ElapseTimer.start();
                                }
                            }else{
                                ElapseTimer.start();
                                model.setStringMeter(uid, it)
                            }

                        }
                    }
                    else ->{
                        Log.i(mtag, "unhandled action")
                    }
                }
            }

        }

        val intentFilter: IntentFilter = IntentFilter()
        filters.forEach { filter ->
            intentFilter.addAction(filter)
        }

        c.registerReceiver(broadcastReceiver, intentFilter)
    }



    private fun runPhysicalNoti(){
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        when (am.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL ->{
                runRingtone()
            }
            else -> {
                runVibrate()
            }
        }
    }
    private fun runVibrate()
    {
        val vib: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
        }

    }

    private fun runRingtone()
    {
        val noti : Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone: Ringtone = RingtoneManager.getRingtone(this.applicationContext, noti)
        ringtone.play()

        val timerTask : TimerTask = object : TimerTask()
        {
            override fun run() {
                val noti1 : Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone1: Ringtone = RingtoneManager.getRingtone(applicationContext, noti1)
                ringtone1.stop()
            }

        }

        val timer : Timer = Timer()
        timer.schedule(timerTask, 1000)

    }

    private fun displayFragment(naviId: Int){
        when(naviId){
            R.id.bottomnavi_devices -> {
                Log.i("bottomNavi", "bottomnavi_devices")
                supportFragmentManager.beginTransaction()
                    .replace(R.id.linearlayout_fragment, FragmentDevices())
                    .commitAllowingStateLoss()
            }
            R.id.bottomnavi_noti -> {
                Log.i("bottomNavi", "bottomnavi_noti")

                // register fragment noti event listener
                val fragmentNoti: FragmentNoti = FragmentNoti()
                fragmentNoti.setListener(object : FragmentNotiListener {
                    override fun onSubmit(item: NotificationItem) {

                        Db.UploadDiary(this@MainActivity, item)
                            .addOnSuccessListener { Toast.makeText(this@MainActivity, "diary updated", Toast.LENGTH_SHORT).show() }
                            .addOnFailureListener { Toast.makeText(this@MainActivity, "failed to diary update", Toast.LENGTH_SHORT).show() }

                        Db.UploadPicture(this@MainActivity, item, 50)
                            ?.addOnSuccessListener { Toast.makeText(this@MainActivity, "picture updated", Toast.LENGTH_SHORT).show() }
                            ?.addOnFailureListener { Toast.makeText(this@MainActivity, "failed to upload picture", Toast.LENGTH_SHORT).show() }

                        // image show on subject
                        diarySubjectModel.addNoti(item)
                    }
                    override fun onRemove(item: NotificationItem) {
                        notiModel.remove(item)
                    }

                    override fun onStartCapture(item: NotificationItem) {
                        notiModel.setCaptureItem(item)
                        startCameraFragment()
                    }
                })

                supportFragmentManager.beginTransaction()
                    .replace(R.id.linearlayout_fragment, fragmentNoti)
                    .commitAllowingStateLoss()
            }
            R.id.bottomnavi_diary -> {
                Log.i("bottomNavi", "bottomnavi_noti")
                supportFragmentManager.beginTransaction()
                    .replace(R.id.linearlayout_fragment, DiaryFragment())
                    .commitAllowingStateLoss()
            }
            R.id.bottomnavi_setting -> {
                Log.i("bottomNavi", "bottomnavi_setting")
                supportFragmentManager.beginTransaction()
                    .replace(R.id.linearlayout_fragment, SettingFragment())
                    .commitAllowingStateLoss()
            }

        }
    }

    private fun startCameraFragment() {
        bottomNavigationView.menu.forEach {
            it.isEnabled = false
        }

        val camfrag = FragmentCamera().apply {
            setListener(object : FragmentCameraListener{
                override fun onCapture(uri: Uri) {
                    val inputStream = contentResolver.openInputStream(uri)
                    val drawable = Drawable.createFromStream(inputStream, uri.toString())
                    notiModel.setCapturedImage(drawable)
                }
            })
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.linearlayout_fragment, camfrag)
            .addToBackStack(BACK_ID_FRAG_CAMERA)
            .commitAllowingStateLoss()
    }


    fun notiDisplay(msgCount: Int){
        // Create an explicit intent for an Activity in your app
        val intent = Intent(this, MainActivity::class.java).apply {

            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, flag)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bottomnavi_devices)
            .setContentTitle("BlueChemi")
            .setContentText("unhandled bites: ${msgCount.toString()}")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            // Set the intent that will fire when the user taps the notification
            //.setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(10, builder.build())
        }


    }
    fun notiChannelCreate(){
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL_ID
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getCurrentAddress(v: View){

        Log.i(TAG, "getCurrentAddress")

        val request: com.google.android.gms.location.LocationRequest = com.google.android.gms.location.LocationRequest.create()
            .setInterval(Locations.INTERVAL)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setFastestInterval(Locations.FASTESTINTERVAL)
            .setWaitForAccurateLocation(true)


        val build = LocationSettingsRequest.Builder().addLocationRequest(request)

        LocationServices
            .getSettingsClient(this)
            .checkLocationSettings(build.build())
            .addOnSuccessListener {
                Log.i(TAG, "location check success")
                Locations.instance?.getCurrentAddress(this)
            }
            .addOnFailureListener {
                Log.i(TAG, "location check failed")
                try{
                    val resolvableApiException = it as ResolvableApiException
                    resolvableApiException.startResolutionForResult(this, ACTIVITY_REQUEST_LOCATION_SERVICE)
                }catch (e: Exception){
                    Log.i(TAG, e.message.toString())
                    Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
                }

            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            ACTIVITY_REQUEST_LOCATION_SERVICE -> {
                if (resultCode == Activity.RESULT_OK){
                    Locations.instance?.getCurrentAddress(this)
                }else{
                    Toast.makeText(this, "location update request denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
