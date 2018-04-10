package com.github.util;

import android.util.Log;

/**
 * Logger.
 */
public class LogWrapper {

    public static void e(String tag, String msg, Throwable e) {
        Log.e(tag, msg, e);
    }

}
