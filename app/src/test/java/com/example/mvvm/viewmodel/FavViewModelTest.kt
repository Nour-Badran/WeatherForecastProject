package com.example.mvvm.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mvvm.FakeWeatherRepository
import com.example.mvvm.favourites.viewmodel.FavViewModel
import com.example.mvvm.weather.model.pojos.FavoritePlaces
import com.example.mvvm.weather.model.repo.IWeatherRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavViewModelTest {
    lateinit var repo : IWeatherRepository
    lateinit var viewModel : FavViewModel

    @Before
    fun setUp(){
        repo = FakeWeatherRepository()
        viewModel = FavViewModel(repo)
    }

    @Test
    fun addPlace_InsertNewPlace_ReturnLocalWithNewPlace() = runTest {
        // Arrange
        val place = FavoritePlaces("Test City", 0.0, 0.0)

        // Act
        viewModel.addPlace(place)

        // Assert
        val result = viewModel.favoritePlaces.value
        assertEquals(1, result.size)
        assertEquals(place, result[0])
    }

    @Test
    fun getFavoritePlaces_ReturnLocal() = runTest {
        // Act
        viewModel.getFavoritePlaces()

        // Assert
        val result = viewModel.favoritePlaces.value
        assertEquals(0, result.size)
    }

    @Test
    fun removePlace_RemoveExistingPlace_ReturnLocalWithoutPlace() = runTest {
        // Arrange
        val place = FavoritePlaces("Test City", 0.0, 0.0)
        viewModel.addPlace(place)

        // Act
        viewModel.removePlace(place)

        // Assert
        val result = viewModel.favoritePlaces.value
        assertEquals(0, result.size)
    }

}