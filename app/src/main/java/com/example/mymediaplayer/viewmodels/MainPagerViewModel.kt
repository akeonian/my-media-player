package com.example.mymediaplayer.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainPagerViewModel: ViewModel() {

    private val _selectedPosition = MutableLiveData(0)
    val selectedPosition: LiveData<Int> = _selectedPosition

    fun setSelectedPosition(position: Int) {
        _selectedPosition.value = position
    }
}