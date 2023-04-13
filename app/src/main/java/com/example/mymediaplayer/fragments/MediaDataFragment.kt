package com.example.mymediaplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mymediaplayer.EventObserver
import com.example.mymediaplayer.MainDirections
import com.example.mymediaplayer.adapters.MediaDataAdapter
import com.example.mymediaplayer.databinding.FragmentMediaDataBinding
import com.example.mymediaplayer.utils.InjectorUtils
import com.example.mymediaplayer.viewmodels.ListState
import com.example.mymediaplayer.viewmodels.MediaDataViewModel

open class MediaDataFragment: Fragment() {

    private val navigationArgs: MediaDataFragmentArgs by navArgs()

    private val viewModel: MediaDataViewModel by viewModels() {
        InjectorUtils.mediaDataViewModelFactory(
            requireContext(), navigationArgs.browseId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMediaDataBinding.inflate(
            inflater, container, false)
        val adapter = MediaDataAdapter {
            viewModel.openMedia(it)
        }
        binding.recyclerView.adapter = adapter
        viewModel.allSongs.observe(viewLifecycleOwner) {
            binding.root.isRefreshing = false
            adapter.submitList(it)
        }
        viewModel.listState.observe(viewLifecycleOwner) {
            it?.let { listState -> binding.bindListState(listState) }
        }
        viewModel.navigationEvent.observe(viewLifecycleOwner, EventObserver {
            val action = MainDirections.openMediaData(it)
            findNavController().navigate(action)
        })
        binding.root.setOnRefreshListener {
            viewModel.refreshData()
        }
        return binding.root
    }

    private fun FragmentMediaDataBinding.bindListState(listState: ListState) {
        when(listState) {
            ListState.ERROR -> {
                error.visibility = View.VISIBLE
                loading.visibility = View.GONE
                noFiles.visibility = View.GONE
            }
            ListState.EMPTY -> {
                error.visibility = View.GONE
                loading.visibility = View.GONE
                noFiles.visibility = View.VISIBLE
            }
            ListState.SUCCESS -> {
                error.visibility = View.GONE
                loading.visibility = View.GONE
                noFiles.visibility = View.GONE
            }
            ListState.LOADING -> {
                error.visibility = View.GONE
                loading.visibility = View.VISIBLE
                noFiles.visibility = View.GONE
            }
        }
    }
}