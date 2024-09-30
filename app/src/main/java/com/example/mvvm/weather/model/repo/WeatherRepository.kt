package com.example.mvvm.weather.model.repo

import com.example.mvvm.weather.model.localdatasource.IWeatherLocalDataSource
import com.example.mvvm.weather.model.localdatasource.WeatherLocalDataSource
import com.example.mvvm.weather.model.pojos.FavoritePlaces
import com.example.mvvm.weather.model.pojos.FiveDayResponse
import com.example.mvvm.weather.model.pojos.WeatherData
import com.example.mvvm.weather.model.remotedatasource.IWeatherRemoteDataSource
import com.example.mvvm.weather.model.remotedatasource.WeatherRemoteDataSource
import com.example.mvvm.weather.model.utilities.mapToFiveDayResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface IWeatherRepository {
    fun getFavPlaces(): Flow<List<FavoritePlaces>>

    suspend fun insertFavoritePlace(place: FavoritePlaces)

    suspend fun deleteFavoritePlace(place: FavoritePlaces)
    fun getWeatherDataFromRemote(lat: Double, lon: Double, apiKey: String, units: String, lang: String?): Flow<WeatherData?>
    fun getWeatherDataFromLocal(): Flow<WeatherData?>

    suspend fun saveWeatherData(weatherData: WeatherData)
    fun getForecastFromRemote(lat: Double, lon: Double, apiKey: String, units: String, lang: String?): Flow<FiveDayResponse?>
    fun getForecastFromLocal(): Flow<FiveDayResponse>

    suspend fun saveForecastData(forecastData: FiveDayResponse)
}

class WeatherRepository(
    private val localDataSource: IWeatherLocalDataSource,
    private val remoteDataSource: IWeatherRemoteDataSource
) : IWeatherRepository {

    override fun getFavPlaces(): Flow<List<FavoritePlaces>> {
        return localDataSource.getFavPlaces()
    }

    override suspend fun insertFavoritePlace(place: FavoritePlaces) {
        localDataSource.insertPlace(place)
    }

    override suspend fun deleteFavoritePlace(place: FavoritePlaces) {
        localDataSource.deletePlace(place)
    }

    override fun getWeatherDataFromRemote(lat: Double, lon: Double, apiKey: String, units: String, lang: String?): Flow<WeatherData?> {
        return remoteDataSource.fetchWeatherData(lat, lon, apiKey, units, lang)
    }

    override fun getWeatherDataFromLocal(): Flow<WeatherData?> {
        return localDataSource.getWeatherData()
    }

    override suspend fun saveWeatherData(weatherData: WeatherData) {
        localDataSource.saveWeatherData(weatherData)
    }

    override fun getForecastFromRemote(lat: Double, lon: Double, apiKey: String, units: String, lang: String?): Flow<FiveDayResponse?> {
        return remoteDataSource.fetchForecast(lat, lon, apiKey, units, lang)
    }

    override fun getForecastFromLocal(): Flow<FiveDayResponse> {
        return localDataSource.getForecastData()
            .map { it.mapToFiveDayResponse() }
    }

    override suspend fun saveForecastData(forecastData: FiveDayResponse) {
        localDataSource.saveForecastData(forecastData)
    }
}


