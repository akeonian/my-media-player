package com.example.mymediaplayer.models

import android.net.Uri

data class DisplayData(
    val title: String?,
    val subtitle: String?,
    val albumUri: Uri?,
    val maxDuration: Long
) {
    fun isEmpty() : Boolean {
        return title.isNullOrEmpty() && subtitle.isNullOrEmpty() && albumUri == null
    }
}