package com.example.mymediaplayer.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.bumptech.glide.Glide.init
import com.example.mymediaplayer.Event
import com.example.mymediaplayer.IMediaServiceConnection

class MediaPlayerViewModel(private val mediaServiceConnection: IMediaServiceConnection): ViewModel() {

    private var accessGrantedEvent: MutableLiveData<Event<Boolean>?> = MutableLiveData(null)
    private val shouldSendAccessGranted = MediatorLiveData<Boolean> ()
    private val accessObserver = Observer<Boolean> {
        if (it) mediaServiceConnection.sendAccessGranted()
    }

    init {
        shouldSendAccessGranted.addSource(mediaServiceConnection.isConnected) { value ->
            shouldSendAccessGranted.value = value && accessGrantedEvent.value?.getContentIfNotHandled() == true
        }
        shouldSendAccessGranted.addSource(accessGrantedEvent) { value ->
            shouldSendAccessGranted.value = value?.getContentIfNotHandled() == true && mediaServiceConnection.isConnected.value == true
        }
        shouldSendAccessGranted.observeForever(accessObserver)
    }

    fun sendAccessGranted() {
        accessGrantedEvent.value = Event(true)
    }

    override fun onCleared() {
        super.onCleared()
        shouldSendAccessGranted.removeObserver(accessObserver)
    }

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