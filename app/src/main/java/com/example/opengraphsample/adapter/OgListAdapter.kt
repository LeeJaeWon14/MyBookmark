package com.example.opengraphsample.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.opengraphsample.Constants
import com.example.opengraphsample.R
import com.example.opengraphsample.repository.room.MyRoomDatabase
import com.example.opengraphsample.repository.room.OgEntity
import com.example.opengraphsample.util.Log
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
                .error(R.drawable.ic_url_thumbnail)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("onLoadFailed()")
                        Log.e("${e?.toString()}")

                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .thumbnail(0.2f)
                .into(ivImage)

            llGroup.apply {
                setOnClickListener {
                    val intent = if(checkedUseExtBrowser()) {
                        Intent(Intent.ACTION_VIEW, Uri.parse(ogList[position].url)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    } else {
                        Intent(itemView.context, WebActivity::class.java).apply {
                            putExtra(Constants.URL, ogList[position].url)
                        }
                    }

                    itemView.context.startActivity(intent)
                }
                setOnLongClickListener { view ->
                    val popup = PopupMenu(itemView.context, view)
                    (itemView.context as Activity).menuInflater.inflate(R.menu.menu_list_pop_up, popup.menu)
                    popup.setOnMenuItemClickListener {
                        when(it.itemId) {
                            R.id.menu_share -> {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, ogList[position].url)
                                }
                                itemView.context.startActivity(Intent.createChooser(intent, ogList[position].url))
                            }
                            R.id.menu_remove -> {
                                AlertDialog.Builder(itemView.context)
                                        .setMessage(itemView.context.getString(R.string.str_delete_confirm))
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
                                            Snackbar.make(view, itemView.context.getString(R.string.str_delete_success), Snackbar.LENGTH_SHORT)
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
                            }
                        }
                        return@setOnMenuItemClickListener true
                    }
                    popup.show()


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