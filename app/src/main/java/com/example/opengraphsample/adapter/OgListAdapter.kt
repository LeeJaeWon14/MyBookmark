package com.example.opengraphsample.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.opengraphsample.R
import com.example.opengraphsample.room.OgEntity
import com.example.opengraphsample.view.WebActivity

class OgListAdapter(private val ogList: List<OgEntity>) : RecyclerView.Adapter<OgListAdapter.OgListHolder>() {
    class OgListHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_og_title)
        val tvSiteName: TextView = view.findViewById(R.id.tv_og_site_name)
        val tvDescription: TextView = view.findViewById(R.id.tv_og_description)
        val ivImage: ImageView = view.findViewById(R.id.iv_og_image)
        val llGroup: LinearLayout = view.findViewById(R.id.ll_group)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OgListHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler, parent, false)
        return OgListHolder(view)
    }

    override fun onBindViewHolder(holder: OgListHolder, position: Int) {
        holder.apply {
            tvTitle.text = ogList[position].title
            tvSiteName.text = ogList[position].siteName
            tvDescription.text = ogList[position].description

            Glide.with(itemView.context)
                .load(ogList[position].image)
                .error(R.drawable.ic_launcher_foreground)
                .thumbnail(0.2f)
                .into(ivImage)

            llGroup.setOnClickListener {
                if(ogList[position].url == "")
                    return@setOnClickListener
                val intent = Intent(itemView.context, WebActivity::class.java).apply {
                    putExtra("url", ogList[position].url)
                }
                itemView.context.startActivity(intent)
//                Toast.makeText(itemView.context, ogList[position].url, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return ogList.size
    }
}