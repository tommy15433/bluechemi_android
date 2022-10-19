package com.example.myapplication.viewnotification

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
//import com.example.myapplication.databinding.LayoutNotiItemBinding

class FragmentNoti : Fragment() {

    var mListener: FragmentNotiListener? = null

    val model: NotificationViewModel by activityViewModels()

    lateinit var recycler: RecyclerView
    lateinit var recyclerAdapter: NotificationRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_noti, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.recycler_notifications)
        recyclerAdapter = NotificationRecyclerAdapter(object : NotificationRecyclerListener{
            override fun onNotiSubmit(item: NotificationItem) {
                // todo: submit log to diary and delete
                Log.i("fragmentnoti", item.toString())

                mListener?.let {
                    it.onSubmit(item)
                    it.onRemove(item)
                }
            }

            override fun onNotiDelete(item: NotificationItem) {
                // todo: just delete log
                Log.i("fragmentnoti", "delete ${item.toString()}")
                mListener?.let {
                    it.onRemove(item)
                }
            }

        })
        recycler.adapter = recyclerAdapter

        model.unreadMessages.observe(viewLifecycleOwner, Observer {
            recyclerAdapter.setItems(it)
        })
    }


    fun setListener(listener: FragmentNotiListener){
        mListener = listener
    }
}