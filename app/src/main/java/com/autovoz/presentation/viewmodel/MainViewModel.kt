package com.autovoz.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.autovoz.data.db.CarLibraryEntity
import com.autovoz.data.db.TripEntity
import com.autovoz.data.repository.AppRepository
import com.autovoz.domain.model.*
import com.autovoz.domain.usecase.CalculateLoadingUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repo = AppRepository(application)
    private val calculator = CalculateLoadingUseCase()

    // ── Settings ──────────────────────────────────────────────────────────────
    val settings: StateFlow<TruckSettings> = repo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, TruckSettings())

    fun saveSettings(s: TruckSettings) = viewModelScope.launch { repo.saveSettings(s) }

    // ── Car Library ───────────────────────────────────────────────────────────
    val library: StateFlow<List<CarLibraryEntity>> = repo.getCarsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun saveCarToLibrary(car: CargoVehicle) = viewModelScope.launch { repo.saveCar(car) }
    fun deleteCarFromLibrary(id: Long) = viewModelScope.launch { repo.deleteCar(id) }

    // ── Trips ─────────────────────────────────────────────────────────────────
    val trips: StateFlow<List<TripEntity>> = repo.getTripsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun deleteTrip(id: Long) = viewModelScope.launch { repo.deleteTrip(id) }

    // ── Current session ───────────────────────────────────────────────────────
    private val _currentVehicles = MutableStateFlow<List<CargoVehicle>>(emptyList())
    val currentVehicles: StateFlow<List<CargoVehicle>> = _currentVehicles

    private val _loadingResult = MutableStateFlow<LoadingResult?>(null)
    val loadingResult: StateFlow<LoadingResult?> = _loadingResult

    fun addVehicle(v: CargoVehicle) {
        _currentVehicles.value = _currentVehicles.value + v.copy(id = System.currentTimeMillis())
        recalculate()
    }

    fun removeVehicle(id: Long) {
        _currentVehicles.value = _currentVehicles.value.filter { it.id != id }
        recalculate()
    }

    fun clearVehicles() {
        _currentVehicles.value = emptyList()
        _loadingResult.value = null
    }

    fun recalculate() {
        val vehicles = _currentVehicles.value
        if (vehicles.isEmpty()) { _loadingResult.value = null; return }
        _loadingResult.value = calculator.execute(vehicles, settings.value)
    }

    fun saveCurrentTrip(name: String) = viewModelScope.launch {
        val result = _loadingResult.value ?: return@launch
        val summary = buildString {
            append("Масса: ${result.totalMass} кг | ")
            append("Передняя ось: ${result.frontAxleLoad.toInt()} кг | ")
            append("Задняя: ${result.rearAxleLoad.toInt()} кг")
            if (result.isOverloaded) append(" | ⚠ ПЕРЕГРУЗ")
        }
        repo.saveTrip(name, _currentVehicles.value, summary)
    }

    fun loadTrip(trip: TripEntity) {
        // Parse JSON back to vehicles list (simple approach)
        try {
            val jsonList = trip.vehiclesJson
            val regex = Regex(""""mass":"(\d+)","length":"([\d.]+)","width":"([\d.]+)","height":"([\d.]+)","name":"([^"]*)"""")
            val vehicles = mutableListOf<CargoVehicle>()
            for (match in regex.findAll(jsonList)) {
                vehicles.add(
                    CargoVehicle(
                        id = System.currentTimeMillis() + vehicles.size,
                        name = match.groupValues[5],
                        mass = match.groupValues[1].toInt(),
                        length = match.groupValues[2].toFloat(),
                        width = match.groupValues[3].toFloat(),
                        height = match.groupValues[4].toFloat()
                    )
                )
            }
            _currentVehicles.value = vehicles
            recalculate()
        } catch (_: Exception) {}
    }
}
