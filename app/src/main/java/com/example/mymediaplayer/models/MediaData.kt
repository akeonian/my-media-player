package com.example.mymediaplayer.models

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil

data class MediaData(
    val id: String,
    val title: String,
    val subtitle: String,
    val albumUri: Uri,
    val isBrowsable: Boolean
    ) {

    companion object {
        val diffCallback = object: DiffUtil.ItemCallback<MediaData>() {
            override fun areItemsTheSame(oldItem: MediaData, newItem: MediaData): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MediaData, newItem: MediaData): Boolean {
                return oldItem == newItem
            }
        }
    }
}
