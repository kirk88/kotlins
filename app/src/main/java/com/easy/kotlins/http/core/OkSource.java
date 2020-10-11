package com.easy.kotlins.http.core;


import java.util.Objects;

/**
 * Create by LiZhanPing on 2020/4/29
 */
public class OkSource<T> {

    private T source;
    private Throwable error;

    private OkSource(T source) {
        this.source = source;
    }

    private OkSource(Throwable error) {
        this.error = error;
    }

    public static <T> OkSource<T> just(T source){
        return new OkSource<>(Objects.requireNonNull(source));
    }

    public static <T> OkSource<T> error(Throwable error){
        return new OkSource<>(error);
    }

    public T getSource() {
        return source;
    }

    public Throwable getError(){
        return error;
    }
}
