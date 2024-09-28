package com.example.mvvm.weather.model.utilities

import android.content.Context
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.mvvm.R
import com.example.mvvm.weather.model.pojos.ForecastEntity
import com.example.mvvm.weather.model.pojos.WeatherEntity
import com.example.mvvm.weather.model.pojos.City
import com.example.mvvm.weather.model.pojos.Clouds
import com.example.mvvm.weather.model.pojos.DailyWeather
import com.example.mvvm.weather.model.pojos.FiveDayResponse
import com.example.mvvm.weather.model.pojos.HourlyWeather
import com.example.mvvm.weather.model.pojos.Main
import com.example.mvvm.weather.model.pojos.Rain
import com.example.mvvm.weather.model.pojos.WeatherApiResponse
import com.example.mvvm.weather.model.pojos.WeatherData
import com.example.mvvm.weather.model.pojos.WeatherItem
import com.example.mvvm.weather.model.pojos.Wind
import com.google.android.material.snackbar.Snackbar
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
        "01n" -> R.drawable.img_14
        "02n" -> R.drawable.img_13
        "03n" -> R.drawable.cloudyday
        "04n" -> R.drawable.cloudyday
        "09n" -> R.drawable.rainyday
        "10n" -> R.drawable.img_15
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
fun isConnectedToInternet(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
}


fun customizeSnackbar(snackbar: Snackbar, context: Context) {
    // Set background with rounded corners
    val snackbarView = snackbar.view

    // Set text properties
    val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    textView.textSize = 18f // Adjust text size as needed
    textView.setTypeface(textView.typeface, Typeface.BOLD) // Set text style to bold
    textView.setTextColor(ContextCompat.getColor(context, android.R.color.white)) // Set text color to white
    textView.gravity = Gravity.CENTER // Center the text
    textView.setPadding(16, 0, 16, 0) // Add padding to the text for better visual appearance

    // Set an action button with customized style
    val actionTextView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
    actionTextView.setTextColor(ContextCompat.getColor(context, R.color.white)) // Customize action color
    actionTextView.setTypeface(actionTextView.typeface, Typeface.BOLD) // Bold action text

    // Set elevation to add a shadow effect
    snackbarView.elevation = 8f
}


// Helper function to map forecast entities back to FiveDayResponse if needed
fun List<ForecastEntity>.mapToFiveDayResponse(): FiveDayResponse {
    return FiveDayResponse(
        cod = "200",
        message = 0,
        cnt = this.size,
        list = this.map { it.toWeatherItem() },
        city = City(name = "")
    )
}
fun ForecastEntity.toWeatherItem() = WeatherItem(
    dt = dt,
    main = Main(
        temp = temp,
        feelsLike = feelsLike,
        tempMin = tempMin,
        tempMax = tempMax,
        pressure = pressure,
        humidity = humidity
    ),
    weather = listOf(
        WeatherData(
            cityName = "",
            temperature = temp,
            description = weatherDescription,
            humidity = humidity,
            windSpeed = windSpeed,
            pressure = pressure,
            clouds = clouds,
            dt = dt,
            forecast = emptyList(),
            iconResId = 0,
            visibility = 0,
            icon = icon
        )
    ),
    clouds = Clouds(clouds),
    wind = Wind(windSpeed),
    dt_txt = dtTxt, // Use dtTxt
    pop = pop,
    rain = Rain(rainVolume)
)

 fun WeatherData.toEntity() = WeatherEntity(
    cityName = cityName,
    temperature = temperature,
    description = description,
    humidity = humidity,
    windSpeed = windSpeed,
    pressure = pressure,
    clouds = clouds,
    dt = dt,
    visibility = visibility,
    iconResId = iconResId,
    icon = icon
)
fun WeatherEntity.toModel(): WeatherData {
    return WeatherData(
        cityName = this.cityName ?: "Unknown",
        temperature = this.temperature ?: 0.0,
        description = this.description ?: "No description",
        humidity = this.humidity ?: 0,
        windSpeed = this.windSpeed ?: 0.0,
        pressure = this.pressure ?: 0,
        clouds = this.clouds ?: 0,
        dt = this.dt ?: 0L,
        visibility = this.visibility ?: 0,
        iconResId = this.iconResId ?: 0,
        forecast = emptyList(), // Placeholder for now
        icon = this.icon ?: ""
    )
}


 fun WeatherItem.toEntity() = ForecastEntity(
    dt = dt,
    temp = main.temp,
    feelsLike = main.feelsLike,
    tempMin = main.tempMin,
    tempMax = main.tempMax,
    pressure = main.pressure,
    humidity = main.humidity,
    windSpeed = wind.speed,
    clouds = clouds.all,
    pop = pop,
    rainVolume = rain?.volume ?: 0.0, // Provide a default value if rain is null
    weatherDescription = weather.firstOrNull()?.description ?: "",
    icon = weather.firstOrNull()?.icon ?: "",
    dtTxt = dt_txt
)