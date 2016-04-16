package com.chenxu.jsonparser;

import android.util.Log;

/**
 * Created by chenxu on 2016/4/16.
 */
public class LogUtil {
    public static final boolean DEBUG=true;
    public static void i(String tag, String msg){
        if (DEBUG){
            Log.i(tag, msg);
        }
    }

    public static void ii(String msg){
        if (DEBUG){
            Log.i("chenxu", msg);
        }
    }
}
