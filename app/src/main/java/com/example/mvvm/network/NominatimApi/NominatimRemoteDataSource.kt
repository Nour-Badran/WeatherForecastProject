package com.example.mvvm.network.NominatimApi

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val retrofit = Retrofit.Builder()
    .baseUrl("https://nominatim.openstreetmap.org/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val nominatimService = retrofit.create(NominatimApiService::class.java)