package com.example.opengraphsample.view

import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.opengraphsample.util.DatabaseBackupHelper
import com.example.opengraphsample.util.Log
import com.example.opengraphsample.util.Pref
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineExceptionHandler
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

    private lateinit var safLauncher: ActivityResultLauncher<String>

    private val addItemExceptionHandler = CoroutineExceptionHandler { _, e ->
        when (e) {
            is Exception -> {

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        initUi()

        shareAction(intent)

        safLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && DatabaseBackupHelper.restoreDatabase(this@MainActivity, it)) {
                Toast.makeText(this@MainActivity, "복원 성공!", Toast.LENGTH_SHORT).show()
//                CoroutineScope(Dispatchers.IO).launch {
//                    updateList(
//                        MyRoomDatabase.getInstance(this@MainActivity).getOgDAO().getOg()
//                    )
//                }
                startActivity(Intent(this@MainActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            } else
                Toast.makeText(this@MainActivity, "복원 실패", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        shareAction(intent)
    }

    override fun onResume() {
        super.onResume()
        Log.e("onResume()")

        // 클립보드에서 가져오기
        val clipManager = getSystemService(ClipboardManager::class.java)
        Handler(Looper.getMainLooper()).post {
            clipManager.primaryClip?.getItemAt(0)?.let { item ->
//                Log.e("clip item is.. ${item.text}")

                if(item.text.matches("^((http|https)://)?([a-zA-Z0-9.-]+)\\.([a-z]+)(/[a-zA-Z0-9._~:/?#@!$&'()*+,;=-]*)?$".toRegex())) {
                    Snackbar.make(binding.btnAddLink, "클립보드에서 가져오시겠습니까?", Snackbar.LENGTH_LONG)
                        .setAction("가져오기") {
                            binding.btnAddLink.performClick()
                        }
                        .show()


//                    AlertDialog.Builder(this)
//                        .setTitle("클립보드에서 URL이 감지됨")
//                        .setMessage("클립보드에서 아래 URL을 추가하겠습니까?\r\n\r\n${item.text}")
//                        .setPositiveButton("예") { _, _ ->
//                            binding.btnAddLink.performClick()
//                        }
//                        .setNegativeButton("아니오", null)
//                        .setCancelable(false)
//                        .show()
                }

            }
        }
    }

    override fun onPause() {
        Log.e("onPause")
        super.onPause()
    }

    override fun onDestroy() {
        Log.e("onDestroy()")
        super.onDestroy()
    }

    private fun initUi() {
        binding.apply {
            // RecyclerView 초기화
            rvLinkList.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)

                // 최상단, 최하단 이동 버튼 숨김처리 이벤트
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
                    }
                })

                CoroutineScope(Dispatchers.IO).launch {
                    updateList(
                        MyRoomDatabase.getInstance(this@MainActivity).getOgDAO().getOg()
                    )
                }
            }

            btnAddLink.setOnClickListener {
                manager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                url = getUrl(edtInputLink.text.toString().trim())
                val ogMap: HashMap<String, String> = HashMap()

                CoroutineScope(Dispatchers.IO).launch {
                    val elements = CrawlingTask.getElements(url)
                    elements?.let {
                        // og:url 태그가 없는 경우가 있어 url 먼저 저장
                        ogMap.put(Constants.URL, url)

                        it.forEach { el ->
                            Log.e("og property > ${el.attr("property")}")
                            when(el.attr("property")) {
//                                "og:url"            -> {
//
//                                }
                                "og:site_name"      -> ogMap.put(Constants.SITE_NAME, el.attr("content") ?: getSiteName(url))
                                "og:title"          -> {
                                    // title로 중복 체크
                                    if(checkDistinctUrl(el.attr("content"))) {
                                        withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "이미 저장된 URL입니다.", Toast.LENGTH_SHORT).show() }
                                        return@launch
                                    }
                                    ogMap.put(Constants.TITLE, el.attr("content") ?: "제목없음")
                                }
                                "og:description"    -> ogMap.put(Constants.DESCRIPTION, el.attr("content") ?: "설명없음")
                                "og:image"          -> ogMap.put(Constants.IMAGE, el.attr("content") ?: "이미지 없음")
                            }
                        }
//                        ogMap.putAll(
//                            checkOg(ogMap) ?: return@launch
//                        )
                        lateinit var entity: OgEntity
                        try {
                            entity = OgEntity(ogMap)
                        } catch (e: NullPointerException) {
                            Log.e(ogMap.toString())
                            Log.stackTrace(e)
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

                        // 추가 완료 후 UI 갱신
                        MyRoomDatabase.getInstance(this@MainActivity).getOgDAO().getOg().also { ogList ->
                            updateList(ogList, true)
                        }

                    } ?: run {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, getString(R.string.str_invalid_url), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                edtInputLink.setText("")
            }

            // 리스트 최상단 이동 버튼
            btnMoveTop.setOnClickListener { rvLinkList.scrollToPosition(0) }
            // 리스트 최하단 이동 버튼
            btnMoveBottom.setOnClickListener {
                rvLinkList.scrollToPosition(rvLinkList.adapter?.itemCount?.minus(1) ?: return@setOnClickListener)
            }

        }
    }

    /**
     * Initialize or update adapter of recyclerview
     */
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
            R.id.menu_oss_menu -> {
                startActivity(
                    Intent(this, OssLicensesMenuActivity::class.java)
                )
            }
            R.id.menu_backup -> {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && DatabaseBackupHelper.backupDatabase(this)) {
                    Toast.makeText(this, "다운로드 폴더에 저장되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.menu_restore -> {
//                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && DatabaseBackupHelper.restoreDatabase(this)) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Toast.makeText(this, "db 파일을 찾아주세요.", Toast.LENGTH_SHORT).show()
                    safLauncher.launch("*/*")
                }
            }
        }
        return true
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


    private suspend fun checkDistinctUrl(title: String): Boolean = withContext(Dispatchers.IO) {
        val entity = MyRoomDatabase.getInstance(this@MainActivity).getOgDAO()
            .checkDistinct(title)
        entity != null
    }
}