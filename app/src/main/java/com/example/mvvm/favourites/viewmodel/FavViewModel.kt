package com.example.mvvm.favourites.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvm.weather.model.pojos.FavoritePlaces
import com.example.mvvm.weather.model.repo.IWeatherRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FavViewModel(private val repository: IWeatherRepository) : ViewModel() {

    private val _favoritePlaces = MutableStateFlow<List<FavoritePlaces>>(emptyList())
    val favoritePlaces: StateFlow<List<FavoritePlaces>> = _favoritePlaces

    //.//////////////////////////////////////////////////////////////Search
    private val _searchQuery = MutableSharedFlow<String>(replay = 1)
    val filteredPlaces = _searchQuery
        .map { query ->
            _favoritePlaces.value.filter { place ->
                place.cityName.startsWith(query, ignoreCase = true)
            }
        }
    fun resetSearchQuery() {
        viewModelScope.launch {
            _searchQuery.emit("")
        }
    }

    fun emitSearchQuery(query: String) {
        viewModelScope.launch {
            _searchQuery.emit(query)
        }
    }
    /////////////////////////////////////////////////////////
    fun getFavoritePlaces() {
        viewModelScope.launch {
            repository.getFavPlaces().collect { favs ->
                _favoritePlaces.value = favs
            }
        }
    }

    fun removePlace(place: FavoritePlaces) {
        viewModelScope.launch {
            repository.deleteFavoritePlace(place)
            getFavoritePlaces()
        }
    }
    fun addPlace(place: FavoritePlaces) {
        viewModelScope.launch {
            repository.insertFavoritePlace(place)
            getFavoritePlaces() // Refresh the list
        }
    }
}