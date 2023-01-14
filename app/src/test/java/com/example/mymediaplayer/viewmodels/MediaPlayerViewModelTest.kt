package com.example.mymediaplayer.viewmodels

import com.example.mymediaplayer.IMediaServiceConnection
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class MediaPlayerViewModelTest {

    @Test
    fun sendPermissionChanged_callsMediaServiceConnection() {
        val mediaServiceConnection = mock<IMediaServiceConnection>()
        val viewModel = MediaPlayerViewModel(mediaServiceConnection)

        viewModel.sendPermissionChanged()

        verify(mediaServiceConnection).sendPermissionChanged()
    }

}