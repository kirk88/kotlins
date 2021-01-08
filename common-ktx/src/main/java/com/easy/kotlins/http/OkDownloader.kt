@file:Suppress("unused")

package com.easy.kotlins.http

import java.io.File

class OkDownloader : OkManager<File, OkDownloadRequest>(OkDownloadRequest()) {

    companion object {

        fun download(block: OkDownloader.() -> Unit): OkDownloader =
            OkDownloader().apply(block)

    }
}
