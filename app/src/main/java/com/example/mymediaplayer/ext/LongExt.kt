package com.example.mymediaplayer.ext

import java.util.concurrent.TimeUnit

fun Long.durationText(): String {
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this)
    return "%d:%02d".format(minutes,seconds)
}