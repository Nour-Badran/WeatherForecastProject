package com.example.mvvm.model

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.mvvm.db.WeatherLocalDataSource
import com.example.mvvm.network.WeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class WeatherRepository(
    private val context: Context,
    private val localDataSource: WeatherLocalDataSource,
    private val remoteDataSource: WeatherRemoteDataSource
) {

    suspend fun getWeatherData(lat: Double, lon: Double, apiKey: String, units: String): Flow<WeatherData?> = flow {
        val data = if (isConnectedToInternet()) {
            val remoteData = remoteDataSource.fetchWeatherData(lat, lon, apiKey, units)
            if (remoteData != null) {
                localDataSource.saveWeatherData(remoteData)
            }
            emit(remoteData)
        } else {
            emit(localDataSource.getWeatherData())
        }
    }

    suspend fun getForecast(lat: Double, lon: Double, apiKey: String, units: String): Flow<FiveDayResponse?> = flow {
        val data = if (isConnectedToInternet()) {
            remoteDataSource.fetchForecast(lat, lon, apiKey, units)
        } else {
            null
        }
        emit(data)
    }

    private fun isConnectedToInternet(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}
