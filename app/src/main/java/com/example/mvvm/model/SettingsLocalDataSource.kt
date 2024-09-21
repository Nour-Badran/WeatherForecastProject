package com.example.mvvm.model

import android.content.Context
import android.content.SharedPreferences

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
}