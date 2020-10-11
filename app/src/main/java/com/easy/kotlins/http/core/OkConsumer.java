package com.easy.kotlins.http.core;


import androidx.annotation.NonNull;

/**
 * Create by LiZhanPing on 2020/7/9
 */
public interface OkConsumer<T> {

    void accept(@NonNull T t) throws Throwable;

}
