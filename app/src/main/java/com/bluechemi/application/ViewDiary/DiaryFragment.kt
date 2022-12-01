package com.bluechemi.application.ViewDiary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bluechemi.application.R

class DiaryFragment : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var recyclerAdapter: DiarySubjectRecyclerAdapter

    val model: DiarySubjectViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diary, container, false)


        val recyclerListener: DiarySubjectRecyclerListener = object : DiarySubjectRecyclerListener{
            override fun onSubjectClicked(item: DiaryItem) {
                if (item.count > 0){
                    val intent: Intent = Intent(view.context, DiaryLogActivity::class.java)

                    intent.putExtra("item", item)
                    startActivity(intent)
                }
            }
        }

        recyclerAdapter = DiarySubjectRecyclerAdapter(recyclerListener)
        recyclerView = view.findViewById(R.id.recycler_diary)
        recyclerView.adapter = recyclerAdapter

        model.diaries.value?.let { recyclerAdapter.setItems(it) }
        model.diaries.observe(this.viewLifecycleOwner, Observer {
            Log.i("DIaryFragment", "Livedate observed")
            recyclerAdapter.setItems(it)
        })

        return view
    }

}