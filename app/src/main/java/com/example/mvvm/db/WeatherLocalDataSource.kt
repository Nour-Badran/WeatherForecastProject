package com.example.mvvm.db

import android.util.Log
import com.example.mvvm.model.Forecast
import com.example.mvvm.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherLocalDataSource(private val weatherDao: WeatherDao) {

    suspend fun saveWeatherData(weatherData: WeatherData) {
        withContext(Dispatchers.IO) {
            weatherDao.deleteAllWeatherData()
            weatherDao.insertWeatherData(weatherData.toEntity())
        }
    }

    suspend fun getWeatherData(): WeatherData? {
        return withContext(Dispatchers.IO) {
            val entity = weatherDao.getWeatherData()
            entity?.toModel()
        }
    }

    // Extension functions to convert WeatherData to WeatherEntity and vice versa
    private fun WeatherData.toEntity() = WeatherEntity(
        cityName = cityName,
        temperature = temperature,
        description = description,
        humidity = humidity,
        windSpeed = windSpeed,
        pressure = pressure,
        clouds = clouds,
        dt = dt,
        visibility =visibility,
        iconResId = iconResId,
        icon = icon
    )

    private fun Forecast.toEntity() = ForecastEntity(
        temp = temp,
        date = date
    )

    private fun WeatherEntity.toModel() = WeatherData(
        cityName = cityName,
        temperature = temperature,
        description = description,
        humidity = humidity,
        windSpeed = windSpeed,
        pressure = pressure,
        clouds = clouds,
        dt = dt,
        visibility = visibility,
        iconResId = iconResId, // Use the stored iconResId from WeatherEntity
        forecast = emptyList() ,
        icon = icon
    )

    private fun ForecastEntity.toModel() = Forecast(
        temp = temp,
        date = date
    )
}
