package com.example.mvvm.viewmodel

import android.app.Application
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.mvvm.db.WeatherDatabase
import com.example.mvvm.db.WeatherLocalDataSource
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
    @RequiresApi(Build.VERSION_CODES.O)
    fun mapDailyWeather(response: FiveDayResponse): List<DailyWeather> {
        val dailyMap = mutableMapOf<LocalDate, DailyWeather>()

        // Iterate over the weather data list
        response.list.forEach { item ->
            val timestamp = item.dt
            val date = Instant.ofEpochSecond(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            val temperature = item.main.temp
            val icon = item.weather[0].icon
            val weatherStatus = item.weather[0].description  // Weather status from the description

            // If it's the first data point for the day, initialize the DailyWeather object
            if (!dailyMap.containsKey(date)) {
                dailyMap[date] = DailyWeather(
                    day = timestamp,      // First timestamp for the day
                    icon = icon,
                    minTemp = temperature,
                    maxTemp = temperature,
                    weatherStatus = weatherStatus  // Set the weather status
                )
            } else {
                // If the day is already present, update the min/max temperatures
                val dailyWeather = dailyMap[date]!!
                dailyWeather.minTemp = minOf(dailyWeather.minTemp, temperature)
                dailyWeather.maxTemp = maxOf(dailyWeather.maxTemp, temperature)
                // Optionally, update weatherStatus if needed
            }
        }

        // Convert the map values to a list and return it
        return dailyMap.values.toList()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun mapHourlyWeatherForToday(response: FiveDayResponse): List<HourlyWeather> {
        // Get the current date in the system's default timezone
        val currentDate = LocalDate.now(ZoneId.systemDefault())

        // Filter hourly data to find entries for today
        val hourlyDataForToday = response.list.filter { item ->
            val forecastDate = Instant.ofEpochSecond(item.dt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            forecastDate == currentDate
        }

        if (hourlyDataForToday.isEmpty()) {
            return emptyList()  // Return empty list or default data
        }

        // Map the filtered data to HourlyWeather
        return hourlyDataForToday.map { item ->
            HourlyWeather(
                day = item.dt,         // timestamp in seconds
                icon = item.weather[0].description,
                temperature = item.main.temp
            )
        }
    }
}
