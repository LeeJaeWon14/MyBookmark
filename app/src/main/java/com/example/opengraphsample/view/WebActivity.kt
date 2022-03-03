package com.example.opengraphsample.view

import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import com.example.opengraphsample.databinding.ActivityWebBinding
import com.example.opengraphsample.util.MyClient

class WebActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            title = intent.getStringExtra("siteName")
        }

        binding.webView.apply {
            webViewClient = MyClient()
            with(settings) {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                cacheMode = WebSettings.LOAD_DEFAULT
                builtInZoomControls = true
                setSupportZoom(true)
            }
            intent.getStringExtra("url")?.let { loadUrl(it) }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> { onBackPressed() }
        }
        return true
    }
}