package com.github.util;

import android.util.Log;

/**
 * Logger utilities.
 */
public class LogUtils {

    public static void e(String tag, String msg, Throwable e) {
        Log.e(tag, msg, e);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

}
