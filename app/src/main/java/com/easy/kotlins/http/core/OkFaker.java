package com.easy.kotlins.http.core;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.easy.kotlins.helper.GsonKt;
import com.easy.kotlins.helper.StringKt;
import com.easy.kotlins.http.core.extension.DownloadExtension;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import kotlin.collections.CollectionsKt;
import kotlin.text.StringsKt;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Create by LiZhanPing on 2020/4/27
 * desc: OkHttp请求封装 post 和 get
 */
public class OkFaker {

    private final Request.Builder mRequestBuilder;

    private Method mMethod;
    private OkHttpClient mClient;
    private HttpUrl.Builder mUrlBuilder;

    private RequestBody mBody;
    private FormBody.Builder mFormBuilder;
    private MultipartBody.Builder mMultiBuilder;

    private Call mCall;

    private Throwable mCreationFailure;
    private volatile boolean mCanceled;
    private boolean mExecuted;

    private DownloadExtension mDownloadExtension;

    private OkFunction<Throwable, ? extends OkSource<?>> mErrorResumeFunc;
    private OkFunction<Response, ? extends OkSource<?>> mResponseFunc;

    private OkConsumer<?> mResultConsumer;
    private OkConsumer<Throwable> mErrorConsumer;

    public enum Method {
        GET, POST
    }

    private OkFaker(OkHttpClient client, String url, Method method) {
        mClient = client;
        mMethod = method;
        url(url);

        mRequestBuilder = new Request.Builder();
    }

    public static OkFaker newGet() {
        return new OkFaker(null, null, Method.GET);
    }

    public static OkFaker newGet(String url) {
        return new OkFaker(null, url, Method.GET);
    }

    public static OkFaker newGet(OkHttpClient client, String url) {
        return new OkFaker(client, url, Method.GET);
    }

    public static OkFaker newPost() {
        return new OkFaker(null, null, Method.POST);
    }

    public static OkFaker newPost(String url) {
        return new OkFaker(null, url, Method.POST);
    }

    public static OkFaker newPost(OkHttpClient client, String url) {
        return new OkFaker(client, url, Method.POST);
    }

    public OkFaker client(OkHttpClient client) {
        mClient = client;
        return this;
    }

    public OkFaker url(String url) {
        if (url == null) return this;
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl != null) {
            mUrlBuilder = httpUrl.newBuilder();
        } else {
            mUrlBuilder = null;
        }
        return this;
    }

    public OkFaker tag(Object tag) {
        mRequestBuilder.tag(tag);
        return this;
    }

    public OkFaker cacheControl(CacheControl cacheControl) {
        mRequestBuilder.cacheControl(cacheControl);
        return this;
    }

    public OkFaker downloadExtension(DownloadExtension downloadExtension) {
        mDownloadExtension = downloadExtension;
        if (mDownloadExtension != null) {
            mDownloadExtension.install(this);
        }
        return this;
    }

    public OkFaker addEncodedQueryParameter(String key, String value) {
        if (mUrlBuilder != null) {
            mUrlBuilder.addEncodedQueryParameter(key, String.valueOf(value));
        }
        return this;
    }

    public OkFaker addQueryParameter(String key, String value) {
        if (mUrlBuilder != null) {
            mUrlBuilder.addQueryParameter(key, String.valueOf(value));
        }
        return this;
    }

    public OkFaker addEncodedQueryParameter(String key, int value) {
        if (mUrlBuilder != null) {
            mUrlBuilder.addEncodedQueryParameter(key, Integer.toString(value));
        }
        return this;
    }

    public OkFaker addQueryParameter(String key, int value) {
        if (mUrlBuilder != null) {
            mUrlBuilder.addQueryParameter(key, Integer.toString(value));
        }
        return this;
    }

    public OkFaker addEncodedQueryParameter(String key, long value) {
        if (mUrlBuilder != null) {
            mUrlBuilder.addEncodedQueryParameter(key, Long.toString(value));
        }
        return this;
    }

    public OkFaker addQueryParameter(String key, long value) {
        if (mUrlBuilder != null) {
            mUrlBuilder.addQueryParameter(key, Long.toString(value));
        }
        return this;
    }

    public OkFaker addEncodedQueryParameter(String key, float value) {
        if (mUrlBuilder != null) {
            mUrlBuilder.addEncodedQueryParameter(key, Float.toString(value));
        }
        return this;
    }

    public OkFaker addQueryParameter(String key, float value) {
        if (mUrlBuilder != null) {
            mUrlBuilder.addQueryParameter(key, Float.toString(value));
        }
        return this;
    }

    public OkFaker addEncodedQueryParameter(String key, double value) {
        if (mUrlBuilder != null) {
            mUrlBuilder.addEncodedQueryParameter(key, Double.toString(value));
        }
        return this;
    }

    public OkFaker addQueryParameter(String key, double value) {
        if (mUrlBuilder != null) {
            mUrlBuilder.addQueryParameter(key, Double.toString(value));
        }
        return this;
    }

    public OkFaker removeAllQueryParameters(String key) {
        if (mUrlBuilder != null) {
            mUrlBuilder.removeAllQueryParameters(key);
        }
        return this;
    }

    public OkFaker removeAllEncodedQueryParameters(String key) {
        if (mUrlBuilder != null) {
            mUrlBuilder.removeAllEncodedQueryParameters(key);
        }
        return this;
    }

    public OkFaker body(MediaType mediaType, String body) {
        mBody = RequestBody.create(mediaType, String.valueOf(body));
        return this;
    }

    public OkFaker body(MediaType mediaType, File file) {
        mBody = RequestBody.create(mediaType, file);
        return this;
    }

    public OkFaker body(RequestBody body) {
        mBody = body;
        return this;
    }

    public OkFaker addFormParameter(String key, String value) {
        ensureFormBody();
        mFormBuilder.add(key, String.valueOf(value));
        return this;
    }

    public OkFaker addFormParameter(String key, int value) {
        ensureFormBody();
        mFormBuilder.add(key, Integer.toString(value));
        return this;
    }

    public OkFaker addFormParameter(String key, long value) {
        ensureFormBody();
        mFormBuilder.add(key, Long.toString(value));
        return this;
    }

    public OkFaker addFormParameter(String key, float value) {
        ensureFormBody();
        mFormBuilder.add(key, Float.toString(value));
        return this;
    }

    public OkFaker addFormParameter(String key, double value) {
        ensureFormBody();
        mFormBuilder.add(key, Double.toString(value));
        return this;
    }

    public OkFaker addFormParameter(String key, boolean value) {
        ensureFormBody();
        mFormBuilder.add(key, Boolean.toString(value));
        return this;
    }

    public OkFaker addFormParameters(Parameters value) {
        ensureFormBody();
        for (Map.Entry<String, String> entry : value) {
            mFormBuilder.add(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public OkFaker addFormParameter(String key, Parameters value) {
        ensureFormBody();
        mFormBuilder.add(key, String.valueOf(value));
        return this;
    }

    public OkFaker addEncodedFormParameter(String key, String value) {
        ensureFormBody();
        mFormBuilder.addEncoded(key, String.valueOf(value));
        return this;
    }

    public OkFaker addEncodedFormParameter(String key, int value) {
        ensureFormBody();
        mFormBuilder.addEncoded(key, Integer.toString(value));
        return this;
    }

    public OkFaker addEncodedFormParameter(String key, long value) {
        ensureFormBody();
        mFormBuilder.addEncoded(key, Long.toString(value));
        return this;
    }

    public OkFaker addEncodedFormParameter(String key, float value) {
        ensureFormBody();
        mFormBuilder.addEncoded(key, Float.toString(value));
        return this;
    }

    public OkFaker addEncodedFormParameter(String key, double value) {
        ensureFormBody();
        mFormBuilder.addEncoded(key, Double.toString(value));
        return this;
    }

    public OkFaker addEncodedFormParameter(String key, boolean value) {
        ensureFormBody();
        mFormBuilder.addEncoded(key, Boolean.toString(value));
        return this;
    }

    public OkFaker addEncodedFormParameters(Parameters value) {
        ensureFormBody();
        for (Map.Entry<String, String> entry : value) {
            mFormBuilder.addEncoded(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public OkFaker addEncodedFormParameter(String key, Parameters value) {
        ensureFormBody();
        mFormBuilder.addEncoded(key, String.valueOf(value));
        return this;
    }

    public OkFaker addPart(MultipartBody.Part part) {
        ensureMultiBody();
        mMultiBuilder.addPart(part);
        return this;
    }

    public OkFaker addPart(RequestBody body) {
        ensureMultiBody();
        mMultiBuilder.addPart(body);
        return this;
    }

    public OkFaker addPart(Headers headers, RequestBody body) {
        ensureMultiBody();
        mMultiBuilder.addPart(headers, body);
        return this;
    }

    public OkFaker addFormDataPart(String name, String value) {
        ensureMultiBody();
        mMultiBuilder.addFormDataPart(name, String.valueOf(value));
        return this;
    }

    public OkFaker addFormDataPart(String name, int value) {
        ensureMultiBody();
        mMultiBuilder.addFormDataPart(name, Integer.toString(value));
        return this;
    }

    public OkFaker addFormDataPart(String name, long value) {
        ensureMultiBody();
        mMultiBuilder.addFormDataPart(name, Long.toString(value));
        return this;
    }

    public OkFaker addFormDataPart(String name, float value) {
        ensureMultiBody();
        mMultiBuilder.addFormDataPart(name, Float.toString(value));
        return this;
    }

    public OkFaker addFormDataPart(String name, double value) {
        ensureMultiBody();
        mMultiBuilder.addFormDataPart(name, Double.toString(value));
        return this;
    }

    public OkFaker addFormDataPart(String name, boolean value) {
        ensureMultiBody();
        mMultiBuilder.addFormDataPart(name, Boolean.toString(value));
        return this;
    }

    public OkFaker addFormDataPart(String name, Parameters value) {
        ensureMultiBody();
        mMultiBuilder.addFormDataPart(name, String.valueOf(value));
        return this;
    }

    public OkFaker addFormDataPart(String name, @Nullable String filename, RequestBody body) {
        ensureMultiBody();
        mMultiBuilder.addFormDataPart(name, filename, body);
        return this;
    }

    public OkFaker addFormDataPart(String name, @Nullable MediaType mediaType, File file) {
        ensureMultiBody();
        mMultiBuilder.addFormDataPart(name, file.getName(), RequestBody.create(mediaType, file));
        return this;
    }

    public OkFaker setHeader(String key, String value) {
        mRequestBuilder.header(key, String.valueOf(value));
        return this;
    }

    public OkFaker setHeader(String key, int value) {
        mRequestBuilder.header(key, Integer.toString(value));
        return this;
    }

    public OkFaker setHeader(String key, long value) {
        mRequestBuilder.header(key, Long.toString(value));
        return this;
    }

    public OkFaker setHeader(String key, float value) {
        mRequestBuilder.header(key, Float.toString(value));
        return this;
    }

    public OkFaker setHeader(String key, double value) {
        mRequestBuilder.header(key, Double.toString(value));
        return this;
    }

    public OkFaker setHeader(String key, boolean value) {
        mRequestBuilder.header(key, Boolean.toString(value));
        return this;
    }


    public OkFaker addHeader(String key, String value) {
        mRequestBuilder.addHeader(key, String.valueOf(value));
        return this;
    }

    public OkFaker addHeader(String key, int value) {
        mRequestBuilder.addHeader(key, Integer.toString(value));
        return this;
    }

    public OkFaker addHeader(String key, long value) {
        mRequestBuilder.addHeader(key, Long.toString(value));
        return this;
    }

    public OkFaker addHeader(String key, float value) {
        mRequestBuilder.addHeader(key, Float.toString(value));
        return this;
    }

    public OkFaker addHeader(String key, double value) {
        mRequestBuilder.addHeader(key, Double.toString(value));
        return this;
    }


    public OkFaker addHeader(String key, boolean value) {
        mRequestBuilder.addHeader(key, Boolean.toString(value));
        return this;
    }

    public OkFaker removeHeader(String key) {
        mRequestBuilder.removeHeader(key);
        return this;
    }

    private void ensureFormBody() {
        if (mFormBuilder == null) {
            mFormBuilder = new FormBody.Builder();
        }
        mMultiBuilder = null;
    }

    private void ensureMultiBody() {
        if (mMultiBuilder == null) {
            mMultiBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        }
        mFormBuilder = null;
    }

    /**
     * response convert to <T> which you need even request error
     * <p>
     * you must make the <T> type equals the method enqueue or safeExecute return type
     */
    public <T> OkFaker mapResponseEvenError(OkFunction<Response, OkSource<T>> func1, OkFunction<Throwable, OkSource<T>> func2) {
        if (func1 != null && func2 != null) {
            mResponseFunc = func1;
            mErrorResumeFunc = func2;
        }
        return this;
    }

    /**
     * response convert to <T> which you need
     * <p>
     * you must make the <T> type equals the method enqueue or safeExecute return type
     */
    public <T> OkFaker mapResponse(OkFunction<Response, OkSource<T>> func) {
        mResponseFunc = func;
        return this;
    }

    /**
     * response convert to <T> which you need
     * <p>
     * you must make the <T> type equals the method enqueue or safeExecute return type
     */
    public <T> OkFaker mapError(OkFunction<Throwable, OkSource<T>> func) {
        mErrorResumeFunc = func;
        return this;
    }

    public <T> OkFaker doOnSuccess(OkConsumer<T> consumer) {
        mResultConsumer = consumer;
        return this;
    }

    public OkFaker doOnError(OkConsumer<Throwable> consumer) {
        mErrorConsumer = consumer;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T execute() throws Exception {
        Response response = rawExecute();

        if (mResponseFunc == null) {
            throw new IllegalStateException("you must call the method mapResponse");
        }

        OkSource<?> source = mResponseFunc.apply(response);

        if (source.getError() != null) {
            throw new Exception(source.getError());
        }

        return (T) source.getSource();
    }

    @SuppressWarnings("unchecked")
    public <T> T safeExecute(T defResult) {
        try {
            Response response = rawExecute();

            if (mResponseFunc == null) {
                throw new IllegalStateException("you must call the method mapResponse or mapResponseEvenError");
            }

            OkSource<?> source = mResponseFunc.apply(response);

            if (source.getError() != null) {
                throw source.getError();
            }

            return (T) source.getSource();
        } catch (Throwable t) {
            if (mErrorResumeFunc != null) {
                try {
                    OkSource<?> source = mErrorResumeFunc.apply(t);
                    if (source.getError() != null) {
                        throw source.getError();
                    }
                    return (T) source.getSource();
                } catch (Throwable t1) {
                    t1.printStackTrace();
                }
            } else {
                t.printStackTrace();
            }
        }
        return defResult;
    }

    @Nullable
    public <T> T safeExecute() {
        return safeExecute(null);
    }

    public Response rawExecute() throws Exception {
        Call call;

        synchronized (this) {
            if (mExecuted) throw new IllegalStateException("Already Executed");
            mExecuted = true;

            call = mCall;

            if (mCreationFailure != null) {
                throw (Exception) mCreationFailure;
            }

            if (call == null) {
                try {
                    call = mCall = createCall();
                } catch (Exception e) {
                    mCreationFailure = e;
                    throw e;
                }
            }
        }

        return call.execute();
    }

    private Call createCall() throws Exception {
        if (mUrlBuilder == null) {
            throw new IllegalArgumentException("request Url is null or invalid");
        }

        final Request.Builder builder;
        switch (mMethod) {
            case POST:
                if (mMultiBuilder != null) {
                    mBody = mMultiBuilder.build();
                } else if (mFormBuilder != null) {
                    mBody = mFormBuilder.build();
                } else if (mBody == null) {
                    mBody = new FormBody.Builder().build();
                }
                builder = mRequestBuilder.url(mUrlBuilder.build()).post(mBody);
                break;
            case GET:
            default:
                builder = mRequestBuilder.url(mUrlBuilder.build()).get();
                break;
        }

        if (mDownloadExtension != null) {
            mDownloadExtension.addHeader(builder);
        }

        return checkClient(mClient).newCall(builder.build());
    }

    private OkHttpClient checkClient(OkHttpClient client) {
        if (client == null) {
            throw new IllegalArgumentException("request must have a OkHttpClient");
        }

        if (mDownloadExtension == null) return client;

        OkHttpClient.Builder builder = client.newBuilder();

        for (Interceptor interceptor : builder.interceptors()) {
            if (interceptor instanceof HttpLoggingInterceptor) {
                HttpLoggingInterceptor loggingInterceptor = ((HttpLoggingInterceptor) interceptor);
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            }
        }
        return builder.build();
    }

    public <T> void enqueue(final OkCallback<T> callback) {
        Call call;
        Throwable failure;

        synchronized (this) {
            call = mCall;
            failure = mCreationFailure;

            if (mExecuted) failure = new IllegalStateException("Already Executed");
            mExecuted = true;

            if (call == null && failure == null) {
                try {
                    call = mCall = createCall();
                } catch (Exception t) {
                    failure = t;
                }
            }
        }

        if (failure != null) {
            onError(mErrorConsumer, callback, failure);
            return;
        }

        if (callback instanceof OkStartedCallback && !mCanceled) {
            onStart((OkStartedCallback<?>) callback);
        }

        call.enqueue(new okhttp3.Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callOnFailure(callback, e);
            }


            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (callback instanceof OkDownloadCallback) {
                    callOnDownloadResponse((OkDownloadCallback) callback, response);
                } else {
                    callOnResponse(callback, response);
                }
            }
        });
    }

    public void enqueue() {
        enqueue(null);
    }

    @SuppressWarnings("unchecked")
    private <T> void callOnFailure(OkCallback<T> callback, Exception e) {
        if (mErrorResumeFunc != null) {
            try {
                OkSource<?> source = mErrorResumeFunc.apply(e);
                if (source.getError() != null) {
                    throw source.getError();
                }
                onSuccess((OkConsumer<T>) mResultConsumer, callback, (T) source.getSource());
            } catch (Throwable t) {
                onError(mErrorConsumer, callback, t);
            }
        } else {
            onError(mErrorConsumer, callback, e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void callOnResponse(OkCallback<T> callback, Response response) {
        if (mResponseFunc != null) {
            try {
                OkSource<?> source = mResponseFunc.apply(response);
                if (source.getError() != null) {
                    throw source.getError();
                }
                onSuccess((OkConsumer<T>) mResultConsumer, callback, (T) source.getSource());
            } catch (Throwable t) {
                onError(mErrorConsumer, callback, t);
            }
        } else if (callback instanceof OkGenericCallback) {
            onResponse((OkConsumer<T>) mResultConsumer, (OkGenericCallback<T>) callback, response);
        } else {
            onError(mErrorConsumer, callback, new IllegalStateException("you must call the method mapResponse or mapResponseEvenError else callback must be OkGenericCallback"));
        }
    }

    @SuppressWarnings("unchecked")
    private void callOnDownloadResponse(OkDownloadCallback callback, Response response) {
        try {
            if (mDownloadExtension == null) {
                throw new NullPointerException("download extension must not be null");
            }

            final DownloadExtension.OnProgressListener progressListener = new DownloadExtension.OnProgressListener() {
                @Override
                public void onProgress(long downloadedBytes, long totalBytes) {
                    OkFaker.this.onProgress(callback, downloadedBytes, totalBytes);
                }
            };

            File destFile = mDownloadExtension.download(response, progressListener);
            if (destFile != null) {
                onSuccess((OkConsumer<File>) mResultConsumer, callback, destFile);
            } else if (isCanceled()) {
                onCancel(callback);
            } else {
                onError(mErrorConsumer, callback, new IOException("download not completed, response code: " + response.code() + " , message: " + response.message()));
            }
        } catch (Exception e) {
            onError(mErrorConsumer, callback, e);
        }
    }


    private <T> void onSuccess(OkConsumer<T> consumer, OkCallback<T> callback, T result) {
        acceptResponseConsumer(consumer, result);
        OkCallbacks.success(callback, result);
    }

    private <T> void onResponse(final OkConsumer<T> consumer, OkGenericCallback<T> callback, Response response) {
        if (consumer != null && callback != null) {
            callback.setInternalCallback(result -> acceptResponseConsumer(consumer, result));
        }
        OkCallbacks.response(callback, response);
    }

    private void onError(OkConsumer<Throwable> consumer, OkCallback<?> callback, Throwable error) {
        acceptErrorConsumer(consumer, error);
        OkCallbacks.error(callback, error);
    }

    private void onStart(OkStartedCallback<?> callback) {
        OkCallbacks.start(callback);
    }

    private void onProgress(OkDownloadCallback callback, long downloadedBytes, long totalBytes) {
        if (isCanceled()) return;

        OkCallbacks.progress(callback, downloadedBytes, totalBytes);
    }

    private void onCancel(OkDownloadCallback callback) {
        OkCallbacks.cancel(callback);
    }

    private <T> void acceptResponseConsumer(OkConsumer<T> consumer, T result) {
        if (consumer == null) {
            return;
        }

        try {
            consumer.accept(Objects.requireNonNull(result));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void acceptErrorConsumer(OkConsumer<Throwable> consumer, Throwable error) {
        if (consumer == null) {
            return;
        }

        try {
            consumer.accept(Objects.requireNonNull(error));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Nullable
    public OkHttpClient client() {
        return mClient;
    }

    @Nullable
    public URL url() {
        return mCall != null ? mCall.request().url().url() : null;
    }

    @Nullable
    public Object tag() {
        return mCall != null ? mCall.request().tag() : null;
    }

    @Nullable
    public CacheControl cacheControl() {
        return mCall != null ? mCall.request().cacheControl() : null;
    }

    public boolean isExecuted() {
        if (mExecuted) {
            return true;
        }
        synchronized (this) {
            return mCall != null && mCall.isExecuted();
        }
    }

    public boolean isCanceled() {
        if (mCanceled) {
            return true;
        }
        synchronized (this) {
            return mCall != null && mCall.isCanceled();
        }
    }

    public void cancel() {
        if (mCanceled) {
            return;
        }

        mCanceled = true;

        Call call;
        synchronized (this) {
            call = mCall;
        }
        if (call != null) {
            call.cancel();
        }
    }

    public static class Parameters implements Iterable<Map.Entry<String, String>> {
        private final HashMap<String, String> map = new HashMap<>();

        public Parameters() {
        }

        public Parameters(Map<String, ?> map) {
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                this.map.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        public Parameters(Object copyFrom, boolean serializeNulls) {
            final String jsonString;
            if (copyFrom instanceof String) {
                jsonString = (String) copyFrom;
            } else if (copyFrom instanceof Parameters) {
                jsonString = copyFrom.toString();
            } else {
                jsonString = GsonKt.toJson(copyFrom);
            }
            JsonObject json = GsonKt.toJsonObject(jsonString);
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                final JsonElement element = entry.getValue();
                if ((element == null || element.isJsonNull()) && !serializeNulls) {
                    continue;
                }
                if (element == null || element.isJsonNull()) {
                    map.put(entry.getKey(), "");
                } else if (element.isJsonArray() || element.isJsonObject()) {
                    map.put(entry.getKey(), element.toString());
                } else if (element.isJsonPrimitive()) {
                    map.put(entry.getKey(), element.getAsString());
                }
            }
        }

        public Parameters(Object copyFrom) {
            this(copyFrom, false);
        }

        public Parameters add(String key, String value) {
            map.put(key, value);
            return this;
        }

        public Parameters add(String key, int value) {
            map.put(key, Integer.toString(value));
            return this;
        }

        public Parameters add(String key, long value) {
            map.put(key, Long.toString(value));
            return this;
        }


        public Parameters add(String key, float value) {
            map.put(key, Float.toString(value));
            return this;
        }

        public Parameters add(String key, double value) {
            map.put(key, Double.toString(value));
            return this;
        }

        public Parameters add(String key, boolean value) {
            map.put(key, Boolean.toString(value));
            return this;
        }

        public String remove(String key) {
            return map.remove(key);
        }

        public String get(String key) {
            return key == null ? null : map.get(key);
        }

        @NonNull
        @Override
        public String toString() {
            return GsonKt.toJson(map);
        }

        @NonNull
        public String toArrayString() {
            return "[" + toString() + "]";
        }

        @NonNull
        @Override
        public Iterator<Map.Entry<String, String>> iterator() {
            return map.entrySet().iterator();
        }
    }

}
