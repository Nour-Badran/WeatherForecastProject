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
    val forecast: List<Forecast>,
    val iconResId : Int
)

data class WeatherApiResponse(
    @SerializedName("name") val cityName: String?,  // Note that the city name is not nested
    @SerializedName("main") val main: Main?,
    @SerializedName("weather") val weather: List<Weather>?,
    @SerializedName("wind") val wind: Wind?,
    @SerializedName("clouds") val clouds: Clouds?,
    @SerializedName("list") val forecast: List<Forecast>?
)


data class City(val name: String)
data class Main(val temp: Double, val humidity: Int, val pressure: Int)
data class Weather(val description: String)
data class Wind(val speed: Double)
data class Clouds(val all: Int)
data class Forecast(val temp: Double, val date: String)
