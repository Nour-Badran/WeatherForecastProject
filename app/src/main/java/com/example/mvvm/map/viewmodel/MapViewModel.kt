package com.example.mvvm.map.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mvvm.map.model.NominatimApi.nominatimService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

class MapViewModel : ViewModel() {

    fun fetchLocationSuggestions(query: String): Flow<List<String>> = flow {
        val suggestions = mutableListOf<String>()
        try {
            val response = withContext(Dispatchers.IO) {
                nominatimService.searchLocations(query).execute()
            }
            if (response.isSuccessful) {
                response.body()?.map { it.display_name }?.let { suggestions.addAll(it) }
            } else {
                // Log the error or handle it accordingly
            }
        } catch (e: Exception) {
            // Log the error
            e.printStackTrace()
        }
        emit(suggestions)
    }

}
