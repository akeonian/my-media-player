package com.example.mymediaplayer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import com.example.mymediaplayer.player.SessionPlayer
import com.example.mymediaplayer.source.MediaSource
import com.example.mymediaplayer.utils.LogUtils

/**
 * - MediaSessionManger manages the MediaSessionCompat.Callback,
 * - joins a MediaRepository with a SessionPlayer, and handles
 * the queue operations
 * */
private const val TAG = "MediaPlaybackManager"

class MediaPlaybackManager(
    private val context: Context,
    private val repository: MediaSource,
    private val sessionPlayer: SessionPlayer
) {

    private val audioManager = context.getSystemService(
        Context.AUDIO_SERVICE
    ) as AudioManager

    private var callback: Callback? = null
    private val audioHandler = AudioFocusHandler(sessionPlayer)

    private var noisyReceiverRegistered = false
    private val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                audioHandler.play(audioManager)
            }
        }
    }
    private var lastPlaybackState = PlaybackStateCompat.STATE_NONE
    private val playbackStateBuilder = PlaybackStateCompat.Builder()
        .setActions(
            PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_STOP or
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        )
        .setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f)
    private val wakeLock: WakeLock

    init {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG
        )
        sessionPlayer.setPlaybackStateListener {
            val playbackState = playbackStateBuilder.apply {
                lastPlaybackState = when (it) {
                    SessionPlayer.State.INITIALIZED -> PlaybackStateCompat.STATE_NONE
                    SessionPlayer.State.PREPARING -> PlaybackStateCompat.STATE_CONNECTING
                    SessionPlayer.State.PREPARED -> PlaybackStateCompat.STATE_PAUSED
                    SessionPlayer.State.ERROR -> {
                        setErrorMessage(
                            sessionPlayer.errorCode,
                            sessionPlayer.errorMessage
                        )
                        PlaybackStateCompat.STATE_ERROR
                    }
                    SessionPlayer.State.BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
                    SessionPlayer.State.PLAYING -> PlaybackStateCompat.STATE_PLAYING
                    SessionPlayer.State.ENDED,
                    SessionPlayer.State.PAUSED -> PlaybackStateCompat.STATE_PAUSED
                    SessionPlayer.State.STOPPED -> PlaybackStateCompat.STATE_STOPPED
                }
                setState(
                    lastPlaybackState,
                    sessionPlayer.seekPosition,
                    1.0f,
                    System.currentTimeMillis()
                )
            }.build()
            callback?.onPlaybackStateChanged(playbackState)
        }
    }

    private fun safelyRegisterReceiver() {
        if (!noisyReceiverRegistered) {
            context.registerReceiver(noisyReceiver, intentFilter)
            noisyReceiverRegistered = true
        }
    }

    private fun safelyUnregisterReceiver() {
        if (noisyReceiverRegistered) {
            context.unregisterReceiver(noisyReceiver)
            noisyReceiverRegistered = false
        }
    }

    /**
     * This releases all the resources, including the ones passed
     * in the constructor and also the callback
     * */
    fun release() {
        callback = null
        audioHandler.pause(audioManager, true)
        sessionPlayer.release()
        safelyUnregisterReceiver()
        if (wakeLock.isHeld) wakeLock.release()
    }

    fun getCurrentState(): PlaybackStateCompat {
        return playbackStateBuilder.build()
    }

    fun setCallback(managerCallback: Callback) {
        callback = managerCallback
    }

    // only those actions which start the playback should
    // call startForeground as the MediaButtonReceiver
    // will not directly call startForegroundService
    // when ACTION_MEDIA_BUTTON is not declared
    val sessionCallback = object : MediaSessionCompat.Callback() {

        override fun onPlay() {
            LogUtils.d("TAG", "onPlay")
            wakeLock.acquire(10*60*1000L /*10 minutes*/)
            audioHandler.play(audioManager)
            safelyRegisterReceiver()
            callback?.onStartingPlayback()
        }

        override fun onPause() {
            LogUtils.d(TAG, "onPause")
            if (wakeLock.isHeld) wakeLock.release()
            audioHandler.pause(audioManager)
            safelyUnregisterReceiver()
        }

        override fun onStop() {
            LogUtils.d(TAG, "onStop")
            if (wakeLock.isHeld) wakeLock.release()
            audioHandler.pause(audioManager, true)
            safelyUnregisterReceiver()
        }

        override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
            mediaId?.apply {
                repository.findMediaItemById(this)?.let {
                    callback?.onMetadataChanged(it)
                    it.description.mediaUri?.apply {
                        sessionPlayer.prepare(this)
                    }
                }
            }
        }

        override fun onPrepareFromSearch(query: String?, extras: Bundle?) {
            TODO("Prepare from search not yet implemented")
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            LogUtils.d(TAG, "onPlayFromMediaId")
            mediaId?.apply {
                repository.findMediaItemById(this)?.let {
                    LogUtils.d(TAG, "onItem found")
                    callback?.onMetadataChanged(it)
                    LogUtils.d(TAG, "mediaUri=${it.description.mediaUri}")
                    it.description.mediaUri?.apply {
                        sessionPlayer.prepare(this)
                    }
                }
                onPlay()
            }
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            Toast.makeText(
                context.applicationContext,
                "Not yet implemented",
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            Toast.makeText(
                context.applicationContext,
                "Not yet implemented",
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onSeekTo(pos: Long) {
            sessionPlayer.seekPosition = pos
            val newState = playbackStateBuilder.setState(
                    lastPlaybackState,
                    pos,
                    1.0f,
                    System.currentTimeMillis()
                ).build()
            callback?.onPlaybackStateChanged(newState)
        }
    }

    private class AudioFocusHandler(private val sessionPlayer: SessionPlayer) {

        private var resumeOnFocus = false
        private var playbackDelayed = false
        private val audioFocusChangeListener = OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> if (resumeOnFocus || playbackDelayed) {
                    resumeOnFocus = false
                    playbackDelayed = false
                    sessionPlayer.playWhenReady = true
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    resumeOnFocus = false
                    playbackDelayed = false
                    sessionPlayer.playWhenReady = false
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    resumeOnFocus = sessionPlayer.playWhenReady
                    sessionPlayer.playWhenReady = false
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    sessionPlayer
                }
            }
        }

        fun play(am: AudioManager) {
            // if playback is delayed then do not play right now
            // because it will be played when the audio focus is gained again
            // The player may not be playing even if the playWhenReady is true
            // if the player is in stopped state
            if (!playbackDelayed && (!sessionPlayer.playWhenReady
                        || (sessionPlayer.state == SessionPlayer.State.STOPPED)
                        || (sessionPlayer.state == SessionPlayer.State.ENDED))) {
                val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val request = AudioFocusRequest
                        .Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(audioFocusChangeListener)
                        .build()
                    am.requestAudioFocus(request)
                } else {
                    am.requestAudioFocus(
                        audioFocusChangeListener,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN
                    )
                }

                when (result) {
                    AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                        playbackDelayed = false
                        sessionPlayer.playWhenReady = true
                    }
                    AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> playbackDelayed = true
                    else -> playbackDelayed = false
                }
            }
        }

        fun pause(am: AudioManager, stop: Boolean = false) {
            playbackDelayed = false
            if (stop) {
                sessionPlayer.stop()
            } else {
                sessionPlayer.playWhenReady = false
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val request = AudioFocusRequest
                    .Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .build()
                am.abandonAudioFocusRequest(request)
            } else {
                am.abandonAudioFocus(audioFocusChangeListener)
            }
        }

    }

    interface Callback {

        /* This should be implemented to start the service when the playback is about to start
        * so that the service does not stop even if there are no bound activities
        * */
        fun onStartingPlayback()

        fun onMetadataChanged(metadata: MediaMetadataCompat)

        fun onPlaybackStateChanged(playbackState: PlaybackStateCompat)

    }

    companion object {
        private const val WAKE_LOCK_TAG = "com.example.mymediaplayer:playback_wake_log_tag"
    }
}