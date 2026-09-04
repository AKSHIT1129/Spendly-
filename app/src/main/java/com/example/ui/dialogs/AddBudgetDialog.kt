package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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

private val BrandLime = Color(0xFFDDF247)
private val TranslucentCardBg = Color(0x3D1F222C)
private val TranslucentCardBorder = Color(0x19FFFFFF)

@Composable
fun AddBudgetDialog(onDismiss: () -> Unit, onSave: (String, Double) -> Unit, currencySymbol: String = "$") {
    var category by remember { mutableStateOf("Food") }
    var limitStr by remember { mutableStateOf("") }
    val categories = listOf("Food", "Rent", "Salary", "Shopping", "Entertainment", "Utilities", "Investment", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = OpaqueDialogBg),
            border = BorderStroke(1.dp, TranslucentCardBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text("Set Threshold Limits", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = limitStr,
                    onValueChange = { limitStr = it },
                    label = { Text("Budget Limit ($currencySymbol)", color = Color.White) },
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
                Spacer(modifier = Modifier.height(14.dp))
                Text("Select Category", color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { cat ->
                        val active = category == cat
                        Button(
                            onClick = { category = cat },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (active) BrandLime else TranslucentCardBg,
                                contentColor = if (active) Color.Black else Color.White
                            ),
                            border = if (!active) BorderStroke(1.dp, TranslucentCardBorder) else null
                        ) {
                            Text(cat, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        val value = limitStr.toDoubleOrNull() ?: 0.0
                        if (value > 0) onSave(category, value)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save threshold")
                }
            }
        }
    }
}
