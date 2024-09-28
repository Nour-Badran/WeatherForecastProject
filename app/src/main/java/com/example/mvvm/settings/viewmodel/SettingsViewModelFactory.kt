package com.example.mvvm.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mvvm.settings.model.SettingsRepository

class SettingsViewModelFactory(
    private val repository: SettingsRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            SettingsViewModel(repository) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
