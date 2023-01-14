package com.example.mymediaplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver

private const val NOTIFICATION_ID = 1000

class MediaNotificationManager(
    private val context: Context, private val mediaSession: MediaSessionCompat) {

    private var isOnGoing = false
    private var currentState: PlaybackStateCompat? = null
    private var currentMetadata: MediaMetadataCompat? = null
    private var notificationListener: NotificationListener? = null

    private val prevAction = NotificationCompat.Action(
        android.R.drawable.ic_media_previous,
        context.getString(R.string.previous),
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        )
    )
    private val nextAction = NotificationCompat.Action(
        android.R.drawable.ic_media_next,
        context.getString(R.string.next),
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        )
    )
    private val playAction = NotificationCompat.Action(
        android.R.drawable.ic_media_play,
        context.getString(R.string.play),
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_PLAY_PAUSE
        )
    )
    private val pauseAction = NotificationCompat.Action(
        android.R.drawable.ic_media_pause,
        context.getString(R.string.pause),
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_PLAY_PAUSE
        )
    )
    private val stopPendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
        context,
        PlaybackStateCompat.ACTION_STOP
    )
    private fun newBuilder() : NotificationCompat.Builder =
        NotificationCompat.Builder(context, MEDIA_NOTIFICATION_CHANNEL_ID)
            .setContentIntent(mediaSession.controller.sessionActivity)
            .setDeleteIntent(stopPendingIntent)
            .setOngoing(isOnGoing)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_headphones)
            .setSilent(true)
            .setVibrate(null)

    private val nm = context.getSystemService(
        Context.NOTIFICATION_SERVICE) as NotificationManager

    fun getId() = NOTIFICATION_ID

    fun getNotification(): Notification {
        return if (currentMetadata == null) {
            newBuilder().setContentTitle("No Media Playing(Default message for no media)")
                .build()
        } else {
            val desc = currentMetadata!!.description
            newBuilder().apply {
                setContentTitle(desc.title)
                setContentText(desc.subtitle)
                setSubText(desc.description)
                setLargeIcon(desc.iconBitmap)
                currentState?.let {
                    val ppAction = when (it.state) {
                        PlaybackStateCompat.STATE_PLAYING -> pauseAction
                        else -> playAction
                    }
                    addAction(prevAction)
                    addAction(ppAction)
                    addAction(nextAction)
                    setStyle(
                        MediaStyle()
                            .setMediaSession(mediaSession.sessionToken)
                            .setShowActionsInCompactView(0, 1, 2)
                            .setShowCancelButton(true)
                            .setCancelButtonIntent(stopPendingIntent)
                    )
                }
            }.build()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (nm.getNotificationChannel(MEDIA_NOTIFICATION_CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    MEDIA_NOTIFICATION_CHANNEL_ID,
                    "MediaPlayer",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                nm.createNotificationChannel(channel)
            }
        }
    }

    fun postNotification() {
        createNotificationChannel()
        val notification = getNotification()
        nm.notify(NOTIFICATION_ID, notification)
        notificationListener?.onNotificationPosted(NOTIFICATION_ID, notification, isOnGoing)
    }

    fun cancelNotification() {
        nm.cancel(NOTIFICATION_ID)
        notificationListener?.onNotificationCancelled()
    }

    fun setMetadata(metadata: MediaMetadataCompat) {
        currentMetadata = metadata
    }

    fun setPlaybackState(playbackState: PlaybackStateCompat) {
        currentState = playbackState
    }

    fun setOnGoing(ongoing: Boolean) {
        isOnGoing = ongoing
    }

    fun setNotificationListener(listener: NotificationListener) {
        notificationListener = listener
    }

    interface NotificationListener {
        fun onNotificationPosted(notificationId: Int, notification: Notification, onGoing: Boolean)
        fun onNotificationCancelled()
    }
}