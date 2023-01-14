package com.example.mymediaplayer.viewmodels

import androidx.lifecycle.MutableLiveData
import com.example.mymediaplayer.IMediaServiceConnection
import com.example.mymediaplayer.LiveDataTest
import com.example.mymediaplayer.models.MediaData
import com.example.mymediaplayer.utils.getOrAwaitValue
import org.junit.Before

import org.junit.Test
import org.mockito.kotlin.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat

class MediaDataViewModelTest: LiveDataTest() {

    private lateinit var mediaServiceConnection: IMediaServiceConnection

    @Before
    fun initServiceConnection() {
        mediaServiceConnection = mock() {
            on { rootMediaId } doReturn "rootId"
            on { isConnected } doReturn MutableLiveData(false)
        }
    }

    @Test
    fun init_ifConnectedAndNonNullId_subscribeWithNonNullId() {
        // Given - MediaServiceConnection is connected
        mediaServiceConnection.isConnected.value = true

        // When - viewModel is initialized
        MediaDataViewModel(mediaServiceConnection, "browseId")

        // Then
        verify(mediaServiceConnection).subscribe(eq("browseId"), any())
    }

    @Test
    fun init_ifConnectedAndNullId_subscribeWithRootId() {
        // Given - MediaServiceConnection is connected
        mediaServiceConnection.isConnected.value = true

        // When - viewModel is initialized
        MediaDataViewModel(mediaServiceConnection, null)

        verify(mediaServiceConnection).subscribe(eq("rootId"), any())
    }

    @Test
    fun init_ifNotConnectedAndNonNullId_subscribeOnConnectWithNonNullId() {
        // Given - MediaServiceConnection is not connected

        // When - viewModel is initialized
        MediaDataViewModel(mediaServiceConnection, "browseId")

        // Then
        verify(mediaServiceConnection, never()).subscribe(any(), any())

        // Connects after viewModel is initialized
        mediaServiceConnection.isConnected.value = true

        verify(mediaServiceConnection).subscribe(eq("browseId"), any())
    }

    @Test
    fun init_ifNotConnectedAndNullId_subscribeOnConnectWithRootId() {
        // Given - MediaServiceConnection is not connected

        // When - viewModel is initialized
        MediaDataViewModel(mediaServiceConnection, null)

        // Then
        verify(mediaServiceConnection, never()).subscribe(any(), any())

        // Connects after viewModel is initialized
        mediaServiceConnection.isConnected.value = true

        // Checks if subscribe is called after connecting
        verify(mediaServiceConnection).subscribe(eq("rootId"), any())
    }

    @Test
    fun openMedia_ifBrowsable_startNavigationEvent() {
        // Given - browsable data
        val data = MediaData("childBrowseId", "title", "subtitle", mock(), true)
        val viewModel = MediaDataViewModel(mediaServiceConnection, null)

        viewModel.openMedia(data)

        assertThat(viewModel.navigationEvent.getOrAwaitValue().getContentIfNotHandled(), `is`("childBrowseId"))
    }

    @Test
    fun refreshData_ifConnected_unsubscribeSubscribe() {
        // Given viewModel with null Id
        val viewModel = MediaDataViewModel(mediaServiceConnection, null)
        mediaServiceConnection.isConnected.value = true
        verify(mediaServiceConnection).subscribe(eq("rootId"), any())
        clearInvocations(mediaServiceConnection)
        // When - data is refreshed
        viewModel.refreshData()

        // Then - call subscribe and unsubscribe, to refresh data
        verify(mediaServiceConnection).unsubscribe(eq("rootId"), any())
        verify(mediaServiceConnection).subscribe(eq("rootId"), any())
    }

    @Test
    fun refreshData_ifNotConnected_doNothing() {
        // Given viewModel with null Id
        val viewModel = MediaDataViewModel(mediaServiceConnection, null)

        // When - data is refreshed
        viewModel.refreshData()

        // Then - call subscribe and unsubscribe, to refresh data
        verify(mediaServiceConnection, never()).unsubscribe(any(), any())
        verify(mediaServiceConnection, never()).subscribe(any(), any())
    }

}
