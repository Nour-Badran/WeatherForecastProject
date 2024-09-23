package com.example.mvvm.db

import android.util.Log
import com.example.mvvm.model.FiveDayResponse
import com.example.mvvm.model.Forecast
import com.example.mvvm.model.WeatherData
import com.example.mvvm.model.WeatherItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WeatherLocalDataSource(private val weatherDao: WeatherDao) {

    suspend fun saveWeatherData(weatherData: WeatherData) {
        withContext(Dispatchers.IO) {
            weatherDao.deleteAllWeatherData()
            weatherDao.insertWeatherData(weatherData.toEntity())
        }
    }

    suspend fun saveForecastData(forecast: FiveDayResponse) {
        withContext(Dispatchers.IO) {
            weatherDao.deleteAllForecastData()
            val forecastEntities = forecast.list.map { it.toEntity() }
            weatherDao.insertForecastData(forecastEntities)
        }
    }

    suspend fun getWeatherData(): WeatherData? {
        return withContext(Dispatchers.IO) {
            val entity = weatherDao.getWeatherData()
            entity?.toModel()
        }
    }

    suspend fun getForecastData(): List<ForecastEntity> {
        return withContext(Dispatchers.IO) {
            weatherDao.getForecastData()
        }
    }
    suspend fun saveFavWeatherData(weatherData: WeatherData) {
        withContext(Dispatchers.IO) {
            weatherDao.insertFavWeatherData(weatherData.toFavEntity())
        }
    }

    suspend fun saveFavForecastData(forecast: FiveDayResponse) {
        withContext(Dispatchers.IO) {
            val forecastEntities = forecast.list.map { it.toFavEntity() }
            weatherDao.insertFavForecastData(forecastEntities)
        }
    }

    suspend fun getFavWeatherData(): WeatherData? {
        return withContext(Dispatchers.IO) {
            val entity = weatherDao.getFavWeatherData()
            entity?.toFavModel()
        }
    }

    suspend fun getFavForecastData(): List<FavForecastEntity> {
        return withContext(Dispatchers.IO) {
            weatherDao.getFavForecastData()
        }
    }
    suspend fun getAllProducts(): Flow<List<FavoritePlaces>> {
        return weatherDao.getAll()
    }
    suspend fun insertPlace(place: FavoritePlaces) {
        weatherDao.insert(place)
    }

    suspend fun deletePlace(place: FavoritePlaces) {
        weatherDao.delete(place)
    }

    private fun WeatherData.toEntity() = WeatherEntity(
        cityName = cityName,
        temperature = temperature,
        description = description,
        humidity = humidity,
        windSpeed = windSpeed,
        pressure = pressure,
        clouds = clouds,
        dt = dt,
        visibility = visibility,
        iconResId = iconResId,
        icon = icon
    )
    private fun WeatherData.toFavEntity() = FavWeatherEntity(
        cityName = cityName,
        temperature = temperature,
        description = description,
        humidity = humidity,
        windSpeed = windSpeed,
        pressure = pressure,
        clouds = clouds,
        dt = dt,
        visibility = visibility,
        iconResId = iconResId,
        icon = icon
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
        iconResId = iconResId,
        forecast = emptyList(),
        icon = icon
    )
    private fun FavWeatherEntity.toFavModel() = WeatherData(
        cityName = cityName,
        temperature = temperature,
        description = description,
        humidity = humidity,
        windSpeed = windSpeed,
        pressure = pressure,
        clouds = clouds,
        dt = dt,
        visibility = visibility,
        iconResId = iconResId,
        forecast = emptyList(),
        icon = icon
    )
    private fun WeatherItem.toEntity() = ForecastEntity(
        dt = dt,
        temp = main.temp,
        feelsLike = main.feelsLike,
        tempMin = main.tempMin,
        tempMax = main.tempMax,
        pressure = main.pressure,
        humidity = main.humidity,
        windSpeed = wind.speed,
        clouds = clouds.all,
        pop = pop,
        rainVolume = rain?.volume ?: 0.0, // Provide a default value if rain is null
        weatherDescription = weather.firstOrNull()?.description ?: "",
        icon = weather.firstOrNull()?.icon ?: "",
        dtTxt = dt_txt
    )
    private fun WeatherItem.toFavEntity() = FavForecastEntity(
        dt = dt,
        temp = main.temp,
        feelsLike = main.feelsLike,
        tempMin = main.tempMin,
        tempMax = main.tempMax,
        pressure = main.pressure,
        humidity = main.humidity,
        windSpeed = wind.speed,
        clouds = clouds.all,
        pop = pop,
        rainVolume = rain?.volume ?: 0.0, // Provide a default value if rain is null
        weatherDescription = weather.firstOrNull()?.description ?: "",
        icon = weather.firstOrNull()?.icon ?: "",
        dtTxt = dt_txt
    )

}
