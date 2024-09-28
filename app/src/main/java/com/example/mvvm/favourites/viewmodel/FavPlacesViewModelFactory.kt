package com.example.mvvm.favourites.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mvvm.weather.model.repo.IWeatherRepository

class FavPlacesViewModelFactory (private val repository: IWeatherRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavViewModel::class.java)) {
            return FavViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}