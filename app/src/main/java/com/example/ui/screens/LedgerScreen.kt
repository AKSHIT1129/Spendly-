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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Member
import com.example.data.model.Transaction
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LedgerScreen(
    transactions: List<Transaction>,
    members: List<Member>,
    onAddClick: () -> Unit,
    onDeleteClick: (Transaction) -> Unit,
    currencySymbol: String = "$"
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LedgerListSubScreen(
            transactions = transactions,
            members = members,
            onDeleteClick = onDeleteClick,
            currencySymbol = currencySymbol
        )

        FloatingActionButton(
            onClick = onAddClick,
            containerColor = Color.White,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 110.dp, end = 16.dp)
                .testTag("add_transaction_fab")
        ) {
            Icon(Icons.Default.Add, "Add Transaction")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LedgerListSubScreen(
    transactions: List<Transaction>,
    members: List<Member>,
    onDeleteClick: (Transaction) -> Unit,
    currencySymbol: String = "$"
) {
    if (transactions.isEmpty()) {
        CardEmptyPlaceholder(
            title = "Awaiting financial stats",
            subtitle = "Settle custom revenue or spend entries to start real-time analytics aggregation."
        )
        return
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(transactions) { tx ->
            val isExpense = tx.amount < 0
            val author = members.find { it.id == tx.memberId }?.name ?: "Guest"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x14FFFFFF))
                    .border(1.dp, Color(0x19FFFFFF), RoundedCornerShape(20.dp))
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { onDeleteClick(tx) }
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isExpense) Color(0x2BFF5252) else Color(0x2B10B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(tx.category),
                            contentDescription = tx.category,
                            tint = if (isExpense) Color(0xFFF87171) else Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.widthIn(max = 160.dp)) {
                        Text(
                            text = tx.description.ifEmpty { tx.category },
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "by $author • ${tx.category}",
                                color = Color(0xFFA1A1AA),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isExpense && (tx.isEssential || tx.category.lowercase() in listOf("rent", "food", "groceries", "utilities", "bills", "healthcare", "medical", "insurance", "transport", "emi", "education"))) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x2B38BDF8))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "Need",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isExpense) {
                                "-${currencySymbol}${String.format("%,.2f", -tx.amount)}"
                            } else {
                                "+${currencySymbol}${String.format("%,.2f", tx.amount)}"
                            },
                            color = if (isExpense) Color(0xFFF87171) else Color(0xFF34D399),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateFormatter.format(Date(tx.date)),
                            color = Color(0x7FFFFFFF),
                            fontSize = 10.sp
                        )
                    }
                    IconButton(
                        onClick = { onDeleteClick(tx) },
                        modifier = Modifier.size(28.dp).testTag("delete_transaction_bin_${tx.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete entry",
                            tint = Color(0xFFF43F5E),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
