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
    val dt: Long,
    val temp: Double,
    val feelsLike: Double,
    val tempMin: Double,
    val tempMax: Double,
    val pressure: Int,
    val humidity: Int,
    val windSpeed: Double,
    val clouds: Int,
    val pop: Double?,
    val rainVolume: Double?,
    val weatherDescription: String,
    val icon: String,
    val dtTxt: String
)



