package com.example.mvvm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvm.network.NominatimApi.nominatimService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import java.util.Locale

class MapViewModel : ViewModel() {


    fun fetchLocationSuggestions(query: String): Flow<List<String>> = flow {
        val suggestions = mutableListOf<String>()

        try {
            withContext(Dispatchers.IO) {
                val response = nominatimService.searchLocations(query).execute()
                if (response.isSuccessful) {
                    response.body()?.map { it.display_name }?.let { suggestions.addAll(it) }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        emit(suggestions)
    }.onStart {
        // Optionally, show a loading state
    }
}
