package com.example.mvvm.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey val cityName: String,
    val temperature: Double,
    val description: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Int,
    val clouds: Int,
    val dt: Long,
    val visibility: Int,
    val iconResId: Int,
    val icon: String
)

@Entity(tableName = "forecast")
data class ForecastEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dt: Long,                  // Date-time in Unix format
    val temp: Double,              // Temperature
    val feelsLike: Double,         // Feels like temperature
    val tempMin: Double,           // Minimum temperature
    val tempMax: Double,           // Maximum temperature
    val pressure: Int,             // Pressure
    val humidity: Int,             // Humidity
    val windSpeed: Double,         // Wind speed
    val windDeg: Int,              // Wind direction in degrees
    val clouds: Int,               // Cloudiness percentage
    val pop: Double?,              // Probability of precipitation
    val rainVolume: Double?,       // Rain volume in mm (if applicable)
    val weatherDescription: String,// Weather description (e.g., "clear sky")
    val icon: String               // Weather icon ID (to load an icon)
)

