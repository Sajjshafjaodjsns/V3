package com.autovoz.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ─── Entities ────────────────────────────────────────────────────────────────

@Entity(tableName = "car_library")
data class CarLibraryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mass: Int,
    val length: Float,
    val width: Float,
    val height: Float
)

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val vehiclesJson: String,    // JSON list of CargoVehicle
    val resultJson: String       // JSON of LoadingResult summary
)

// ─── DAOs ─────────────────────────────────────────────────────────────────────

@Dao
interface CarLibraryDao {
    @Query("SELECT * FROM car_library ORDER BY name ASC")
    fun getAll(): Flow<List<CarLibraryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(car: CarLibraryEntity): Long

    @Delete
    suspend fun delete(car: CarLibraryEntity)

    @Query("DELETE FROM car_library WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY createdAt DESC")
    fun getAll(): Flow<List<TripEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trip: TripEntity): Long

    @Delete
    suspend fun delete(trip: TripEntity)

    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getById(id: Long): TripEntity?
}

// ─── Database ────────────────────────────────────────────────────────────────

@Database(
    entities = [CarLibraryEntity::class, TripEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun carLibraryDao(): CarLibraryDao
    abstract fun tripDao(): TripDao
}
