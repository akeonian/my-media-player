package com.example.mymediaplayer.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.SubscriptionCallback
import android.util.Log
import androidx.lifecycle.*
import com.example.mymediaplayer.*
import com.example.mymediaplayer.ext.id
import com.example.mymediaplayer.ext.isPlayEnabled
import com.example.mymediaplayer.ext.isPlaying
import com.example.mymediaplayer.ext.isPrepared
import com.example.mymediaplayer.models.MediaData
import com.example.mymediaplayer.utils.UriUtils

class MediaDataViewModel(
    private val mediaServiceConnection: IMediaServiceConnection,
    private val browseId: String?
) : ViewModel() {

    private val parentId get() = browseId ?: mediaServiceConnection.rootMediaId

    private val _allSongs = MutableLiveData<List<MediaData>>(emptyList())
    val allSongs: LiveData<List<MediaData>> = _allSongs

    private val _navigationEvent = MutableLiveData<Event<String>>()
    val navigationEvent: LiveData<Event<String>> = _navigationEvent

    private val subscriptionCallback = object : SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            val itemList = children.map { child ->
                val iconUri = child.description.iconUri
                    ?: UriUtils.drawableUri(R.drawable.ic_image)
                MediaData(
                    child.mediaId!!,
                    child.description.title.toString(),
                    child.description.subtitle.toString(),
                    iconUri,
                    child.isBrowsable
                )
            }
            _allSongs.postValue(itemList)
        }

        override fun onError(parentId: String) {
            super.onError(parentId)
            _allSongs.postValue(emptyList())
        }
    }

    private val connectedObserver = Observer<Boolean> {
        if (it) {
            mediaServiceConnection.subscribe(
                parentId, subscriptionCallback)
        }
    }

    init {
        mediaServiceConnection.isConnected.observeForever(connectedObserver)
    }

    fun openMedia(media: MediaData) {
        if (media.isBrowsable) {
            _navigationEvent.value = Event(media.id)
        } else {
            playMedia(media)
        }
    }

    fun refreshData() {
        if (mediaServiceConnection.isConnected.value == true) {
            mediaServiceConnection.unsubscribe(
                parentId, subscriptionCallback)
            mediaServiceConnection.subscribe(
                parentId, subscriptionCallback)
        }
    }

    private fun playMedia(media: MediaData, pauseAllowed: Boolean = true) {
        val nowPlaying = mediaServiceConnection.nowPlaying.value
        val transportControls = mediaServiceConnection.transportControls

        val isPrepared = mediaServiceConnection
            .playbackState.value?.isPrepared ?: false
        if (isPrepared && nowPlaying?.id == media.id) {
            mediaServiceConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (pauseAllowed) transportControls.play() else Unit
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Log.w(
                            TAG, "Playable item clicked but neither" +
                                    " play or pause are enabled! (mediaId=${media.id})"
                        )
                    }
                }
            }
        } else {
            transportControls.playFromMediaId(media.id, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaServiceConnection.isConnected.removeObserver(connectedObserver)
        mediaServiceConnection.isConnected.value?.also { connected ->
            if (connected) mediaServiceConnection.unsubscribe(
                parentId, subscriptionCallback)
        }
    }

    class Factory(
        private val connection: IMediaServiceConnection,
        private val browseId: String?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MediaDataViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MediaDataViewModel(connection, browseId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

private const val TAG = "MediaDataViewModel"