package com.example.mymediaplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mymediaplayer.R
import com.example.mymediaplayer.databinding.ListItemAllSongsBinding
import com.example.mymediaplayer.models.MediaData
import com.example.mymediaplayer.utils.LogUtils

class MediaDataAdapter(
    private val itemClickListener: (MediaData) -> Unit
): ListAdapter<MediaData, MediaDataAdapter.ViewHolder>(MediaData.diffCallback) {

    class ViewHolder(
        private val binding: ListItemAllSongsBinding
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MediaData) {
            binding.titleView.text = item.title
            binding.subtitleView.text = item.subtitle

            Glide.with(binding.albumArt)
                .load(item.albumUri)
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_broken)
                .into(binding.albumArt)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemAllSongsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        LogUtils.d("TAG", "onBindViewHolder=$item")
        holder.bind(item)
        holder.itemView.setOnClickListener { itemClickListener(item) }
    }

}