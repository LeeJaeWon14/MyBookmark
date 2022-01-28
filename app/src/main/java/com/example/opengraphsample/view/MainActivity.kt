package com.example.opengraphsample.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.opengraphsample.R
import com.example.opengraphsample.adapter.OgListAdapter
import com.example.opengraphsample.databinding.ActivityMainBinding
import com.example.opengraphsample.network.CrawlingTask
import com.example.opengraphsample.room.MyRoomDatabase
import com.example.opengraphsample.room.OgEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.NullPointerException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val ogMap: HashMap<String, String> = HashMap()
    private val ogList: MutableLiveData<List<OgEntity>> by lazy { MutableLiveData<List<OgEntity>>() }
    private lateinit var manager: InputMethodManager
    private var url: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        binding.apply {
            btnAddLink.setOnClickListener {
                manager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                url = edtInputLink.text.toString().trim()
                CoroutineScope(Dispatchers.IO).launch {
                    val elements = CrawlingTask.getElements(url)
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
                        ogMap.putAll(checkOg(ogMap))
                        var entity: OgEntity? = null
                        try {
                            entity = OgEntity(
                                0,
                                ogMap.get("url")!!,
                                ogMap.get("siteName")!!,
                                ogMap.get("title")!!,
                                ogMap.get("description")!!,
                                ogMap.get("image")!!
                            )
                        } catch (e: NullPointerException) {
                            Log.e("Jsoup", ogMap.toString())
                            e.printStackTrace()
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, getString(R.string.str_not_supported_og), Toast.LENGTH_SHORT).show()
                            }
                            return@launch
                        }
                        Log.e("Jsoup", entity.toString())
                        MyRoomDatabase.getInstance(this@MainActivity).getOgDAO()
                            .insertOg(entity)
                    } ?: run {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, getString(R.string.str_invalid_url), Toast.LENGTH_SHORT).show()
                        }
                    }
                    ogList.postValue(
                        MyRoomDatabase.getInstance(this@MainActivity).getOgDAO().getOg()
                    )
                }
                edtInputLink.setText("")
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

    private fun checkOg(ogMap: HashMap<String, String>) : HashMap<String, String> {
        ogMap.get("url")?.let {

        } ?: run {
            ogMap.put("url", url)
        }
        ogMap.get("site_name")?.let {

        } ?: run {
            ogMap.put("siteName", getSiteName(url))
        }

        return ogMap
    }

    private fun getSiteName(url: String) : String = url.split("://")[1].split("/")[0]
}