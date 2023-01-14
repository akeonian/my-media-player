package com.example.mymediaplayer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule

abstract class LiveDataTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

}