package com.autovoz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.autovoz.presentation.components.SaveTripDialog
import com.autovoz.presentation.screens.*
import com.autovoz.presentation.theme.AutovozTheme
import com.autovoz.presentation.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutovozTheme {
                AutovozApp(vm)
            }
        }
    }
}

@Composable
fun AutovozApp(vm: MainViewModel) {
    val navController = rememberNavController()
    var showSaveDialog by remember { mutableStateOf(false) }

    NavHost(navController = navController, startDestination = Screen.Trips.route) {

        composable(Screen.Trips.route) {
            TripsScreen(
                vm = vm,
                onNewTrip = { navController.navigate(Screen.NewTrip.route) },
                onOpenTrip = { trip ->
                    vm.loadTrip(trip)
                    navController.navigate(Screen.Result.route)
                },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.NewTrip.route) {
            NewTripScreen(
                vm = vm,
                onCalculate = { navController.navigate(Screen.Result.route) },
                onBack = { navController.popBackStack() },
                onLibrary = { navController.navigate(Screen.Library.route) }
            )
        }

        composable(Screen.Result.route) {
            ResultScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onSave = { showSaveDialog = true }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                vm = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Library.route) {
            LibraryScreen(
                vm = vm,
                onBack = { navController.popBackStack() }
            )
        }
    }

    if (showSaveDialog) {
        SaveTripDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { name ->
                vm.saveCurrentTrip(name)
                showSaveDialog = false
                navController.navigate(Screen.Trips.route) {
                    popUpTo(Screen.Trips.route) { inclusive = true }
                }
            }
        )
    }
}
