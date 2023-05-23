package com.bluechemi.application.ViewDiary

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bluechemi.application.R
import com.bluechemi.application.ViewDiary.DiaryLogRecyclerAdapter.*
import com.bluechemi.application.firebase.Db
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlin.coroutines.coroutineContext

class DiaryLogRecyclerAdapter(
    val logs: List<DiaryLog>
    ): RecyclerView.Adapter<ViewHolder>() {

    lateinit var context: Context

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val textviewDiaryLogTime: TextView
        val textviewDiaryLogWeather: TextView
        val textviewDiaryLogWaveHeight: TextView
        val textviewDiaryLogMemo: TextView
        val imageviewCaptured: ImageView

        init {
            textviewDiaryLogTime = itemView.findViewById(R.id.textview_diary_log_time)
            textviewDiaryLogWeather = itemView.findViewById(R.id.textview_diary_log_weather)
            textviewDiaryLogWaveHeight = itemView.findViewById(R.id.textview_diary_log_waveheight)
            textviewDiaryLogMemo = itemView.findViewById(R.id.textview_diary_log_memo)
            imageviewCaptured = itemView.findViewById(R.id.imageview_captured)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context

        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_diary_log_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        logs.get(position).let {
            holder.textviewDiaryLogTime.text = it.time
            holder.textviewDiaryLogWeather.text = it.weather
            holder.textviewDiaryLogWaveHeight.text = it.waveheight
            holder.textviewDiaryLogMemo.text = it.message
            it.imagePath?.let{ imagePath ->
//                holder.imageviewCaptured.setImageURI(Uri.parse(uri))
//                Glide.with(context)
//                    //.load(Firebase.storage.reference.child(imagePath))
//                    .load(Firebase.storage.reference.child("Testing/date-time"))
//                    .into(holder.imageviewCaptured)

                Db.DownloadPictureFromCache(context, imagePath)?.let{
                    holder.imageviewCaptured.setImageBitmap(BitmapFactory.decodeByteArray(it, 0, it.size))
                }?: run{
                    Db.DownloadPicture(context, imagePath).addOnSuccessListener {
                        Log.i("test", "downloadedsize: ${it.size}")
                        holder.imageviewCaptured.setImageBitmap(BitmapFactory.decodeByteArray(it, 0, it.size))
                        Db.UploadPictureToCache(context, imagePath, it)
                    }
                }

            }

        }
    }

    override fun getItemCount(): Int = logs.count()
}