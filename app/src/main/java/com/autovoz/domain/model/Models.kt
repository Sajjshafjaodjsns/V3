package com.autovoz.domain.model

data class CargoVehicle(
    val id: Long = 0,
    val name: String = "",
    val mass: Int,       // kg
    val length: Float,   // m
    val width: Float,    // m
    val height: Float    // m
)

data class TruckSettings(
    val unladenMass: Int = 10500,           // kg
    val maxTotalMass: Int = 44000,          // kg
    val maxFrontAxleLoad: Int = 7500,       // kg
    val maxRearAxleLoad: Int = 11500,       // kg
    val wheelbase: Float = 4.5f,            // m
    val platformLength: Float = 12.0f,      // m
    val platformWidth: Float = 2.5f,        // m
    val maxHeightLowerDeck: Float = 2.0f,   // m
    val maxHeightUpperDeck: Float = 1.8f,   // m
    val frontOverhang: Float = 1.2f,        // m  (from front axle to platform start)
    val frontAxleLoadRatio: Float = 0.40f   // 40% unladen mass on front axle
)

enum class Deck { LOWER, UPPER }

data class PlacedVehicle(
    val vehicle: CargoVehicle,
    val deck: Deck,
    val positionFromFront: Float  // metres from front edge of platform to front bumper of car
)

data class LoadingResult(
    val placements: List<PlacedVehicle>,
    val totalMass: Int,
    val frontAxleLoad: Float,
    val rearAxleLoad: Float,
    val isOverloaded: Boolean,
    val warnings: List<String>,
    val unplacedVehicles: List<CargoVehicle>
)
