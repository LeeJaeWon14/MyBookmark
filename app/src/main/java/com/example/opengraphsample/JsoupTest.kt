package com.example.opengraphsample

import org.jsoup.Jsoup

fun main() {
    Thread {
        val document = Jsoup.connect("https://velog.io/@jeep_chief_14").get()
        val elements = document.select("h2") //document.select("div.sc-tilXH hRLnHq h2")
        elements?.let {
            it.forEach { el ->
                println(el.text())
            }
        }
    }.start()

//    CoroutineScope(Dispatchers.IO).launch {
//        val document = Jsoup.connect("https://velog.io/@jeep_chief_14").get()
//        val elements = document.select("h2") //document.select("div.sc-tilXH hRLnHq h2")
//        elements?.let {
//            it.forEach { el ->
//                println(el.text())
//            }
//        }
//    }
}