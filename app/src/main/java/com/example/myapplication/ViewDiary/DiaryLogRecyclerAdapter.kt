package com.example.myapplication.ViewDiary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.ViewDiary.DiaryLogRecyclerAdapter.*

class DiaryLogRecyclerAdapter(
    val logs: List<DiaryLog>
    ): RecyclerView.Adapter<ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val textviewDiaryLogTime: TextView
        val textviewDiaryLogWeather: TextView
        val textviewDiaryLogWaveHeight: TextView
        val textviewDiaryLogMemo: TextView

        init {
            textviewDiaryLogTime = itemView.findViewById(R.id.textview_diary_log_time)
            textviewDiaryLogWeather = itemView.findViewById(R.id.textview_diary_log_weather)
            textviewDiaryLogWaveHeight = itemView.findViewById(R.id.textview_diary_log_waveheight)
            textviewDiaryLogMemo = itemView.findViewById(R.id.textview_diary_log_memo)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_diary_log_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        logs.get(position).let {
            holder.textviewDiaryLogTime.text = it.time
            holder.textviewDiaryLogWeather.text = it.weather
            holder.textviewDiaryLogWaveHeight.text = it.waveheight
            holder.textviewDiaryLogMemo.text = it.message
        }
    }

    override fun getItemCount(): Int = logs.count()
}