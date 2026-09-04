package com.example.ui.dialogs

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Member

private val DeepSpaceBlack = Color(0xFF09080E)
private val BrandLime = Color(0xFFDDF247)
private val BrandViolet = Color(0xFF8B5CF6)
private val TranslucentCardBg = Color(0x3D1F222C)
private val TranslucentCardBorder = Color(0x19FFFFFF)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    members: List<Member>,
    onDismiss: () -> Unit,
    onSave: (Double, String, String, Int, Boolean, Boolean, Boolean) -> Unit,
    currencySymbol: String = "$",
    initialIsExpense: Boolean = true
) {
    var isExpense by remember { mutableStateOf(initialIsExpense) }
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var description by remember { mutableStateOf("") }
    var chosenMemberId by remember { mutableStateOf(members.firstOrNull()?.id ?: 1) }
    var isShared by remember { mutableStateOf(false) }
    var isEssential by remember(category) {
        mutableStateOf(category.lowercase() in listOf("food", "rent", "utilities", "bills", "healthcare"))
    }

    val categories = listOf("Food", "Rent", "Salary", "Shopping", "Entertainment", "Utilities", "Investment", "Other")

    // Subtle violet & lime breathing glow animation for high contrast depth
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .drawBehind {
                    // Draw localized subtle ambient glows underneath the dialog for depth
                    drawCircle(
                        color = BrandViolet.copy(alpha = 0.08f * ambientPulse),
                        radius = size.minDimension * 0.7f,
                        center = androidx.compose.ui.geometry.Offset(x = size.width * 0.1f, y = size.height * 0.15f)
                    )
                    drawCircle(
                        color = BrandLime.copy(alpha = 0.06f * (1f - ambientPulse)),
                        radius = size.minDimension * 0.6f,
                        center = androidx.compose.ui.geometry.Offset(x = size.width * 0.9f, y = size.height * 0.85f)
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
                            BrandViolet.copy(alpha = 0.3f),
                            BrandLime.copy(alpha = 0.3f),
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
                    // Top Bar Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Record Transaction",
                            color = Color.White,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        )
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

                    Spacer(modifier = Modifier.height(20.dp))

                    // Redesigned dynamic switch: Expense vs Income
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0x0CFFFFFF))
                            .border(1.dp, Color(0x10FFFFFF), RoundedCornerShape(18.dp))
                            .padding(4.dp)
                    ) {
                        // Expense Switch Pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isExpense) Color(0xFFF87171).copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    width = if (isExpense) 1.5.dp else 0.dp,
                                    color = if (isExpense) Color(0xFFF87171).copy(alpha = 0.5f) else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { isExpense = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Expense",
                                color = if (isExpense) Color(0xFFF87171) else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        // Income Switch Pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (!isExpense) BrandLime.copy(alpha = 0.12f) else Color.Transparent)
                                .border(
                                    width = if (!isExpense) 1.5.dp else 0.dp,
                                    color = if (!isExpense) BrandLime.copy(alpha = 0.5f) else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { isExpense = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Income",
                                color = if (!isExpense) BrandLime else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Crisp Large Numeric Field for Amount
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x0CFFFFFF))
                            .border(1.dp, Color(0x19FFFFFF), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currencySymbol,
                                color = if (isExpense) Color(0xFFF87171) else BrandLime,
                                fontSize = 28.sp,
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
                                cursorBrush = Brush.verticalGradient(listOf(BrandLime, BrandViolet)),
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

                    Spacer(modifier = Modifier.height(18.dp))

                    // Sleek Description Field
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("What is this for?", color = Color.Gray, fontSize = 14.sp) },
                        label = { Text("Description", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0x0CFFFFFF),
                            unfocusedContainerColor = Color(0x0CFFFFFF),
                            focusedBorderColor = BrandViolet,
                            unfocusedBorderColor = Color(0x1AFFFFFF),
                            focusedLabelColor = BrandViolet
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Minimalist Chips Category Grid
                    Text(
                        text = "SELECT CATEGORY",
                        color = Color.LightGray.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { cat ->
                            val isSelected = category.equals(cat, ignoreCase = true)
                            val chipBg = if (isSelected) BrandLime.copy(alpha = 0.15f) else Color(0x0CFFFFFF)
                            val chipBorder = if (isSelected) BrandLime else Color(0x1FFFFFFF)
                            val chipContentColor = if (isSelected) BrandLime else Color.White

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(chipBg)
                                    .border(1.dp, chipBorder, RoundedCornerShape(14.dp))
                                    .clickable { category = cat }
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(cat),
                                    contentDescription = cat,
                                    tint = chipContentColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cat,
                                    color = chipContentColor,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Assigned Member Carousel Row
                    Text(
                        text = "ASSIGNED TO",
                        color = Color.LightGray.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(members) { m ->
                            val isSelected = chosenMemberId == m.id
                            val avatarBorderColor = if (isSelected) BrandViolet else Color.Transparent
                            val avatarBg = if (isSelected) BrandViolet.copy(alpha = 0.2f) else Color(0x0CFFFFFF)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(76.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { chosenMemberId = m.id }
                                    .padding(vertical = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(avatarBg)
                                        .border(2.dp, avatarBorderColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = m.name.take(2).uppercase(),
                                        color = if (isSelected) BrandViolet else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = m.name,
                                    color = if (isSelected) Color.White else Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    if (isExpense) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x0CFFFFFF))
                                .border(
                                    width = 1.dp,
                                    color = if (isEssential) BrandLime.copy(alpha = 0.5f) else Color(0x1AFFFFFF),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { isEssential = !isEssential }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (isEssential) BrandLime.copy(alpha = 0.2f) else Color(0x14FFFFFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isEssential) Icons.Default.CheckCircle else Icons.Default.Info,
                                        contentDescription = "Essential",
                                        tint = if (isEssential) BrandLime else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isEssential) "Essential (Needs)" else "Discretionary (Wants)",
                                        color = if (isEssential) BrandLime else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "50/30/20 Rule: Needs vs Wants",
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Switch(
                                checked = isEssential,
                                onCheckedChange = { isEssential = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = BrandLime,
                                    uncheckedThumbColor = Color.LightGray,
                                    uncheckedTrackColor = Color(0x1FFFFFFF)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Sleek glowing record button
                    Button(
                        onClick = {
                            val value = amountStr.toDoubleOrNull() ?: 0.0
                            if (value > 0) {
                                onSave(value, category, description, chosenMemberId, isShared, isExpense, isEssential)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(
                                width = 1.5.dp,
                                brush = Brush.horizontalGradient(listOf(BrandLime, BrandViolet)),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .background(
                                color = BrandLime,
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        Text(
                            text = "Record Transaction",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

private fun getCategoryIcon(cat: String): ImageVector {
    return when (cat.lowercase()) {
        "food" -> Icons.Default.Restaurant
        "rent" -> Icons.Default.Home
        "salary" -> Icons.Default.AccountBalanceWallet
        "shopping" -> Icons.Default.ShoppingBag
        "entertainment" -> Icons.Default.Tv
        "utilities" -> Icons.Default.ElectricalServices
        "investment" -> Icons.Default.ShowChart
        "transfer" -> Icons.Default.SwapHoriz
        else -> Icons.Default.Category
    }
}
