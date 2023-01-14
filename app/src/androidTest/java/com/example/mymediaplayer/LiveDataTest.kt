package com.example.mymediaplayer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mymediaplayer.utils.LogUtils.testing
import org.junit.Rule

abstract class LiveDataTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
}