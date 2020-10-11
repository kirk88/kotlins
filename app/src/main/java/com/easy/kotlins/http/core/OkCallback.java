package com.easy.kotlins.http.core;


import androidx.annotation.NonNull;

/**
 * Create by LiZhanPing on 2020/4/29
 */
public interface OkCallback<T> {

    void onSuccess(@NonNull T result);

    void onError(Throwable error);

}
