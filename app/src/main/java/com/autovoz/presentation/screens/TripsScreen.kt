package com.autovoz.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autovoz.data.db.TripEntity
import com.autovoz.presentation.theme.*
import com.autovoz.presentation.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(
    vm: MainViewModel,
    onNewTrip: () -> Unit,
    onOpenTrip: (TripEntity) -> Unit,
    onSettings: () -> Unit
) {
    val trips by vm.trips.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("АвтовозЛоадер", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = TruckOrange
                ),
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки", tint = OnDark)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { vm.clearVehicles(); onNewTrip() },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Новый рейс", fontSize = 16.sp) },
                containerColor = TruckOrange,
                contentColor = androidx.compose.ui.graphics.Color.Black
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        if (trips.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.LocalShipping,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = TruckOrange.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Нет сохранённых рейсов",
                        color = OnDark.copy(alpha = 0.5f),
                        fontSize = 18.sp
                    )
                    Text(
                        "Нажмите «Новый рейс» для начала",
                        color = OnDark.copy(alpha = 0.35f),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(trips, key = { it.id }) { trip ->
                    TripCard(
                        trip = trip,
                        onOpen = { onOpenTrip(trip) },
                        onDelete = { vm.deleteTrip(trip.id) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun TripCard(trip: TripEntity, onOpen: () -> Unit, onDelete: () -> Unit) {
    val fmt = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    var showDelete by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpen() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocalShipping,
                contentDescription = null,
                tint = TruckOrange,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(trip.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = OnDark)
                Text(
                    fmt.format(Date(trip.createdAt)),
                    fontSize = 13.sp,
                    color = OnDark.copy(alpha = 0.6f)
                )
                Text(
                    trip.resultJson,
                    fontSize = 12.sp,
                    color = if (trip.resultJson.contains("ПЕРЕГРУЗ")) RedError else GreenOk
                )
            }
            IconButton(onClick = { showDelete = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = RedError.copy(alpha = 0.7f))
            }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Удалить рейс?") },
            text = { Text("«${trip.name}» будет удалён без возможности восстановления.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDelete = false }) {
                    Text("Удалить", color = RedError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Отмена") }
            },
            containerColor = DarkSurface
        )
    }
}
