package com.example.mvvm.model

import android.content.Context
import com.example.mvvm.db.FavForecastEntity
import com.example.mvvm.db.FavoritePlaces
import com.example.mvvm.db.WeatherLocalDataSource
import com.example.mvvm.network.weatherApi.WeatherRemoteDataSource
import com.example.mvvm.utilities.isConnectedToInternet
import com.example.mvvm.utilities.mapToFiveDayResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withTimeoutOrNull

class WeatherRepository(
    private val context: Context,
    private val localDataSource: WeatherLocalDataSource,
    private val remoteDataSource: WeatherRemoteDataSource
) {
    fun getFavPlaces(): Flow<List<FavoritePlaces>> {
        return localDataSource.getAllProducts()
    }

    suspend fun insertFavoritePlace(place: FavoritePlaces) {
        localDataSource.insertPlace(place)
    }

    suspend fun deleteFavoritePlace(place: FavoritePlaces) {
        localDataSource.deletePlace(place)
    }
    suspend fun getWeatherData(lat: Double, lon: Double, apiKey: String, units: String,lang: String? = null,favDatabase: Boolean = false): Flow<Pair<WeatherData?, Boolean>> = flow {
        val isLocalData: Boolean
        val data = if (isConnectedToInternet(context)) {
            // Set a timeout duration for remote data fetching
            val remoteData = withTimeoutOrNull(5000L) {
                remoteDataSource.fetchWeatherData(lat, lon, apiKey, units, lang)
            }
            if (remoteData != null) {
                if (!favDatabase) {
                    localDataSource.saveWeatherData(remoteData)
                }
                emit(Pair(remoteData, false))  // Data from remote source, not local
            } else {
                // Fallback to local data if network is slow or data is null
                isLocalData = true
                val localData = if (!favDatabase) localDataSource.getWeatherData().firstOrNull() else null
                emit(Pair(localData, isLocalData))
            }
        } else {
            // No internet connection, fetch from local database
            isLocalData = true
            val localData = if (!favDatabase) localDataSource.getWeatherData().firstOrNull() else null
            emit(Pair(localData, isLocalData))
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
                emit(remoteForecast)
            } else {
                // Fallback to local data if network is slow or data is null
                if(!favDatabase)
                {
                    localDataSource.getForecastData().collect { forecastList ->
                        emit(forecastList.takeIf { it.isNotEmpty() }?.mapToFiveDayResponse())
                    }
                }
                else
                {
                    val forecast: List<FavForecastEntity>
                    forecast = localDataSource.getFavForecastData()  // handle fav
                    //emit(forecast.takeIf { it.isNotEmpty() }?.let { forecast.mapToFavFiveDayResponse() })
                }
            }
        } else {
            // Fetch from local database if no internet connection
            if(!favDatabase)
            {
                localDataSource.getForecastData().collect { forecastList ->
                    emit(forecastList.takeIf { it.isNotEmpty() }?.mapToFiveDayResponse())
                }
            }
            else
            {
                val forecast: List<FavForecastEntity>
                forecast = localDataSource.getFavForecastData() // handle fav
                //emit(forecast.takeIf { it.isNotEmpty() }?.let { forecast.mapToFavFiveDayResponse() })
            }
        }
    }
}

sealed class ApiState<out T> {
    data class Success<out T>(val data: T) : ApiState<T>()
    data class Error(val exception: Throwable) : ApiState<Nothing>()
    object Loading : ApiState<Nothing>()
}