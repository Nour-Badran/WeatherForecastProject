package com.example.mvvm.network

import com.example.mvvm.utilities.mapToWeatherData
import com.example.mvvm.model.FiveDayResponse
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

    suspend fun fetchWeatherData(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String
    ): WeatherData? {
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

    suspend fun fetchForecast(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String
    ): FiveDayResponse? {
        return try {
            val response = weatherService.get5DayForecast(lat, apiKey, lon, units)
            if (response.isSuccessful) {
                return response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }








}
