package com.autovoz.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autovoz.domain.model.Deck
import com.autovoz.domain.model.LoadingResult
import com.autovoz.domain.model.TruckSettings
import com.autovoz.presentation.theme.*
import com.autovoz.presentation.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val result by vm.loadingResult.collectAsState()
    val settings by vm.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Результат расчёта", fontWeight = FontWeight.Bold) },
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
                    IconButton(onClick = onSave) {
                        Icon(Icons.Default.Save, contentDescription = "Сохранить", tint = OnDark)
                    }
                }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        if (result == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Нет данных для отображения", color = OnDark.copy(alpha = 0.5f))
            }
            return@Scaffold
        }

        val r = result!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status banner
            StatusBanner(r)

            // Mass summary card
            MassSummaryCard(r, settings)

            // Axle load gauges
            AxleGaugesCard(r, settings)

            // Platform visualization
            PlatformVisualizationCard(r, settings)

            // Warnings
            if (r.warnings.isNotEmpty()) {
                WarningsCard(r.warnings)
            }

            // Unplaced vehicles
            if (r.unplacedVehicles.isNotEmpty()) {
                UnplacedCard(r)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun StatusBanner(result: LoadingResult) {
    val bgColor = if (result.isOverloaded) RedError.copy(alpha = 0.15f) else GreenOk.copy(alpha = 0.12f)
    val iconColor = if (result.isOverloaded) RedError else GreenOk
    val icon = if (result.isOverloaded) Icons.Default.Warning else Icons.Default.CheckCircle
    val text = if (result.isOverloaded) "ОБНАРУЖЕН ПЕРЕГРУЗ" else "ПОГРУЗКА В НОРМЕ"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                text,
                color = iconColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun MassSummaryCard(result: LoadingResult, settings: TruckSettings) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Массы", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TruckOrange)
            Divider(color = OnDark.copy(alpha = 0.1f))
            MassRow("Снаряжённая масса", "${settings.unladenMass} кг")
            MassRow("Груз (${result.placements.size} авт.)", "+${result.placements.sumOf { it.vehicle.mass }} кг")
            Divider(color = TruckOrange.copy(alpha = 0.4f))
            MassRow(
                "Полная масса",
                "${result.totalMass} кг",
                highlight = result.totalMass > settings.maxTotalMass
            )
            MassRow("Допустимо", "${settings.maxTotalMass} кг", subtext = true)
        }
    }
}

@Composable
fun MassRow(label: String, value: String, highlight: Boolean = false, subtext: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            color = if (subtext) OnDark.copy(alpha = 0.5f) else OnDark.copy(alpha = 0.8f),
            fontSize = if (subtext) 13.sp else 15.sp
        )
        Text(
            value,
            color = if (highlight) RedError else if (subtext) OnDark.copy(alpha = 0.5f) else OnDark,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (subtext) 13.sp else 15.sp
        )
    }
}

@Composable
fun AxleGaugesCard(result: LoadingResult, settings: TruckSettings) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Нагрузки на оси", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TruckOrange)
            Divider(color = OnDark.copy(alpha = 0.1f))
            AxleGauge(
                label = "Передняя ось",
                load = result.frontAxleLoad,
                maxLoad = settings.maxFrontAxleLoad.toFloat()
            )
            AxleGauge(
                label = "Задняя тележка",
                load = result.rearAxleLoad,
                maxLoad = settings.maxRearAxleLoad.toFloat()
            )
        }
    }
}

@Composable
fun AxleGauge(label: String, load: Float, maxLoad: Float) {
    val ratio = (load / maxLoad).coerceIn(0f, 1.2f)
    val barColor = when {
        ratio > 1.0f -> RedError
        ratio > 0.85f -> YellowWarn
        else -> GreenOk
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = OnDark.copy(alpha = 0.8f), fontSize = 14.sp)
            Text(
                "${load.toInt()} / ${"%.0f".format(maxLoad)} кг",
                color = barColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .background(DarkSurfaceVariant, RoundedCornerShape(9.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(barColor, RoundedCornerShape(9.dp))
            )
        }
        Text(
            "${"%.0f".format(ratio * 100)}% от допустимой нагрузки",
            color = OnDark.copy(alpha = 0.45f),
            fontSize = 11.sp
        )
    }
}

@Composable
fun PlatformVisualizationCard(result: LoadingResult, settings: TruckSettings) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Схема размещения (вид сбоку)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TruckOrange)
            Divider(color = OnDark.copy(alpha = 0.1f))

            // Legend
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendItem(GreenOk, "Лёгкие")
                LegendItem(YellowWarn, "Средние")
                LegendItem(RedError, "Тяжёлые")
            }

            Spacer(Modifier.height(4.dp))

            val lower = result.placements.filter { it.deck == Deck.LOWER }
            val upper = result.placements.filter { it.deck == Deck.UPPER }
            val platformLen = settings.platformLength
            val allMasses = result.placements.map { it.vehicle.mass }
            val minMass = allMasses.minOrNull() ?: 1
            val maxMass = allMasses.maxOrNull() ?: 1

            // Upper deck
            Text("Верхний ярус", color = OnDark.copy(alpha = 0.6f), fontSize = 12.sp)
            PlatformRow(
                placements = upper,
                platformLength = platformLen,
                minMass = minMass,
                maxMass = maxMass
            )

            Spacer(Modifier.height(2.dp))

            // Lower deck
            Text("Нижний ярус", color = OnDark.copy(alpha = 0.6f), fontSize = 12.sp)
            PlatformRow(
                placements = lower,
                platformLength = platformLen,
                minMass = minMass,
                maxMass = maxMass
            )

            Spacer(Modifier.height(8.dp))

            // Placement list
            result.placements.forEachIndexed { i, p ->
                val deckLabel = if (p.deck == Deck.LOWER) "Нижний" else "Верхний"
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "${i + 1}. ${p.vehicle.name.ifEmpty { "Авт." }} — $deckLabel ярус",
                        color = OnDark.copy(alpha = 0.75f), fontSize = 13.sp
                    )
                    Text(
                        "${p.vehicle.mass} кг  поз: ${"%.1f".format(p.positionFromFront)}м",
                        color = OnDark.copy(alpha = 0.5f), fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PlatformRow(
    placements: List<com.autovoz.domain.model.PlacedVehicle>,
    platformLength: Float,
    minMass: Int,
    maxMass: Int
) {
    val bgColor = DarkSurfaceVariant
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        val w = size.width
        val h = size.height
        val scaleX = w / platformLength

        // Platform outline
        drawRect(color = OnDark.copy(alpha = 0.15f), size = Size(w, h), style = Stroke(2f))

        for (p in placements) {
            val ratio = if (maxMass > minMass) (p.vehicle.mass - minMass).toFloat() / (maxMass - minMass) else 0.5f
            val carColor = lerpColor(GreenOk, RedError, ratio)

            val x = p.positionFromFront * scaleX
            val carW = p.vehicle.length * scaleX - 4f

            drawRect(
                color = carColor.copy(alpha = 0.85f),
                topLeft = Offset(x + 2f, 4f),
                size = Size(carW.coerceAtLeast(8f), h - 8f)
            )
            // wheel dots
            val wheelY = h - 6f
            drawCircle(color = Color.Black.copy(alpha = 0.7f), radius = 4f, center = Offset(x + 10f, wheelY))
            drawCircle(color = Color.Black.copy(alpha = 0.7f), radius = 4f, center = Offset(x + carW - 6f, wheelY))
        }
    }
}

fun lerpColor(a: Color, b: Color, t: Float): Color {
    val clamped = t.coerceIn(0f, 1f)
    return Color(
        red = a.red + (b.red - a.red) * clamped,
        green = a.green + (b.green - a.green) * clamped,
        blue = a.blue + (b.blue - a.blue) * clamped
    )
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(4.dp))
        Text(label, color = OnDark.copy(alpha = 0.6f), fontSize = 12.sp)
    }
}

@Composable
fun WarningsCard(warnings: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = RedError.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = YellowWarn, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Предупреждения", fontWeight = FontWeight.Bold, color = YellowWarn, fontSize = 15.sp)
            }
            warnings.forEach { w ->
                Text("• $w", color = OnDark.copy(alpha = 0.85f), fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun UnplacedCard(result: LoadingResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Не размещено (${result.unplacedVehicles.size})",
                fontWeight = FontWeight.Bold,
                color = RedError,
                fontSize = 15.sp
            )
            result.unplacedVehicles.forEach { car ->
                Text(
                    "• ${car.name.ifEmpty { "Автомобиль" }} — ${car.mass} кг, ${car.length}×${car.height} м",
                    color = OnDark.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }
    }
}
