package com.example.mvvm.weather.model.remotedatasource

import com.example.mvvm.weather.model.pojos.FiveDayResponse
import com.example.mvvm.weather.model.pojos.WeatherData
import com.example.mvvm.weather.model.utilities.mapToWeatherData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
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

     fun fetchWeatherData(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String,
        lang: String? = null
    ): Flow<WeatherData?> {
        return flow {
            val response = weatherService.getWeatherLatLong(lat, lon, apiKey, units, lang)
            if (response.isSuccessful) {
                // Emit the mapped weather data
                emit(response.body()?.let { mapToWeatherData(it) })
            } else {
                emit(null) // Emit null if the response is not successful
            }
        }.catch {
            emit(null) // Handle any exceptions and emit null
        }
    }

     fun fetchForecast(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String,
        lang: String? = null
    ): Flow<FiveDayResponse?> {
        return flow {
            val response = weatherService.get5DayForecast(lat, lon,apiKey,  units,lang)
            if (response.isSuccessful) {
                emit(response.body())
            } else {
                null
            }
        }.catch {
            emit(null)
        }
    }
}
