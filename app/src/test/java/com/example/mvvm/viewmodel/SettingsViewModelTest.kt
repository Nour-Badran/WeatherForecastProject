package com.example.mvvm.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mvvm.repo.FakeSettingsRepository
import com.example.mvvm.settings.model.ISettingsRepository
import com.example.mvvm.settings.viewmodel.SettingsViewModel
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest{

    @get:Rule
    val rule = InstantTaskExecutorRule() // To run LiveData synchronously

    private lateinit var fakeRepository: ISettingsRepository
    private lateinit var settingsViewModel: SettingsViewModel

    @Before
    fun setUp() {
        fakeRepository = FakeSettingsRepository()
        settingsViewModel = SettingsViewModel(fakeRepository)
    }

    @Test
    fun getInitialSettings_ReturnDefault() {
        assertEquals("GPS", settingsViewModel.location.value)
        assertEquals("English", settingsViewModel.language.value)
        assertEquals("Celsius", settingsViewModel.temperature.value)
        assertEquals("Meter / Sec", settingsViewModel.windSpeed.value)
    }

    @Test
    fun updateLocation_SetNewLocation_ReturnsUpdatedLocation() = runTest {
        // GIVEN
        val newLocation = "Giza"

        // WHEN
        settingsViewModel.updateLocation(newLocation)

        // THEM
        assertEquals(newLocation, settingsViewModel.location.value)
    }
    @Test
    fun updateWindSpeed_SetNewWindSpeed_ReturnsUpdatedWindSpeed() = runTest {
        // GIVEN
        val newWindSpeed = "Kilometer / Hour"

        // WHEN
        settingsViewModel.updateWindSpeed(newWindSpeed)

        //THEN
        assertEquals(newWindSpeed, settingsViewModel.windSpeed.value)
    }

}