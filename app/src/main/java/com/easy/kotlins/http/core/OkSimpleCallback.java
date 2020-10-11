package com.easy.kotlins.http.core;


import androidx.annotation.NonNull;

/**
 * Create by LiZhanPing on 2020/4/27
 */
public abstract class OkSimpleCallback<T> extends OkGenericCallback<T> {

    @Override
    public void onSuccess(@NonNull T result) {

    }

    @Override
    public void onError(Throwable error) {

    }
}
