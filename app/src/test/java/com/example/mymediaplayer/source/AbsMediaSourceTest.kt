package com.example.mymediaplayer.source

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before

import org.junit.Test

class AbsMediaSourceTest {

    private lateinit var testMediaSource: TestMediaSource

    @Before
    fun init() {
        testMediaSource = TestMediaSource()
    }

    @Test
    fun state_initial_isCreated() {
        assertThat(testMediaSource.state, `is`(AbsMediaSource.State.STATE_CREATED))
    }

    @Test
    fun whenReady_ifAlreadyInitialized_callsWithoutErrorReturnsTrue() {
        // Given - Already initialized
        testMediaSource.setState(AbsMediaSource.State.STATE_INITIALIZED)

        // When - whenReady is called
        var callbackCalled = false
        var loaded = false
        val returnedValue = testMediaSource.whenReady {
            callbackCalled = true
            loaded = it
        }

        // Then - callback called immediately
        assertThat(callbackCalled, `is`(true))
        assertThat(returnedValue, `is`(true))
        assertThat(loaded, `is`(true))
    }

    @Test
    fun whenReady_ifError_callsWithoutErrorReturnsTrue() {
        // Given - error state
        testMediaSource.setState(AbsMediaSource.State.STATE_ERROR)

        // When - whenReady is called
        var callbackCalled = false
        var loaded = false
        val returnedValue = testMediaSource.whenReady {
            loaded = it
            callbackCalled = true
        }

        // Then - callback called immediately
        assertThat(callbackCalled, `is`(true))
        assertThat(returnedValue, `is`(true))
        assertThat(loaded, `is`(false))
    }

    @Test
    fun whenReady_ifInitializedLater_callsLaterWithLoadedReturnsFalse() {
        // Given - Already initialized
        testMediaSource.setState(AbsMediaSource.State.STATE_CREATED)

        // When - whenReady is called
        var callbackCalled = false
        var loaded = false
        val returnedValue = testMediaSource.whenReady {
            callbackCalled = true
            loaded = it
        }

        // Then - callback called immediately
        assertThat(callbackCalled, `is`(false))
        assertThat(returnedValue, `is`(false))

        // When - state changes to initialized
        testMediaSource.setState(AbsMediaSource.State.STATE_INITIALIZED)

        // Then callback called without error
        assertThat(callbackCalled, `is`(true))
        assertThat(loaded, `is`(true))
    }

    @Test
    fun whenReady_ifErrorLater_callsLaterNotLoadedReturnsFalse() {
        // Given - Already initialized
        testMediaSource.setState(AbsMediaSource.State.STATE_CREATED)

        // When - whenReady is called
        var callbackCalled = false
        var loaded = false
        val returnedValue = testMediaSource.whenReady {
            callbackCalled = true
            loaded = it
        }

        // Then - callback called immediately
        assertThat(callbackCalled, `is`(false))
        assertThat(returnedValue, `is`(false))

        // When - state changes to initialized
        testMediaSource.setState(AbsMediaSource.State.STATE_ERROR)

        // Then callback called without error
        assertThat(callbackCalled, `is`(true))
        assertThat(loaded, `is`(false))
    }

    @Test
    fun whenReady_ifInitializingLater_doNothing() {
        // Given - Already initialized
        testMediaSource.setState(AbsMediaSource.State.STATE_CREATED)

        // When - whenReady is called
        var callbackCalled = false
        var loaded = false
        val returnedValue = testMediaSource.whenReady {
            callbackCalled = true
            loaded = it
        }

        // Then - callback called immediately
        assertThat(callbackCalled, `is`(false))
        assertThat(returnedValue, `is`(false))

        // When - state changes to initialized
        testMediaSource.setState(AbsMediaSource.State.STATE_INITIALIZING)

        // Then callback called without error
        assertThat(callbackCalled, `is`(false))
        assertThat(loaded, `is`(false))
    }

}

class TestMediaSource: AbsMediaSource() {

    override suspend fun load() {
        TODO("Not yet implemented")
    }

    override fun findMediaItemById(mediaId: String): MediaMetadataCompat? {
        TODO("Not yet implemented")
    }

    override fun getRootId(): String {
        TODO("Not yet implemented")
    }

    override fun getChildren(browseId: String): MutableList<MediaBrowserCompat.MediaItem> {
        TODO("Not yet implemented")
    }

    fun setState(s: State) { _state = s }
}