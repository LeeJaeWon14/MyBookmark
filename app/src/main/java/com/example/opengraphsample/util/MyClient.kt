package com.example.opengraphsample.util

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.opengraphsample.R
import com.example.opengraphsample.view.WebActivity

class MyClient : WebViewClient() {
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        Log.e("${url}\nload finish!")
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Log.e("${url}\nload finish!")
        (view?.context as WebActivity).title = view.title
    }

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        super.onReceivedError(view, request, error)

//        Toast.makeText(view?.context, "Error!, code is ${error?.errorCode}", Toast.LENGTH_SHORT).show()
        Log.e("code is ${error?.errorCode} and description is ${error?.description}")
        Log.e("request is here, ${request?.requestHeaders?.toString()}")

        view?.let {
            when(error?.description) {
                it.context.getString(R.string.str_err_cleartext) -> {
                    it.loadUrl(request?.url.toString().replace("http", "https"))
                }
                else -> {
                    // not impl.
                }
            }
        }
    }
}