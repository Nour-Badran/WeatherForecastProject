package com.example.mvvm.model

import android.content.Context
import android.content.SharedPreferences
import com.example.mvvm.R

class SettingsLocalDataSource(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    // Location
    fun getLocation(): String = sharedPreferences.getString("location", "GPS") ?: "GPS"
    fun setLocation(location: String) {
        editor.putString("location", location).apply()
    }

    // Language
    fun getLanguage(): String = sharedPreferences.getString("language", "English") ?: "English"
    fun setLanguage(language: String) {
        editor.putString("language", language).apply()
    }

    // Wind Speed
    fun getWindSpeed(): String = sharedPreferences.getString("wind_speed", "Meter / Sec") ?: "Meter / Sec"
    fun setWindSpeed(windSpeed: String) {
        editor.putString("wind_speed", windSpeed).apply()
    }

    // Temperature
    fun getTemperature(): String = sharedPreferences.getString("temperature", "Celsius") ?: "Celsius"
    fun setTemperature(temperature: String) {
        editor.putString("temperature", temperature).apply()
    }

    // Notifications
    fun isNotificationsEnabled(): Boolean = sharedPreferences.getBoolean("notifications_enabled", true)
    fun setNotificationsEnabled(enabled: Boolean) {
        editor.putBoolean("notifications_enabled", enabled).apply()
    }

    // Methods to save and retrieve IDs
    fun getLocationId(): Int = sharedPreferences.getInt("location_id", R.id.gps_radio_button)
    fun setLocationId(id: Int) {
        editor.putInt("location_id", id).apply()
    }

    fun getLanguageId(): Int = sharedPreferences.getInt("language_id", R.id.english_radio_button)
    fun setLanguageId(id: Int) {
        editor.putInt("language_id", id).apply()
    }

    fun getWindSpeedId(): Int = sharedPreferences.getInt("wind_speed_id", R.id.meter_sec_radio_button)
    fun setWindSpeedId(id: Int) {
        editor.putInt("wind_speed_id", id).apply()
    }

    fun getTemperatureId(): Int = sharedPreferences.getInt("temperature_id", R.id.celsius_radio_button)
    fun setTemperatureId(id: Int) {
        editor.putInt("temperature_id", id).apply()
    }
}
