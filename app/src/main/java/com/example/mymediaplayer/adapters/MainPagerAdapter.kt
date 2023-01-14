package com.example.mymediaplayer.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mymediaplayer.R
import com.example.mymediaplayer.fragments.PagerDataFragment
import com.example.mymediaplayer.source.ALL_ALBUMS_ROOT_ID
import com.example.mymediaplayer.source.ALL_ARTISTS_ROOT_ID
import com.example.mymediaplayer.source.ALL_GENRES_ROOT_ID
import com.example.mymediaplayer.source.ALL_SONGS_ROOT_ID

class MainPagerAdapter(fa: FragmentActivity): FragmentStateAdapter(fa) {

    private val titles = listOf(
        fa.getString(R.string.songs),
        fa.getString(R.string.albums),
        fa.getString(R.string.artists),
        fa.getString(R.string.genres)
    )

    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        val browseId = when(position) {
            0 -> ALL_SONGS_ROOT_ID
            1 -> ALL_ALBUMS_ROOT_ID
            2 -> ALL_ARTISTS_ROOT_ID
            3 -> ALL_GENRES_ROOT_ID
            else -> null
        }
        return PagerDataFragment.createInstance(PagerDataFragment::class.java, position, browseId)
    }
    
    fun getTitle(position: Int): String {
        return titles[position]
    }

}