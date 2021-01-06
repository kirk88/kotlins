package com.easy.kotlins.http

import java.io.File

class OkDownloader : OkFaker<File>(OkRequestMethod.GET) {

    private var onProgressApplied = false
    private val progressActions: MutableList<ProgressAction> by lazy { mutableListOf() }

    fun onProgress(action: ProgressAction): OkDownloader = this.apply {
        onProgressApplied = true
        progressActions.add(action)
    }

    override fun enqueue(): OkDownloader = this.apply {
        request.enqueue(object : OkDownloadCallback {

            override fun onStart() {
                dispatchStartAction()
            }

            override fun onProgress(downloadedBytes: Long, totalBytes: Long) {
                dispatchProgressAction(downloadedBytes, totalBytes)
            }

            override fun onSuccess(result: File) {
                dispatchSuccessAction(result)
            }

            override fun onError(error: Exception) {
                dispatchErrorAction(error)
            }

            override fun onCancel() {
                dispatchCancelAction()
            }

            override fun onComplete() {
                dispatchCompleteAction()
            }

        })
    }

    private fun dispatchProgressAction(downloadedBytes: Long, totalBytes: Long) {
        if (!onProgressApplied) return

        for (action in progressActions) {
            action(downloadedBytes, totalBytes)
        }
    }

    companion object {

        fun download(action: OkDownloader.() -> Unit): OkDownloader =
            OkDownloader().apply(action)

    }
}

typealias ProgressAction = (downloadedBytes: Long, totalBytes: Long) -> Unit