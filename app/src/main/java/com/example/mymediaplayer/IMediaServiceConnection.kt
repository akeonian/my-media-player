package com.example.mymediaplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData

interface IMediaServiceConnection {
    val isConnected: MutableLiveData<Boolean>
    val rootMediaId: String
    val playbackState: MutableLiveData<PlaybackStateCompat>
    val nowPlaying: MutableLiveData<MediaMetadataCompat>
    val transportControls: MediaControllerCompat.TransportControls

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback)
    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback)
    fun sendAccessGranted()
}