package com.example.mvvm.model

class SettingsRepository(private val localDataSource: SettingsLocalDataSource) {

    // Fetch settings
    fun getLocation(): String = localDataSource.getLocation()
    fun getLanguage(): String = localDataSource.getLanguage()
    fun getWindSpeed(): String = localDataSource.getWindSpeed()
    fun getTemperature(): String = localDataSource.getTemperature()
    fun isNotificationsEnabled(): Boolean = localDataSource.isNotificationsEnabled()

    // Save settings
    fun setLocation(location: String) = localDataSource.setLocation(location)
    fun setLanguage(language: String) = localDataSource.setLanguage(language)
    fun setWindSpeed(windSpeed: String) = localDataSource.setWindSpeed(windSpeed)
    fun setTemperature(temperature: String) = localDataSource.setTemperature(temperature)
    fun setNotificationsEnabled(enabled: Boolean) = localDataSource.setNotificationsEnabled(enabled)

    fun getLocationId(): Int = localDataSource.getLocationId()
    fun setLocationId(id: Int) = localDataSource.setLocationId(id)

    fun getLanguageId(): Int = localDataSource.getLanguageId()
    fun setLanguageId(id: Int) = localDataSource.setLanguageId(id)

    fun getWindSpeedId(): Int = localDataSource.getWindSpeedId()
    fun setWindSpeedId(id: Int) = localDataSource.setWindSpeedId(id)

    fun getTemperatureId(): Int = localDataSource.getTemperatureId()
    fun setTemperatureId(id: Int) = localDataSource.setTemperatureId(id)

    fun getLatLon(): Pair<Double,Double> = localDataSource.getLatLon()
    fun setLatLon(lat: Double, lon: Double) = localDataSource.setLatLon(lat,lon)
}