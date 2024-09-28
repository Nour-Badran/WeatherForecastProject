package com.example.mvvm.settings.model

interface ISettingsRepository {
    fun getLocation(): String
    fun getLanguage(): String
    fun getWindSpeed(): String
    fun getTemperature(): String
    fun isNotificationsEnabled(): Boolean
    fun setLocation(location: String)
    fun setLanguage(language: String)
    fun setWindSpeed(windSpeed: String)
    fun setTemperature(temperature: String)
    fun setNotificationsEnabled(enabled: Boolean)
    fun getLocationId(): Int
    fun setLocationId(id: Int)
    fun getLanguageId(): Int
    fun setLanguageId(id: Int)
    fun getWindSpeedId(): Int
    fun setWindSpeedId(id: Int)
    fun getTemperatureId(): Int
    fun setTemperatureId(id: Int)
    fun getLatLon(): Pair<Double, Double>
    fun setLatLon(lat: Double, lon: Double)
}

class SettingsRepository(private val localDataSource: SettingsLocalDataSource) :
    ISettingsRepository {

    override fun getLocation(): String = localDataSource.getLocation()
    override fun getLanguage(): String = localDataSource.getLanguage()
    override fun getWindSpeed(): String = localDataSource.getWindSpeed()
    override fun getTemperature(): String = localDataSource.getTemperature()
    override fun isNotificationsEnabled(): Boolean = localDataSource.isNotificationsEnabled()

    override fun setLocation(location: String) = localDataSource.setLocation(location)
    override fun setLanguage(language: String) = localDataSource.setLanguage(language)
    override fun setWindSpeed(windSpeed: String) = localDataSource.setWindSpeed(windSpeed)
    override fun setTemperature(temperature: String) = localDataSource.setTemperature(temperature)
    override fun setNotificationsEnabled(enabled: Boolean) = localDataSource.setNotificationsEnabled(enabled)

    override fun getLocationId(): Int = localDataSource.getLocationId()
    override fun setLocationId(id: Int) = localDataSource.setLocationId(id)

    override fun getLanguageId(): Int = localDataSource.getLanguageId()
    override fun setLanguageId(id: Int) = localDataSource.setLanguageId(id)

    override fun getWindSpeedId(): Int = localDataSource.getWindSpeedId()
    override fun setWindSpeedId(id: Int) = localDataSource.setWindSpeedId(id)

    override fun getTemperatureId(): Int = localDataSource.getTemperatureId()
    override fun setTemperatureId(id: Int) = localDataSource.setTemperatureId(id)

    override fun getLatLon(): Pair<Double,Double> = localDataSource.getLatLon()
    override fun setLatLon(lat: Double, lon: Double) = localDataSource.setLatLon(lat,lon)
}