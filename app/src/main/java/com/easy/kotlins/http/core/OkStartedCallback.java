package com.easy.kotlins.http.core;

/**
 * Create by LiZhanPing on 2020/9/12
 */
public interface OkStartedCallback<T> extends OkCallback<T>{

    void onStart();

}
