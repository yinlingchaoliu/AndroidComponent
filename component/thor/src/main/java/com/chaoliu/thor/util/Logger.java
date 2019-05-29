package com.chaoliu.thor.util;

import android.util.Log;

/**
 * 打印日志统一管理
 */
public final class Logger {

    private static boolean debug = true;

    public static void setEnable(boolean enable) {
        debug = enable;
    }

    public static void v(String tag, String msg) {
        if (debug) Log.v( tag, msg );
    }

    public static void d(String tag, String msg) {
        if (debug) Log.d( tag, msg );
    }

    public static void i(String tag, String msg) {
        if (debug) Log.i( tag, msg );
    }

    public static void w(String tag, String msg) {
        if (debug) Log.w( tag, msg );
    }

    public static void e(String tag, String msg) {
        if (debug) Log.e( tag, msg );
    }
}