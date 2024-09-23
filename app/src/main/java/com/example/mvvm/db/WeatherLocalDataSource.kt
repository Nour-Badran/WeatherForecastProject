package com.example.mvvm.db

import android.util.Log
import com.example.mvvm.model.FiveDayResponse
import com.example.mvvm.model.Forecast
import com.example.mvvm.model.WeatherData
import com.example.mvvm.model.WeatherItem
import com.example.mvvm.utilities.toEntity
import com.example.mvvm.utilities.toFavEntity
import com.example.mvvm.utilities.toFavModel
import com.example.mvvm.utilities.toModel
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
    fun getAllProducts(): Flow<List<FavoritePlaces>> {
        return weatherDao.getAll()
    }
    suspend fun insertPlace(place: FavoritePlaces) {
        weatherDao.insert(place)
    }

    suspend fun deletePlace(place: FavoritePlaces) {
        weatherDao.delete(place)
    }
}
