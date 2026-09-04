package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.OpaqueDialogBg

private val BrandLime = Color(0xFFDDF247)
private val TranslucentCardBg = Color(0x3D1F222C)
private val TranslucentCardBorder = Color(0x19FFFFFF)

@Composable
fun AddMemberDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Family member") }
    var colHex by remember { mutableStateOf("#10B981") }

    val activeColors = listOf("#10B981", "#6366F1", "#F59E0B", "#EF4444", "#EC4899", "#8B5CF6")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = OpaqueDialogBg),
            border = BorderStroke(1.dp, TranslucentCardBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text("Provision New Profile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Profile Name", color = Color.White) },
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
                Spacer(modifier = Modifier.height(14.dp))
                Text("Assigned Role", color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Admin", "Member", "Child").forEach { r ->
                        val active = r == role
                        Button(
                            onClick = { role = r },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (active) BrandLime else TranslucentCardBg,
                                contentColor = if (active) Color.Black else Color.White
                            ),
                            border = if (!active) BorderStroke(1.dp, TranslucentCardBorder) else null
                        ) {
                            Text(r, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text("Vault Color", color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    activeColors.forEach { color ->
                        val active = color == colHex
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(color)))
                                .border(if (active) 2.dp else 0.dp, Color.White, CircleShape)
                                .clickable { colHex = color }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { if (name.isNotEmpty()) onSave(name, role, colHex) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Profile")
                }
            }
        }
    }
}
