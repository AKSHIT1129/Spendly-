package com.example.ui.dialogs

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Member

private val DeepSpaceBlack = Color(0xFF09080E)
private val GlowingOrange = Color(0xFFC06240)
private val BrandLime = Color(0xFFDDF247)
private val TranslucentCardBg = Color(0x3D1F222C)
private val TranslucentCardBorder = Color(0x19FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMoneyDialog(
    members: List<Member>,
    onDismiss: () -> Unit,
    onSend: (Double, Int) -> Unit, // amount, memberId
    currencySymbol: String = "$"
) {
    var amountStr by remember { mutableStateOf("") }
    var selectedMemberId by remember { mutableStateOf(members.firstOrNull()?.id ?: 1) }

    // Vibrant Orange and Neon Lime breathing breathing-glow transition
    val infiniteTransition = rememberInfiniteTransition(label = "orange_lime_glow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val selectedMember = remember(members, selectedMemberId) {
        members.find { it.id == selectedMemberId }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .drawBehind {
                    // Luxurious, vibrant background ambient glows
                    drawCircle(
                        color = GlowingOrange.copy(alpha = 0.09f * pulseGlow),
                        radius = size.minDimension * 0.75f,
                        center = androidx.compose.ui.geometry.Offset(x = size.width * 0.9f, y = size.height * 0.15f)
                    )
                    drawCircle(
                        color = BrandLime.copy(alpha = 0.07f * (1f - pulseGlow)),
                        radius = size.minDimension * 0.65f,
                        center = androidx.compose.ui.geometry.Offset(x = size.width * 0.1f, y = size.height * 0.85f)
                    )
                }
        ) {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = DeepSpaceBlack),
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            TranslucentCardBorder,
                            GlowingOrange.copy(alpha = 0.35f),
                            BrandLime.copy(alpha = 0.35f),
                            TranslucentCardBorder
                        )
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Screen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Send Money",
                                color = Color.White,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Instant peer transfer",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0x1AFFFFFF), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    // Horizontal circular member list with glow highlight styling
                    Text(
                        text = "SELECT RECIPIENT",
                        color = Color.LightGray.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(members) { recipient ->
                            val isChosen = selectedMemberId == recipient.id
                            // Highlight avatar border when selected
                            val avatarBorderBrush = if (isChosen) {
                                Brush.horizontalGradient(listOf(GlowingOrange, BrandLime))
                            } else {
                                Brush.linearGradient(listOf(Color(0x15FFFFFF), Color(0x15FFFFFF)))
                            }
                            val avatarBg = if (isChosen) GlowingOrange.copy(alpha = 0.15f) else Color(0x0CFFFFFF)
                            val textColor = if (isChosen) Color.White else Color.LightGray

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(78.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { selectedMemberId = recipient.id }
                                    .background(if (isChosen) Color(0x08FFFFFF) else Color.Transparent)
                                    .padding(vertical = 10.dp, horizontal = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(avatarBg)
                                        .border(2.dp, avatarBorderBrush, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = recipient.name.take(2).uppercase(),
                                        color = if (isChosen) GlowingOrange else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = recipient.name,
                                    color = textColor,
                                    fontSize = 12.sp,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Large, Elegant Numeric input container
                    Text(
                        text = "TRANSFER AMOUNT",
                        color = Color.LightGray.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x0CFFFFFF))
                            .border(1.dp, Color(0x19FFFFFF), RoundedCornerShape(20.dp))
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currencySymbol,
                                color = GlowingOrange,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            BasicTextField(
                                value = amountStr,
                                onValueChange = { amountStr = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.SansSerif
                                ),
                                cursorBrush = Brush.verticalGradient(listOf(GlowingOrange, BrandLime)),
                                decorationBox = { innerTextField ->
                                    if (amountStr.isEmpty()) {
                                        Text(
                                            text = "0.00",
                                            color = Color.Gray.copy(alpha = 0.7f),
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    innerTextField()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (selectedMember != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Sending directly to ${selectedMember.name}'s wallet.",
                            color = Color.LightGray.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Sleek Glassmorphic Send button with Orange and Lime outline & glowing edge
                    Button(
                        onClick = {
                            val value = amountStr.toDoubleOrNull() ?: 0.0
                            if (value > 0) {
                                onSend(value, selectedMemberId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .border(
                                width = 1.5.dp,
                                brush = Brush.horizontalGradient(listOf(GlowingOrange, BrandLime)),
                                shape = RoundedCornerShape(22.dp)
                            )
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        GlowingOrange.copy(alpha = 0.7f),
                                        GlowingOrange.copy(alpha = 0.9f)
                                    )
                                ),
                                shape = RoundedCornerShape(22.dp)
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Send Money Now",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
