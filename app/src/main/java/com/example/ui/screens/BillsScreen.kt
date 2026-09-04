package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BillReminder
import java.text.SimpleDateFormat
import java.util.*

private val BrandLime = Color(0xFFDDF247)
private val TranslucentCardBg = Color(0x3D1F222C)
private val TranslucentCardBorder = Color(0x19FFFFFF)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BillsScreen(
    billReminders: List<BillReminder>,
    onAddBillClick: () -> Unit,
    onTogglePaid: (BillReminder) -> Unit,
    onSimulateAlert: (BillReminder) -> Unit,
    onDeleteBill: (BillReminder) -> Unit,
    currencySymbol: String = "$",
    isEmbedded: Boolean = false
) {
    val today = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    if (billReminders.isEmpty()) {
        CardEmptyPlaceholder(
            title = "All utilities clear",
            subtitle = "Set up structured alert reminders on electricity, internet, or credit lines easily."
        )
        if (!isEmbedded) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
                FloatingActionButton(
                    onClick = onAddBillClick,
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = 120.dp, end = 16.dp)
                        .testTag("add_bill_empty_fab")
                ) {
                    Icon(Icons.Default.Add, "add_btn")
                }
            }
        }
        return
    }

    if (isEmbedded) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            billReminders.forEach { bill ->
                BillReminderRow(
                    bill = bill,
                    today = today,
                    currencySymbol = currencySymbol,
                    onTogglePaid = onTogglePaid,
                    onSimulateAlert = onSimulateAlert,
                    onDeleteBill = onDeleteBill
                )
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(billReminders) { bill ->
                    BillReminderRow(
                        bill = bill,
                        today = today,
                        currencySymbol = currencySymbol,
                        onTogglePaid = onTogglePaid,
                        onSimulateAlert = onSimulateAlert,
                        onDeleteBill = onDeleteBill
                    )
                }
            }

            FloatingActionButton(
                onClick = onAddBillClick,
                containerColor = Color.White,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 120.dp, end = 16.dp)
                    .testTag("add_bill_fab")
            ) {
                Icon(Icons.Default.Add, "New Alert")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BillReminderRow(
    bill: BillReminder,
    today: Long,
    currencySymbol: String,
    onTogglePaid: (BillReminder) -> Unit,
    onSimulateAlert: (BillReminder) -> Unit,
    onDeleteBill: (BillReminder) -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(TranslucentCardBg)
            .border(1.dp, TranslucentCardBorder, RoundedCornerShape(20.dp))
            .combinedClickable(onClick = {}, onLongClick = { onDeleteBill(bill) })
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Checkbox(
                checked = bill.isPaid,
                onCheckedChange = { onTogglePaid(bill) },
                colors = CheckboxDefaults.colors(checkedColor = BrandLime, uncheckedColor = Color.White)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = bill.title,
                        color = if (bill.isPaid) Color.Gray else Color.White,
                        textDecoration = if (bill.isPaid) TextDecoration.LineThrough else null,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val badgeText: String
                    val badgeColor: Color
                    val badgeBg: Color

                    when {
                        bill.isPaid -> {
                            badgeText = "Paid"
                            badgeColor = Color(0xFF4ADE80)
                            badgeBg = Color(0xFF4ADE80).copy(alpha = 0.15f)
                        }
                        bill.dueDate < today -> {
                            badgeText = "Overdue"
                            badgeColor = Color(0xFFF87171)
                            badgeBg = Color(0xFFF87171).copy(alpha = 0.15f)
                        }
                        else -> {
                            val daysRemaining = ((bill.dueDate - today) / 86400000).toInt()
                            when {
                                daysRemaining == 0 -> {
                                    badgeText = "Due Today"
                                    badgeColor = Color(0xFFFB923C)
                                    badgeBg = Color(0xFFFB923C).copy(alpha = 0.15f)
                                }
                                daysRemaining == 1 -> {
                                    badgeText = "Due Tomorrow"
                                    badgeColor = Color(0xFFFBBF24)
                                    badgeBg = Color(0xFFFBBF24).copy(alpha = 0.15f)
                                }
                                else -> {
                                    badgeText = "Due in $daysRemaining Days"
                                    badgeColor = BrandLime
                                    badgeBg = BrandLime.copy(alpha = 0.15f)
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text("Due: ${dateFormatter.format(Date(bill.dueDate))}", color = Color.Gray, fontSize = 11.sp)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${currencySymbol}${String.format("%.0f", bill.amount)}", color = Color.White, fontWeight = FontWeight.ExtraBold)
            IconButton(onClick = { onSimulateAlert(bill) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.NotificationsActive, "Ping alert", tint = BrandLime, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = { onDeleteBill(bill) }, modifier = Modifier.size(28.dp).testTag("delete_bill_bin_${bill.id}")) {
                Icon(Icons.Default.Delete, "Delete reminder", tint = Color(0xFFF43F5E), modifier = Modifier.size(16.dp))
            }
        }
    }
}
