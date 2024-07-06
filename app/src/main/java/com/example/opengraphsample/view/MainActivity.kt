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
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var manager: InputMethodManager
    private var url: String = ""

    private var lastScrollTime = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        initUi()

        shareAction(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        shareAction(intent)
    }

    private fun initUi() {
        binding.apply {
            // RecyclerView 초기화
            CoroutineScope(Dispatchers.IO).launch {
                val itemList = MyRoomDatabase.getInstance(this@MainActivity).getOgDAO().getOg()
                withContext(Dispatchers.Main) {
                    rvLinkList.apply {
                        layoutManager = LinearLayoutManager(this@MainActivity)
                        addOnScrollListener(object : RecyclerView.OnScrollListener() {
                            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                                super.onScrolled(recyclerView, dx, dy)

//                            Log.e("onScrolled.. $lastScrollTime")
                                CoroutineScope(Dispatchers.Main).launch {
                                    lastScrollTime += 1
                                    if(btnMoveTop.isVisible) return@launch

                                    btnMoveTop.isVisible = true
                                    btnMoveBottom.isVisible = true

                                    CoroutineScope(Dispatchers.Default).launch {
                                        delay(500)
//                                    Log.e("Enter default scope..")
                                        repeat(lastScrollTime) {
                                            if(lastScrollTime < 0) {
                                                Log.e("hide button")
                                                withContext(Dispatchers.Main) {
                                                    delay(500)
                                                    btnMoveTop.isVisible = false
                                                    btnMoveBottom.isVisible = false
                                                }
                                                return@launch
                                            }
                                            Thread.sleep(100)
                                            lastScrollTime -= 10
//                                        Log.e("decrease time.. $lastScrollTime")
                                        }
                                    }


                                }
//                            if(dy < 0) {
//                                // 위 스크롤
//                                Log.e("Scroll up..")
//                            } else {
//                                // 아래 스크롤
//                                Log.e("Scroll down..")
//                            }
                            }
                        })
                    }
                    updateList(itemList)
                }
            }
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
//                                        if(checkDistinctUrl(content)) {
//                                            withContext(Dispatchers.Main) {
//                                                Toast.makeText(this@MainActivity, "이미 저장된 URL입니다.", Toast.LENGTH_SHORT).show()
//                                            }
//                                            return@launch
//                                        }
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
                        ogMap.putAll(
                            checkOg(ogMap) ?: return@launch
                        )
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
//                    ogList.postValue(
//                        MyRoomDatabase.getInstance(this@MainActivity).getOgDAO().getOg()
//                    )
                    // 추가 완료 후 UI 갱신
                    MyRoomDatabase.getInstance(this@MainActivity).getOgDAO().getOg().also { ogList ->
                        updateList(ogList, true)
                    }
                }
                edtInputLink.setText("")
            }

            btnMoveTop.setOnClickListener { rvLinkList.scrollToPosition(0) }
            btnMoveBottom.setOnClickListener { rvLinkList.scrollToPosition(rvLinkList.adapter?.itemCount?.minus(1) ?: return@setOnClickListener) }

        }
    }

    private fun updateList(list: List<OgEntity>, isAdd: Boolean = false) = CoroutineScope(Dispatchers.Main).launch {
        supportActionBar?.title = String.format(getString(R.string.str_toolbar_title), list.count())
        binding.rvLinkList.run {
            adapter = OgListAdapter(list)
            scrollToPosition(adapter?.itemCount?.minus(1) ?: return@launch)
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

    private suspend fun checkOg(ogMap: HashMap<String, String>) : HashMap<String, String>? {
        val checkUrl = ogMap.get(Constants.URL) ?: return null
        if(checkDistinctUrl(checkUrl)) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "이미 저장된 URL입니다.", Toast.LENGTH_SHORT).show()
            }
            return null
        }
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
        val entity = withContext(Dispatchers.IO) {
            MyRoomDatabase.getInstance(this@MainActivity).getOgDAO()
                .checkDistinct(url)
        }
        return entity != null
    }
}