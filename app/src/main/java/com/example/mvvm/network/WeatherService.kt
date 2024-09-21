package com.example.mvvm.network

import com.example.mvvm.model.FiveDayResponse
import com.example.mvvm.model.WeatherApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("data/2.5/weather")
    suspend fun getWeatherLatLong(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String,
        @Query("lang") lang: String? = null // Optional language parameter
    ): Response<WeatherApiResponse>

    @GET("data/2.5/forecast")
    suspend fun get5DayForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String,
        @Query("lang") lang: String? = null // Optional language parameter
    ): Response<FiveDayResponse>
}

