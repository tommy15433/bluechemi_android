package com.bluechemi.application.viewnotification

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.RecyclerView
import com.bluechemi.application.databinding.LayoutNotiItemBinding

class NotificationRecyclerAdapter(
    val listener: NotificationRecyclerListener
    ): RecyclerView.Adapter<NotificationRecyclerAdapter.ViewHolder>() {

    private val mItems: ArrayList<NotificationItem> = arrayListOf()

    lateinit var mParent : ViewGroup

    class ViewHolder(val binding: LayoutNotiItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        mParent = parent

        val layoutInfalter = LayoutInflater.from(parent.context)
        val binding = LayoutNotiItemBinding.inflate(layoutInfalter, parent, false)

        return ViewHolder(binding)
    }

    var cnt: Int = 0
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        mItems[position]?.let { item ->

            holder.binding.notiItem = item
            holder.binding.edittextBiteLogItemNote.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus){
                    val imm = mParent.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
            holder.binding.buttonCamera.setOnClickListener {
                listener?.onScreenCapture(item)
            }

            holder.binding.layoutBiteLogItemOthers.setOnClickListener {
                // todo: show alert dialog with writable note. buttons are delete, cancel, submit

                AlertDialog
                    .Builder(holder.binding.textviewBiteLogItemTime.context)
                    .setPositiveButton("submit", DialogInterface.OnClickListener { dialog, which ->
                        listener.onNotiSubmit(item)
                    })
                    .setNegativeButton("delete", DialogInterface.OnClickListener { dialog, which ->
                        listener.onNotiDelete(item)
                    })
                    .setNeutralButton("cancel", DialogInterface.OnClickListener { dialog, which ->

                    })
                    .show()
            }

        }
    }

    override fun getItemCount(): Int {
        return mItems.count()
    }

    fun setItems(array: ArrayList<NotificationItem>){
        mItems.clear()
        array.forEach {
            mItems.add(it)
        }

        notifyDataSetChanged()
    }
    fun removeItemAt(idx: Int){
        mItems.removeAt(idx)
        notifyItemRemoved(idx)
    }
    fun addItem(item: NotificationItem){
        mItems.add(item)
        notifyItemInserted(mItems.count()-1)
    }
    fun getItemAt(idx: Int): NotificationItem{
        return mItems[idx]
    }
    fun getStringAt(idx: Int): String{
        return mItems[idx].toString()

    }

}