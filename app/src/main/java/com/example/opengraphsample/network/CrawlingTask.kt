package com.example.opengraphsample.network

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

object CrawlingTask {
    fun getElements(url: String) : Elements? {
        var document: Document? = null
        var elements: Elements? = null
        try {
            document = Jsoup.connect(url).get()
            elements = document.select("meta[property^=og:]")
        } catch(e: Exception) { e.printStackTrace() }
        return elements
    }
}