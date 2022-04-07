package com.example.opengraphsample

fun main() {
//    Thread {
//        val document = Jsoup.connect("https://velog.io/@jeep_chief_14").get()
//        val elements = document.select("h2") //document.select("div.sc-tilXH hRLnHq h2")
//        elements?.let {
//            it.forEach { el ->
//                println(el.text())
//            }
//        }
//    }.start()

//    val print = solution(readLine().toString())
//    println(print)




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

fun solution(s: String): String {
    val intList = mutableListOf<Int>()
    s.split(" ").forEach {
        intList.add(it.toInt())
    }
    intList.sort()
    return "${intList[0]} ${intList[intList.size -1]}"
}

fun solution(numbers: IntArray): String {

    var answer = ""
    return answer
}