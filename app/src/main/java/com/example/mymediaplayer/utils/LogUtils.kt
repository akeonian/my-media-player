package com.example.mymediaplayer.utils

import android.util.Log

object LogUtils {

    var testing = false

    fun d( tag: String, message: String) {
        if (!testing) Log.d(tag, message)
    }

    fun w(tag: String, message: String) {
        if (!testing) Log.w(tag, message)
    }

}