package com.example.mvvm.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mvvm.model.WeatherRepository

class HomeViewModelFactory(
    private val application: Application,
    private val weatherRepository: WeatherRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(application, weatherRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
