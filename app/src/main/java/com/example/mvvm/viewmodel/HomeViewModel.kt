package com.example.mvvm.viewmodel

import android.app.Application
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.mvvm.db.WeatherDatabase
import com.example.mvvm.db.WeatherLocalDataSource
import com.example.mvvm.mapDailyWeather
import com.example.mvvm.mapHourlyWeatherForToday
import com.example.mvvm.model.DailyWeather
import com.example.mvvm.model.FiveDayResponse
import com.example.mvvm.model.HourlyWeather
import com.example.mvvm.model.WeatherData
import com.example.mvvm.model.WeatherRepository
import com.example.mvvm.network.WeatherRemoteDataSource
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val weatherDao = WeatherDatabase.getDatabase(application).weatherDao()
    private val weatherRepository = WeatherRepository(
        context = application,  // Pass context for network checks
        localDataSource = WeatherLocalDataSource(weatherDao),
        remoteDataSource = WeatherRemoteDataSource()
    )

    private val _weatherData = MutableLiveData<WeatherData>()
    val weatherData: LiveData<WeatherData> get() = _weatherData

    private val _dailyWeather = MutableLiveData<List<DailyWeather>>()
    val dailyWeather = _dailyWeather

    private val _hourlyWeather = MutableLiveData<List<HourlyWeather>>()
    val hourlyWeather = _hourlyWeather// create functions

    fun fetchWeatherData(lat: Double, lon: Double, apiKey: String, units: String) {
        viewModelScope.launch {
            val weather = weatherRepository.getWeatherData(lat, lon, apiKey, units)
            _weatherData.postValue(weather!!)
        }
    }
    fun fetchForecastData(lat: Double, lon: Double, apiKey: String, units: String) {
        viewModelScope.launch {
            val forecast = weatherRepository.getForecast(lat, lon, apiKey, units)
            if(forecast!=null)
            {
                val daily = mapDailyWeather(forecast)
                _dailyWeather.postValue(daily)
                val hourly = mapHourlyWeatherForToday(forecast)
                _hourlyWeather.postValue(hourly)
            }
        }
    }

}
