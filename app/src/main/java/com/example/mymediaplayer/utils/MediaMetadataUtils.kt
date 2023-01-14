package com.example.mymediaplayer.utils

import android.support.v4.media.MediaMetadataCompat

object MediaMetadataUtils {
    fun createMetadata(
        mediaId: String,
        title: String,
        subtitle: String,
        description: String,
        artUri: String,
        mediaUri: String,
        duration: Long
    ): MediaMetadataCompat {
        return MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, subtitle)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, description)
            .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, artUri)
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mediaUri)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
            .build()
    }
}