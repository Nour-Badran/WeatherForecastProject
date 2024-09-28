package com.example.mvvm.weather.viewmodel

sealed class ApiState<out T> {
    data class Success<out T>(val data: T) : ApiState<T>()
    data class Error(val exception: Throwable) : ApiState<Nothing>()
    object Loading : ApiState<Nothing>()
}