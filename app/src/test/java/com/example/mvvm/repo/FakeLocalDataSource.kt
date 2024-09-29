package com.example.mvvm.repo

import com.example.mvvm.weather.model.localdatasource.IWeatherLocalDataSource
import com.example.mvvm.weather.model.pojos.FavoritePlaces
import com.example.mvvm.weather.model.pojos.FiveDayResponse
import com.example.mvvm.weather.model.pojos.ForecastEntity
import com.example.mvvm.weather.model.pojos.WeatherData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

class FakeLocalDataSource : IWeatherLocalDataSource{
    private val favoritePlacesFlow : MutableList<FavoritePlaces> = mutableListOf()

    override suspend fun saveWeatherData(weatherData: WeatherData) {
        TODO("Not yet implemented")
    }

    override suspend fun saveForecastData(forecast: FiveDayResponse) {
        TODO("Not yet implemented")
    }

    override fun getWeatherData(): Flow<WeatherData> {
        TODO("Not yet implemented")
    }

    override fun getForecastData(): Flow<List<ForecastEntity>> {
        TODO("Not yet implemented")
    }

    override fun getFavPlaces(): Flow<MutableList<FavoritePlaces>> {
        return flow {
            emit(favoritePlacesFlow)
        }
    }

    override suspend fun insertPlace(place: FavoritePlaces) {
        favoritePlacesFlow.add(place)
    }

    override suspend fun deletePlace(place: FavoritePlaces) {
        favoritePlacesFlow.remove(place)
    }
}