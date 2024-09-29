package com.example.mvvm.weather.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvm.weather.model.pojos.DailyWeather
import com.example.mvvm.weather.model.pojos.HourlyWeather
import com.example.mvvm.weather.model.pojos.WeatherData
import com.example.mvvm.weather.model.repo.IWeatherRepository
import com.example.mvvm.weather.model.utilities.mapDailyWeather
import com.example.mvvm.weather.model.utilities.mapHourlyWeatherForTodayAndTomorrow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class HomeViewModel(
    application: Application,
    private val weatherRepository: IWeatherRepository
) : AndroidViewModel(application) {

    private val _weatherData = MutableStateFlow<ApiState<WeatherData?>>(ApiState.Loading)
    val weatherData: StateFlow<ApiState<WeatherData?>> = _weatherData

    private val _dailyWeather = MutableStateFlow<ApiState<List<DailyWeather>>>(ApiState.Loading)
    val dailyWeather: StateFlow<ApiState<List<DailyWeather>>> = _dailyWeather

    private val _hourlyWeather = MutableStateFlow<ApiState<List<HourlyWeather>>>(ApiState.Loading)
    val hourlyWeather: StateFlow<ApiState<List<HourlyWeather>>> = _hourlyWeather

    private val _isLocalDataUsed = MutableStateFlow(false)
    val isLocalDataUsed: StateFlow<Boolean> = _isLocalDataUsed

    fun fetchWeatherData(lat: Double, lon: Double, apiKey: String, units: String, lang: String? = null, fav: Boolean) {
        viewModelScope.launch {
            _weatherData.value = ApiState.Loading
            try {
                val remoteData = if (isConnectedToInternet()) {
                    withTimeoutOrNull(5000L) {
                        weatherRepository.getWeatherDataFromRemote(lat, lon, apiKey, units, lang)
                    }
                } else {
                    null
                }

                // Check if remote data is available
                if (remoteData != null) {
                    remoteData.collect { weatherData ->
                        if (!fav && weatherData != null) {
                            weatherRepository.saveWeatherData(weatherData)
                        }
                        _weatherData.value = ApiState.Success(weatherData)
                        _isLocalDataUsed.value = false
                    }
                } else {
                    // If no remote data, try to fetch from local
                    if (!fav) {
                        _isLocalDataUsed.value = true
                        weatherRepository.getWeatherDataFromLocal()
                            .collect { localWeatherData ->
                                _weatherData.value = ApiState.Success(localWeatherData)
                            }
                    } else
                        _weatherData.value = ApiState.Error(Throwable("Failed to fetch weather data and no local data available"))
                }
            } catch (e: Exception) {
                _weatherData.value = ApiState.Error(e)
            }
        }
    }

    fun fetchForecastData(lat: Double, lon: Double, apiKey: String, units: String, lang: String? = null, fav: Boolean) {
        viewModelScope.launch {
            _dailyWeather.value = ApiState.Loading
            _hourlyWeather.value = ApiState.Loading
            try {
                val remoteForecast = if (isConnectedToInternet()) {
                    withTimeoutOrNull(5000L) {
                        weatherRepository.getForecastFromRemote(lat, lon, apiKey, units, lang)
                    }
                } else {
                    null
                }

                // Check if remote forecast is available
                if (remoteForecast != null) {
                    remoteForecast.collect { forecastData ->
                        if (forecastData != null) {
                            if (!fav) {
                                weatherRepository.saveForecastData(forecastData)
                            }
                            _dailyWeather.value = ApiState.Success(mapDailyWeather(forecastData))
                            _hourlyWeather.value =
                                ApiState.Success(mapHourlyWeatherForTodayAndTomorrow(forecastData))
                        }
                    }
                } else {
                    // If no remote forecast, try to fetch from local
                    if (!fav) {
                        weatherRepository.getForecastFromLocal()
                            .collect { localForecastData ->
                                _dailyWeather.value =
                                    ApiState.Success(mapDailyWeather(localForecastData))
                                _hourlyWeather.value = ApiState.Success(
                                    mapHourlyWeatherForTodayAndTomorrow(localForecastData)
                                )
                            }
                    } else {
                        _dailyWeather.value = ApiState.Error(Throwable("Failed to fetch weather data and no local data available"))
                        _hourlyWeather.value = ApiState.Error(Throwable("Failed to fetch weather data and no local data available"))
                    }

                }
            } catch (e: Exception) {
                _dailyWeather.value = ApiState.Error(e)
                _hourlyWeather.value = ApiState.Error(e)
            }
        }
    }

    private fun isConnectedToInternet(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
