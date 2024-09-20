package com.example.mvvm.utilities

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.mvvm.R
import com.example.mvvm.model.DailyWeather
import com.example.mvvm.model.FiveDayResponse
import com.example.mvvm.model.HourlyWeather
import com.example.mvvm.model.WeatherApiResponse
import com.example.mvvm.model.WeatherData
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun setIcon(id: String) : Int {
    val iconResource = when (id) {
        "01d" -> R.drawable.sun
        "02d" -> R.drawable._02d
        "03d" -> R.drawable.cloudyday
        "04d" -> R.drawable.cloudyday
        "09d" -> R.drawable._10d
        "10d" -> R.drawable._10d
        "11d" -> R.drawable._11d
        "13d" -> R.drawable._13d
        "50d" -> R.drawable.thunderstorms
        "01n" -> R.drawable._01n
        "02n" -> R.drawable._02n
        "03n" -> R.drawable.cloudyday
        "04n" -> R.drawable.cloudyday
        "09n" -> R.drawable.rainyday
        "10n" -> R.drawable._10n
        "11n" -> R.drawable._11d
        "13n" -> R.drawable._13d
        "50n" -> R.drawable.thunderstorms
        else -> R.drawable._load  // Default loading icon
    }
    return iconResource
}
fun mapToWeatherData(apiResponse: WeatherApiResponse): WeatherData {
    Log.d("Before Map", apiResponse.dt.toString())
    return WeatherData(
        cityName = apiResponse.cityName ?: "",  // Map the root-level city name
        temperature = apiResponse.main?.temp ?: 0.0,
        description = apiResponse.weather?.firstOrNull()?.description ?: "",
        humidity = apiResponse.main?.humidity ?: 0,
        windSpeed = apiResponse.wind?.speed ?: 0.0,
        pressure = apiResponse.main?.pressure ?: 0,
        clouds = apiResponse.clouds?.all ?: 0,
        dt = apiResponse.dt,
        visibility = apiResponse.visibility,
        iconResId = mapToWeatherIcon(apiResponse.weather?.firstOrNull()?.description ?: ""),
        forecast = apiResponse.forecast ?: emptyList(),
        icon = apiResponse.weather?.get(0)?.icon ?: ""
    )

}
fun mapToWeatherIcon(description: String): Int {
    return when {
        description.contains("clear", ignoreCase = true) -> R.drawable.sun
        description.contains("clouds", ignoreCase = true) -> R.drawable.cloudyday
        description.contains("rain", ignoreCase = true) -> R.drawable.rainyday
        description.contains("thunderstorm", ignoreCase = true) -> R.drawable.thunderstorms
        description.contains("snow", ignoreCase = true) -> R.drawable.snow
        description.contains("mist", ignoreCase = true) -> R.drawable.fog
        else -> R.drawable.sun // Default weather icon
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
fun mapHourlyWeatherForTodayAndTomorrow(response: FiveDayResponse): List<HourlyWeather> {
    // Get the current date and tomorrow's date in the system's default timezone
    val currentDate = LocalDate.now(ZoneId.systemDefault())
    val tomorrowDate = currentDate.plusDays(1)

    // Filter hourly data to find entries for today and tomorrow
    val hourlyDataForTodayAndTomorrow = response.list.filter { item ->
        val forecastDate = Instant.ofEpochSecond(item.dt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        forecastDate == currentDate || forecastDate == tomorrowDate
    }

    if (hourlyDataForTodayAndTomorrow.isEmpty()) {
        return emptyList()  // Return empty list or default data
    }

    // Map the filtered data to HourlyWeather
    return hourlyDataForTodayAndTomorrow.map { item ->
        HourlyWeather(
            day = item.dt,         // timestamp in seconds
            icon = item.weather[0].icon,
            temperature = item.main.temp
        )
    }
}
fun capitalizeFirstLetter(text: String): String {
    return text.split(" ").joinToString(" ") { it.capitalize() }
}