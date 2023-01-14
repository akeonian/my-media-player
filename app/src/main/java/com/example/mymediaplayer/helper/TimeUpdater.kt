package com.example.mymediaplayer.helper

import android.os.Handler
import android.os.Looper
import com.example.mymediaplayer.utils.LogUtils

private const val TAG = "ProgressBarUpdater"

class TimeUpdater(private val updateDelay: Long = 1000L, private val progressHandler: Handler = Handler(Looper.getMainLooper())) {

    private var lastProgress = 0L
    private var lastUpdateTime = 0L
    private var playingSpeed = 1.0f
    private var updateRunnable: Runnable = Runnable {}

    fun startAndAttach(newProgress: Long, updateTime: Long, speed: Float, onTimeUpdated: (Long) -> Unit) {
        playingSpeed = speed
        lastProgress = newProgress
        lastUpdateTime = updateTime
        stop()
        updateRunnable = Runnable {
            progressHandler.postDelayed(updateRunnable, updateDelay)
            val currentTime = getCalculatedProgress()
            LogUtils.d(TAG, "currentTime=$currentTime, starTime=$lastProgress")
            onTimeUpdated(currentTime) // calling after posting so it can be stopped in the onTimeUpdated
        }
        progressHandler.post(updateRunnable)
    }

    fun stopAndDetach(onTimeUpdated: (Long) -> Unit = {}) {
        onTimeUpdated(getCalculatedProgress())
        stop()
        updateRunnable = Runnable {}
    }

    fun start() {
        stop()
        progressHandler.post(updateRunnable)
    }

    fun stop() {
        // Using Blocks {} does not cancel the callback
        progressHandler.removeCallbacks(updateRunnable)
    }

    private fun getCalculatedProgress(): Long {
        return lastProgress + ((System.currentTimeMillis() - lastUpdateTime)*playingSpeed.toDouble()).toLong()
    }
}