package com.example.mvvm.model

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.mvvm.db.FavForecastEntity
import com.example.mvvm.db.FavoritePlaces
import com.example.mvvm.db.ForecastEntity
import com.example.mvvm.db.WeatherLocalDataSource
import com.example.mvvm.network.WeatherRemoteDataSource
import com.example.mvvm.utilities.isConnectedToInternet
import com.example.mvvm.utilities.mapToFavFiveDayResponse
import com.example.mvvm.utilities.mapToFiveDayResponse
import com.example.mvvm.utilities.toWeatherItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withTimeoutOrNull

class WeatherRepository(
    private val context: Context,
    private val localDataSource: WeatherLocalDataSource,
    private val remoteDataSource: WeatherRemoteDataSource
) {
     suspend fun getFavoriteProducts(): Flow<List<FavoritePlaces>> {
        return localDataSource.getAllProducts()
    }

    suspend fun insertFavoritePlace(place: FavoritePlaces) {
        localDataSource.insertPlace(place)
    }

    suspend fun deleteFavoritePlace(place: FavoritePlaces) {
        localDataSource.deletePlace(place)
    }
    suspend fun getWeatherData(lat: Double, lon: Double, apiKey: String, units: String,lang: String? = null,favDatabase: Boolean = false): Flow<WeatherData?> = flow {
        val data = if (isConnectedToInternet(context)) {
            // Set a timeout duration for remote data fetching
            val remoteData = withTimeoutOrNull(5000L) { // Timeout after 5 seconds
                remoteDataSource.fetchWeatherData(lat, lon, apiKey, units , lang)
            }
            if (remoteData != null) {
                // Save data to the local database
                if(!favDatabase)
                {
                    localDataSource.saveWeatherData(remoteData)
                }
                else
                {
                    localDataSource.saveFavWeatherData(remoteData)  //handle fav
                }
                emit(remoteData)
            } else {
                // Fallback to local data if network is slow or data is null
                if(!favDatabase)
                {
                    emit(localDataSource.getWeatherData())
                }
                else
                {
                    emit(localDataSource.getFavWeatherData()) // handle fav
                }
            }
        } else {
            // Fetch from local database if no internet connection
            if(!favDatabase)
            {
                emit(localDataSource.getWeatherData())
            }
            else
            {
                emit(localDataSource.getFavWeatherData()) //handle fav
            }
        }
    }.shareIn(CoroutineScope(Dispatchers.Default), SharingStarted.Lazily)

    suspend fun getForecast(lat: Double, lon: Double, apiKey: String, units: String,lang: String? = null,favDatabase: Boolean = false): Flow<FiveDayResponse?> = flow {
        val data = if (isConnectedToInternet(context)) {
            // Timeout for remote forecast data fetching
            val remoteForecast = withTimeoutOrNull(5000L) { // Timeout after 5 seconds
                remoteDataSource.fetchForecast(lat, lon, apiKey, units,lang)
            }
            if (remoteForecast != null) {
                // Save forecast data locally
                if(!favDatabase)
                {
                    localDataSource.saveForecastData(remoteForecast)
                }
                else
                {
                    localDataSource.saveFavForecastData(remoteForecast) // handle fav
                }
                emit(remoteForecast)
            } else {
                // Fallback to local data if network is slow or data is null
                if(!favDatabase)
                {
                    val forecast: List<ForecastEntity>
                    forecast = localDataSource.getForecastData()
                    emit(forecast.takeIf { it.isNotEmpty() }?.let { forecast.mapToFiveDayResponse() })
                }
                else
                {
                    val forecast: List<FavForecastEntity>
                    forecast = localDataSource.getFavForecastData()  // handle fav
                    emit(forecast.takeIf { it.isNotEmpty() }?.let { forecast.mapToFavFiveDayResponse() })
                }
            }
        } else {
            // Fetch from local database if no internet connection
            if(!favDatabase)
            {
                val forecast: List<ForecastEntity>
                forecast = localDataSource.getForecastData()
                emit(forecast.takeIf { it.isNotEmpty() }?.let { forecast.mapToFiveDayResponse() })
            }
            else
            {
                val forecast: List<FavForecastEntity>
                forecast = localDataSource.getFavForecastData() // handle fav
                emit(forecast.takeIf { it.isNotEmpty() }?.let { forecast.mapToFavFiveDayResponse() })
            }
        }
    }
}

