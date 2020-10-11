package com.easy.kotlins.http.core;

import java.io.File;

/**
 * Create by LiZhanPing on 2020/7/18
 * desc: 下载监听
 */
public interface OkDownloadCallback extends OkStartedCallback<File>{

    void onProgress(long downloadedBytes, long totalBytes);

    void onCancel();
}
