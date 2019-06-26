package com.vincent.lib.wifimanger.util;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

public class ToastUtil {

    private static Toast mToast = null;
    private static Context mContext = null;

    public static void show(Context context, String text) {
        show(context, text, Toast.LENGTH_SHORT);
    }

    public static void show(Context context, String text, int duration) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (mToast == null) {
            mToast = Toast.makeText(context, text, duration);
        } else {
            mToast.setText(text);
            mToast.setDuration(duration);
        }

        mToast.show();

    }

    public static void show(String tip) {
        show(mContext, tip);
    }

    public static void show(int id) {
        String tip = mContext.getString(id);
        show(tip);
    }

    public static void init(Context context) {

        mContext = context;
    }
}