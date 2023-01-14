package com.example.mymediaplayer.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymediaplayer.IMediaServiceConnection

class MediaPlayerViewModel(private val mediaServiceConnection: IMediaServiceConnection): ViewModel() {

    fun sendPermissionChanged() =
        mediaServiceConnection.sendPermissionChanged()

    class Factory(private val connection: IMediaServiceConnection): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MediaPlayerViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MediaPlayerViewModel(connection) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}