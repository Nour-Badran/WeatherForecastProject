package com.example.mvvm.model

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.mvvm.db.ForecastEntity
import com.example.mvvm.db.WeatherLocalDataSource
import com.example.mvvm.network.WeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull

class WeatherRepository(
    private val context: Context,
    private val localDataSource: WeatherLocalDataSource,
    private val remoteDataSource: WeatherRemoteDataSource
) {

    suspend fun getWeatherData(lat: Double, lon: Double, apiKey: String, units: String): Flow<WeatherData?> = flow {
        val data = if (isConnectedToInternet()) {
            // Set a timeout duration for remote data fetching
            val remoteData = withTimeoutOrNull(5000L) { // Timeout after 5 seconds
                remoteDataSource.fetchWeatherData(lat, lon, apiKey, units)
            }
            if (remoteData != null) {
                // Save data to the local database
                localDataSource.saveWeatherData(remoteData)
                emit(remoteData)
            } else {
                // Fallback to local data if network is slow or data is null
                emit(localDataSource.getWeatherData())
            }
        } else {
            // Fetch from local database if no internet connection
            emit(localDataSource.getWeatherData())
        }
    }

    suspend fun getForecast(lat: Double, lon: Double, apiKey: String, units: String): Flow<FiveDayResponse?> = flow {
        val data = if (isConnectedToInternet()) {
            // Timeout for remote forecast data fetching
            val remoteForecast = withTimeoutOrNull(5000L) { // Timeout after 5 seconds
                remoteDataSource.fetchForecast(lat, lon, apiKey, units)
            }
            if (remoteForecast != null) {
                // Save forecast data locally
                localDataSource.saveForecastData(remoteForecast)
                emit(remoteForecast)
            } else {
                // Fallback to local data if network is slow or data is null
                val forecast = localDataSource.getForecastData()
                emit(forecast.takeIf { it.isNotEmpty() }?.let { forecast.mapToFiveDayResponse() })
            }
        } else {
            // Fetch from local database if no internet connection
            val forecast = localDataSource.getForecastData()
            emit(forecast.takeIf { it.isNotEmpty() }?.let { forecast.mapToFiveDayResponse() })
        }
    }

    private fun isConnectedToInternet(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    // Helper function to map forecast entities back to FiveDayResponse if needed
    private fun List<ForecastEntity>.mapToFiveDayResponse(): FiveDayResponse {
        return FiveDayResponse(
            cod = "200",
            message = 0,
            cnt = this.size,
            list = this.map { it.toWeatherItem() },
            city = City(name = "")
        )
    }

    private fun ForecastEntity.toWeatherItem() = WeatherItem(
        dt = dt,
        main = Main(
            temp = temp,
            feelsLike = feelsLike,
            tempMin = tempMin,
            tempMax = tempMax,
            pressure = pressure,
            humidity = humidity
        ),
        weather = listOf(WeatherData(
            cityName = "",
            temperature = temp,
            description = weatherDescription,
            humidity = humidity,
            windSpeed = windSpeed,
            pressure = pressure,
            clouds = clouds,
            dt = dt,
            forecast = emptyList(),
            iconResId = 0,
            visibility = 0,
            icon = icon
        )),
        clouds = Clouds(clouds),
        wind = Wind(windSpeed),
        dt_txt = dtTxt, // Use dtTxt
        pop = pop,
        rain = Rain(rainVolume)
    )
}

