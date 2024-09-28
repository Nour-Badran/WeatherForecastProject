package com.example.mvvm.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mvvm.FakeWeatherRepository
import com.example.mvvm.weather.viewmodel.ApiState
import com.example.mvvm.weather.viewmodel.HomeViewModel
import com.example.mvvm.weather.model.repo.IWeatherRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import junit.framework.TestCase.assertEquals

@RunWith(AndroidJUnit4::class)
class HomeViewModelTest {

    lateinit var repo: IWeatherRepository
    lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        repo = FakeWeatherRepository()
        val testingContext: Application = ApplicationProvider.getApplicationContext()
        viewModel = HomeViewModel(testingContext, repo)
    }

    @Test
    fun fetchWeatherData_WithValidData_ReturnsSuccess() = runTest {
        // Arrange
        val lat = 12.34
        val lon = 56.78
        val apiKey = "test_api_key"
        val units = "metric"
        val lang = "en"
        val fav = false

        // Act
        viewModel.fetchWeatherData(lat, lon, apiKey, units, lang, fav)

        // Assert
        val result = viewModel.weatherData.first()
        println("Result: $result")
        assert(result is ApiState.Success)
        assertEquals((result as ApiState.Success).data, repo.getWeatherDataFromRemote(lat, lon, apiKey, units, lang))
    }


//    @Test
//    fun fetchWeatherData_NoInternet_ReturnsLocalData() = runTest {
//        // Arrange
//        val lat = 12.34
//        val lon = 56.78
//        val apiKey = "test_api_key"
//        val units = "metric"
//        val lang = "en"
//        val fav = false
//
//        // Mock no internet connectivity
//        viewModel = HomeViewModel(ApplicationProvider.getApplicationContext(), repo) {
//            false // Always return no internet
//        }
//
//        // Act
//        viewModel.fetchWeatherData(lat, lon, apiKey, units, lang, fav)
//
//        // Assert
//        val result = viewModel.weatherData.first()
//        assert(result is ApiState.Success)
//        assertEquals((result as ApiState.Success).data, repo.getFakeWeatherDataFromLocal())
//    }
//
//    @Test
//    fun fetchForecastData_WithValidData_ReturnsSuccess() = runTest {
//        // Arrange
//        val lat = 12.34
//        val lon = 56.78
//        val apiKey = "test_api_key"
//        val units = "metric"
//        val lang = "en"
//        val fav = false
//
//        // Act
//        viewModel.fetchForecastData(lat, lon, apiKey, units, lang, fav)
//
//        // Assert
//        val dailyWeatherResult = viewModel.dailyWeather.first()
//        val hourlyWeatherResult = viewModel.hourlyWeather.first()
//
//        assert(dailyWeatherResult is ApiState.Success)
//        assert(hourlyWeatherResult is ApiState.Success)
//
//        assertEquals(
//            (dailyWeatherResult as ApiState.Success).data,
//            repo.getFakeDailyWeather()
//        )
//        assertEquals(
//            (hourlyWeatherResult as ApiState.Success).data,
//            repo.getFakeHourlyWeather()
//        )
//    }
//
//    @Test
//    fun fetchForecastData_NoInternet_ReturnsLocalData() = runTest {
//        // Arrange
//        val lat = 12.34
//        val lon = 56.78
//        val apiKey = "test_api_key"
//        val units = "metric"
//        val lang = "en"
//        val fav = false
//
//        // Mock no internet connectivity
//        viewModel = HomeViewModel(ApplicationProvider.getApplicationContext(), repo) {
//            false // Always return no internet
//        }
//
//        // Act
//        viewModel.fetchForecastData(lat, lon, apiKey, units, lang, fav)
//
//        // Assert
//        val dailyWeatherResult = viewModel.dailyWeather.first()
//        val hourlyWeatherResult = viewModel.hourlyWeather.first()
//
//        assert(dailyWeatherResult is ApiState.Success)
//        assert(hourlyWeatherResult is ApiState.Success)
//
//        assertEquals(
//            (dailyWeatherResult as ApiState.Success).data,
//            repo.getFakeDailyWeatherFromLocal()
//        )
//        assertEquals(
//            (hourlyWeatherResult as ApiState.Success).data,
//            repo.getFakeHourlyWeatherFromLocal()
//        )
//    }

}