package com.example.mymediaplayer.utils

import android.content.ComponentName
import android.content.Context
import androidx.annotation.VisibleForTesting
import com.example.mymediaplayer.IMediaServiceConnection
import com.example.mymediaplayer.MediaServiceConnection
import com.example.mymediaplayer.service.MediaPlaybackService
import com.example.mymediaplayer.viewmodels.MediaDataViewModel
import com.example.mymediaplayer.viewmodels.MediaPlayerViewModel
import com.example.mymediaplayer.viewmodels.PlayerViewModel

object InjectorUtils {

    @Volatile
    var mediaServiceConnection: IMediaServiceConnection? = null
        @VisibleForTesting set

    private fun provideMediaServiceConnection(context: Context): IMediaServiceConnection {
        synchronized(this) {
            return mediaServiceConnection ?: MediaServiceConnection.getInstance(
                context,
                ComponentName(context, MediaPlaybackService::class.java)
            )
        }
    }

    fun mediaDataViewModelFactory(context: Context, browseId: String?): MediaDataViewModel.Factory {
        return MediaDataViewModel.Factory(
            provideMediaServiceConnection(context.applicationContext), browseId)
    }

    fun smallPlayerViewModelFactory(context: Context): PlayerViewModel.Factory {
        return PlayerViewModel.Factory(
            provideMediaServiceConnection(context.applicationContext))
    }

    fun mediaPlayerViewModelFactory(context: Context): MediaPlayerViewModel.Factory {
        return MediaPlayerViewModel.Factory(
            provideMediaServiceConnection(context.applicationContext))
    }
}