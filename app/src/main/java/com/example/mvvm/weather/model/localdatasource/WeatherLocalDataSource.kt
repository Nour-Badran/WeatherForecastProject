package com.example.mvvm.weather.model.localdatasource

import com.example.mvvm.weather.model.db.WeatherDao
import com.example.mvvm.weather.model.pojos.FavoritePlaces
import com.example.mvvm.weather.model.pojos.FiveDayResponse
import com.example.mvvm.weather.model.pojos.ForecastEntity
import com.example.mvvm.weather.model.pojos.WeatherData
import com.example.mvvm.weather.model.utilities.toEntity
import com.example.mvvm.weather.model.utilities.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    fun getWeatherData(): Flow<WeatherData> {
        return weatherDao.getWeatherData().map { it.toModel() }
    }

    fun getForecastData(): Flow<List<ForecastEntity>> {
        return weatherDao.getForecastData()
    }

    fun getFavPlaces(): Flow<List<FavoritePlaces>> {
        return weatherDao.getAll()
    }

    suspend fun insertPlace(place: FavoritePlaces) {
        weatherDao.insert(place)
    }

    suspend fun deletePlace(place: FavoritePlaces) {
        weatherDao.delete(place)
    }
}
