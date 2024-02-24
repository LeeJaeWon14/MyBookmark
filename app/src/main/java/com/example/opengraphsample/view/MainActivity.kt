package com.example.opengraphsample.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.opengraphsample.Constants
import com.example.opengraphsample.R
import com.example.opengraphsample.adapter.OgListAdapter
import com.example.opengraphsample.databinding.ActivityMainBinding
import com.example.opengraphsample.databinding.LayoutSettingDialogBinding
import com.example.opengraphsample.network.CrawlingTask
import com.example.opengraphsample.repository.room.MyRoomDatabase
import com.example.opengraphsample.repository.room.OgEntity
import com.example.opengraphsample.util.Log
import com.example.opengraphsample.util.Pref
import com.google.android.gms.oss.licenses.OssLicensesActivity
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val ogList: MutableLiveData<List<OgEntity>> by lazy { MutableLiveData<List<OgEntity>>() }
    private lateinit var manager: InputMethodManager
    private var url: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        initUi()
        ogList.observe(this@MainActivity, Observer {
            updateList(it, true)
        })
        shareAction(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        shareAction(intent)
    }

    private fun initUi() {
        binding.apply {
            rvLinkList.layoutManager = LinearLayoutManager(this@MainActivity)
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
                                        if(checkDistinctUrl(content)) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(this@MainActivity, "이미 저장된 URL입니다.", Toast.LENGTH_SHORT).show()
                                            }
                                            return@launch
                                        }
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
        }
    }

    private fun updateList(list: List<OgEntity>, isAdd: Boolean = false) {
        supportActionBar?.title = String.format(getString(R.string.str_toolbar_title), list.count())
        binding.rvLinkList.run {
            adapter = OgListAdapter(list)
            scrollToPosition(adapter?.itemCount!! -1)
        }

        if(isAdd)
            Toast.makeText(this@MainActivity, getString(R.string.str_add_success), Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_setting -> {
                val dlgBinding = LayoutSettingDialogBinding.inflate(layoutInflater)
                val dlg = AlertDialog.Builder(this).create().apply {
                    setView(dlgBinding.root)
                    setCancelable(false)
                    window?.setBackgroundDrawableResource(R.drawable.border)
                }

                dlgBinding.apply {
                    chkExtBrowserUse.run {
                        isChecked = Pref.getInstance(this@MainActivity)?.getBoolean(Pref.USE_EXT_BROWSER)!!
                        setOnCheckedChangeListener { compoundButton: CompoundButton, checked: Boolean ->
                            Pref.getInstance(this@MainActivity)?.setValue(Pref.USE_EXT_BROWSER, checked)
                        }
                    }

                    btnCloseDialog.setOnClickListener { dlg.dismiss() }
                }
                dlg.show()

            }
            R.id.menu_oss_lic -> {
                startActivity(
                    Intent(this, OssLicensesActivity::class.java)
                )
            }
            R.id.menu_oss_menu -> {
                startActivity(
                    Intent(this, OssLicensesMenuActivity::class.java)
                )
            }
        }
        return true
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

    private fun shareAction(intent: Intent?) {
        Log.e("shareAction")
        intent?.let { _intent ->
            when(_intent.action) {
                Intent.ACTION_SEND -> {
                    if(_intent.type == "text/plain") {
                        binding.edtInputLink.setText(_intent.getStringExtra(Intent.EXTRA_TEXT))
                        binding.btnAddLink.performClick()
                    }
                }
            }
        } ?: Toast.makeText(this@MainActivity, getString(R.string.str_unknown_intent), Toast.LENGTH_SHORT).show()
    }

    private fun getUrl(url: String) : String {
        return if(url.contains("http://") || url.contains("https://")) url
                else "https://".plus(url)
    }


    private suspend fun checkDistinctUrl(url: String) : Boolean {
//        val deferred = CoroutineScope(Dispatchers.IO).async {
//            val entity = MyRoomDatabase.getInstance(this@MainActivity).getOgDAO()
//                .checkDistinct(url)
//            Log.e(entity.toString())
//            entity != null
//        }
//
//        return deferred.await()
        val entity = withContext(Dispatchers.IO) {
            MyRoomDatabase.getInstance(this@MainActivity).getOgDAO()
                .checkDistinct(url)
//        Log.e(entity.toString())
        }
        return entity != null
    }
}