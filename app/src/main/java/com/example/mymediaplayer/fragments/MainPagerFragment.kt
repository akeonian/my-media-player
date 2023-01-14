package com.example.mymediaplayer.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.mymediaplayer.adapters.MainPagerAdapter
import com.example.mymediaplayer.databinding.FragmentMainPagerBinding
import com.example.mymediaplayer.viewmodels.MainPagerViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy

private const val TAG = "MainPagerFragment"

class MainPagerFragment: Fragment() {

    private val viewModel: MainPagerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMainPagerBinding.inflate(
            inflater, container, false)

        val adapter = MainPagerAdapter(requireActivity())

        binding.viewPager.adapter = adapter
        // If hide able tabs are implemented, itemCount might change
        viewModel.selectedPosition.value?.let { oldPosition ->
            if (oldPosition < adapter.itemCount) {
                binding.viewPager.setCurrentItem(oldPosition, false)
            }
        }
        binding.viewPager.registerOnPageChangeCallback(object: OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.setSelectedPosition(position)
            }
        })
        val tlm = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = adapter.getTitle(position)
        }
        tlm.attach()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
    }
}