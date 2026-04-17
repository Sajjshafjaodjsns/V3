package com.autovoz.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autovoz.presentation.theme.*
import com.autovoz.presentation.screens.AppTextField
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SaveTripDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    val defaultName = SimpleDateFormat("Рейс dd.MM.yyyy", Locale.getDefault()).format(Date())
    var name by remember { mutableStateOf(defaultName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Сохранить рейс", fontWeight = FontWeight.Bold, color = TruckOrange) },
        text = {
            Column {
                Text("Введите название для сохранения:", color = OnDark.copy(alpha = 0.7f))
                Spacer(Modifier.height(10.dp))
                AppTextField(value = name, onValueChange = { name = it }, label = "Название рейса")
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = TruckOrange)
            ) {
                Text("Сохранить", color = androidx.compose.ui.graphics.Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
