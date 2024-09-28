package com.example.mvvm

import com.example.mvvm.settings.model.ISettingsRepository

class FakeSettingsRepository : ISettingsRepository {
    private var location = "GPS"
    private var language = "English"
    private var windSpeed = "Meter / Sec"
    private var temperature = "Celsius"
    private var notificationsEnabled = true
    private var locationId = 0
    private var languageId = 0
    private var windSpeedId = 0
    private var temperatureId = 0
    private var latLon = Pair(0.0, 0.0)

    override fun getLocation(): String = location
    override fun setLocation(location: String) {
        this.location = location
    }

    override fun getLanguage(): String = language
    override fun setLanguage(language: String) {
        this.language = language
    }

    override fun getWindSpeed(): String = windSpeed
    override fun setWindSpeed(windSpeed: String) {
        this.windSpeed = windSpeed
    }

    override fun getTemperature(): String = temperature
    override fun setTemperature(temperature: String) {
        this.temperature = temperature
    }

    override fun isNotificationsEnabled(): Boolean = notificationsEnabled
    override fun setNotificationsEnabled(enabled: Boolean) {
        this.notificationsEnabled = enabled
    }

    override fun getLocationId(): Int = locationId
    override fun setLocationId(id: Int) {
        this.locationId = id
    }

    override fun getLanguageId(): Int = languageId
    override fun setLanguageId(id: Int) {
        this.languageId = id
    }

    override fun getWindSpeedId(): Int = windSpeedId
    override fun setWindSpeedId(id: Int) {
        this.windSpeedId = id
    }

    override fun getTemperatureId(): Int = temperatureId
    override fun setTemperatureId(id: Int) {
        this.temperatureId = id
    }

    override fun getLatLon(): Pair<Double, Double> = latLon
    override fun setLatLon(lat: Double, lon: Double) {
        this.latLon = Pair(lat, lon)
    }
}
