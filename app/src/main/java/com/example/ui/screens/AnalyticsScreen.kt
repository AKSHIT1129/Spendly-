package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Budget
import com.example.data.model.Member
import com.example.data.model.Transaction

private val BrandLime = Color(0xFFDDF247)
private val TranslucentCardBg = Color(0x3D1F222C)
private val TranslucentCardBorder = Color(0x19FFFFFF)

@Composable
fun AnalyticsScreen(
    transactions: List<Transaction>,
    members: List<Member>,
    budgets: List<Budget>,
    currencySymbol: String = "$"
) {
    if (transactions.isEmpty()) {
        CardEmptyPlaceholder(
            title = "Awaiting financial stats",
            subtitle = "Settle custom revenue or spend entries to start real-time analytics aggregation."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            val income = transactions.filter { it.amount > 0 }.sumOf { it.amount }
            val expense = transactions.filter { it.amount < 0 }.sumOf { -it.amount }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = TranslucentCardBg),
                    border = BorderStroke(1.dp, TranslucentCardBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x2110B981)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = "Income",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sourced", fontSize = 11.sp, color = Color(0xFFA1A1AA), fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${currencySymbol}${String.format("%,.0f", income)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = TranslucentCardBg),
                    border = BorderStroke(1.dp, TranslucentCardBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x21FF5252)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = "Expense",
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Expended", fontSize = 11.sp, color = Color(0xFFA1A1AA), fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${currencySymbol}${String.format("%,.0f", expense)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF87171)
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TranslucentCardBg),
                border = BorderStroke(1.dp, TranslucentCardBorder),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Spend Categorically Breakdown",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    val spendsByCategory = remember(transactions) {
                        transactions
                            .filter { it.amount < 0 }
                            .groupBy { it.category }
                            .mapValues { entry -> entry.value.sumOf { -it.amount } }
                    }

                    if (spendsByCategory.isEmpty()) {
                        Text(
                            text = "No expenses logged details",
                            color = Color(0xFFA1A1AA),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        val totalSpend = spendsByCategory.values.sum()

                        val premiumColors = listOf(
                            BrandLime, // Lime-yellow
                            Color(0xFF38BDF8), // Sky blue
                            Color(0xFFF472B6), // Pink
                            Color(0xFFFB923C), // Orange
                            Color(0xFF2DD4BF), // Teal/Turquoise
                            Color(0xFFC084FC), // Purple
                            Color(0xFF34D399), // Emerald green
                            Color(0xFFF87171)  // Sunset red
                        )

                        val categoryColors = remember(spendsByCategory) {
                            spendsByCategory.keys.toList().mapIndexed { index, cat ->
                                cat to premiumColors[index % premiumColors.size]
                            }.toMap()
                        }

                        var animationPlayed by remember { mutableStateOf(false) }
                        val progressAnimation by animateFloatAsState(
                            targetValue = if (animationPlayed) 1f else 0f,
                            animationSpec = tween(
                                durationMillis = 1000,
                                delayMillis = 100,
                                easing = FastOutSlowInEasing
                            ),
                            label = "DonutChartAnimation"
                        )

                        LaunchedEffect(key1 = transactions) {
                            animationPlayed = true
                        }

                        // Donut Chart container
                        Box(
                            modifier = Modifier.size(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                var currentStartAngle = -90f
                                val spacing = if (spendsByCategory.size > 1) 4f else 0f
                                val strokeWidthPx = 36f

                                spendsByCategory.forEach { (cat, amt) ->
                                    val color = categoryColors[cat] ?: BrandLime
                                    val spendRatio = if (totalSpend > 0) (amt / totalSpend).toFloat() else 0f
                                    val sweepAngle = spendRatio * 360f

                                    if (sweepAngle > 0f) {
                                        val adjustedSweep = if (sweepAngle > spacing) sweepAngle - spacing else sweepAngle
                                        
                                        drawArc(
                                            color = color,
                                            startAngle = currentStartAngle + (spacing / 2f),
                                            sweepAngle = adjustedSweep * progressAnimation,
                                            useCenter = false,
                                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                                        )
                                        currentStartAngle += sweepAngle
                                    }
                                }
                            }

                            // Center text
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Total Spend",
                                    color = Color(0xFFA1A1AA),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                               )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${currencySymbol}${String.format("%,.0f", totalSpend)}",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Category list legend
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            spendsByCategory.forEach { (cat, amt) ->
                                val color = categoryColors[cat] ?: BrandLime
                                val percent = if (totalSpend > 0) (amt / totalSpend).toFloat() else 0f
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x08FFFFFF))
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = cat,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = "${currencySymbol}${String.format("%,.0f", amt)} (${String.format("%.0f", percent * 100)}%)",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            val income = transactions.filter { it.amount > 0 }.sumOf { it.amount }
            val expenseTxs = transactions.filter { it.amount < 0 }
            val totalExpense = expenseTxs.sumOf { -it.amount }

            val essentialExpense = expenseTxs.filter {
                it.isEssential || it.category.lowercase() in listOf("rent", "food", "groceries", "utilities", "bills", "healthcare", "medical", "insurance", "transport", "emi", "education")
            }.sumOf { -it.amount }

            val wantsExpense = (totalExpense - essentialExpense).coerceAtLeast(0.0)
            val savings = (income - totalExpense).coerceAtLeast(0.0)

            val baseAmount = if (income > 0) income else totalExpense.coerceAtLeast(1.0)
            val essentialPct = (essentialExpense / baseAmount).toFloat().coerceIn(0f, 1f)
            val wantsPct = (wantsExpense / baseAmount).toFloat().coerceIn(0f, 1f)
            val savingsPct = (savings / baseAmount).toFloat().coerceIn(0f, 1f)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TranslucentCardBg),
                border = BorderStroke(1.dp, TranslucentCardBorder),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Essentials vs. Wants (50/30/20)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Target: 50% Needs • 30% Wants • 20% Savings",
                                color = Color(0xFFA1A1AA),
                                fontSize = 11.sp
                            )
                        }

                        val isOnTrack = essentialPct <= 0.55f
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isOnTrack) Color(0x2110B981) else Color(0x21F59E0B))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isOnTrack) "✨ Balanced" else "⚠️ High Needs",
                                color = if (isOnTrack) Color(0xFF10B981) else Color(0xFFF59E0B),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Segmented modern progress bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color(0x1AFFFFFF))
                    ) {
                        if (essentialPct > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(essentialPct.coerceAtLeast(0.01f))
                                    .fillMaxHeight()
                                    .background(Color(0xFF38BDF8))
                            )
                        }
                        if (wantsPct > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(wantsPct.coerceAtLeast(0.01f))
                                    .fillMaxHeight()
                                    .background(Color(0xFFF472B6))
                            )
                        }
                        if (savingsPct > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(savingsPct.coerceAtLeast(0.01f))
                                    .fillMaxHeight()
                                    .background(BrandLime)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 Metric Rows
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Essentials
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x08FFFFFF))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF38BDF8))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Needs / Essentials", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            Text(
                                text = "${currencySymbol}${String.format("%,.0f", essentialExpense)} (${String.format("%.0f", essentialPct * 100)}% / 50%)",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Wants
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x08FFFFFF))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF472B6))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Wants / Discretionary", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            Text(
                                text = "${currencySymbol}${String.format("%,.0f", wantsExpense)} (${String.format("%.0f", wantsPct * 100)}% / 30%)",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Savings
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x08FFFFFF))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(BrandLime)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Savings / Retained", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            Text(
                                text = "${currencySymbol}${String.format("%,.0f", savings)} (${String.format("%.0f", savingsPct * 100)}% / 20%)",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
