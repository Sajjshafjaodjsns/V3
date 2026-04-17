package com.autovoz.domain.usecase

import com.autovoz.domain.model.*

/**
 * Greedy heuristic packing algorithm:
 * 1. Sort vehicles: first by height (tall ones must go to lower deck), then by mass desc.
 * 2. Fill lower deck front-to-back, then upper deck front-to-back.
 * 3. Calculate axle loads via lever rule.
 */
class CalculateLoadingUseCase {

    private val GAP = 0.20f  // minimum gap between vehicles, metres

    fun execute(vehicles: List<CargoVehicle>, settings: TruckSettings): LoadingResult {
        val warnings = mutableListOf<String>()

        // Sort: vehicles that exceed upper deck height first (must go lower),
        // then by mass descending (heavier → lower & forward)
        val sorted = vehicles.sortedWith(
            compareByDescending<CargoVehicle> { it.height > settings.maxHeightUpperDeck }
                .thenByDescending { it.mass }
        )

        val lowerPlacements = mutableListOf<PlacedVehicle>()
        val upperPlacements = mutableListOf<PlacedVehicle>()
        val unplaced = mutableListOf<CargoVehicle>()

        var lowerFront = 0f
        var upperFront = 0f

        for (car in sorted) {
            val tooTallForUpper = car.height > settings.maxHeightUpperDeck
            val tooTallForLower = car.height > settings.maxHeightLowerDeck

            when {
                tooTallForLower -> {
                    warnings.add("${car.name.ifEmpty { "Автомобиль ${car.mass}кг" }}: высота ${car.height}м превышает оба яруса — не размещён.")
                    unplaced.add(car)
                }
                tooTallForUpper -> {
                    // Must go lower deck
                    val pos = lowerFront
                    if (pos + car.length <= settings.platformLength) {
                        lowerPlacements.add(PlacedVehicle(car, Deck.LOWER, pos))
                        lowerFront = pos + car.length + GAP
                    } else {
                        warnings.add("${car.name.ifEmpty { "Автомобиль ${car.mass}кг" }}: не помещается на нижнем ярусе.")
                        unplaced.add(car)
                    }
                }
                else -> {
                    // Try lower deck first (heavier vehicles prefer lower)
                    val lowerPos = lowerFront
                    val upperPos = upperFront
                    if (lowerPos + car.length <= settings.platformLength) {
                        lowerPlacements.add(PlacedVehicle(car, Deck.LOWER, lowerPos))
                        lowerFront = lowerPos + car.length + GAP
                    } else if (upperPos + car.length <= settings.platformLength) {
                        upperPlacements.add(PlacedVehicle(car, Deck.UPPER, upperPos))
                        upperFront = upperPos + car.length + GAP
                    } else {
                        warnings.add("${car.name.ifEmpty { "Автомобиль ${car.mass}кг" }}: оба яруса заполнены — не размещён.")
                        unplaced.add(car)
                    }
                }
            }
        }

        val allPlacements = lowerPlacements + upperPlacements
        val cargoMass = allPlacements.sumOf { it.vehicle.mass }
        val totalMass = settings.unladenMass + cargoMass

        // Axle load calculation via lever rule
        // Platform front is at distance (frontOverhang) ahead of front axle
        // Rear axle is at (frontOverhang + wheelbase) from front of platform (approx)
        val frontAxleToplatformStart = settings.frontOverhang
        val rearAxlePos = frontAxleToplatformStart + settings.wheelbase // from platform front

        // Centre of gravity of all cargo (from platform front)
        val cargoCog: Float = if (allPlacements.isEmpty()) rearAxlePos
        else allPlacements.sumOf { p ->
            val centerPos = p.positionFromFront + p.vehicle.length / 2.0
            centerPos * p.vehicle.mass
        }.toFloat() / cargoMass

        // Unladen axle loads
        val unladenFront = settings.unladenMass * settings.frontAxleLoadRatio
        val unladenRear = settings.unladenMass * (1f - settings.frontAxleLoadRatio)

        // Cargo axle loads via lever rule around front axle
        // Taking moments around front axle:
        // rearLoad * wheelbase = cargoMass * (cargoCog - frontAxleToplatformStart + frontAxleToplatformStart)
        // i.e. position of CoG relative to front axle
        val cogFromFrontAxle = cargoCog - frontAxleToplatformStart + frontAxleToplatformStart
        // Actually: platform starts at frontAxleToplatformStart AHEAD of front axle,
        // so distance from front axle = cargoCog + frontAxleToplatformStart
        val cogDistFromFrontAxle = cargoCog + frontAxleToplatformStart

        val cargoRearLoad = if (settings.wheelbase > 0)
            cargoMass * cogDistFromFrontAxle / settings.wheelbase
        else cargoMass * 0.6f

        val cargoFrontLoad = cargoMass - cargoRearLoad

        val frontAxleLoad = unladenFront + cargoFrontLoad
        val rearAxleLoad = unladenRear + cargoRearLoad

        if (totalMass > settings.maxTotalMass)
            warnings.add("⚠ Полная масса ${totalMass} кг превышает допустимые ${settings.maxTotalMass} кг!")
        if (frontAxleLoad > settings.maxFrontAxleLoad)
            warnings.add("⚠ Нагрузка на переднюю ось ${frontAxleLoad.toInt()} кг превышает допустимые ${settings.maxFrontAxleLoad} кг!")
        if (rearAxleLoad > settings.maxRearAxleLoad)
            warnings.add("⚠ Нагрузка на заднюю тележку ${rearAxleLoad.toInt()} кг превышает допустимые ${settings.maxRearAxleLoad} кг!")

        return LoadingResult(
            placements = allPlacements,
            totalMass = totalMass,
            frontAxleLoad = frontAxleLoad,
            rearAxleLoad = rearAxleLoad,
            isOverloaded = totalMass > settings.maxTotalMass ||
                    frontAxleLoad > settings.maxFrontAxleLoad ||
                    rearAxleLoad > settings.maxRearAxleLoad,
            warnings = warnings,
            unplacedVehicles = unplaced
        )
    }
}
