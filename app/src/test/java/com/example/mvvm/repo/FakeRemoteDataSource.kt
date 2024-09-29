package com.example.mvvm.repo

import com.example.mvvm.weather.model.pojos.FiveDayResponse
import com.example.mvvm.weather.model.pojos.WeatherData
import com.example.mvvm.weather.model.remotedatasource.IWeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow

class FakeRemoteDataSource:IWeatherRemoteDataSource {
    override fun fetchWeatherData(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String,
        lang: String?
    ): Flow<WeatherData?> {
        TODO("Not yet implemented")
    }

    override fun fetchForecast(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String,
        lang: String?
    ): Flow<FiveDayResponse?> {
        TODO("Not yet implemented")
    }
}