package com.example.opengraphsample.adapter

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.opengraphsample.repository.room.OgEntity

class PagingAdapter : PagingDataAdapter<OgEntity, PagingAdapter.PagingViewHolder>(DIFF_UTIL) {
    companion object {
        private val DIFF_UTIL = object : DiffUtil.ItemCallback<OgEntity>() {
            override fun areItemsTheSame(oldItem: OgEntity, newItem: OgEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: OgEntity, newItem: OgEntity): Boolean {
                return oldItem == newItem
            }
        }

        private val DIFF_1 = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                TODO("Not yet implemented")
            }

            override fun getNewListSize(): Int {
                TODO("Not yet implemented")
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                TODO("Not yet implemented")
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                TODO("Not yet implemented")
            }
        }
    }
    class PagingViewHolder(view: View) : RecyclerView.ViewHolder(view) {}

    override fun onBindViewHolder(holder: PagingViewHolder, position: Int) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagingViewHolder {
        TODO("Not yet implemented")
    }
}