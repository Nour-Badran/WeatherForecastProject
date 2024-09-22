package com.example.mvvm.view

import com.example.mvvm.db.FavoritePlaces

interface OnLocationSelectedListener {
    fun onLocationSelected(locationName: FavoritePlaces)
}
