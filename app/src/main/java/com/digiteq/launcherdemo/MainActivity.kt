package com.digiteq.launcherdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.digiteq.launcherdemo.helper.ItemTouchHelperCallback
import com.digiteq.launcherdemo.helper.OnStartDragListener

class MainActivity : AppCompatActivity() {

    private lateinit var  itemTouchHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupGridRecycler()
    }

    private fun setupGridRecycler() {
        val dataList = ArrayList((0..50).toMutableList())

        val rv = findViewById<RecyclerView>(R.id.icons)
        rv.layoutManager = PagerLayoutManager()
        val adapter = PagerRecyclerAdapter(dataList, object : OnStartDragListener {
            override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
                itemTouchHelper.startDrag(viewHolder)
            }
        })
        rv.adapter = adapter
        val callback = ItemTouchHelperCallback(adapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(rv)
        PagerSnapHelper().attachToRecyclerView(rv)
    }
}