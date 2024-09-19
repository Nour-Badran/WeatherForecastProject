package com.example.mvvm.network

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.mvvm.R
import com.example.mvvm.model.DailyWeather
import com.example.mvvm.model.FiveDayResponse
import com.example.mvvm.model.HourlyWeather
import com.example.mvvm.model.WeatherApiResponse
import com.example.mvvm.model.WeatherData
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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


    private fun mapToWeatherData(apiResponse: WeatherApiResponse): WeatherData {
        Log.d("Before Map", apiResponse.dt.toString())
        return WeatherData(
            cityName = apiResponse.cityName ?: "",  // Map the root-level city name
            temperature = apiResponse.main?.temp ?: 0.0,
            description = apiResponse.weather?.firstOrNull()?.description ?: "",
            humidity = apiResponse.main?.humidity ?: 0,
            windSpeed = apiResponse.wind?.speed ?: 0.0,
            pressure = apiResponse.main?.pressure ?: 0,
            clouds = apiResponse.clouds?.all ?: 0,
            dt = apiResponse.dt,
            visibility = apiResponse.visibility,
            iconResId = mapToWeatherIcon(apiResponse.weather?.firstOrNull()?.description ?: ""),
            forecast = apiResponse.forecast ?: emptyList(),
            icon = apiResponse.weather?.get(0)?.icon ?: ""
        )

    }

    private fun mapToWeatherIcon(description: String): Int {
        return when {
            description.contains("clear", ignoreCase = true) -> R.drawable.sun
            description.contains("clouds", ignoreCase = true) -> R.drawable.cloudyday
            description.contains("rain", ignoreCase = true) -> R.drawable.rainyday
            description.contains("thunderstorm", ignoreCase = true) -> R.drawable.thunderstorms
            description.contains("snow", ignoreCase = true) -> R.drawable.snow
            description.contains("mist", ignoreCase = true) -> R.drawable.fog
            else -> R.drawable.sun // Default weather icon
        }
    }



}
