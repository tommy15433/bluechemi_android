package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

//// adding onswipe action to recycler adapter
//class MainActivity3 : AppCompatActivity() {
//
//    lateinit var viewModel: MainActivityViewModel
//
//    val TAG = "MAINACTIVITY3"
//
//    lateinit var imageView: ImageView
//    lateinit var recycler: RecyclerView
//    lateinit var recyclerAdapter: BiteLogRecyclerAdapter
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main3)
//
//        recycler = findViewById(R.id.recycler_bite_logg)
//        recyclerAdapter = BiteLogRecyclerAdapter(mutableListOf())
//        recycler.adapter = recyclerAdapter
//
//
//        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
//            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
//            ItemTouchHelper.LEFT
//        ){
//            override fun onMove(
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder,
//                target: RecyclerView.ViewHolder
//            ): Boolean {
//                return false
//
//            }
//
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                val alertDialog = AlertDialog.Builder(this@MainActivity3)
//                alertDialog.setTitle("Remove Log?")
//                alertDialog.setMessage("Tap Yes to remove the log")
//
//                alertDialog.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
//                    viewModel.delete(viewHolder.layoutPosition)
//                }
//                alertDialog.setNegativeButton("No") { _: DialogInterface, _: Int ->
//                    recyclerAdapter.refresh()
//                }
//
//                alertDialog.show()
//            }
//
//        }
//        ItemTouchHelper(itemTouchCallback).attachToRecyclerView(recycler)
//
////        imageView = findViewById(R.id.imageview)
////        imageView.setImageDrawable(R.drawable.ic_refreshbutton)
//        findViewById<TextView>(R.id.textview_hightop).setOnClickListener{
//            val lowview = findViewById<View?>(R.id.layout_lowtop)
//            if (lowview.visibility == View.VISIBLE){
//                lowview.visibility = View.GONE
//            }
//            else{
//                lowview.visibility = View.VISIBLE
//            }
//        }
//
//        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
//        viewModel.value.observe(this, Observer {
//            recyclerAdapter.set(it)
//        })
//
//        viewModel.add(BiteLog("a", "b"))
//        viewModel.add(BiteLog("c", "d"))
//        viewModel.add(BiteLog("e", "f"))
//    }
//
//
//}

//
// noti
//

//
//fun notiDisplay(){
//    // Create an explicit intent for an Activity in your app
//    val intent = Intent(this, MainActivity2::class.java).apply {
//
//        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//    }
//    val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
//    val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, flag)
//
//    val builder = NotificationCompat.Builder(this, CHANNEL_ID)
//        .setSmallIcon(R.drawable.ic_addbutton)
//        .setContentTitle("My notification")
//        .setContentText("Hello World!")
//        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//        // Set the intent that will fire when the user taps the notification
//        //.setContentIntent(pendingIntent)
//        .setAutoCancel(true)
//
//
//    with(NotificationManagerCompat.from(this)) {
//        // notificationId is a unique int for each notification that you must define
//        notify(10, builder.build())
//    }
//
//
//}
//fun notiChannelCreate(){
//    // Create the NotificationChannel, but only on API 26+ because
//    // the NotificationChannel class is new and not in the support library
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        val name = CHANNEL_ID
//        val descriptionText = "description"
//        val importance = NotificationManager.IMPORTANCE_DEFAULT
//        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
//            description = descriptionText
//        }
//        // Register the channel with the system
//        val notificationManager: NotificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.createNotificationChannel(channel)
//    }
//}