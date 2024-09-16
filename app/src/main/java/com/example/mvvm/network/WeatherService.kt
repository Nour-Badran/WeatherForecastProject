package com.example.mvvm.network

import com.example.mvvm.model.WeatherApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("data/2.5/weather")
    suspend fun getWeatherSync(
        @Query("q") location: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): Response<WeatherApiResponse>

    @GET("data/2.5/weather")
    suspend fun getWeatherLatLong(
        @Query("lat") lat: Double,
        @Query("appid") apiKey: String,
        @Query("lon") lon: Double,
        @Query("units") units: String
    ): Response<WeatherApiResponse>
}
