package com.autovoz.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autovoz.domain.model.TruckSettings
import com.autovoz.presentation.theme.*
import com.autovoz.presentation.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: MainViewModel, onBack: () -> Unit) {
    val settings by vm.settings.collectAsState()
    var local by remember(settings) { mutableStateOf(settings) }
    var saved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки автовоза", fontWeight = FontWeight.Bold) },
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
        containerColor = DarkBackground,
        bottomBar = {
            Surface(color = DarkSurface, tonalElevation = 8.dp) {
                Button(
                    onClick = {
                        vm.saveSettings(local)
                        saved = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TruckOrange)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (saved) "Сохранено ✓" else "Сохранить настройки",
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.Black
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsSection("Массы") {
                IntField("Снаряжённая масса, кг", local.unladenMass) { local = local.copy(unladenMass = it) }
                IntField("Макс. полная масса, кг", local.maxTotalMass) { local = local.copy(maxTotalMass = it) }
                IntField("Допуст. нагрузка — передн. ось, кг", local.maxFrontAxleLoad) { local = local.copy(maxFrontAxleLoad = it) }
                IntField("Допуст. нагрузка — задн. тележка, кг", local.maxRearAxleLoad) { local = local.copy(maxRearAxleLoad = it) }
            }

            SettingsSection("Геометрия платформы") {
                FloatField("Колёсная база, м", local.wheelbase) { local = local.copy(wheelbase = it) }
                FloatField("Длина платформы, м", local.platformLength) { local = local.copy(platformLength = it) }
                FloatField("Ширина платформы, м", local.platformWidth) { local = local.copy(platformWidth = it) }
                FloatField("Макс. высота — нижний ярус, м", local.maxHeightLowerDeck) { local = local.copy(maxHeightLowerDeck = it) }
                FloatField("Макс. высота — верхний ярус, м", local.maxHeightUpperDeck) { local = local.copy(maxHeightUpperDeck = it) }
                FloatField("Передний свес (до платформы), м", local.frontOverhang) { local = local.copy(frontOverhang = it) }
            }

            SettingsSection("Распределение снаряжённой массы") {
                val pct = (local.frontAxleLoadRatio * 100).toInt()
                Text(
                    "Передняя ось: $pct%  |  Задняя: ${100 - pct}%",
                    color = OnDark.copy(alpha = 0.8f), fontSize = 14.sp
                )
                Slider(
                    value = local.frontAxleLoadRatio,
                    onValueChange = { local = local.copy(frontAxleLoadRatio = it); saved = false },
                    valueRange = 0.2f..0.6f,
                    steps = 7,
                    colors = SliderDefaults.colors(thumbColor = TruckOrange, activeTrackColor = TruckOrange)
                )
            }

            // Info block
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = TruckOrange.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Значения по умолчанию соответствуют Scania P-series 4×4 с надстройкой Uçsuoğlu.",
                        color = OnDark.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, color = TruckOrange, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Divider(color = OnDark.copy(alpha = 0.1f))
            content()
        }
    }
}

@Composable
fun IntField(label: String, value: Int, onChange: (Int) -> Unit) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    AppTextField(
        value = text,
        onValueChange = {
            text = it
            it.toIntOrNull()?.let(onChange)
        },
        label = label,
        keyboardType = KeyboardType.Number
    )
}

@Composable
fun FloatField(label: String, value: Float, onChange: (Float) -> Unit) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    AppTextField(
        value = text,
        onValueChange = {
            text = it
            it.replace(',', '.').toFloatOrNull()?.let(onChange)
        },
        label = label,
        keyboardType = KeyboardType.Decimal
    )
}
