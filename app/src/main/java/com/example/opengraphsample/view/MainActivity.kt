package com.example.opengraphsample.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.opengraphsample.adapter.OgListAdapter
import com.example.opengraphsample.databinding.ActivityMainBinding
import com.example.opengraphsample.network.CrawlingTask
import com.example.opengraphsample.room.MyRoomDatabase
import com.example.opengraphsample.room.OgEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val ogMap: HashMap<String, String> = HashMap()
    private val ogList: MutableLiveData<List<OgEntity>> by lazy { MutableLiveData<List<OgEntity>>() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            btnAddLink.setOnClickListener {
//                Toast.makeText(this@MainActivity, edtInputLink.text.toString().trim(), Toast.LENGTH_SHORT).show()
                CoroutineScope(Dispatchers.IO).launch {
                    val elements = CrawlingTask.getElements(edtInputLink.text.toString().trim())
                    elements?.let {
                        it.forEach { el ->
                            when(el.attr("property")) {
                                "og:url" -> {
                                    el.attr("content")?.let { content ->
                                        ogMap.put("url", content)
                                    } ?: run {
                                        ogMap.put("url", "")
                                    }
                                }
                                "og:site_name" -> {
                                    el.attr("content")?.let { content ->
                                        ogMap.put("siteName", content)
                                    } ?: run {
                                        ogMap.put("siteName", "")
                                    }
                                }
                                "og:title" -> {
                                    el.attr("content")?.let { content ->
                                        ogMap.put("title", content)
                                    } ?: run {
                                        ogMap.put("title", "")
                                    }
                                }
                                "og:description" -> {
                                    el.attr("content")?.let { content ->
                                        ogMap.put("description", content)
                                    } ?: run {
                                        ogMap.put("description", "")
                                    }
                                }
                                "og:image" -> { ogMap.put("image", el.attr("content")) }
                            }
                        }

                        val entity = OgEntity(
                            ogMap.get("url")!!,
                            ogMap.get("siteName")!!,
                            ogMap.get("title")!!,
                            ogMap.get("description")!!,
                            ogMap.get("image")!!
                        )
                        Log.e("Jsoup", entity.toString())
                        MyRoomDatabase.getInstance(this@MainActivity).getOgDAO()
                            .insertOg(entity)
                    }
                    ogList.postValue(
                        MyRoomDatabase.getInstance(this@MainActivity).getOgDAO().getOg()
                    )
                }
            }

            CoroutineScope(Dispatchers.IO).launch {
                val itemList = MyRoomDatabase.getInstance(this@MainActivity).getOgDAO().getOg()
                withContext(Dispatchers.Main) {
                    rvLinkList.layoutManager = LinearLayoutManager(this@MainActivity)
                    rvLinkList.adapter = OgListAdapter(itemList)
                }
            }


            ogList.observe(this@MainActivity, Observer {
                rvLinkList.adapter = OgListAdapter(it)
            })
        }
    }
}