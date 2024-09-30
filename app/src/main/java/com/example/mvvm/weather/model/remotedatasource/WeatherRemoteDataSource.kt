package com.example.mvvm.weather.model.remotedatasource

import com.example.mvvm.weather.model.pojos.FiveDayResponse
import com.example.mvvm.weather.model.pojos.WeatherData
import com.example.mvvm.weather.model.utilities.mapToWeatherData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface IWeatherRemoteDataSource {
    fun fetchWeatherData(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String,
        lang: String? = null
   ): Flow<WeatherData?>

    fun fetchForecast(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String,
        lang: String? = null
  ): Flow<FiveDayResponse?>
}

class WeatherRemoteDataSource : IWeatherRemoteDataSource {

    private val weatherService: WeatherService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherService = retrofit.create(WeatherService::class.java)
    }

     override fun fetchWeatherData(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String,
        lang: String?
     ): Flow<WeatherData?> {
        return flow {
            val response = weatherService.getWeatherLatLong(lat, lon, apiKey, units, lang)
            if (response.isSuccessful) {
                emit(response.body()?.let { mapToWeatherData(it) })
            } else {
                emit(null)
            }
        }.catch {
            emit(null)
        }
    }

     override fun fetchForecast(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String,
        lang: String?
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
