package com.example.mymediaplayer.service

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.mymediaplayer.ACTION_ACCESS_GRANTED
import com.example.mymediaplayer.MediaNotificationManager
import com.example.mymediaplayer.PENDING_INTENT_FLAG_DEFAULT
import com.example.mymediaplayer.READ_PERMISSION
import com.example.mymediaplayer.player.MediaPlayerSP
import com.example.mymediaplayer.source.LocalMediaSource
import com.example.mymediaplayer.source.MediaSource
import com.example.mymediaplayer.source.UnknownBrowseIdException
import com.example.mymediaplayer.utils.LogUtils
import kotlinx.coroutines.*

private const val LOG_TAG = "MediaPlaybackService"
private const val EMPTY_ROOT_ID = "empty_root_id"

const val LOAD_ERROR = "load_error"

class MediaPlaybackService: MediaBrowserServiceCompat() {

    private lateinit var serviceIntent: Intent
    private lateinit var repository: MediaSource
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaPlaybackManager: MediaPlaybackManager
    private lateinit var mediaNotificationManager: MediaNotificationManager

    private var isForeground = false
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onCreate() {
        super.onCreate()
        LogUtils.d(LOG_TAG, "onCreate(), isForeground=$isForeground")
        serviceIntent = Intent(applicationContext, MediaPlaybackService::class.java)
        repository = LocalMediaSource(this)

        checkLoadRepository()

        val sessionActivityPendingIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, PENDING_INTENT_FLAG_DEFAULT)
        }
        mediaSession = MediaSessionCompat(
            this, LOG_TAG
        ).apply {
            isActive = true
            setSessionActivity(sessionActivityPendingIntent)
            setSessionToken(sessionToken)
        }
        mediaPlaybackManager = MediaPlaybackManager(
            this,
            repository,
            MediaPlayerSP(this)
        )

        // Join MediaSession and MediaPlaybackManager together
        mediaSession.setCallback(mediaPlaybackManager.sessionCallback)
        mediaSession.setPlaybackState(mediaPlaybackManager.getCurrentState())
        mediaPlaybackManager.setCallback(MediaPlaybackCallback())

        mediaNotificationManager = MediaNotificationManager(this, mediaSession).apply {
            setNotificationListener(MediaNotificationListener())
        }
    }

    private fun checkLoadRepository() {
        if (ContextCompat.checkSelfPermission(this, READ_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            serviceScope.launch { repository.load() }
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return if (allowBrowsing(clientPackageName, clientUid)) {
            BrowserRoot(repository.getRootId(), null)
        } else {
            BrowserRoot(EMPTY_ROOT_ID, null)
        }
    }

    private fun allowBrowsing(clientPackageName: String, clientUid: Int): Boolean {
        // TODO: Not yet Implemented, should implement checking the package and clientUid which are allowed
        return true
    }

    override fun onCustomAction(action: String, extras: Bundle?, result: Result<Bundle>) {
        if (action == ACTION_ACCESS_GRANTED) {
            Log.d(LOG_TAG, "received access granted message")
            checkLoadRepository()
            result.sendResult(null)
            return
        }
        super.onCustomAction(action, extras, result)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        // Empty roo, no result
        if (EMPTY_ROOT_ID == parentId) {
            result.sendResult(null)
            return
        }

        // result will be detached if result not sent
        val resultSent = repository.whenReady {
            try {
                val items = repository.getChildren(parentId)
                result.sendResult(items)
            } catch (e: UnknownBrowseIdException) {
                val errorExtras = Bundle().apply {
                    putString(LOAD_ERROR, e.message)
                }
                result.sendError(errorExtras)
                e.printStackTrace()
            }
        }

        if (!resultSent) result.detach()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        LogUtils.d(LOG_TAG, "onTaskRemoved, isForeground=$isForeground")
        if (!isForeground) {
            release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtils.d(LOG_TAG, "onDestroy")
        release()
    }

    private fun release() {
        LogUtils.d(LOG_TAG, "release")
        mediaPlaybackManager.release()
        mediaSession.apply {
            isActive = false
            release()
        }
        serviceJob.cancel()
        mediaNotificationManager.cancelNotification()
    }

    private inner class MediaNotificationListener : MediaNotificationManager.NotificationListener {

        private fun startForegroundServiceOnce(notificationId: Int, notification: Notification) {
            if (!isForeground) {
                ContextCompat.startForegroundService(
                    applicationContext, serviceIntent)
                startForeground(notificationId, notification)
                isForeground = true
            }
        }

        private fun stopForegroundOnce(removeNotification: Boolean) {
            if (isForeground) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(if (removeNotification) STOP_FOREGROUND_REMOVE else STOP_FOREGROUND_DETACH)
                } else {
                    stopForeground(removeNotification)
                }
                isForeground = false
            }
        }

        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            onGoing: Boolean
        ) {
            if (onGoing) {
                startForegroundServiceOnce(notificationId, notification)
            } else {
                stopForegroundOnce(false)
            }
        }

        override fun onNotificationCancelled() {
            stopForegroundOnce(true)
            stopSelf()
        }

    }

    private inner class MediaPlaybackCallback: MediaPlaybackManager.Callback {

        override fun onStartingPlayback() {
            // - service should be started explicitly every time the music is played
            // - every time service is started, startForeground should be called
            mediaNotificationManager.apply {
                setOnGoing(true)
                postNotification()
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            mediaSession.setMetadata(metadata)
            mediaNotificationManager.apply {
                setMetadata(metadata)
                postNotification()
            }
        }

        override fun onPlaybackStateChanged(playbackState: PlaybackStateCompat) {
            mediaSession.setPlaybackState(playbackState)
            when (playbackState.state) {
                PlaybackStateCompat.STATE_PAUSED -> {
                    mediaNotificationManager.apply {
                        setOnGoing(false)
                        setPlaybackState(playbackState)
                        postNotification()
                    }
                }
                PlaybackStateCompat.STATE_STOPPED -> {
                    mediaNotificationManager.apply {
                        setOnGoing(false)
                        setPlaybackState(playbackState)
                        cancelNotification()
                    }
                }
                else -> {
                    mediaNotificationManager.apply {
                        setPlaybackState(playbackState)
                        postNotification()
                    }
                }
            }
        }
    }
}