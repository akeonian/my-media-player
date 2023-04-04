package com.example.mymediaplayer

import android.app.PendingIntent
import android.os.Build

val READ_PERMISSION = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
    android.Manifest.permission.READ_EXTERNAL_STORAGE
} else {
    android.Manifest.permission.READ_MEDIA_AUDIO
}
val PENDING_INTENT_FLAG_DEFAULT =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

const val MEDIA_NOTIFICATION_CHANNEL_ID = "com.example.mymediaplayer.channel_id"