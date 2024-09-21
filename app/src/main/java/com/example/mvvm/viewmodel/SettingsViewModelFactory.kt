package com.example.mvvm.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mvvm.model.SettingsRepository

class SettingsViewModelFactory(
    private val application: Application,
    private val repository: SettingsRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            SettingsViewModel(application, repository) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
