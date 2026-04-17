package com.autovoz.presentation.screens

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
import com.autovoz.data.db.CarLibraryEntity
import com.autovoz.domain.model.CargoVehicle
import com.autovoz.presentation.theme.*
import com.autovoz.presentation.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(vm: MainViewModel, onBack: () -> Unit) {
    val library by vm.library.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Библиотека автомобилей", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = OnDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = TruckOrange
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdd = true },
                containerColor = TruckOrange
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить", tint = androidx.compose.ui.graphics.Color.Black)
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        if (library.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = OnDark.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Библиотека пуста", color = OnDark.copy(alpha = 0.4f), fontSize = 18.sp)
                    Text(
                        "Добавьте часто перевозимые\nмодели для быстрого выбора",
                        color = OnDark.copy(alpha = 0.3f),
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(library, key = { it.id }) { entity ->
                    LibraryCard(entity = entity, onDelete = { vm.deleteCarFromLibrary(entity.id) })
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showAdd) {
        AddVehicleDialog(
            onDismiss = { showAdd = false },
            onAdd = { car ->
                vm.saveCarToLibrary(car)
                showAdd = false
            },
            onSaveToLibrary = {}
        )
    }
}

@Composable
fun LibraryCard(entity: CarLibraryEntity, onDelete: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = TruckOrange, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entity.name.ifEmpty { "Автомобиль" }, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnDark)
                Text(
                    "${entity.mass} кг  •  ${entity.length}×${entity.width}×${entity.height} м",
                    fontSize = 13.sp,
                    color = OnDark.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = { confirmDelete = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = RedError.copy(alpha = 0.7f))
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Удалить из библиотеки?") },
            text = { Text("«${entity.name}» будет удалён из библиотеки.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); confirmDelete = false }) {
                    Text("Удалить", color = RedError)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Отмена") }
            },
            containerColor = DarkSurface
        )
    }
}
