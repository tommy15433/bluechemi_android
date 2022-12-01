package com.bluechemi.application.ViewDiary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bluechemi.application.R

class DiarySubjectRecyclerAdapter(val listener: DiarySubjectRecyclerListener) : RecyclerView.Adapter<DiarySubjectRecyclerAdapter.ViewHolder>() {

    private var mItems: MutableList<DiaryItem> = mutableListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textviewDate: TextView
        val textviewAddress: TextView
        val textviewCount: TextView

        val layoutSubject: View

        init {
            textviewDate = itemView.findViewById(R.id.textview_diary_subject_date)
            textviewAddress = itemView.findViewById(R.id.textview_diary_subject_address)
            textviewCount = itemView.findViewById(R.id.textview_diary_subject_count)

            layoutSubject = itemView.findViewById(R.id.layout_diary_subject_item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_diary_subject_item, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.textviewDate.text = mItems[position].date
        holder.textviewAddress.text = mItems[position].address
        holder.textviewCount.text = mItems[position].count.toString()

        holder.layoutSubject.setOnClickListener {
            listener.onSubjectClicked(mItems[position])
        }
    }

    override fun getItemCount(): Int {
        return mItems.count()
    }

    fun setItems(items: ArrayList<DiaryItem>){
        mItems = items
        notifyDataSetChanged()
    }
}