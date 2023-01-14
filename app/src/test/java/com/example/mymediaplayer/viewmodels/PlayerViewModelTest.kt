package com.example.mymediaplayer.viewmodels

import android.support.v4.media.session.MediaControllerCompat.TransportControls
import android.support.v4.media.session.PlaybackStateCompat
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.mymediaplayer.IMediaServiceConnection
import com.example.mymediaplayer.utils.getOrAwaitValue
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule

import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.*

class PlayerViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: PlayerViewModel
    private lateinit var mediaServiceConnection: IMediaServiceConnection

    @Before
    fun initViewModel() {
        val tc: TransportControls = mock()
        val ps = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_PAUSED, 123456789, 1.5f, 987654321)
            .build()
        mediaServiceConnection = mock {
            // Return mocked transportControls, to test correct playback function call
            on { transportControls } doReturn tc
            on { isConnected } doReturn MutableLiveData(false)
            on { playbackState } doReturn MutableLiveData(ps)
        }

        viewModel = PlayerViewModel(mediaServiceConnection)
    }

    @Test
    fun playNext_ifConnected_skipToNextCalled() {

        // Given - connected to media service
        mediaServiceConnection.isConnected.value = true

        // When - viewModel plays next
        viewModel.playNext()

        // Then - check if calls the transportControls to play next
        verify(mediaServiceConnection.transportControls).skipToNext()
    }

    @Test
    fun playNext_ifNotConnected_skipToNextNotCalled() {
        // Given - not connected to media service

        // When - viewModel plays next
        viewModel.playNext()

        // Then - check if it does not call the transportControls to play next
        verify(mediaServiceConnection.transportControls, never()).skipToNext()
    }

    @Test
    fun playPause_ifPlaying_pauseCalled() {
        // Given - If it is playing then it will be connected
        mediaServiceConnection.isConnected.value = true
        val playbackState = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f, System.currentTimeMillis())
            .build()
        mediaServiceConnection.playbackState.value = playbackState

        // When - viewModel playPause
        viewModel.playPause()

        // Then - check if it calls the pause function
        verify(mediaServiceConnection.transportControls).pause()
    }

    @Test
    fun playPause_ifConnectedAndPaused_playCalled() {
        // Given - If it is playing then it will be connected
        mediaServiceConnection.isConnected.value = true
        val playbackState = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_PAUSED, 0, 1.0f, System.currentTimeMillis())
            .build()
        mediaServiceConnection.playbackState.value = playbackState

        // When - viewModel playPause
        viewModel.playPause()

        // Then - check if it calls the pause function
        verify(mediaServiceConnection.transportControls).play()
    }

    @Test
    fun playPause_ifNotConnectedAndPaused_playNotCalled() {
        // Given - If the playback is paused
        val playbackState = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_PAUSED, 0, 1.0f, System.currentTimeMillis())
            .build()
        mediaServiceConnection.playbackState.value = playbackState

        // When - viewModel playPause
        viewModel.playPause()

        // Then - it should not call play
        verify(mediaServiceConnection.transportControls, never()).play()
    }

    @Test
    fun playPrevious_ifConnected_skipToPreviousCalled() {
        // Given - connected to media service
        mediaServiceConnection.isConnected.value = true

        // When - viewModel plays next
        viewModel.playPrevious()

        // Then - check if calls the transportControls to play previous
        verify(mediaServiceConnection.transportControls).skipToPrevious()
    }

    @Test
    fun playPrevious_ifNotConnected_skipToPreviousNotCalled() {
        // Given - not connected to media service

        // When - viewModel plays next
        viewModel.playPrevious()

        // Then - check if it does not call the transportControls to skipToPrevious
        verify(mediaServiceConnection.transportControls, never()).skipToPrevious()
    }

    @Test
    fun setSeekPosition_ifConnected_seekPositionCalled() {
        // Given - connected to media service
        mediaServiceConnection.isConnected.value = true

        // When - viewModel changes seek position
        viewModel.setSeekPosition(100)

        // Then - check if seekTo is called once with 100
        verify(mediaServiceConnection.transportControls).seekTo(100)
    }

    @Test
    fun setSeekPosition_ifNotConnected_seekPositionNotCalled() {
        // Given - not connected to media service
        mediaServiceConnection.isConnected.value = false

        // When - viewModel changes seek position
        viewModel.setSeekPosition(100)

        // Then - check if seekTo is not called with any value
        verify(mediaServiceConnection.transportControls, never()).seekTo(anyLong())
    }

    @Test
    fun playingData_playbackState_transformedCorrectly() {
        // Given - current playback state
        val playbackState = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_PLAYING, 123456789, 1.5f, 987654321)
            .build()
        mediaServiceConnection.playbackState.value = playbackState

        // When - playingData is retrieved
        val playingData = viewModel.playingData.getOrAwaitValue()

        // Then - The Data should match the playback State of mediaServiceConnection
        assertThat(playingData.isPlaying, `is`(true))
        assertThat(playingData.playingSpeed, `is`(1.5f))
        assertThat(playingData.seekPosition, `is`(123456789))
        assertThat(playingData.lastUpdateTime, `is`(987654321))
    }

}