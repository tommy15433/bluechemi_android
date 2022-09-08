package com.example.myapplication.ViewDiary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class DiaryLogActivity : AppCompatActivity() {

    lateinit var  recyclerView: RecyclerView
    lateinit var recyclerAdapter: DiaryLogRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_log)

        val item: DiaryItem = intent.extras?.get("item") as DiaryItem

        // display back button on appbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        recyclerAdapter = DiaryLogRecyclerAdapter(item.diaryLogs)
        recyclerView = findViewById(R.id.recycler_diary_log)
        recyclerView.adapter = recyclerAdapter

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.appbar_diary, menu)
//        return true
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> {
                super.onBackPressed()
                finish()
                return true
            }
            else -> return true
        }
    }
}