package com.example.mymediaplayer.helper

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class TimeUpdaterTest {

    private lateinit var timeUpdater: TimeUpdater

    @Before
    fun initUpdater() {
        timeUpdater = TimeUpdater(1000, Handler(Looper.getMainLooper()))
    }

    @After
    fun stopUpdater() {
        timeUpdater.stopAndDetach()
    }

    @Test
    fun startAndAttach_callbackCalledImmediately() {

        // Given - started and Attached
        var calledAfterDelay = false
        timeUpdater.startAndAttach(10, 0, 1.0f) {
            calledAfterDelay = true
        }
        ShadowLooper.runUiThreadTasks()
        // Then - check if called immediately
        assertThat(calledAfterDelay, `is`(true))
    }

    @Test
    fun startAndAttach_callbackCalledAgainAfterDelays() {

        // Given - started and Attached
        var calledAfterDelay: Boolean
        timeUpdater.startAndAttach(10, 0, 1.0f) {
            calledAfterDelay = true
        }
        ShadowLooper.runUiThreadTasks()
        // reset after immediate call
        calledAfterDelay = false

        // When - after 1000 milliseconds passed
        Robolectric.getForegroundThreadScheduler().advanceBy(1000, TimeUnit.MILLISECONDS)

        // Then - check if called
        assertThat(calledAfterDelay, `is`(true))

        // reset after first delay
        calledAfterDelay = false

        // after another 1000 milliseconds passed
        Robolectric.getForegroundThreadScheduler().advanceBy(1000, TimeUnit.MILLISECONDS)

        // Then - check if called
        assertThat(calledAfterDelay, `is`(true))

    }

    @Test
    fun stop_called_callbackNotCalled() {

        // Given - started and Attached
        var calledAfterDelay = false
        timeUpdater.startAndAttach(10, 0, 1.0f) {
            calledAfterDelay = true
        }
        // When - stopped and 1000 milliseconds passed
        timeUpdater.stop()
        Robolectric.getForegroundThreadScheduler().advanceBy(1000, TimeUnit.MILLISECONDS)

        // Then - check if called
        assertThat(calledAfterDelay, `is`(false))
    }

    @Test
    fun start_called_callbackCalled() {

        // Given - started and Attached
        var calledAfterDelay = false
        timeUpdater.startAndAttach(10, 0, 1.0f) {
            calledAfterDelay = true
        }
        // When - stopped and 1000 milliseconds passed
        timeUpdater.stop()
        Robolectric.getForegroundThreadScheduler().advanceBy(1000, TimeUnit.MILLISECONDS)

        // Then - check if called
        assertThat(calledAfterDelay, `is`(false))

        // On started again
        timeUpdater.start()
        ShadowLooper.runUiThreadTasks()
        // callback called
        assertThat(calledAfterDelay, `is`(true))
    }

    @Test
    fun start_called_callbackCalledAfterDelay() {

        // Given - started and Attached
        var calledAfterDelay = false
        timeUpdater.startAndAttach(10, 0, 1.0f) {
            calledAfterDelay = true
        }
        // When - stopped and 1000 milliseconds passed
        timeUpdater.stop()
        Robolectric.getForegroundThreadScheduler().advanceBy(1000, TimeUnit.MILLISECONDS)

        // Then - check if called
        assertThat(calledAfterDelay, `is`(false))

        // On started again
        timeUpdater.start()
        ShadowLooper.runUiThreadTasks()

        // reset after immediate call
        calledAfterDelay = false

        Robolectric.getForegroundThreadScheduler().advanceBy(1000, TimeUnit.MILLISECONDS)

        // check if called after delay
        assertThat(calledAfterDelay, `is`(true))
    }

    @Test
    fun stopAndDetach_ifStarted_stopsAndDetaches() {
        // Given - started and Attached
        var calledAfterDelay = false
        timeUpdater.startAndAttach(10, 0, 1.0f) {
            calledAfterDelay = true
        }

        // When - stopped and detached
        timeUpdater.stopAndDetach()

        // Then - check callback should not be called after starting
        timeUpdater.start()
        assertThat(calledAfterDelay, `is`(false))
    }

}