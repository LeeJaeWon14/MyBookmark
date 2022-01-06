package com.example.opengraphsample.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebViewClient
import com.example.opengraphsample.R
import com.example.opengraphsample.databinding.ActivityWebBinding

class WebActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.webView.apply {
            webViewClient = WebViewClient()
            with(settings) {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                cacheMode = WebSettings.LOAD_DEFAULT
                builtInZoomControls = true
                setSupportZoom(true)
            }
            intent.getStringExtra("url")?.let { loadUrl(it) }
        }
        binding.btnBack.setOnClickListener { finish() }
    }
}