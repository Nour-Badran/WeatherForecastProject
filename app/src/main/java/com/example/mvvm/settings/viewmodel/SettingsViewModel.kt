package com.example.mvvm.settings.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mvvm.settings.model.ISettingsRepository

class SettingsViewModel( private val repository: ISettingsRepository)
    : ViewModel() {

    private val _location = MutableLiveData<String>()
    val location: LiveData<String> get() = _location

    private val _language = MutableLiveData<String>()
    val language: LiveData<String> get() = _language

    private val _windSpeed = MutableLiveData<String>()
    val windSpeed: LiveData<String> get() = _windSpeed

    private val _temperature = MutableLiveData<String>()
    val temperature: LiveData<String> get() = _temperature

    private val _notificationsEnabled = MutableLiveData<Boolean>()
    val notificationsEnabled: LiveData<Boolean> get() = _notificationsEnabled

    init {
        _location.value = repository.getLocation()
        _language.value = repository.getLanguage()
        _windSpeed.value = repository.getWindSpeed()
        _temperature.value = repository.getTemperature()
        _notificationsEnabled.value = repository.isNotificationsEnabled()
    }
    fun updateLocation(newLocation: String) {
        _location.value = newLocation
        repository.setLocation(newLocation)
    }
    fun updateLanguage(newLanguage: String) {
        _language.value = newLanguage
        repository.setLanguage(newLanguage)
    }
    fun updateWindSpeed(newWindSpeed: String) {
        _windSpeed.value = newWindSpeed
        repository.setWindSpeed(newWindSpeed)
    }
    fun updateTemperature(newTemperature: String) {
        _temperature.value = newTemperature
        repository.setTemperature(newTemperature)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        repository.setNotificationsEnabled(enabled)
    }
    fun updateLocation(location: String, id: Int) {
        repository.setLocation(location)
        repository.setLocationId(id)
    }

    fun updateLanguage(language: String, id: Int) {
        repository.setLanguage(language)
        repository.setLanguageId(id)
    }

    fun updateWindSpeed(windSpeed: String, id: Int) {
        repository.setWindSpeed(windSpeed)
        repository.setWindSpeedId(id)
    }

    fun updateTemperature(temperature: String, id: Int) {
        repository.setTemperature(temperature)
        repository.setTemperatureId(id)
    }

    fun getLocationId(): Int = repository.getLocationId()
    fun getLanguageId(): Int = repository.getLanguageId()
    fun getWindSpeedId(): Int = repository.getWindSpeedId()
    fun getTemperatureId(): Int = repository.getTemperatureId()

    fun getLatLon(): Pair<Double,Double> = repository.getLatLon()
    fun setLatLon(lat: Double, lon: Double) = repository.setLatLon(lat,lon)
}