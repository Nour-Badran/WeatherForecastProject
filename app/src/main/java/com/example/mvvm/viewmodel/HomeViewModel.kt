package com.example.mvvm.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.mvvm.db.WeatherDatabase
import com.example.mvvm.db.WeatherLocalDataSource
import com.example.mvvm.model.WeatherData
import com.example.mvvm.model.WeatherRepository
import com.example.mvvm.network.WeatherRemoteDataSource
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val weatherDao = WeatherDatabase.getDatabase(application).weatherDao()
    private val weatherRepository = WeatherRepository(
        context = application,  // Pass context for network checks
        localDataSource = WeatherLocalDataSource(weatherDao),
        remoteDataSource = WeatherRemoteDataSource()
    )

    private val _weatherData = MutableLiveData<WeatherData>()
    val weatherData: LiveData<WeatherData> get() = _weatherData

    fun fetchWeatherData(lat: Double, lon: Double, apiKey: String, units: String) {
        viewModelScope.launch {
            val weather = weatherRepository.getWeatherData(lat, lon, apiKey, units)
            _weatherData.postValue(weather!!)
        }
    }
}
