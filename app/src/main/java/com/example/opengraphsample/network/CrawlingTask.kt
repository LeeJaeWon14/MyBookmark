package com.example.opengraphsample.network

import com.example.opengraphsample.util.Log
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

object CrawlingTask {
    fun getElements(url: String) : Elements? {
        var document: Document? = null
        var elements: Elements? = null
        try {
            document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .followRedirects(true)
                .get()
            document?.let {
                elements = document.select("meta[property^=og:]")
            } ?: run {
                Log.e("Not found og tag..")
                return null
            }
        } catch (httpException: HttpStatusException) {
            Log.e("http exception!! ${httpException.message}")
            return null
        } catch(e: Exception) { e.printStackTrace() }
        return elements
    }

    fun getTag(url: String, tag: String) : Elements? {
        return try {
            Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .followRedirects(true)
                .get()
                .select(tag)
        } catch (e: Exception) {
            null
        }
    }
}