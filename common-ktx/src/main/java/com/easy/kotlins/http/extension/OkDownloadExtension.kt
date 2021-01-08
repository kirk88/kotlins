package com.easy.kotlins.http.extension

import com.easy.kotlins.http.ProgressAction
import okhttp3.Response
import java.io.File

interface OkDownloadExtension : OkExtension {

    fun onResponse(response: Response, action: ProgressAction): File

}