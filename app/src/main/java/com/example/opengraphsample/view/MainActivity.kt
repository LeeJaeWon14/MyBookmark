package com.example.opengraphsample.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.opengraphsample.Constants
import com.example.opengraphsample.R
import com.example.opengraphsample.adapter.OgListAdapter
import com.example.opengraphsample.databinding.ActivityMainBinding
import com.example.opengraphsample.databinding.LayoutProgressDialogBinding
import com.example.opengraphsample.databinding.LayoutSettingDialogBinding
import com.example.opengraphsample.network.CrawlingTask
import com.example.opengraphsample.repository.room.MyRoomDatabase
import com.example.opengraphsample.repository.room.OgEntity
import com.example.opengraphsample.util.Log
import com.example.opengraphsample.util.Pref
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
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
                val ogMap: HashMap<String, String> = HashMap()
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
                        lateinit var entity: OgEntity
                        try {
                            entity = OgEntity(ogMap)
                        } catch (e: NullPointerException) {
                            Log.e(ogMap.toString())
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, getString(R.string.str_not_supported_og), Toast.LENGTH_SHORT).show()
                            }
                            return@launch
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        Log.e(entity.toString())
                        MyRoomDatabase.getInstance(this@MainActivity).getOgDAO()
                            .insertOg(entity)
                    } ?: run {
                        withContext(Dispatchers.Main) {
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
                    updateList(itemList)
                }
            }


            ogList.observe(this@MainActivity, Observer {
                updateList(it)
            })
        }

        shareAction()
    }

    private fun updateList(list: List<OgEntity>) {
        supportActionBar?.title = String.format(getString(R.string.str_toolbar_title), list.count())
        binding.rvLinkList.adapter = OgListAdapter(list)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_setting -> {
                val dlgBinding = LayoutSettingDialogBinding.inflate(layoutInflater)
                val dlg = AlertDialog.Builder(this).create()
                dlg.setView(dlgBinding.root)

                dlgBinding.apply {
                    chkExtBrowserUse.run {
                        isChecked = Pref.getInstance(this@MainActivity)?.getBoolean(Pref.USE_EXT_BROWSER)!!
                        setOnCheckedChangeListener { compoundButton: CompoundButton, checked: Boolean ->
                            Pref.getInstance(this@MainActivity)?.setValue(Pref.USE_EXT_BROWSER, checked)
                        }
                    }

                    btnCloseDialog.setOnClickListener { dlg.dismiss() }
                }
                dlg.setCancelable(false)
                dlg.show()

            }
        }
        return true
    }

    private fun checkOg(ogMap: HashMap<String, String>) : HashMap<String, String> {
        ogMap.run {
            put(Constants.URL, get(Constants.URL) ?: url)
            put(Constants.SITE_NAME, get(Constants.SITE_NAME) ?: getSiteName(url))
            put(Constants.TITLE, get(Constants.TITLE) ?: "????????????")
            put(Constants.DESCRIPTION, get(Constants.DESCRIPTION) ?: "????????????")
            put(Constants.IMAGE, get(Constants.IMAGE) ?: "????????? ??????")
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


    private suspend fun checkDistinctUrl(url: String) : Boolean {
        val deferred = CoroutineScope(Dispatchers.IO).async {
            val entity = MyRoomDatabase.getInstance(this@MainActivity).getOgDAO()
                .checkDistinct(url)
            Log.e(entity.toString())
            entity != null
        }

        Log.e(deferred.await().toString())
        return deferred.await()
    }
}