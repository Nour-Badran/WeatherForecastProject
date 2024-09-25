package com.example.mvvm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvm.db.WeatherDatabase
import com.example.mvvm.db.WeatherLocalDataSource
import com.example.mvvm.model.ApiState
import com.example.mvvm.utilities.mapDailyWeather
import com.example.mvvm.utilities.mapHourlyWeatherForTodayAndTomorrow
import com.example.mvvm.model.DailyWeather
import com.example.mvvm.model.HourlyWeather
import com.example.mvvm.model.WeatherData
import com.example.mvvm.model.WeatherRepository
import com.example.mvvm.network.WeatherRemoteDataSource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    application: Application,
    private val weatherRepository: WeatherRepository
) : AndroidViewModel(application) {

    private val _weatherData = MutableStateFlow<ApiState<WeatherData?>>(ApiState.Loading)
    val weatherData: StateFlow<ApiState<WeatherData?>> = _weatherData

    private val _dailyWeather = MutableStateFlow<ApiState<List<DailyWeather>>>(ApiState.Loading)
    val dailyWeather: StateFlow<ApiState<List<DailyWeather>>> = _dailyWeather

    private val _hourlyWeather = MutableStateFlow<ApiState<List<HourlyWeather>>>(ApiState.Loading)
    val hourlyWeather: StateFlow<ApiState<List<HourlyWeather>>> = _hourlyWeather

    private val _isLocalDataUsed = MutableStateFlow(false)
    val isLocalDataUsed: StateFlow<Boolean> = _isLocalDataUsed
    fun fetchWeatherData(lat: Double, lon: Double, apiKey: String, units: String,lang: String? = null,favDatabase: Boolean = false) {
        viewModelScope.launch {
            try {
                weatherRepository.getWeatherData(lat, lon, apiKey, units, lang, favDatabase)
                    .onStart { _weatherData.value = ApiState.Loading }
                    .collect { (weatherData, isLocalData) ->
                        if (weatherData != null) {
                            _weatherData.value = ApiState.Success(weatherData)
                        } else {
                            _weatherData.value = ApiState.Error(Exception("No data found"))
                        }
                        // Update state to notify UI if local data is used
                        if (isLocalData) {
                            _isLocalDataUsed.value = true
                        }
                    }
            } catch (e: Exception) {
                _weatherData.value = ApiState.Error(e)
            }
        }
    }

    fun fetchForecastData(lat: Double, lon: Double, apiKey: String, units: String,lang: String? = null,favDatabase: Boolean = false) {
        viewModelScope.launch {
            try{
                weatherRepository.getForecast(lat, lon, apiKey, units,lang,favDatabase)
                    .onStart {
                        _dailyWeather.value = ApiState.Loading
                        _hourlyWeather.value = ApiState.Loading
                    }
                    .collect { forecast ->
                        if (forecast != null) {
                            _dailyWeather.value = ApiState.Success(mapDailyWeather(forecast))
                            _hourlyWeather.value = ApiState.Success(
                                mapHourlyWeatherForTodayAndTomorrow(forecast)
                            )
                        }
                    }
            } catch (e: Exception) {
                _dailyWeather.value = ApiState.Error(e)
                _hourlyWeather.value = ApiState.Error(e)
            }

        }
    }
}
