package com.example.mymediaplayer.models

data class PlayingData(
    val isPlaying: Boolean,
    val seekPosition: Long,
    val lastUpdateTime: Long,
    val playingSpeed: Float
)
