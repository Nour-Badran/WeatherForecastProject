package com.example.mvvm

fun setIcon(id: String) : Int {
    val iconResource = when (id) {
        "01d" -> R.drawable.sun
        "02d" -> R.drawable._02d
        "03d" -> R.drawable.cloudyday
        "04d" -> R.drawable.cloudyday
        "09d" -> R.drawable.rainyday
        "10d" -> R.drawable._10d
        "11d" -> R.drawable._11d
        "13d" -> R.drawable._13d
        "50d" -> R.drawable.thunderstorms
        "01n" -> R.drawable.sun
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
