package com.example.mymediaplayer

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData
import com.example.mymediaplayer.ext.id
import com.example.mymediaplayer.utils.LogUtils

private const val TAG = "MediaServiceConnection"
const val ACTION_PERMISSION_GRANTED = "com.example.mymediaplayer.action.permission_granted"

class MediaServiceConnection private constructor(
    applicationContext: Context, serviceComponent: ComponentName
) : IMediaServiceConnection {

    override val isConnected = MutableLiveData(false)

    override val rootMediaId: String get() = mediaBrowser.root

    override val playbackState = MutableLiveData(EMPTY_PLAYBACK_STATE)
    override val nowPlaying = MutableLiveData(NOTHING_PLAYING)

    override val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(applicationContext)
    private val mediaBrowser = MediaBrowserCompat(
        applicationContext,
        serviceComponent,
        mediaBrowserConnectionCallback,
        null
    ).apply { connect() }
    private lateinit var mediaController: MediaControllerCompat

    override fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) =
        mediaBrowser.subscribe(parentId, callback)

    override fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) =
        mediaBrowser.unsubscribe(parentId, callback)

    override fun sendPermissionChanged() =
        mediaBrowser.sendCustomAction(ACTION_PERMISSION_GRANTED, null, null)


    private inner class MediaBrowserConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
            LogUtils.d(TAG, "onConnected()")
            isConnected.postValue(true)
        }

        override fun onConnectionSuspended() = isConnected.postValue(false)

        override fun onConnectionFailed() = isConnected.postValue(false)

    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            playbackState.postValue(state ?: EMPTY_PLAYBACK_STATE)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            nowPlaying.postValue(
                if (metadata?.id == null)
                    NOTHING_PLAYING
                else
                    metadata
            )
        }

        override fun onSessionDestroyed() = mediaBrowserConnectionCallback.onConnectionSuspended()
    }

    companion object {
        @Volatile
        private var INSTANCE: MediaServiceConnection? = null

        fun getInstance(context: Context, serviceComponent: ComponentName) =
            INSTANCE ?: synchronized(this) {
                MediaServiceConnection(context, serviceComponent)
                    .also { INSTANCE = it }
            }
    }
}

val EMPTY_PLAYBACK_STATE: PlaybackStateCompat = PlaybackStateCompat.Builder()
    .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
    .build()

val NOTHING_PLAYING: MediaMetadataCompat = MediaMetadataCompat.Builder()
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
    .build()