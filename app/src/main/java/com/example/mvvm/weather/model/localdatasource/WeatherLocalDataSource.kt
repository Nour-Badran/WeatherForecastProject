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

interface IWeatherLocalDataSource {
    suspend fun saveWeatherData(weatherData: WeatherData)

    suspend fun saveForecastData(forecast: FiveDayResponse)
    fun getWeatherData(): Flow<WeatherData>
    fun getForecastData(): Flow<List<ForecastEntity>>
    fun getFavPlaces(): Flow<List<FavoritePlaces>>

    suspend fun insertPlace(place: FavoritePlaces)

    suspend fun deletePlace(place: FavoritePlaces)
}

class WeatherLocalDataSource(private val weatherDao: WeatherDao) : IWeatherLocalDataSource {

    override suspend fun saveWeatherData(weatherData: WeatherData) {
        withContext(Dispatchers.IO) {
            weatherDao.deleteAllWeatherData()
            weatherDao.insertWeatherData(weatherData.toEntity())
        }
    }

    override suspend fun saveForecastData(forecast: FiveDayResponse) {
        withContext(Dispatchers.IO) {
            weatherDao.deleteAllForecastData()
            val forecastEntities = forecast.list.map { it.toEntity() }
            weatherDao.insertForecastData(forecastEntities)
        }
    }

    override fun getWeatherData(): Flow<WeatherData> {
        return weatherDao.getWeatherData().map { it.toModel() }
    }

    override fun getForecastData(): Flow<List<ForecastEntity>> {
        return weatherDao.getForecastData()
    }

    override fun getFavPlaces(): Flow<List<FavoritePlaces>> {
        return weatherDao.getAll()
    }

    override suspend fun insertPlace(place: FavoritePlaces) {
        weatherDao.insert(place)
    }

    override suspend fun deletePlace(place: FavoritePlaces) {
        weatherDao.delete(place)
    }
}
