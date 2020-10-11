package com.easy.kotlins.http.core;



import androidx.annotation.NonNull;

import com.easy.kotlins.helper.GsonKt;
import com.google.gson.internal.$Gson$Types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Create by LiZhanPing on 2020/4/27
 * desc: 请求回调抽象类(含解析)
 */
public abstract class OkGenericCallback<T> implements OkCallback<T> {

    private final Type mType;

    private InternalCallback<T> mInternalCallback;

    protected OkGenericCallback() {
        mType = getSuperclassTypeParameter(getClass());
    }

    private static Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            return null;
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
    }

    @SuppressWarnings("unchecked")
    final T parseResponse(String string) throws Exception {
        if (string == null) {
            throw new NullPointerException("can not parse a null response");
        }

        if (mType == null) {
            throw new IllegalArgumentException("Missing type parameter.");
        }

        final T result;

        if (String.class.equals(mType)) {
            result = (T) string;
        } else {
            result = GsonKt.parseJsonObject(string, String.class);
        }

        if (mInternalCallback != null) {
            mInternalCallback.onResult(result);
        }

        return result;
    }

    @Override
    public void onSuccess(@NonNull T result) {

    }

    @Override
    public void onError(Throwable error) {

    }

    final void setInternalCallback(InternalCallback<T> callback) {
        mInternalCallback = callback;
    }

    interface InternalCallback<T> {
        void onResult(T result);
    }
}
