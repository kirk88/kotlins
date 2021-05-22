package com.example.sample

import androidx.multidex.MultiDexApplication
import com.nice.kotlins.http.OkFaker
import okhttp3.OkHttpClient

class App : MultiDexApplication() {
}

fun main() {
    val result = OkFaker.get<String>().client(OkHttpClient()).url("https://movie.douban.com/explore#!type=movie&tag=热门&sort=recommend&page_limit=20&page_start=0").executeOrNull()
    println(result)
}