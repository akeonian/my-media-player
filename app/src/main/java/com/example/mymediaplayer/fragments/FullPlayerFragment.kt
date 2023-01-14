package com.example.mymediaplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.mymediaplayer.R
import com.example.mymediaplayer.databinding.FragmentFullPlayerBinding
import com.example.mymediaplayer.ext.durationText
import com.example.mymediaplayer.helper.FlingGestureHelper
import com.example.mymediaplayer.helper.TimeUpdater
import com.example.mymediaplayer.models.DisplayData
import com.example.mymediaplayer.models.PlayingData
import com.example.mymediaplayer.utils.InjectorUtils
import com.example.mymediaplayer.viewmodels.PlayerViewModel

private const val TAG = "FullPlayerFragment"

class FullPlayerFragment: Fragment() {

    private val viewModel: PlayerViewModel by viewModels {
        InjectorUtils.smallPlayerViewModelFactory(requireContext())
    }

    private val seekBarUpdater = TimeUpdater()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFullPlayerBinding.inflate(
            inflater, container, false)

        viewModel.displayData.observe(viewLifecycleOwner) {
            bindDisplayData(binding, it)
        }
        viewModel.playingData.observe(viewLifecycleOwner) {
            bindPlayingData(binding, it)
        }
        binding.previous.setOnClickListener { viewModel.playPrevious() }
        binding.playPause.setOnClickListener { viewModel.playPause() }
        binding.next.setOnClickListener { viewModel.playNext() }
        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, user: Boolean) {
                if (user) viewModel.setSeekPosition(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar) {
                seekBarUpdater.stop()
            }

            override fun onStopTrackingTouch(p0: SeekBar) {
                if (viewModel.playingData.value?.isPlaying == true) {
                    seekBarUpdater.start()
                }
            }

        })

        FlingGestureHelper(
            binding.root,
            flingDown = {
                findNavController().navigateUp()
            }
        ).attach()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        seekBarUpdater.start()
    }

    override fun onStop() {
        super.onStop()
        seekBarUpdater.stop()
    }

    private fun bindPlayingData(binding: FragmentFullPlayerBinding, it: PlayingData) {
        val onTimeUpdated = { newTime: Long ->
            binding.seekBar.progress = newTime.toInt()
            binding.currentTime.text = newTime.durationText()
        }
        if (it.isPlaying) {
            seekBarUpdater.startAndAttach(it.seekPosition, it.lastUpdateTime, it.playingSpeed, onTimeUpdated)
            binding.playPause.setImageResource(R.drawable.ic_pause)
        }
        else {
            seekBarUpdater.stopAndDetach(onTimeUpdated)
            binding.playPause.setImageResource(R.drawable.ic_play)
        }
    }

    private fun bindDisplayData(binding: FragmentFullPlayerBinding, displayData: DisplayData) {
        if (displayData.isEmpty()) goBack()
        else {
            binding.title.text = getNullRes(displayData.title, R.string.unknown_title)
            binding.subtitle.text = getNullRes(displayData.subtitle, R.string.unknown_subtitle)
            binding.seekBar.max = displayData.maxDuration.toInt()
            binding.maxDuration.text = displayData.maxDuration.durationText()

            Glide.with(binding.albumArt)
                .load(displayData.albumUri)
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_broken)
                .into(binding.albumArt)
        }
    }

    private fun getNullRes(text: String?, @StringRes res: Int) =
        if (text.isNullOrEmpty()) getString(res) else text

    private fun goBack() {
        (activity as AppCompatActivity).onSupportNavigateUp()
    }
}