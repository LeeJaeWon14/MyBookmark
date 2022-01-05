package com.example.opengraphsample

import java.util.*

fun main() {
    val a = arrayOf("강종필", "이재원", "정일현")
    val b = arrayOf("천재", "멍청이", "병신")
    for(idx in a.indices)
        println(getRandomName(a).plus(" ${getRandomName(b)}"))
}

fun getRandomName(array: Array<String>) : String = array[Random().nextInt(array.size)]