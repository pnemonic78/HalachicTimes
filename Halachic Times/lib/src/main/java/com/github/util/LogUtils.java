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

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void v(String tag, String msg) {
        Log.v(tag, msg);
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }
}
