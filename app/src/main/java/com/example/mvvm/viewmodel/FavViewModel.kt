package com.example.mvvm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvm.db.FavoritePlaces
import com.example.mvvm.model.WeatherRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FavViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _favoritePlaces = MutableStateFlow<List<FavoritePlaces>>(emptyList())
    val favoritePlaces: StateFlow<List<FavoritePlaces>> = _favoritePlaces

    // SharedFlow for search queries
    private val _searchQuery = MutableSharedFlow<String>(replay = 1)
    val filteredPlaces = _searchQuery
        .filter { it.isNotEmpty() }
        .map { query ->
            _favoritePlaces.value.filter { place ->
                place.cityName.startsWith(query, ignoreCase = true)
            }
        }
    fun resetSearchQuery() {
        viewModelScope.launch {
            _searchQuery.emit("") // Emit an empty string to show all places
        }
    }

    fun emitSearchQuery(query: String) {
        viewModelScope.launch {
            _searchQuery.emit(query)
        }
    }

    fun getFavoriteProducts() {
        viewModelScope.launch {
            repository.getFavoriteProducts().collect { favs ->
                _favoritePlaces.value = favs
            }
        }
    }

    fun removeFavorite(place: FavoritePlaces) {
        viewModelScope.launch {
            repository.deleteFavoritePlace(place)
            getFavoriteProducts()
        }
    }
    fun addPlace(place: FavoritePlaces) {
        viewModelScope.launch {
            repository.insertFavoritePlace(place) // Implement this in your repository
            getFavoriteProducts() // Refresh the list
        }
    }
}