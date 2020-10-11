package com.easy.kotlins.http.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.facebook.stetho.BuildConfig;

import java.util.Objects;

import okhttp3.Response;

/**
 * Create by LiZhanPing on 2020/4/27
 * desc: 请求回调处理
 */
final class OkCallbacks {

    private static final int MSG_WHAT_BASE = 1000000000;
    private static final int MSG_WHAT_ON_START = MSG_WHAT_BASE + 1;
    private static final int MSG_WHAT_ON_SUCCESS = MSG_WHAT_BASE + 2;
    private static final int MSG_WHAT_ON_ERROR = MSG_WHAT_BASE + 3;
    private static final int MSG_WHAT_ON_UPDATE = MSG_WHAT_BASE + 4;
    private static final int MSG_WHAT_ON_CANCEL = MSG_WHAT_BASE + 5;

    static final class MessageBody {
        final OkCallback<?> callback;
        final Object[] args;

        private MessageBody(OkCallback<?> callback, Object... args) {
            this.callback = callback;
            this.args = args;
        }
    }

    static final Handler HANDLER = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            MessageBody body = (MessageBody) msg.obj;
            switch (msg.what) {
                case MSG_WHAT_ON_START: {
                    @SuppressWarnings("unchecked") OkStartedCallback<Object> callback = (OkStartedCallback<Object>) body.callback;
                    callOnStart(callback);
                }
                break;
                case MSG_WHAT_ON_SUCCESS: {
                    @SuppressWarnings("unchecked") OkCallback<Object> callback = (OkCallback<Object>) body.callback;
                    callOnSuccess(callback, body.args[0]);
                }
                break;
                case MSG_WHAT_ON_ERROR: {
                    callOnError(body.callback, (Throwable) body.args[0]);
                }
                break;
                case MSG_WHAT_ON_UPDATE: {
                    callOnProgress((OkDownloadCallback) body.callback, (long) body.args[0], (long) body.args[1]);
                }
                break;
                case MSG_WHAT_ON_CANCEL: {
                    callOnCancel((OkDownloadCallback) body.callback);
                }
                break;
            }
        }
    };

    static <T> void response(final OkGenericCallback<T> callback, final Response response) {
        if (callback != null) {
            T result = null;
            Exception failure = null;
            try {
                result = callback.parseResponse(Objects.requireNonNull(response.body()).string());

                if (result == null) {
                    throw new NullPointerException("result is null");
                }
            } catch (Exception e) {
                failure = e;
            }

            if (failure != null) {
                HANDLER.obtainMessage(MSG_WHAT_ON_ERROR, new MessageBody(callback, failure)).sendToTarget();
            } else {
                HANDLER.obtainMessage(MSG_WHAT_ON_SUCCESS, new MessageBody(callback, result)).sendToTarget();
            }
        }
    }

    static <T> void success(final OkCallback<T> callback, final T result) {
        if (callback != null) {
            HANDLER.obtainMessage(MSG_WHAT_ON_SUCCESS, new MessageBody(callback, result)).sendToTarget();
        }
    }

    static void error(final OkCallback<?> callback, final Throwable error) {
        if (callback != null) {
            HANDLER.obtainMessage(MSG_WHAT_ON_ERROR, new MessageBody(callback, error)).sendToTarget();
        }
    }

    static void start(final OkStartedCallback<?> callback) {
        if (callback != null) {
            HANDLER.obtainMessage(MSG_WHAT_ON_START, new MessageBody(callback)).sendToTarget();
        }
    }

    static void cancel(final OkDownloadCallback callback) {
        if (callback != null) {
            HANDLER.obtainMessage(MSG_WHAT_ON_CANCEL, new MessageBody(callback)).sendToTarget();
        }
    }

    static void progress(final OkDownloadCallback callback, final long downloadedBytes, final long totalBytes) {
        if (callback != null) {
            HANDLER.obtainMessage(MSG_WHAT_ON_UPDATE, new MessageBody(callback, downloadedBytes, totalBytes)).sendToTarget();
        }
    }

    private static void callOnStart(final OkStartedCallback<?> callback) {
        try {
            callback.onStart();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void callOnCancel(final OkDownloadCallback callback) {
        try {
            callback.onCancel();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void callOnProgress(final OkDownloadCallback callback, long downloadedBytes, long totalBytes) {
        try {
            callback.onProgress(downloadedBytes, totalBytes);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static <T> void callOnSuccess(final OkCallback<T> callback, final T result) {
        try {
            callback.onSuccess(result);
        } catch (Exception e) {
            callOnError(callback, e);
        }
    }

    private static void callOnError(final OkCallback<?> callback, Throwable e) {
        try {
            if (BuildConfig.DEBUG) {
                Log.e("OkFaker", e.getLocalizedMessage(), e);
            }

            callback.onError(e);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
