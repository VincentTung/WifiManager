package com.vincent.lib.wifimanger.util;

import io.reactivex.disposables.Disposable;

public class DisposableUtil {

    public static void dispose(Disposable disposable) {

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        disposable = null;
    }

    public static boolean isValid(Disposable disposable) {
        return null != disposable && !disposable.isDisposed();
    }
}