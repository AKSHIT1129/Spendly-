package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.OpaqueDialogBg
import com.example.data.model.Member

private val BrandLime = Color(0xFFDDF247)
private val TranslucentCardBg = Color(0x3D1F222C)
private val TranslucentCardBorder = Color(0x19FFFFFF)

@Composable
fun MemberProfileDetailDialog(member: Member, transactionsCount: Int, onDismiss: () -> Unit, onDelete: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = OpaqueDialogBg),
            border = BorderStroke(1.dp, TranslucentCardBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                Text(member.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Role: ${member.role}", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Total transactions recorded: $transactionsCount", color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = TranslucentCardBg, contentColor = Color.White),
                        border = BorderStroke(1.dp, TranslucentCardBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF87171), contentColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete Profile")
                    }
                }
            }
        }
    }
}
