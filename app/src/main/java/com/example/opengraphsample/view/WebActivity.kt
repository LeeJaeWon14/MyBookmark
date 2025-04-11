package com.example.opengraphsample.view

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.opengraphsample.BuildConfig
import com.example.opengraphsample.Constants
import com.example.opengraphsample.R
import com.example.opengraphsample.databinding.ActivityWebBinding
import com.example.opengraphsample.util.Log

class WebActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            title = intent.getStringExtra(Constants.URL) ?: "NO DATA"
        }

        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)

        binding.webView.apply {
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    // Intent Scheme 처리 추가
//                    if(url?.startsWith("intent") == true) {
//
//                    }
                    return url?.startsWith("intent") == true
                }
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    Log.e("${url}\nload load!")
                    
                    // URL Scheme 처리 추가
                    if(url?.startsWith("http") == false) {
                        try {
                            startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            )
                            finish()
                        } catch (e: ActivityNotFoundException) {
                            Log.e(e.toString())
                        }
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.e("${url}\nload finish!")
                    Log.e("This page title is ${view?.title}")

                    supportActionBar?.title = view?.title
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)

                    Log.e("code is ${error?.errorCode} and description is ${error?.description}")
                    view?.let {
                        when(error?.description) {
                            it.context.getString(R.string.str_err_cleartext) -> {
                                finish()
                            }
                            "net::ERR_UNKNOWN_URL_SCHEME" -> {
                                it.goBack()
                            }
                            else -> {
                                // not impl.
                            }
                        }
                    }
                }
            }
            with(settings) {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                javaScriptCanOpenWindowsAutomatically = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                cacheMode = WebSettings.LOAD_DEFAULT
                builtInZoomControls = true
                setSupportZoom(true)
                textZoom = 95
                domStorageEnabled = true
            }
            intent.getStringExtra(Constants.URL)?.let { loadUrl(it) }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> { finish() }
        }
        return true
    }

    override fun onBackPressed() {
        binding.webView.run {
            if(canGoBack()) {
                goBack()
            } else super.onBackPressed()
        }
    }
}