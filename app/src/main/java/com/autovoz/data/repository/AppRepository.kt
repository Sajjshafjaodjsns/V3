package com.autovoz.data.repository

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.autovoz.data.db.*
import com.autovoz.domain.model.CargoVehicle
import com.autovoz.domain.model.TruckSettings
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.dataStore by preferencesDataStore(name = "truck_settings")

class AppRepository(private val context: Context) {

    private val db = Room.databaseBuilder(context, AppDatabase::class.java, "autovoz.db")
        .fallbackToDestructiveMigration()
        .build()

    private val carDao = db.carLibraryDao()
    private val tripDao = db.tripDao()

    // ── Settings ──────────────────────────────────────────────────────────────

    private object Keys {
        val UNLADEN_MASS = intPreferencesKey("unladen_mass")
        val MAX_TOTAL_MASS = intPreferencesKey("max_total_mass")
        val MAX_FRONT_AXLE = intPreferencesKey("max_front_axle")
        val MAX_REAR_AXLE = intPreferencesKey("max_rear_axle")
        val WHEELBASE = floatPreferencesKey("wheelbase")
        val PLATFORM_LENGTH = floatPreferencesKey("platform_length")
        val PLATFORM_WIDTH = floatPreferencesKey("platform_width")
        val MAX_H_LOWER = floatPreferencesKey("max_h_lower")
        val MAX_H_UPPER = floatPreferencesKey("max_h_upper")
        val FRONT_OVERHANG = floatPreferencesKey("front_overhang")
        val FRONT_RATIO = floatPreferencesKey("front_ratio")
    }

    val settingsFlow: Flow<TruckSettings> = context.dataStore.data.map { p ->
        TruckSettings(
            unladenMass = p[Keys.UNLADEN_MASS] ?: 10500,
            maxTotalMass = p[Keys.MAX_TOTAL_MASS] ?: 44000,
            maxFrontAxleLoad = p[Keys.MAX_FRONT_AXLE] ?: 7500,
            maxRearAxleLoad = p[Keys.MAX_REAR_AXLE] ?: 11500,
            wheelbase = p[Keys.WHEELBASE] ?: 4.5f,
            platformLength = p[Keys.PLATFORM_LENGTH] ?: 12.0f,
            platformWidth = p[Keys.PLATFORM_WIDTH] ?: 2.5f,
            maxHeightLowerDeck = p[Keys.MAX_H_LOWER] ?: 2.0f,
            maxHeightUpperDeck = p[Keys.MAX_H_UPPER] ?: 1.8f,
            frontOverhang = p[Keys.FRONT_OVERHANG] ?: 1.2f,
            frontAxleLoadRatio = p[Keys.FRONT_RATIO] ?: 0.40f
        )
    }

    suspend fun saveSettings(s: TruckSettings) {
        context.dataStore.edit { p ->
            p[Keys.UNLADEN_MASS] = s.unladenMass
            p[Keys.MAX_TOTAL_MASS] = s.maxTotalMass
            p[Keys.MAX_FRONT_AXLE] = s.maxFrontAxleLoad
            p[Keys.MAX_REAR_AXLE] = s.maxRearAxleLoad
            p[Keys.WHEELBASE] = s.wheelbase
            p[Keys.PLATFORM_LENGTH] = s.platformLength
            p[Keys.PLATFORM_WIDTH] = s.platformWidth
            p[Keys.MAX_H_LOWER] = s.maxHeightLowerDeck
            p[Keys.MAX_H_UPPER] = s.maxHeightUpperDeck
            p[Keys.FRONT_OVERHANG] = s.frontOverhang
            p[Keys.FRONT_RATIO] = s.frontAxleLoadRatio
        }
    }

    // ── Car Library ───────────────────────────────────────────────────────────

    fun getCarsFlow(): Flow<List<CarLibraryEntity>> = carDao.getAll()

    suspend fun saveCar(car: CargoVehicle) {
        carDao.insert(
            CarLibraryEntity(
                id = car.id,
                name = car.name,
                mass = car.mass,
                length = car.length,
                width = car.width,
                height = car.height
            )
        )
    }

    suspend fun deleteCar(id: Long) = carDao.deleteById(id)

    // ── Trips ─────────────────────────────────────────────────────────────────

    fun getTripsFlow(): Flow<List<TripEntity>> = tripDao.getAll()

    suspend fun saveTrip(name: String, vehicles: List<CargoVehicle>, resultSummary: String): Long {
        return tripDao.insert(
            TripEntity(
                name = name,
                vehiclesJson = Json.encodeToString(vehicles.map {
                    mapOf(
                        "id" to it.id.toString(),
                        "name" to it.name,
                        "mass" to it.mass.toString(),
                        "length" to it.length.toString(),
                        "width" to it.width.toString(),
                        "height" to it.height.toString()
                    )
                }),
                resultJson = resultSummary
            )
        )
    }

    suspend fun getTrip(id: Long): TripEntity? = tripDao.getById(id)

    suspend fun deleteTrip(id: Long) {
        val t = tripDao.getById(id) ?: return
        tripDao.delete(t)
    }
}
