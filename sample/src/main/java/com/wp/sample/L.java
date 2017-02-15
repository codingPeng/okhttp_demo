package com.wp.sample;

import android.util.Log;

/**
 * Created by Administrator on 2016/10/10 0005.
 */

public class L {

    private static boolean debug = true;
    private static final String TAG = "my_okhttp";

    public static void e(String msg) {

        if (debug) {
            Log.e(TAG, msg);
        }
    }
}
