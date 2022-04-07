package com.example.opengraphsample.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.opengraphsample.R
import com.example.opengraphsample.repository.room.MyRoomDatabase
import com.example.opengraphsample.repository.room.OgEntity
import com.example.opengraphsample.util.Pref
import com.example.opengraphsample.view.WebActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OgListAdapter(private val _ogList: List<OgEntity>) : RecyclerView.Adapter<OgListAdapter.OgListHolder>() {
    private val ogList = _ogList.toMutableList()
    private lateinit var context: Context
    private val undoBundle = Bundle()
    private val undoMap: HashMap<String, Any> = HashMap()

    class OgListHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_og_title)
        val tvSiteName: TextView = view.findViewById(R.id.tv_og_site_name)
        val tvDescription: TextView = view.findViewById(R.id.tv_og_description)
        val ivImage: ImageView = view.findViewById(R.id.iv_og_image)
        val llGroup: LinearLayout = view.findViewById(R.id.ll_group)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OgListHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler, parent, false)
        context = parent.context
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

            llGroup.apply {
                setOnClickListener {
                    if(ogList[position].url == "")
                        return@setOnClickListener
                    if(checkedUseExtBrowser()) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ogList[position].url))
                        itemView.context.startActivity(intent)
                    }
                    else {
                        val intent = Intent(itemView.context, WebActivity::class.java).apply {
                            putExtra("url", ogList[position].url)
                            putExtra("siteName", ogList[position].siteName)
                        }
                        itemView.context.startActivity(intent)
                    }

//                Toast.makeText(itemView.context, ogList[position].url, Toast.LENGTH_SHORT).show()
                }
                setOnLongClickListener {
                    AlertDialog.Builder(itemView.context)
                            .setMessage("삭제하시겠습니까?")
                            .setPositiveButton("삭제") { _, _ ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    undoMap.apply {
                                        put("position", position)
                                        put("og", ogList[position])
                                    }
                                    MyRoomDatabase.getInstance(itemView.context).getOgDAO()
                                            .deleteOg(ogList[position])
                                    ogList.removeAt(position)
                                }
                                notifyDataSetChanged()
                                Snackbar.make(it, itemView.context.getString(R.string.str_delete_success), Snackbar.LENGTH_SHORT)
                                        .setAction(itemView.context.getString(R.string.str_undo_delete)) {
                                            val pos = undoMap.get("position") as Int
                                            val og = undoMap.get("og") as OgEntity
                                            CoroutineScope(Dispatchers.IO).launch {
                                                MyRoomDatabase.getInstance(itemView.context).getOgDAO()
                                                        .insertOg(og)
                                            }
                                            ogList.add(pos, og)
                                            notifyDataSetChanged()
                                        }
                                        .show()
                            }
                            .setNegativeButton("취소", null)
                            .setCancelable(false)
                            .show()

                    true
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return ogList.size
    }

    private fun checkedUseExtBrowser() : Boolean = Pref.getInstance(context)?.getBoolean(Pref.USE_EXT_BROWSER)!!
}