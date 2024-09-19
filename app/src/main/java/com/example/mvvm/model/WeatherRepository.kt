package com.example.mvvm.model

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.mvvm.db.WeatherLocalDataSource
import com.example.mvvm.network.WeatherRemoteDataSource

class WeatherRepository(
    private val context: Context,  // Pass context to check for internet connection
    private val localDataSource: WeatherLocalDataSource,
    private val remoteDataSource: WeatherRemoteDataSource
) {

    suspend fun getWeatherData(lat: Double, lon: Double, apiKey: String, units: String): WeatherData? {
        return if (isConnectedToInternet()) {
            // Fetch from remote data source
            val remoteData = remoteDataSource.fetchWeatherData(lat, lon, apiKey, units)
            remoteData?.let {
                // Save to local data source for offline use
                localDataSource.saveWeatherData(it)
            }
            remoteData
        } else {
            // No internet, retrieve from local data source
            localDataSource.getWeatherData()
        }
    }
    suspend fun getForecast(lat: Double, lon: Double, apiKey: String, units: String): FiveDayResponse? {
        return if (isConnectedToInternet()) {
            // Fetch from remote data source
            val remoteData = remoteDataSource.fetchForecast(lat, lon, apiKey, units)
//            remoteData?.let {
//                // Save to local data source for offline use
//                localDataSource.saveWeatherData(it)
//            }
            remoteData
        } else {
            // No internet, retrieve from local data source
            //localDataSource.getWeatherData()
            return null
        }
    }

    private fun isConnectedToInternet(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
}
