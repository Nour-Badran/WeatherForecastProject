package com.example.mvvm.network

import com.example.mvvm.model.WeatherApiResponse
import com.example.mvvm.model.WeatherData
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRemoteDataSource {

    private val weatherService: WeatherService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherService = retrofit.create(WeatherService::class.java)
    }

    suspend fun fetchWeatherData(lat: Double, lon: Double, apiKey: String, units: String): WeatherData? {
        return try {
            val response = weatherService.getWeatherLatLong(lat, apiKey, lon, units)
            if (response.isSuccessful) {
                response.body()?.let { mapToWeatherData(it) }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun mapToWeatherData(apiResponse: WeatherApiResponse): WeatherData {
        return WeatherData(
            cityName = apiResponse.city?.name ?: "",
            temperature = apiResponse.main?.temp ?: 0.0,
            description = apiResponse.weather?.firstOrNull()?.description ?: "",
            humidity = apiResponse.main?.humidity ?: 0,
            windSpeed = apiResponse.wind?.speed ?: 0.0,
            pressure = apiResponse.main?.pressure ?: 0,
            clouds = apiResponse.clouds?.all ?: 0,
            forecast = apiResponse.forecast ?: emptyList()
        )
    }
}