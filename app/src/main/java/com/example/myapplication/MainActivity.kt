package com.example.myapplication

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.forEach
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.AppSettings.Settings
import com.example.myapplication.BlueChemi.BlueChemiIntentFilters
import com.example.myapplication.ViewDiary.DiaryFragment
import com.example.myapplication.ViewDiary.DiarySubjectViewModel
import com.example.myapplication.viewnotification.FragmentNoti
import com.example.myapplication.viewnotification.FragmentNotiListener
import com.example.myapplication.viewnotification.NotificationItem
import com.example.myapplication.viewnotification.NotificationViewModel
import com.example.myapplication.utils.*
import com.example.myapplication.viewDevices.*
import com.example.myapplication.weatherApi.ForecastParser
import com.example.myapplication.weatherApi.ForecastResponse
import com.google.android.gms.location.LocationResult
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"


    val CHANNEL_ID = "입질알림"

    lateinit var bottomNavigationView: BottomNavigationView

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

        // set initial view to FragmentA
        supportFragmentManager.beginTransaction().add(R.id.linearlayout_fragment, FragmentDevices())
            .commit()

        Firebase.firestore.collection(getString(R.string.db_root))
            .document(Settings.APP_UUID)
            .collection(getString(R.string.db_diary))
            .get(Source.CACHE)
            .addOnSuccessListener {
                it.documents.forEach {

                    val date = it.data?.get(getString(R.string.db_key_diary_date))?: "unknown date"
                    val time = it.data?.get(getString(R.string.db_key_diary_time))?: "unknown time"
                    val id = it.data?.get(getString(R.string.db_key_diary_id))?: "unknown id"
                    val address = it.data?.get(getString(R.string.db_key_diary_address))?: "unknown address"
                    val sky = it.data?.get(getString(R.string.db_key_diary_sky))?: "unknown sky"
                    val wav = it.data?.get(getString(R.string.db_key_diary_wav))?: "unknown wav"
                    val note = it.data?.get(getString(R.string.db_key_diary_note))?: "unknown note"

                    diarySubjectModel.addNoti(
                        NotificationItem(
                            id.toString(),
                            date.toString(),
                            time.toString(),
                            address.toString(),
                            sky.toString(),
                            wav.toString(),
                            note.toString()
                        )
                    )
                }
            }

        initBottomNavigation()
        initLocation()
        registerBroadcast(this)

        notiChannelCreate()
    }

    private fun initLocation() {

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
            // LIFO
            if (supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name == "preFragDev") {
                devSettingModel.UID?.let { uid ->
                    val name = devSettingModel.usernameString.get()
                        ?: getString(R.string.db_username_default)
                    val sense = devSettingModel.sensitivityValue.get() ?: Settings.Sensitivity.def
                    val bright = devSettingModel.brightnessValue.get() ?: Settings.LedBrightness.def

                    val settingData = hashMapOf(
                        getString(R.string.db_sensitivity) to sense,
                        getString(R.string.db_brightness) to bright,
                        getString(R.string.db_username) to name
                    )
                    Settings.deviceHashMap.get(uid)?.putAll(settingData)
                    val db =Firebase.firestore.collection(getString(R.string.db_root))
                        .document(Settings.APP_UUID)
                        .collection(getString(R.string.db_doc_setting))
                        .document(uid)
                        .set(settingData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "server updated", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
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
            BlueChemiIntentFilters.ACTION_BATTERY_CHANGED)

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

                        //runPhysicalNoti()
                        if (ElapseTimer.hasElapsed(Settings.BITE_DETECTION_INTERVAL_MAX_MS)){

                            val noti: NotificationItem = NotificationItem(
                                uid,
                                getCurrentDate(),
                                getCurrentTime(),
                                Locations.instance?.addressResult?.getAddressLine(0).toString(),
                                ForecastParser.lastResponse.parseCategory(ForecastResponse.CATEGORY.SKY)?.getInfo()?: "없음",
                                ForecastParser.lastResponse.parseCategory(ForecastResponse.CATEGORY.WAVE_HEIGHT)?.getInfo()?: "없음")

                            notiModel.add(noti)
                            notiDisplay(notiModel.unreadMessageCount)

                            ElapseTimer.start()
                        }else{

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
                fragmentNoti.setListener(object : FragmentNotiListener{
                    override fun onSubmit(item: NotificationItem) {
                        diarySubjectModel.addNoti(item)

                        // upload to server
                        val map = hashMapOf(
                            getString(R.string.db_key_diary_date) to item.date,
                            getString(R.string.db_key_diary_id) to item.devUuid,
                            getString(R.string.db_key_diary_address) to item.address,
                            getString(R.string.db_key_diary_time) to item.time,
                            getString(R.string.db_key_diary_sky) to item.weather,
                            getString(R.string.db_key_diary_wav) to item.waveHeight,
                            getString(R.string.db_key_diary_note) to item.message

//                            ("ID" to item.devUuid) as Pair<Any, Any>,
//                            ("SKY" to ForecastParser.lastResponse.parseCategory(ForecastResponse.CATEGORY.SKY)?.getInfo()?: "없음") as Pair<Any, Any>,
//                            ("WAV" to ForecastParser.lastResponse.parseCategory(ForecastResponse.CATEGORY.WAVE_HEIGHT)?.getInfo()?: "없음") as Pair<Any, Any>,
                        )


                        val db = Firebase.firestore
                        db.collection(getString(R.string.db_root))
                            .document(Settings.APP_UUID)
                            .collection(getString(R.string.db_diary))
                            .add(map)
                            .addOnSuccessListener {
                                Toast.makeText(this@MainActivity, "diary updated", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@MainActivity, "diary update failed", Toast.LENGTH_SHORT).show()
                            }

                    }
                    override fun onRemove(item: NotificationItem) {
                        notiModel.remove(item)
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

}
