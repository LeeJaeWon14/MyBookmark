package com.example.opengraphsample.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.opengraphsample.Constants
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
                url = getUrl(edtInputLink.text.toString().trim())
                Log.e("jsoup", "url is $url")
                CoroutineScope(Dispatchers.IO).launch {
                    val elements = CrawlingTask.getElements(url)
                    elements?.let {
                        it.forEach { el ->
                            when(el.attr("property")) {
                                "og:url" -> {
                                    el.attr("content")?.let { content ->
                                        ogMap.put(Constants.URL, content)
                                    }
                                }
                                "og:site_name" -> {
                                    el.attr("content")?.let { content ->
                                        ogMap.put(Constants.SITE_NAME, content)
                                    }
                                }
                                "og:title" -> {
                                    el.attr("content")?.let { content ->
                                        ogMap.put(Constants.TITLE, content)
                                    }
                                }
                                "og:description" -> {
                                    el.attr("content")?.let { content ->
                                        ogMap.put(Constants.DESCRIPTION, content)
                                    }
                                }
                                "og:image" -> {
                                    el.attr("content")?.let { content ->
                                        ogMap.put(Constants.IMAGE, content)
                                    }

                                }
                            }
                        }
                        ogMap.putAll(checkOg(ogMap))
                        var entity: OgEntity? = null
                        try {
                            entity = OgEntity(
                                0,
                                ogMap.get(Constants.URL)!!,
                                ogMap.get(Constants.SITE_NAME)!!,
                                ogMap.get(Constants.TITLE)!!,
                                ogMap.get(Constants.DESCRIPTION)!!,
                                ogMap.get(Constants.IMAGE)!!
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

        shareAction()
    }

    private fun checkOg(ogMap: HashMap<String, String>) : HashMap<String, String> {
        ogMap.run {
            put(Constants.URL, get(Constants.URL) ?: url)
            put(Constants.SITE_NAME, get(Constants.SITE_NAME) ?: getSiteName(url))
            put(Constants.TITLE, get(Constants.TITLE) ?: "제목없음")
            put(Constants.DESCRIPTION, get(Constants.DESCRIPTION) ?: "설명없음")
            put(Constants.IMAGE, get(Constants.IMAGE) ?: "이미지 없음")
        }
        return ogMap
    }

    private fun getSiteName(url: String) : String = url.split("://")[1].split("/")[0]

    private fun shareAction() {
        when(intent.action) {
            Intent.ACTION_SEND -> {
                if(intent.type == "text/plain") {
                    binding.edtInputLink.setText(intent.getStringExtra(Intent.EXTRA_TEXT))
                }
            }
        }
    }

    private fun getUrl(url: String) : String {
        return if(url.contains("http://") || url.contains("https://")) url
                else "https://".plus(url)
    }
}