package com.iflytek.util.log;

import android.os.SystemClock;
import android.util.Log;
/**
 * 带运行时间值的Logging.
 * @author zhangyun
 *
 */
public class LoggingTime {

    private static long mLogTime = 0;

    /** Reset time value to elapsed real time. */
    public static void resetTime() {
        mLogTime = SystemClock.elapsedRealtime();
    }

    /** Add time value to TAG */
    private static String addTimeMsg(String msg) {
        return msg + " T:" + (SystemClock.elapsedRealtime() - mLogTime);
    }

    public static void v(String tag, String msg) {
        if (Logging.mLoggingEnabled) {
            
            Log.v(tag, addTimeMsg(msg));
        }
    }

    public static void d(String tag, String msg) {
        if (Logging.mLoggingEnabled) {
            
            Log.d(tag, addTimeMsg(msg));
        }
    }

    public static void i(String tag, String msg) {
        if (Logging.mLoggingEnabled) {
            
            Log.i(tag, addTimeMsg(msg));
        }
    }

    public static void w(String tag, String msg) {
        if (Logging.mLoggingEnabled) {
            
            Log.w(tag, addTimeMsg(msg));
        }
    }

    public static void e(String tag, String msg) {
        if (Logging.mLoggingEnabled) {
            
            Log.e(tag, addTimeMsg(msg));
        }
    }

    public static int v(String tag, String msg, Throwable tr) {
        int result = 0;
        if (Logging.mLoggingEnabled) {
            result = Log.v(tag, msg, tr);
        }
        return result;
    }
    public static int d(String tag, String msg, Throwable tr) {
        int result = 0;
        if (Logging.mLoggingEnabled) {
            result = Log.d(tag, msg, tr);
        }
        return result;
    }
    public static int i(String tag, String msg, Throwable tr) {
        int result = 0;
        if (Logging.mLoggingEnabled) {
            result = Log.i(tag, msg, tr);
        }
        return result;
    }
    public static int w(String tag, String msg, Throwable tr) {
        int result = 0;
        if (Logging.mLoggingEnabled) {
            result = Log.w(tag, msg, tr);
        }
        return result;
    }
    public static int e(String tag, String msg, Throwable tr) {
        int result = 0;
        if (Logging.mLoggingEnabled) {
            result = Log.e(tag, msg, tr);
        }
        return result;
    } 
}
