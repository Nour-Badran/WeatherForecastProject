package com.example.mvvm.settings.model

import android.content.Context
import android.content.SharedPreferences
import com.example.mvvm.R

class SettingsLocalDataSource(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun getLocation(): String = sharedPreferences.getString("location", "GPS") ?: "GPS"
    fun setLocation(location: String) {
        editor.putString("location", location).apply()
    }

    fun getLanguage(): String = sharedPreferences.getString("language", "English") ?: "English"
    fun setLanguage(language: String) {
        editor.putString("language", language).apply()
    }

    fun getWindSpeed(): String = sharedPreferences.getString("wind_speed", "Meter / Sec") ?: "Meter / Sec"
    fun setWindSpeed(windSpeed: String) {
        editor.putString("wind_speed", windSpeed).apply()
    }

    fun getTemperature(): String = sharedPreferences.getString("temperature", "Celsius") ?: "Celsius"
    fun setTemperature(temperature: String) {
        editor.putString("temperature", temperature).apply()
    }

    fun isNotificationsEnabled(): Boolean = sharedPreferences.getBoolean("notifications_enabled", true)
    fun setNotificationsEnabled(enabled: Boolean) {
        editor.putBoolean("notifications_enabled", enabled).apply()
    }

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

    fun setLatLon(lat: Double,lon: Double)
    {
        editor.putFloat("latitude", lat.toFloat())
        editor.putFloat("longitude", lon.toFloat())
        editor.apply()
    }
    fun getLatLon(): Pair<Double, Double> {
        val lat = sharedPreferences.getFloat("latitude", 0.0f).toDouble()
        val lon = sharedPreferences.getFloat("longitude", 0.0f).toDouble()

        return Pair(lat, lon)
    }
}
