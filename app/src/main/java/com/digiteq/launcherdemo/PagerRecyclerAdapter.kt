package com.digiteq.launcherdemo

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digiteq.launcherdemo.helper.ItemTouchHelperAdapter
import com.digiteq.launcherdemo.helper.OnStartDragListener
import java.util.*

class PagerRecyclerAdapter(
    private val stringList: ArrayList<Int>,
    private val listener: OnStartDragListener
) :
    RecyclerView.Adapter<PagerRecyclerAdapter.PagerViewHolder>(),
    ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        return PagerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_icon, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return stringList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.nameView).text = stringList[position].toString()
        holder.itemView.setOnLongClickListener {
            listener.onStartDrag(holder)
            return@setOnLongClickListener true
        }
//        holder.itemView.setOnTouchListener { _, event ->
//            val action = event.action
//            if(action == MotionEvent.ACTION_DOWN)
//                listener.onStartDrag(holder)
//            return@setOnTouchListener false
//        }
    }

    class PagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    private fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(stringList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(stringList, i, i - 1)
            }
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
//        Collections.swap(stringList, fromPosition, toPosition)
//        notifyItemMoved(fromPosition, toPosition)
        onRowMoved(fromPosition, toPosition)
//        notifyItemMoved(fromPosition, toPosition)
        notifyDataSetChanged()
        return true
    }

    override fun onItemDismiss(position: Int) {
        stringList.removeAt(position)
        notifyItemRemoved(position)
    }
}