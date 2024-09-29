package com.example.mvvm.alert.AlertViewModel
import android.app.PendingIntent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvm.alert.model.AlertEntity
import com.example.mvvm.alert.model.AlertRepository
import com.example.mvvm.weather.viewmodel.ApiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AlertViewModel(private val repository: AlertRepository) : ViewModel() {

    private val _alerts = MutableStateFlow<ApiState<List<AlertEntity>>>(ApiState.Loading)
    val alerts: StateFlow<ApiState<List<AlertEntity>>> get() = _alerts

    fun insertAlert(alert: AlertEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAlert(alert)
        }
    }

    fun deleteAlert(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAlert(id)
            loadAlerts()
        }
    }

    fun setAlert(alert: AlertEntity, pendingIntent: PendingIntent) {
        repository.setAlert(alert, pendingIntent)
        insertAlert(alert)
    }

    fun cancelAlert(pendingIntent: PendingIntent, id: Int) {
        repository.cancelAlert(pendingIntent)
        deleteAlert(id)
    }

     fun loadAlerts() {
         _alerts.value = ApiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllAlerts().collectLatest { alertList ->
                _alerts.value = ApiState.Success(alertList)
            }
        }
    }
}

