package com.jz.upgrade

import android.util.Log

class LogUtil {
    companion object {

        var printLog = false

        fun e(TAG: String, msg: String) {
            if (printLog)
                Log.e(TAG, msg)
        }

        fun i(TAG: String, msg: String) {
            if (printLog)
                Log.i(TAG, msg)
        }

        fun w(TAG: String, msg: String) {
            if (printLog)
                Log.w(TAG, msg)
        }

        fun d(TAG: String, msg: String) {
            if (printLog)
                Log.d(TAG, msg)
        }
    }
}
