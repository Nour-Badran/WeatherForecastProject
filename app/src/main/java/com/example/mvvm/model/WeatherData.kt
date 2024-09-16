package com.example.mvvm.model

data class WeatherData(
    val cityName: String,
    val temperature: Double,
    val description: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Int,
    val clouds: Int,
    val forecast: List<Forecast>
)

data class WeatherApiResponse(
    val city: City?,
    val main: Main?,
    val weather: List<Weather>?,
    val wind: Wind?,
    val clouds: Clouds?,
    val forecast: List<Forecast>?
)

data class City(val name: String)
data class Main(val temp: Double, val humidity: Int, val pressure: Int)
data class Weather(val description: String)
data class Wind(val speed: Double)
data class Clouds(val all: Int)
data class Forecast(val temp: Double, val date: String)
