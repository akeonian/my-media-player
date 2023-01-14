package com.example.mymediaplayer.source

import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaMetadataCompat

interface MediaSource {

    suspend fun load()

    fun whenReady (performAction: (Boolean) -> Unit): Boolean

    fun findMediaItemById(mediaId: String): MediaMetadataCompat?

    fun getRootId(): String

    fun getChildren(browseId: String): MutableList<MediaItem>

}

class UnknownBrowseIdException(id: String): Exception("Unknown Browse Id=$id")