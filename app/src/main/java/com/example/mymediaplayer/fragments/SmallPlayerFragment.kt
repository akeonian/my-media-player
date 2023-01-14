package com.example.mymediaplayer.fragments

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.mymediaplayer.MainDirections
import com.example.mymediaplayer.R
import com.example.mymediaplayer.databinding.FragmentSmallPlayerBinding
import com.example.mymediaplayer.helper.FlingGestureHelper
import com.example.mymediaplayer.helper.TimeUpdater
import com.example.mymediaplayer.models.DisplayData
import com.example.mymediaplayer.models.PlayingData
import com.example.mymediaplayer.utils.InjectorUtils
import com.example.mymediaplayer.viewmodels.PlayerViewModel

private const val TAG = "SmallPlayerFragment"

class SmallPlayerFragment: Fragment() {

    private val viewModel: PlayerViewModel by viewModels {
        InjectorUtils.smallPlayerViewModelFactory(requireContext())
    }

    private val progressBarUpdater = TimeUpdater()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSmallPlayerBinding.inflate(
            inflater, container, false)
        binding.root.visibility = GONE
        binding.root.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        viewModel.displayData.observe(viewLifecycleOwner) {
            bindData(binding, it)
        }
        binding.root.setOnClickListener {
            viewModel.playPause()
        }
        viewModel.playingData.observe(viewLifecycleOwner) {
            bindPlayingData(binding, it)
        }

        FlingGestureHelper(
            binding.root,
            flingLeft = { viewModel.playPrevious() },
            flingRight = { viewModel.playNext() },
            flingUp = {
                val action = MainDirections.openFullPlayer()
                findNavController().navigate(action)
                      }
        ).attach()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        progressBarUpdater.start()
    }

    override fun onStop() {
        super.onStop()
        progressBarUpdater.stop()
    }

    private fun bindData(binding: FragmentSmallPlayerBinding, displayData: DisplayData) {
        val showUI = !displayData.isEmpty()
        if (showUI) {
            binding.titleView.text = displayData.title
            binding.subtitleView.text = displayData.subtitle
            binding.progressBar.max = displayData.maxDuration.toInt()

            Glide.with(binding.albumArt)
                .load(displayData.albumUri)
                .error(R.drawable.ic_broken)
                .placeholder(R.drawable.ic_image)
                .into(binding.albumArt)
        }
        enablePlayer(showUI)
    }

    private fun bindPlayingData(binding: FragmentSmallPlayerBinding, playingData: PlayingData) {
        if (playingData.isPlaying) {
            progressBarUpdater.startAndAttach(playingData.seekPosition, playingData.lastUpdateTime, playingData.playingSpeed) {
                binding.progressBar.progress = it.toInt()
            }
        } else {
            progressBarUpdater.stopAndDetach()
        }
    }

    private fun enablePlayer(showUI: Boolean) {
        val newVisibility = if (showUI) VISIBLE else GONE
        if (requireView().visibility != newVisibility) {
            requireView().visibility = newVisibility
            // TODO: Transition animation for state change
        }
    }
}