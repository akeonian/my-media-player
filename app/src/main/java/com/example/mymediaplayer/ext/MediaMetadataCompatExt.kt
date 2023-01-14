package com.example.mymediaplayer.ext

import android.net.Uri
import android.support.v4.media.MediaMetadataCompat

val MediaMetadataCompat.id: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)

val MediaMetadataCompat.title: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_TITLE)

val MediaMetadataCompat.subtitle: String?
    get() = getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE)

val MediaMetadataCompat.albumUri: Uri?
    get() = getString(MediaMetadataCompat.METADATA_KEY_ART_URI)?.run { Uri.parse(this) }

val MediaMetadataCompat.maxDuration : Long
    get() = getLong(MediaMetadataCompat.METADATA_KEY_DURATION)