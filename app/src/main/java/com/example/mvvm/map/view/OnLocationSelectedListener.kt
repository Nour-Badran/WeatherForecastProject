package com.example.mvvm.map.view

import com.example.mvvm.weather.model.pojos.FavoritePlaces

interface OnLocationSelectedListener {
    fun onLocationSelected(locationName: FavoritePlaces)
}
