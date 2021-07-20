package com.nice.jsonparser

import com.google.gson.Gson

object GlobalGson {

    @PublishedApi
    internal var gson = Gson()
        private set

    fun setGson(gson: Gson) {
        this.gson = gson
    }

}