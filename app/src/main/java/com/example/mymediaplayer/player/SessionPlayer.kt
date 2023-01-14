package com.example.mymediaplayer.player

import android.net.Uri

/**
 * SessionPlayer interface for {@link MediaBrowserService}
 * Implementation should have:
 * - All the functions of the implementations should be callable
 * from any state of the SessionPlayer.
 * - The implementations can handle the invalid states silently
 * or change states internally to a valid state.
 * - It should not throw but use State.ERROR with errorCode
 * and errorMessage to inform about errors.
 * */
interface SessionPlayer {

    val state: State

    var seekPosition: Long

    val errorCode: Int

    val errorMessage: String

    // Prepares the media from the given Uri
    fun prepare(uri: Uri)

    var playWhenReady: Boolean

    fun stop()

    fun setPlaybackStateListener(listener: PlaybackStateListener)

    fun reset()

    fun release()

    fun interface PlaybackStateListener {
        fun onPlaybackStateChanged(state: State)
    }

    enum class State {
        INITIALIZED, PREPARING, PREPARED, PLAYING, BUFFERING, PAUSED, ENDED, STOPPED, ERROR
    }
}