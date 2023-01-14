package com.example.mymediaplayer.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mymediaplayer.utils.getOrAwaitValue
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule

import org.junit.Test

class MainPagerViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: MainPagerViewModel

    @Before
    fun initViewModel() {
        viewModel = MainPagerViewModel()
    }

    @Test
    fun setSelectedPosition_changesSelectedPosition() {
        assertThat(viewModel.selectedPosition.getOrAwaitValue(), `is`(0))
        viewModel.setSelectedPosition(3)
        assertThat(viewModel.selectedPosition.getOrAwaitValue(), `is`(3))
    }
}