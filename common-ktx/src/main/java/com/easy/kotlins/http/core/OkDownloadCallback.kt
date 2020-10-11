package com.easy.kotlins.http.core

import java.io.File

/**
 * Create by LiZhanPing on 2020/7/18
 * desc: 下载监听
 */
interface OkDownloadCallback : OkCallback<File> {
    fun onProgress(downloadedBytes: Long, totalBytes: Long)
    fun onCancel()
}