package com.autovoz.presentation.screens

sealed class Screen(val route: String) {
    object Trips : Screen("trips")
    object NewTrip : Screen("new_trip")
    object Result : Screen("result")
    object Settings : Screen("settings")
    object Library : Screen("library")
}
