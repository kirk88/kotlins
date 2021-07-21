package com.nice.okfaker

import com.google.gson.Gson
import com.google.gson.GsonBuilder

internal val GSON: Gson = GsonBuilder()
    .serializeNulls()
    .setLenient()
    .create()