package com.example.mymediaplayer.viewmodels

import androidx.lifecycle.*
import com.example.mymediaplayer.IMediaServiceConnection
import com.example.mymediaplayer.ext.*
import com.example.mymediaplayer.models.DisplayData
import com.example.mymediaplayer.models.PlayingData

private const val TAG = "PlayerViewModel"

class PlayerViewModel(private val mediaServiceConnection: IMediaServiceConnection): ViewModel() {

    val playingData: LiveData<PlayingData> = Transformations.map(mediaServiceConnection.playbackState) {
        PlayingData(
            isPlaying = it.isPlaying,
            seekPosition = it.position,
            lastUpdateTime = it.lastPositionUpdateTime,
            playingSpeed = it.playbackSpeed
        )
    }

    // This is observed by the MediaDataViewModel, so it will be updated with new values
    private val isConnected: LiveData<Boolean> = mediaServiceConnection.isConnected

    val displayData: LiveData<DisplayData> = Transformations.map(mediaServiceConnection.nowPlaying) {
        DisplayData(it.title, it.subtitle, it.albumUri, it.maxDuration)
    }

    fun playPause() {
        // playingData will have null value if it is not observed by any PlayerViewModel user
        if (mediaServiceConnection.playbackState.value?.isPlaying == false) play() else pause()
    }

    private fun play() {
        if (isConnected.value == true)
            mediaServiceConnection.transportControls.play()
    }

    private fun pause() {
        if (isConnected.value == true)
            mediaServiceConnection.transportControls.pause()
    }

    fun playNext() {
        if (isConnected.value == true)
            mediaServiceConnection.transportControls.skipToNext()
    }

    fun playPrevious() {
        if (mediaServiceConnection.isConnected.value == true)
            mediaServiceConnection.transportControls.skipToPrevious()
    }

    fun setSeekPosition(progressMilliSec: Int) {
        if (isConnected.value == true)
            mediaServiceConnection.transportControls.seekTo(progressMilliSec.toLong())
    }

    class Factory(private val connection: IMediaServiceConnection): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PlayerViewModel(connection) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}