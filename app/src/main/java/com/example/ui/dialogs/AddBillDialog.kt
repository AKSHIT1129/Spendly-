package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.OpaqueDialogBg
import java.text.SimpleDateFormat
import java.util.*

private val BrandLime = Color(0xFFDDF247)
private val TranslucentCardBg = Color(0x3D1F222C)
private val TranslucentCardBorder = Color(0x19FFFFFF)

@Composable
fun AddBillDialog(onDismiss: () -> Unit, onSave: (String, Double, Long, String) -> Unit, currencySymbol: String = "$") {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Utilities") }
    var selectedDueDate by remember { mutableStateOf(System.currentTimeMillis() + 86400000) } // default tomorrow
    var showDatePicker by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val calendar = remember { Calendar.getInstance().apply { timeInMillis = selectedDueDate } }
    val dateDisplayFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    if (showDatePicker) {
        DisposableEffect(Unit) {
            val dpd = android.app.DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val newCal = Calendar.getInstance()
                    newCal.set(Calendar.YEAR, year)
                    newCal.set(Calendar.MONTH, month)
                    newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    selectedDueDate = newCal.timeInMillis
                    showDatePicker = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dpd.setOnDismissListener {
                showDatePicker = false
            }
            dpd.show()
            onDispose {
                dpd.dismiss()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = OpaqueDialogBg),
            border = BorderStroke(1.dp, TranslucentCardBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text("New Utility Alert", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Issuer Name", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = TranslucentCardBg,
                        unfocusedContainerColor = TranslucentCardBg,
                        focusedBorderColor = BrandLime,
                        unfocusedBorderColor = TranslucentCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Bill Amount ($currencySymbol)", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = TranslucentCardBg,
                        unfocusedContainerColor = TranslucentCardBg,
                        focusedBorderColor = BrandLime,
                        unfocusedBorderColor = TranslucentCardBorder
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = dateDisplayFormatter.format(Date(selectedDueDate)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Due Date", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = TranslucentCardBg,
                        unfocusedContainerColor = TranslucentCardBg,
                        focusedBorderColor = BrandLime,
                        unfocusedBorderColor = TranslucentCardBorder
                    ),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.Event, contentDescription = "Select Date", tint = BrandLime)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        val value = amountStr.toDoubleOrNull() ?: 0.0
                        if (title.isNotEmpty() && value > 0) {
                            onSave(title, value, selectedDueDate, category)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enable Utility Reminder")
                }
            }
        }
    }
}
