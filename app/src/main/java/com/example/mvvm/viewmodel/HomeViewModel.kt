package com.example.mvvm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvm.db.WeatherDatabase
import com.example.mvvm.db.WeatherLocalDataSource
import com.example.mvvm.mapDailyWeather
import com.example.mvvm.mapHourlyWeatherForTodayAndTomorrow
import com.example.mvvm.model.DailyWeather
import com.example.mvvm.model.FiveDayResponse
import com.example.mvvm.model.HourlyWeather
import com.example.mvvm.model.WeatherData
import com.example.mvvm.model.WeatherRepository
import com.example.mvvm.network.WeatherRemoteDataSource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val weatherDao = WeatherDatabase.getDatabase(application).weatherDao()
    private val weatherRepository = WeatherRepository(
        context = application,
        localDataSource = WeatherLocalDataSource(weatherDao),
        remoteDataSource = WeatherRemoteDataSource()
    )

    private val _weatherData = MutableStateFlow<WeatherData?>(null)
    val weatherData: StateFlow<WeatherData?> = _weatherData

    private val _dailyWeather = MutableStateFlow<List<DailyWeather>>(emptyList())
    val dailyWeather: StateFlow<List<DailyWeather>> = _dailyWeather

    private val _hourlyWeather = MutableStateFlow<List<HourlyWeather>>(emptyList())
    val hourlyWeather: StateFlow<List<HourlyWeather>> = _hourlyWeather

    fun fetchWeatherData(lat: Double, lon: Double, apiKey: String, units: String) {
        viewModelScope.launch {
            weatherRepository.getWeatherData(lat, lon, apiKey, units)
                .collect { weatherData ->
                    _weatherData.value = weatherData
                }
        }
    }

    fun fetchForecastData(lat: Double, lon: Double, apiKey: String, units: String) {
        viewModelScope.launch {
            weatherRepository.getForecast(lat, lon, apiKey, units)
                .collect { forecast ->
                    if (forecast != null) {
                        _dailyWeather.value = mapDailyWeather(forecast)
                        _hourlyWeather.value = mapHourlyWeatherForTodayAndTomorrow(forecast)
                    }
                }
        }
    }
}
