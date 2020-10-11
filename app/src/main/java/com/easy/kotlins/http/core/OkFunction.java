package com.easy.kotlins.http.core;


import androidx.annotation.NonNull;

/**
 * Create by LiZhanPing on 2020/4/29
 */
public interface OkFunction<T, R> {

    R apply(@NonNull T r) throws Exception;
}
