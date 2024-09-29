package com.example.mvvm.repo
import com.example.mvvm.weather.model.pojos.FavoritePlaces
import com.example.mvvm.weather.model.pojos.FiveDayResponse
import com.example.mvvm.weather.model.repo.IWeatherRepository
import com.example.mvvm.weather.model.pojos.WeatherData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeWeatherRepository : IWeatherRepository {
    private val favoritePlacesFlow = MutableStateFlow<List<FavoritePlaces>>(emptyList())

    override fun getFavPlaces(): Flow<List<FavoritePlaces>> {
        return favoritePlacesFlow.asStateFlow()
    }

    override suspend fun insertFavoritePlace(place: FavoritePlaces) {
        val currentList = favoritePlacesFlow.value.toMutableList()
        currentList.add(place)
        favoritePlacesFlow.value = currentList
    }

    override suspend fun deleteFavoritePlace(place: FavoritePlaces) {
        val currentList = favoritePlacesFlow.value.toMutableList()
        currentList.remove(place)
        favoritePlacesFlow.value = currentList
    }

    override fun getWeatherDataFromRemote(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String,
        lang: String?
    ): Flow<WeatherData?> {
        TODO("Not yet implemented")
    }

    override fun getWeatherDataFromLocal(): Flow<WeatherData?> {
        TODO("Not yet implemented")
    }

    override suspend fun saveWeatherData(weatherData: WeatherData) {
        TODO("Not yet implemented")
    }

    override fun getForecastFromRemote(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String,
        lang: String?
    ): Flow<FiveDayResponse?> {
        TODO("Not yet implemented")
    }

    override fun getForecastFromLocal(): Flow<FiveDayResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun saveForecastData(forecastData: FiveDayResponse) {
        TODO("Not yet implemented")
    }


}
