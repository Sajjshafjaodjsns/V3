package com.autovoz.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autovoz.data.db.CarLibraryEntity
import com.autovoz.domain.model.CargoVehicle
import com.autovoz.presentation.theme.*
import com.autovoz.presentation.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTripScreen(
    vm: MainViewModel,
    onCalculate: () -> Unit,
    onBack: () -> Unit,
    onLibrary: () -> Unit
) {
    val vehicles by vm.currentVehicles.collectAsState()
    val library by vm.library.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showLibraryPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Список автомобилей", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = OnDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = TruckOrange
                ),
                actions = {
                    IconButton(onClick = onLibrary) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Библиотека", tint = OnDark)
                    }
                }
            )
        },
        bottomBar = {
            Surface(color = DarkSurface, tonalElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showLibraryPicker = true },
                        modifier = Modifier.weight(1f).height(52.dp),
                        enabled = library.isNotEmpty()
                    ) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Из библиотеки", fontSize = 14.sp)
                    }
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.weight(1f).height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Вручную", fontSize = 14.sp)
                    }
                    Button(
                        onClick = onCalculate,
                        modifier = Modifier.weight(1f).height(52.dp),
                        enabled = vehicles.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = TruckOrange)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Расчёт", fontSize = 14.sp, color = androidx.compose.ui.graphics.Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Counter badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Добавлено: ${vehicles.size} / 8",
                    color = if (vehicles.size >= 8) YellowWarn else OnDark.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Spacer(Modifier.weight(1f))
                val totalMass = vehicles.sumOf { it.mass }
                if (totalMass > 0) {
                    Text(
                        "Итого: $totalMass кг",
                        color = TruckOrange,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (vehicles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = OnDark.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Добавьте автомобили для погрузки",
                            color = OnDark.copy(alpha = 0.4f),
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(vehicles, key = { it.id }) { car ->
                        VehicleCard(car = car, onRemove = { vm.removeVehicle(car.id) })
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }

    if (showAddDialog) {
        AddVehicleDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { car ->
                if (vehicles.size < 8) {
                    vm.addVehicle(car)
                }
                showAddDialog = false
            },
            onSaveToLibrary = { car -> vm.saveCarToLibrary(car) }
        )
    }

    if (showLibraryPicker) {
        LibraryPickerDialog(
            library = library,
            onDismiss = { showLibraryPicker = false },
            onSelect = { entity ->
                if (vehicles.size < 8) {
                    vm.addVehicle(
                        CargoVehicle(
                            name = entity.name,
                            mass = entity.mass,
                            length = entity.length,
                            width = entity.width,
                            height = entity.height
                        )
                    )
                }
                showLibraryPicker = false
            }
        )
    }
}

@Composable
fun VehicleCard(car: CargoVehicle, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = TruckOrange,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    car.name.ifEmpty { "Автомобиль" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = OnDark
                )
                Text(
                    "${car.mass} кг  •  ${car.length}×${car.width}×${car.height} м",
                    fontSize = 13.sp,
                    color = OnDark.copy(alpha = 0.65f)
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.RemoveCircleOutline,
                    contentDescription = "Удалить",
                    tint = RedError.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleDialog(
    onDismiss: () -> Unit,
    onAdd: (CargoVehicle) -> Unit,
    onSaveToLibrary: (CargoVehicle) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var mass by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Добавить автомобиль", fontWeight = FontWeight.Bold, color = TruckOrange)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTextField(value = name, onValueChange = { name = it }, label = "Модель (необязательно)")
                AppTextField(
                    value = mass, onValueChange = { mass = it }, label = "Масса, кг",
                    keyboardType = KeyboardType.Number
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTextField(
                        value = length, onValueChange = { length = it }, label = "Длина, м",
                        keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f)
                    )
                    AppTextField(
                        value = width, onValueChange = { width = it }, label = "Ширина, м",
                        keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f)
                    )
                    AppTextField(
                        value = height, onValueChange = { height = it }, label = "Высота, м",
                        keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f)
                    )
                }
                if (error.isNotEmpty()) {
                    Text(error, color = RedError, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Column {
                Button(
                    onClick = {
                        val car = buildCar(name, mass, length, width, height)
                        if (car == null) { error = "Проверьте введённые данные"; return@Button }
                        onAdd(car)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = TruckOrange)
                ) {
                    Text("Добавить", color = androidx.compose.ui.graphics.Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    onClick = {
                        val car = buildCar(name, mass, length, width, height)
                        if (car == null) { error = "Проверьте данные перед сохранением"; return@OutlinedButton }
                        onSaveToLibrary(car)
                        onAdd(car)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Добавить и сохранить в библиотеку")
                }
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Отмена")
                }
            }
        },
        dismissButton = {}
    )
}

fun buildCar(name: String, mass: String, length: String, width: String, height: String): CargoVehicle? {
    return try {
        CargoVehicle(
            name = name.trim(),
            mass = mass.trim().toInt(),
            length = length.trim().replace(',', '.').toFloat(),
            width = width.trim().replace(',', '.').toFloat(),
            height = height.trim().replace(',', '.').toFloat()
        )
    } catch (_: Exception) { null }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TruckOrange,
            unfocusedBorderColor = OnDark.copy(alpha = 0.3f),
            focusedLabelColor = TruckOrange,
            cursorColor = TruckOrange,
            focusedTextColor = OnDark,
            unfocusedTextColor = OnDark
        ),
        singleLine = true
    )
}

@Composable
fun LibraryPickerDialog(
    library: List<CarLibraryEntity>,
    onDismiss: () -> Unit,
    onSelect: (CarLibraryEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Библиотека автомобилей", color = TruckOrange, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(library) { entity ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        onClick = { onSelect(entity) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = TruckOrange, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(entity.name.ifEmpty { "Автомобиль" }, color = OnDark, fontWeight = FontWeight.SemiBold)
                                Text("${entity.mass} кг  •  ${entity.length}×${entity.width}×${entity.height} м", color = OnDark.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )
}
