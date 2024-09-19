package com.example.mvvm.model

import com.google.gson.annotations.SerializedName

data class WeatherData(
    val cityName: String,
    val temperature: Double,
    val description: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Int,
    val clouds: Int,
    val dt: Long,
    val forecast: List<Forecast>,
    val iconResId: Int,
    val visibility: Int,
    val icon: String
)

data class WeatherApiResponse(
    @SerializedName("name") val cityName: String?,  // Note that the city name is not nested
    @SerializedName("main") val main: Main?,
    @SerializedName("weather") val weather: List<WeatherData>?,
    @SerializedName("wind") val wind: Wind?,
    @SerializedName("clouds") val clouds: Clouds?,
    @SerializedName("list") val forecast: List<Forecast>?,
    @SerializedName("dt") val dt: Long,
    @SerializedName("visibility") val visibility: Int
)

data class City(val name: String)
data class Main(
    val temp: Double,
    val feelsLike: Double,
    val tempMin: Double,
    val tempMax: Double,
    val pressure: Int,
    val humidity: Int,
    val seaLevel: Int? = null,
    val grndLevel: Int? = null,
    val temp_kf: Double
)

data class Wind(val speed: Double)
data class Clouds(val all: Int)
data class Forecast(val temp: Double, val date: String)

data class FiveDayResponse(
    val cod: String,
    val message: Int,
    val cnt: Int,
    val list: List<WeatherItem>,
    val city: City
)

class WeatherItem(
    val dt: Long,
    val main: Main,
    val weather: List<WeatherData>,
    val clouds: Clouds,
    val wind: Wind,
    val dt_txt: String,
    val pop: Double?,
    val rain: Rain,
)

data class Rain(@SerializedName("3h") val volume: Double? = null)
data class HourlyWeather(val day: Long, val icon: String, val temperature: Double)
data class DailyWeather(
    val day: Long,
    val icon: String,
    var minTemp: Double,
    var maxTemp: Double,
    val weatherStatus: String
)