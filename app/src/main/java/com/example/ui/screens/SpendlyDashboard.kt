package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

import java.util.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.AppNotification
import com.example.ui.dialogs.*
import com.example.ui.graphics.*
import java.text.SimpleDateFormat

// Theme adaptor to automatically route old design lime to the new Fintech mockup neon lime (#DDF247)
private fun Color(color: Long): Color {
    val targetColor = if (color == 0xFFE2F163L) 0xFFDDF247L else color
    return androidx.compose.ui.graphics.Color(targetColor)
}

private val BrandLime = androidx.compose.ui.graphics.Color(0xFFDDF247)
private val TranslucentCardBg = androidx.compose.ui.graphics.Color(0x3D1F222C)
private val TranslucentCardBorder = androidx.compose.ui.graphics.Color(0x19FFFFFF)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SpendlyDashboard(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val members by viewModel.members.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val savingGoals by viewModel.savingGoals.collectAsStateWithLifecycle()
    val billReminders by viewModel.billReminders.collectAsStateWithLifecycle()

    val selectedMemberId by viewModel.selectedMemberId.collectAsStateWithLifecycle()
    val inAppNotification by viewModel.notification.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val notificationsList by viewModel.notificationsList.collectAsStateWithLifecycle()
    var showNotificationsDialog by remember { mutableStateOf(false) }

    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val currencyRate by viewModel.currencyRate.collectAsStateWithLifecycle()

    // Screen display triggers
    val onboardingFromViewModel by viewModel.showOnboarding.collectAsStateWithLifecycle()
    var showOnboarding by remember(onboardingFromViewModel) { mutableStateOf(onboardingFromViewModel) }
    var showProfileSetupDialog by remember { mutableStateOf(false) }
    var showExchangeScreen by remember { mutableStateOf(false) } // Screen 3
    var activeCategoryIndex by remember { mutableIntStateOf(0) } // Category Slider: 0: Recent Transactions, 1: Active Budgets, 2: Goals Vault, 3: Upcoming Bills
    var activeBottomDockTab by remember { mutableIntStateOf(0) } // Dock: 0: Home, 1: Transactions scroll, 2: Monthly chart analysis, 3: Profile list

    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showAddTxDialog by remember { mutableStateOf(false) }
    var showSendMoneyDialog by remember { mutableStateOf(false) }
    var txDialogTypeIsExpense by remember { mutableStateOf(true) }
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddBillDialog by remember { mutableStateOf(false) }
    var showDepositGoalDialog by remember { mutableStateOf<SavingGoal?>(null) }
    var showMemberProfileManageDialog by remember { mutableStateOf<Member?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val currentMember = remember(members, selectedMemberId) {
        members.find { it.id == selectedMemberId }
    }

    // Dynamic currency support elements
    val convertedTransactions = remember(transactions, currencyRate) {
        transactions.map { it.copy(amount = it.amount * currencyRate) }
    }
    val convertedBudgets = remember(budgets, currencyRate) {
        budgets.map { it.copy(monthlyLimit = it.monthlyLimit * currencyRate) }
    }
    val convertedSavingGoals = remember(savingGoals, currencyRate) {
        savingGoals.map { it.copy(
            currentAmount = it.currentAmount * currencyRate,
            targetAmount = it.targetAmount * currencyRate
        ) }
    }
    val convertedBillReminders = remember(billReminders, currencyRate) {
        billReminders.map { it.copy(amount = it.amount * currencyRate) }
    }

    // Filter transactions list
    val filteredTransactions = remember(convertedTransactions, selectedMemberId, searchQuery) {
        val baseList = if (selectedMemberId == null) {
            convertedTransactions
        } else {
            convertedTransactions.filter { it.memberId == selectedMemberId }
        }
        if (searchQuery.isEmpty()) {
            baseList
        } else {
            baseList.filter {
                it.description.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val filteredBudgets = remember(convertedBudgets, searchQuery) {
        if (searchQuery.isEmpty()) {
            convertedBudgets
        } else {
            convertedBudgets.filter { it.category.contains(searchQuery, ignoreCase = true) }
        }
    }

    val filteredSavingGoals = remember(convertedSavingGoals, searchQuery) {
        if (searchQuery.isEmpty()) {
            convertedSavingGoals
        } else {
            convertedSavingGoals.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    val filteredBillReminders = remember(convertedBillReminders, searchQuery) {
        if (searchQuery.isEmpty()) {
            convertedBillReminders
        } else {
            convertedBillReminders.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val cosmicBgModifier = Modifier
        .fillMaxSize()
        .drawBehind {
            // Background Base: Deep pitch black `#09080E`
            drawRect(color = Color(0xFF09080E))

            if (size.width > 0f) {
                // Top-Left Ambient Glow: Cosmic indigo/violet `#2C1E3D` with 55% opacity
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF2C1E3D).copy(alpha = 0.55f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(0f, 0f),
                        radius = size.width * 1.2f
                    ),
                    radius = size.width * 1.2f,
                    center = androidx.compose.ui.geometry.Offset(0f, 0f)
                )

                // Top-Right Ambient Glow: Warm sunset orange / amber copper `#C06240` with 35% opacity
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFC06240).copy(alpha = 0.35f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width, 0f),
                        radius = size.width * 1.2f
                    ),
                    radius = size.width * 1.2f,
                    center = androidx.compose.ui.geometry.Offset(size.width, 0f)
                )
            }
        }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = showOnboarding,
            transitionSpec = {
                (slideInHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)) + fadeIn()) togetherWith
                        (slideOutHorizontally() + fadeOut())
            },
            label = "applet_onboarding"
        ) { onboardingActive ->
            if (onboardingActive) {
                OnboardingScreen(
                    onGetStarted = {
                        showProfileSetupDialog = true
                    }
                )
            } else if (showExchangeScreen) {
                ExchangeScreen(
                    members = members,
                    currencySymbol = currencySymbol,
                    onBack = { showExchangeScreen = false },
                    onExchangeCompleted = { fromId, toId, amt ->
                        val fromName = members.find { it.id == fromId }?.name ?: "Source"
                        val toName = members.find { it.id == toId }?.name ?: "Target"

                        viewModel.addTransaction(-amt, "Transfer", "Transferred to $toName", fromId, false)
                        viewModel.addTransaction(amt, "Transfer", "Received from $fromName", toId, false)
                        viewModel.showInAppNotification("Transferred $currencySymbol${String.format("%,.0f", amt)} successfully!")
                        showExchangeScreen = false
                    }
                )
            } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    bottomBar = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Transparent)
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .padding(start = 24.dp, end = 24.dp, bottom = 20.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(76.dp)
                                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(26.dp), clip = false),
                                shape = RoundedCornerShape(26.dp),
                                color = Color(0xCA1F222C), // Translucent dark grey frosted style
                                border = BorderStroke(1.dp, Color(0x19FFFFFF)), // Subtle 1dp border
                                tonalElevation = 8.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FloatingDockItem(
                                        icon = Icons.Default.Home,
                                        contentDescription = "Home",
                                        isSelected = activeBottomDockTab == 0,
                                        onSelect = {
                                            activeBottomDockTab = 0
                                            activeCategoryIndex = 0
                                        }
                                    )
                                    FloatingDockItem(
                                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = "Transactions",
                                        isSelected = activeBottomDockTab == 1,
                                        onSelect = {
                                            activeBottomDockTab = 1
                                            activeCategoryIndex = 0
                                        }
                                    )
                                    FloatingDockItem(
                                        icon = Icons.Default.Category,
                                        contentDescription = "Budgets",
                                        isSelected = activeBottomDockTab == 2,
                                        onSelect = {
                                            activeBottomDockTab = 2
                                        }
                                    )
                                    FloatingDockItem(
                                        icon = Icons.Default.BarChart,
                                        contentDescription = "Charts",
                                        isSelected = activeBottomDockTab == 3,
                                        onSelect = { activeBottomDockTab = 3 }
                                    )
                                    FloatingDockItem(
                                        icon = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        isSelected = activeBottomDockTab == 4,
                                        onSelect = { activeBottomDockTab = 4 }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = cosmicBgModifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        AnimatedContent(
                            targetState = activeBottomDockTab,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                            },
                            label = "dock_tab_routing"
                        ) { dockTab ->
                            when (dockTab) {
                                1 -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(top = 16.dp, start = 20.dp, end = 20.dp)
                                    ) {
                                        Text("Wallet Transactions", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.height(14.dp))
                                        LedgerListSubScreen(
                                            transactions = filteredTransactions,
                                            members = members,
                                            onDeleteClick = { viewModel.deleteTransaction(it) },
                                            currencySymbol = currencySymbol
                                        )
                                    }
                                }
                                2 -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(top = 16.dp, start = 20.dp, end = 20.dp)
                                    ) {
                                        Text("Budgets & Savings", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.height(14.dp))
                                        BudgetsAndGoalsListSubScreen(
                                            transactions = convertedTransactions,
                                            budgets = filteredBudgets,
                                            goals = filteredSavingGoals,
                                            onAddBudgetClick = { showAddBudgetDialog = true },
                                            onAddGoalClick = { showAddGoalDialog = true },
                                            onDepositGoalClick = { showDepositGoalDialog = it },
                                            onDeleteBudget = { viewModel.deleteBudget(it) },
                                            onDeleteGoal = { viewModel.deleteSavingGoal(it) },
                                            currencySymbol = currencySymbol
                                        )
                                    }
                                }
                                3 -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(top = 16.dp, start = 20.dp, end = 20.dp)
                                    ) {
                                        Text("Financial Growth", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.height(14.dp))
                                        AnalyticsScreen(
                                            transactions = filteredTransactions,
                                            members = members,
                                            budgets = convertedBudgets,
                                            currencySymbol = currencySymbol
                                        )
                                    }
                                }
                                4 -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                            .padding(top = 16.dp, start = 20.dp, end = 20.dp)
                                    ) {
                                        Text("Profiles & Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            item {
                                                MemberAvatarItem(
                                                    displayName = "All Vaults",
                                                    initials = "ALL",
                                                    signatureColor = Color(0xFFE2F163),
                                                    isSelected = selectedMemberId == null,
                                                    onSelect = { viewModel.selectMember(null) },
                                                    onManage = {},
                                                    isAllShared = true
                                                )
                                            }
                                            items(members) { item ->
                                                val parsedColor = parseHexColor(item.colorHex)
                                                val initials = item.name.trim().split(" ")
                                                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                                    .take(2)
                                                    .joinToString("")
                                                MemberAvatarItem(
                                                    displayName = item.name,
                                                    initials = initials.ifEmpty { "U" },
                                                    signatureColor = parsedColor,
                                                    isSelected = selectedMemberId == item.id,
                                                    onSelect = { viewModel.selectMember(item.id) },
                                                    onManage = { showMemberProfileManageDialog = item }
                                                )
                                            }
                                            item {
                                                IconButton(
                                                    onClick = { showAddMemberDialog = true },
                                                    modifier = Modifier
                                                        .size(62.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0x33FFFFFF))
                                                ) {
                                                    Icon(Icons.Default.Add, "Add Member", tint = Color.White)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))
                                        Card(
                                             modifier = Modifier
                                                 .fillMaxWidth()
                                                 .testTag("preferences_card")
                                                 .padding(vertical = 12.dp),
                                             shape = RoundedCornerShape(16.dp),
                                             colors = CardDefaults.cardColors(
                                                 containerColor = Color(0x1AFFFFFF)
                                             ),
                                             border = BorderStroke(1.dp, Color(0x22FFFFFF))
                                         ) {
                                             Column(
                                                 modifier = Modifier
                                                     .fillMaxWidth()
                                                     .padding(16.dp)
                                             ) {
                                                 Row(
                                                     modifier = Modifier.fillMaxWidth(),
                                                     verticalAlignment = Alignment.CenterVertically
                                                 ) {
                                                     Icon(
                                                         imageVector = Icons.Default.Settings,
                                                         contentDescription = "Preferences",
                                                         tint = Color(0xFFE2F163),
                                                         modifier = Modifier.size(20.dp)
                                                     )
                                                     Spacer(modifier = Modifier.width(8.dp))
                                                     Text(
                                                         text = "System Preferences",
                                                         fontSize = 16.sp,
                                                         fontWeight = FontWeight.Bold,
                                                         color = Color.White
                                                     )
                                                 }
                                                 Spacer(modifier = Modifier.height(16.dp))
                                                 HorizontalDivider(color = Color(0x19FFFFFF))
                                                 Spacer(modifier = Modifier.height(16.dp))

                                                 Row(
                                                     modifier = Modifier.fillMaxWidth(),
                                                     horizontalArrangement = Arrangement.SpaceBetween,
                                                     verticalAlignment = Alignment.CenterVertically
                                                 ) {
                                                     Column(modifier = Modifier.weight(1f)) {
                                                         Text(
                                                             text = "Onboarding Walkthrough",
                                                             fontSize = 14.sp,
                                                             fontWeight = FontWeight.Medium,
                                                             color = Color.White
                                                         )
                                                         Text(
                                                             text = "Replay the classy walkthrough sequence",
                                                             fontSize = 11.sp,
                                                             color = Color.LightGray
                                                         )
                                                     }

                                                     Button(
                                                         onClick = { showOnboarding = true },
                                                         colors = ButtonDefaults.buttonColors(
                                                             containerColor = Color(0xFFDDF247),
                                                             contentColor = Color.Black
                                                         ),
                                                         shape = RoundedCornerShape(8.dp),
                                                         contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                         modifier = Modifier.height(32.dp).testTag("replay_onboarding_trigger")
                                                     ) {
                                                         Text("Replay", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                     }
                                                 }

                                                 Spacer(modifier = Modifier.height(16.dp))
                                                 HorizontalDivider(color = Color(0x19FFFFFF))
                                                 Spacer(modifier = Modifier.height(12.dp))
                                                 Row(
                                                     modifier = Modifier.fillMaxWidth(),
                                                     horizontalArrangement = Arrangement.SpaceBetween,
                                                     verticalAlignment = Alignment.CenterVertically
                                                 ) {
                                                     Column {
                                                         Text(
                                                             text = "Active Currency",
                                                             fontSize = 14.sp,
                                                             fontWeight = FontWeight.Medium,
                                                             color = Color.White
                                                         )
                                                         Text(
                                                             text = "Convert balances and limits dynamically",
                                                             fontSize = 11.sp,
                                                             color = Color.LightGray
                                                         )
                                                     }

                                                     val currentCurrency by viewModel.currency.collectAsStateWithLifecycle()
                                                     var showDropdown by remember { mutableStateOf(false) }

                                                     Box {
                                                         Row(
                                                             modifier = Modifier
                                                                 .clip(RoundedCornerShape(8.dp))
                                                                 .background(Color(0x22FFFFFF))
                                                                 .clickable { showDropdown = true }
                                                                 .padding(horizontal = 12.dp, vertical = 6.dp)
                                                                 .testTag("currency_selector_trigger"),
                                                             verticalAlignment = Alignment.CenterVertically,
                                                             horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                         ) {
                                                             val displayIcon = when (currentCurrency) {
                                                                 "INR" -> "₹"
                                                                 "USD" -> "$"
                                                                 "EUR" -> "€"
                                                                 else -> "₹"
                                                             }
                                                             Text(
                                                                 text = "$displayIcon $currentCurrency",
                                                                 fontSize = 14.sp,
                                                                 fontWeight = FontWeight.Bold,
                                                                 color = Color(0xFFE2F163)
                                                             )
                                                             Icon(
                                                                 imageVector = Icons.Default.ArrowDropDown,
                                                                 contentDescription = "Open selector",
                                                                 tint = Color.White,
                                                                 modifier = Modifier.size(18.dp)
                                                             )
                                                         }

                                                         DropdownMenu(
                                                             expanded = showDropdown,
                                                             onDismissRequest = { showDropdown = false },
                                                             modifier = Modifier.background(Color(0xFF1E1E1E))
                                                         ) {
                                                             DropdownMenuItem(
                                                                 text = { Text("INR (Rupees ₹)", color = Color.White) },
                                                                 onClick = {
                                                                     viewModel.setCurrency("INR")
                                                                     showDropdown = false
                                                                 }
                                                             )
                                                             DropdownMenuItem(
                                                                 text = { Text("USD (Dollars $)", color = Color.White) },
                                                                 onClick = {
                                                                     viewModel.setCurrency("USD")
                                                                     showDropdown = false
                                                                 }
                                                             )
                                                             DropdownMenuItem(
                                                                 text = { Text("EUR (Euros €)", color = Color.White) },
                                                                 onClick = {
                                                                     viewModel.setCurrency("EUR")
                                                                     showDropdown = false
                                                                 }
                                                             )
                                                         }
                                                     }
                                                 }
                                             }
                                         }

                                         Spacer(modifier = Modifier.height(12.dp))
                                         Row(
                                             modifier = Modifier.fillMaxWidth(),
                                             horizontalArrangement = Arrangement.SpaceBetween,
                                             verticalAlignment = Alignment.CenterVertically
                                         ) {
                                             Text("Bill Reminders", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                             IconButton(
                                                 onClick = { showAddBillDialog = true },
                                                 modifier = Modifier.size(36.dp).testTag("add_bill_settings_icon")
                                             ) {
                                                 Icon(Icons.Default.Add, "Add Bill", tint = Color.White)
                                             }
                                         }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        BillsScreen(
                                            billReminders = convertedBillReminders, isEmbedded = true,
                                            onAddBillClick = { showAddBillDialog = true },
                                            onTogglePaid = { viewModel.toggleBillPaid(it) },
                                            onSimulateAlert = { viewModel.simulateBillNotification(it) },
                                            onDeleteBill = { viewModel.deleteBillReminder(it) },
                                            currencySymbol = currencySymbol
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        // Danger Zone / Clear All Card
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("danger_zone_card")
                                                .padding(vertical = 12.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color(0x22FF5F5F)
                                            ),
                                            border = BorderStroke(1.dp, Color(0x44FF5F5F))
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Danger Zone",
                                                        tint = Color(0xFFF87171),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Danger Zone",
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFF87171)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = "Permanently and irreversibly delete all transactions, budgets, saving vault targets, and upcoming bill reminders.",
                                                    fontSize = 12.sp,
                                                    color = Color.LightGray
                                                )
                                                Spacer(modifier = Modifier.height(14.dp))

                                                var showWipeConfirmDialog by remember { mutableStateOf(false) }
                                                Button(
                                                    onClick = { showWipeConfirmDialog = true },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFFEF4444),
                                                        contentColor = Color.White
                                                     ),
                                                     shape = RoundedCornerShape(10.dp),
                                                     modifier = Modifier.fillMaxWidth().testTag("clear_all_data_button")
                                                 ) {
                                                     Icon(Icons.Default.Delete, "Wipe", modifier = Modifier.size(16.dp))
                                                     Spacer(modifier = Modifier.width(8.dp))
                                                     Text("Clear All Data", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                 }

                                                 if (showWipeConfirmDialog) {
                                                     AlertDialog(
                                                         onDismissRequest = { showWipeConfirmDialog = false },
                                                         title = { Text("Reset Finances Application?", color = Color.White, fontWeight = FontWeight.Bold) },
                                                         text = { Text("Are you sure you want to clear all history records? This operation is permanent and cannot be undone.", color = Color.LightGray) },
                                                         containerColor = Color(0xFF1E1E1E),
                                                         confirmButton = {
                                                             TextButton(
                                                                 onClick = {
                                                                     viewModel.clearAllData()
                                                                     showWipeConfirmDialog = false
                                                                 }
                                                             ) {
                                                                 Text("RESET ALL", color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                                                             }
                                                         },
                                                         dismissButton = {
                                                             TextButton(onClick = { showWipeConfirmDialog = false }) {
                                                                 Text("CANCEL", color = Color.White)
                                                             }
                                                         }
                                                     )
                                                 }
                                             }
                                         }
                                    }
                                }
                                else -> {
                                    // Dock Tab 0: Core mockup screen 2 (Home Dashboard)
                                    val today = remember {
                                        java.util.Calendar.getInstance().apply {
                                            set(java.util.Calendar.HOUR_OF_DAY, 0)
                                            set(java.util.Calendar.MINUTE, 0)
                                            set(java.util.Calendar.SECOND, 0)
                                            set(java.util.Calendar.MILLISECOND, 0)
                                        }.timeInMillis
                                    }
                                    val threeDaysFromNow = today + 3 * 24 * 60 * 60 * 1000L
                                    val upcomingBillsNext3Days = remember(convertedBillReminders, today) {
                                        convertedBillReminders.filter { 
                                            !it.isPaid && it.dueDate <= threeDaysFromNow
                                        }
                                    }
                                    
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(top = 14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                val greetName = currentMember?.name ?: "Akshit"
                                                Text(
                                                    text = "Hello, $greetName",
                                                    fontSize = 24.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    letterSpacing = (-0.5).sp
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                ) {
                                                    Text(
                                                        text = "Welcome to Spendly Space",
                                                        fontSize = 13.sp,
                                                        color = Color(0xFFA1A1AA)
                                                    )
                                                    Text(
                                                        text = "•",
                                                        fontSize = 12.sp,
                                                        color = Color(0x4DFFFFFF)
                                                    )
                                                    Text(
                                                        text = "Built by Akshit",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFFE2F163)
                                                    )
                                                }
                                            }

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(
                                                    onClick = { isSearchActive = !isSearchActive },
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0x2BFFFFFF))
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Search,
                                                        contentDescription = "Search",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                Box {
                                                    IconButton(
                                                        onClick = {
                                                            showNotificationsDialog = true
                                                        },
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0x2BFFFFFF))
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Notifications,
                                                            contentDescription = "Alerts",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(if (notificationsList.isNotEmpty()) Color(0xFFE2F163) else Color.Transparent)
                                                            .align(Alignment.TopEnd)
                                                            .offset(x = (-2).dp, y = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        AnimatedVisibility(
                                            visible = isSearchActive,
                                            enter = slideInVertically() + fadeIn(),
                                            exit = slideOutVertically() + fadeOut()
                                        ) {
                                            OutlinedTextField(
                                                value = searchQuery,
                                                onValueChange = { searchQuery = it },
                                                placeholder = { Text("Filter logs...", color = Color.Gray, fontSize = 13.sp) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                                                    .height(52.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = TranslucentCardBg,
                                                    unfocusedContainerColor = TranslucentCardBg,
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White,
                                                    focusedBorderColor = BrandLime,
                                                    unfocusedBorderColor = TranslucentCardBorder
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                                                singleLine = true
                                            )
                                        }

                                        if (isSearchActive && searchQuery.isNotEmpty()) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 22.dp, vertical = 12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Search Results",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                TextButton(
                                                    onClick = { searchQuery = "" }
                                                ) {
                                                    Text(
                                                        text = "Clear",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = BrandLime
                                                    )
                                                }
                                            }

                                            val hasNoMatches = filteredTransactions.isEmpty() &&
                                                    filteredBudgets.isEmpty() &&
                                                    filteredSavingGoals.isEmpty() &&
                                                    filteredBillReminders.isEmpty()

                                            if (hasNoMatches) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(32.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Icon(
                                                            imageVector = Icons.Default.Search,
                                                            contentDescription = null,
                                                            tint = Color.Gray,
                                                            modifier = Modifier.size(48.dp)
                                                         )
                                                        Spacer(modifier = Modifier.height(16.dp))
                                                        Text(
                                                            text = "No matches found for \"$searchQuery\"",
                                                            color = Color.LightGray,
                                                            textAlign = TextAlign.Center,
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                }
                                            } else {
                                                LazyColumn(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(horizontal = 20.dp),
                                                    verticalArrangement = Arrangement.spacedBy(11.dp),
                                                    contentPadding = PaddingValues(bottom = 120.dp)
                                                ) {
                                                    if (filteredTransactions.isNotEmpty()) {
                                                        item {
                                                            Text(
                                                                text = "Transactions (${filteredTransactions.size})",
                                                                color = BrandLime,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(vertical = 6.dp)
                                                            )
                                                        }
                                                        items(filteredTransactions) { tx ->
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
                                                                        onLongClick = { viewModel.deleteTransaction(tx) }
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
                                                                        Text(
                                                                            text = "by $author • ${tx.category}",
                                                                            color = Color(0xFFA1A1AA),
                                                                            fontSize = 11.sp,
                                                                            maxLines = 1,
                                                                            overflow = TextOverflow.Ellipsis
                                                                        )
                                                                    }
                                                                }
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
                                                            }
                                                        }
                                                    }

                                                    if (filteredBudgets.isNotEmpty()) {
                                                        item {
                                                            Text(
                                                                text = "Category Budgets (${filteredBudgets.size})",
                                                                color = BrandLime,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(vertical = 6.dp)
                                                            )
                                                        }
                                                        items(filteredBudgets) { budget ->
                                                            val spent = remember(convertedTransactions, budget) {
                                                                val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                                                                convertedTransactions
                                                                    .filter { 
                                                                        it.category.equals(budget.category, ignoreCase = true) && 
                                                                        it.amount < 0 && 
                                                                        sdf.format(Date(it.date)) == budget.monthYear
                                                                    }
                                                                    .sumOf { -it.amount }
                                                            }
                                                            val ratio = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit).toFloat().coerceIn(0f, 1f) else 0f
                                                            val color = if (ratio > 0.85f) Color(0xFFF87171) else BrandLime

                                                            Card(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .combinedClickable(onClick = {}, onLongClick = { viewModel.deleteBudget(budget) }),
                                                                shape = RoundedCornerShape(20.dp),
                                                                colors = CardDefaults.cardColors(containerColor = TranslucentCardBg),
                                                                border = BorderStroke(1.dp, TranslucentCardBorder)
                                                            ) {
                                                                Column(modifier = Modifier.padding(16.dp)) {
                                                                    Row(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        Text(budget.category, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                                                        Text("${currencySymbol}${String.format("%.0f", spent)} / ${currencySymbol}${String.format("%.0f", budget.monthlyLimit)}", color = Color.White, fontSize = 12.sp)
                                                                    }
                                                                    Spacer(modifier = Modifier.height(10.dp))
                                                                    LinearProgressIndicator(
                                                                        progress = ratio,
                                                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                                                        color = color,
                                                                        trackColor = Color(0x33FFFFFF)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }

                                                    if (filteredSavingGoals.isNotEmpty()) {
                                                        item {
                                                             Text(
                                                                text = "Saving Goals (${filteredSavingGoals.size})",
                                                                color = BrandLime,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(vertical = 6.dp)
                                                            )
                                                        }
                                                        items(filteredSavingGoals) { goal ->
                                                            val ratio = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                                                            Card(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .combinedClickable(
                                                                        onClick = { showDepositGoalDialog = goal },
                                                                        onLongClick = { viewModel.deleteSavingGoal(goal) }
                                                                    ),
                                                                shape = RoundedCornerShape(20.dp),
                                                                colors = CardDefaults.cardColors(containerColor = TranslucentCardBg),
                                                                border = BorderStroke(1.dp, TranslucentCardBorder)
                                                            ) {
                                                                Column(modifier = Modifier.padding(16.dp)) {
                                                                    Row(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        Column(modifier = Modifier.weight(1f)) {
                                                                            Text(goal.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                                            Spacer(modifier = Modifier.height(2.dp))
                                                                            Text("Target Date: ${goal.targetDate}", color = Color(0xFFA1A1AA), fontSize = 11.sp)
                                                                        }
                                                                        Text("${currencySymbol}${String.format("%.0f", goal.currentAmount)} / ${currencySymbol}${String.format("%.0f", goal.targetAmount)}", color = Color.White, fontSize = 12.sp)
                                                                    }
                                                                    Spacer(modifier = Modifier.height(10.dp))
                                                                    LinearProgressIndicator(
                                                                        progress = ratio,
                                                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                                                        color = Color(0xFF10B981),
                                                                        trackColor = Color(0x33FFFFFF)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }

                                                    if (filteredBillReminders.isNotEmpty()) {
                                                        item {
                                                            Text(
                                                                text = "Upcoming Bills (${filteredBillReminders.size})",
                                                                color = BrandLime,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(vertical = 6.dp)
                                                            )
                                                        }
                                                        items(filteredBillReminders) { bill ->
                                                            BillReminderRow(
                                                                bill = bill,
                                                                today = today,
                                                                currencySymbol = currencySymbol,
                                                                onTogglePaid = { viewModel.toggleBillPaid(it) },
                                                                onSimulateAlert = { viewModel.simulateBillNotification(it) },
                                                                onDeleteBill = { viewModel.deleteBillReminder(it) }
                                                            )
                                                        }
                                                     }
                                                }
                                            }
                                        } else {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .verticalScroll(rememberScrollState())
                                            ) {
                                                Spacer(modifier = Modifier.height(18.dp))

                                        // Total funds across ALL profiles card as requested
                                        val totalSumAllProfiles = convertedTransactions.sumOf { it.amount }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp)
                                                .shadow(elevation = 0.dp, shape = RoundedCornerShape(26.dp), clip = false)
                                                .clip(RoundedCornerShape(26.dp))
                                                .background(Color(0x3D1F222C))
                                                .border(1.dp, Color(0x19FFFFFF), RoundedCornerShape(26.dp))
                                                .padding(horizontal = 24.dp, vertical = 28.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                                val cardLabel = "Total Funds (All Profiles)"
                                                Text(
                                                    text = cardLabel,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFFA1A1AA),
                                                    fontWeight = FontWeight.Medium,
                                                    letterSpacing = 0.5.sp
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = if (totalSumAllProfiles >= 0) {
                                                        "${currencySymbol}${String.format("%,.2f", totalSumAllProfiles)}"
                                                    } else {
                                                        "-${currencySymbol}${String.format("%,.2f", -totalSumAllProfiles)}"
                                                    },
                                                    fontSize = 44.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White,
                                                    letterSpacing = (-1.2).sp
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))

                                                val surplusLabel = if (totalSumAllProfiles >= 0) "+5.03%" else "-2.45%"
                                                Text(
                                                    text = surplusLabel,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (totalSumAllProfiles >= 0) BrandLime else Color(0xFFF87171),
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(if (totalSumAllProfiles >= 0) BrandLime.copy(alpha = 0.13f) else Color(0xFFF87171).copy(alpha = 0.13f))
                                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        // Quick shortcut buttons to "Add Transaction" or "Send Money"
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Add Transaction Button
                                            Button(
                                                onClick = {
                                                    txDialogTypeIsExpense = false
                                                    showAddTxDialog = true
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(52.dp)
                                                    .testTag("add_transaction_shortcut_btn"),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = BrandLime,
                                                    contentColor = Color.Black
                                                ),
                                                shape = RoundedCornerShape(16.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Add Transaction", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // Send Money Button
                                            Button(
                                                onClick = {
                                                    showSendMoneyDialog = true
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(52.dp)
                                                    .testTag("send_money_shortcut_btn"),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = TranslucentCardBg,
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(16.dp),
                                                border = BorderStroke(1.dp, TranslucentCardBorder)
                                            ) {
                                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Send Money", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        // Spending Donut Chart Preview Section
                                        val spendsByCategory = remember(convertedTransactions) {
                                            convertedTransactions
                                                .filter { it.amount < 0 }
                                                .groupBy { it.category }
                                                .mapValues { entry -> entry.value.sumOf { -it.amount } }
                                        }
                                        
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp),
                                            shape = RoundedCornerShape(26.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0x3D1F222C)),
                                            border = BorderStroke(1.dp, Color(0x19FFFFFF))
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                    text = "Spending Categorially",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.height(14.dp))
                                                
                                                if (spendsByCategory.isEmpty()) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(100.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("No spending logged this month yet.", color = Color.Gray, fontSize = 12.sp)
                                                    }
                                                } else {
                                                    val totalSpend = spendsByCategory.values.sum()
                                                    val premiumColors = listOf(
                                                        BrandLime, // Lime-yellow
                                                        Color(0xFF38BDF8), // Sky blue
                                                        Color(0xFFF472B6), // Pink
                                                        Color(0xFFFB923C), // Orange
                                                        Color(0xFF2DD4BF), // Teal/Turquoise
                                                        Color(0xFFC084FC), // Purple
                                                        Color(0xFFF87171)  // Sunset red
                                                    )
                                                    val categoryColors = remember(spendsByCategory) {
                                                        spendsByCategory.keys.toList().mapIndexed { index, cat ->
                                                            cat to premiumColors[index % premiumColors.size]
                                                        }.toMap()
                                                    }
                                                    
                                                     Row(
                                                         modifier = Modifier.fillMaxWidth(),
                                                         verticalAlignment = Alignment.CenterVertically
                                                     ) {
                                                         // Small Donut Canvas (Left)
                                                         Box(
                                                             modifier = Modifier.size(100.dp),
                                                             contentAlignment = Alignment.Center
                                                         ) {
                                                             Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                                                                 var currentStartAngle = -90f
                                                                 val spacing = if (spendsByCategory.size > 1) 4f else 0f
                                                                 val strokeWidthPx = 20f
                                                                 
                                                                 spendsByCategory.forEach { (cat, amt) ->
                                                                     val color = categoryColors[cat] ?: Color(0xFFE2F163)
                                                                     val spendRatio = if (totalSpend > 0) (amt / totalSpend).toFloat() else 0f
                                                                     val sweepAngle = spendRatio * 360f
                                                                     
                                                                     if (sweepAngle > 0f) {
                                                                         val adjustedSweep = if (sweepAngle > spacing) sweepAngle - spacing else sweepAngle
                                                                         drawArc(
                                                                             color = color,
                                                                             startAngle = currentStartAngle + (spacing / 2f),
                                                                             sweepAngle = adjustedSweep,
                                                                             useCenter = false,
                                                                             style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                                                         )
                                                                         currentStartAngle += sweepAngle
                                                                     }
                                                                 }
                                                             }
                                                             
                                                             // Central aggregate text
                                                             Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                 Text(
                                                                     text = "Spent",
                                                                     color = Color.LightGray,
                                                                     fontSize = 9.sp,
                                                                     fontWeight = FontWeight.Medium
                                                                 )
                                                                 Text(
                                                                     text = "${currencySymbol}${String.format("%.0f", totalSpend)}",
                                                                     color = Color.White,
                                                                     fontSize = 12.sp,
                                                                     fontWeight = FontWeight.Bold
                                                                 )
                                                             }
                                                         }
                                                         
                                                         Spacer(modifier = Modifier.width(20.dp))
                                                         
                                                         // Legends on the Right (up to top 3)
                                                         val topSpending = remember(spendsByCategory) {
                                                             spendsByCategory.toList().sortedByDescending { it.second }.take(3)
                                                         }
                                                         Column(
                                                             verticalArrangement = Arrangement.spacedBy(6.dp),
                                                             modifier = Modifier.weight(1f)
                                                         ) {
                                                             topSpending.forEach { (cat, amt) ->
                                                                 val color = categoryColors[cat] ?: Color(0xFFE2F163)
                                                                 val percent = if (totalSpend > 0) (amt / totalSpend).toFloat() else 0f
                                                                 Row(
                                                                     modifier = Modifier.fillMaxWidth(),
                                                                     horizontalArrangement = Arrangement.SpaceBetween,
                                                                     verticalAlignment = Alignment.CenterVertically
                                                                 ) {
                                                                     Row(verticalAlignment = Alignment.CenterVertically) {
                                                                         Box(
                                                                             modifier = Modifier
                                                                                 .size(8.dp)
                                                                                 .clip(CircleShape)
                                                                                 .background(color)
                                                                         )
                                                                         Spacer(modifier = Modifier.width(8.dp))
                                                                         Text(
                                                                             text = cat,
                                                                             color = Color.White,
                                                                             fontSize = 11.sp,
                                                                             fontWeight = FontWeight.Medium,
                                                                             maxLines = 1,
                                                                             overflow = TextOverflow.Ellipsis
                                                                         )
                                                                     }
                                                                     Text(
                                                                         text = "${currencySymbol}${String.format("%.0f", amt)} (${String.format("%.0f", percent * 100)}%)",
                                                                         color = Color.LightGray,
                                                                         fontSize = 11.sp,
                                                                         fontWeight = FontWeight.SemiBold
                                                                     )
                                                                 }
                                                             }
                                                         }
                                                     }
                                                 }
                                             }
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        // Upcoming Bills Section
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Bills Due Shortly (3 Days)",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }

                                            if (upcomingBillsNext3Days.isEmpty()) {
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = CardDefaults.cardColors(containerColor = TranslucentCardBg),
                                                    shape = RoundedCornerShape(16.dp),
                                                    border = BorderStroke(1.dp, TranslucentCardBorder)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(24.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Icon(
                                                                imageVector = Icons.Default.CheckCircle,
                                                                contentDescription = null,
                                                                tint = Color(0xFF34D399),
                                                                modifier = Modifier.size(24.dp)
                                                            )
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            Text(
                                                                text = "No bills due in the next 3 days 🎉",
                                                                color = Color.Gray,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        }
                                                    }
                                                }
                                            } else {
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    upcomingBillsNext3Days.forEach { bill ->
                                                        BillReminderRow(
                                                            bill = bill,
                                                            today = today,
                                                            currencySymbol = currencySymbol,
                                                            onTogglePaid = { viewModel.toggleBillPaid(it) },
                                                            onSimulateAlert = { viewModel.simulateBillNotification(it) },
                                                            onDeleteBill = { viewModel.deleteBillReminder(it) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(90.dp))

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                        ) {
                                            if (false) AnimatedContent(
                                                targetState = activeCategoryIndex,
                                                transitionSpec = {
                                                    (slideInVertically(initialOffsetY = { 80 }) + fadeIn()) togetherWith
                                                            (slideOutVertically(targetOffsetY = { -80 }) + fadeOut())
                                                },
                                                label = "active_content_transitions"
                                            ) { index ->
                                                when (index) {
                                                    1 -> {
                                                        BudgetsAndGoalsListSubScreen(
                                                            transactions = convertedTransactions,
                                                            budgets = filteredBudgets,
                                                            goals = emptyList(),
                                                            onAddBudgetClick = { showAddBudgetDialog = true },
                                                            onAddGoalClick = {},
                                                            onDepositGoalClick = {},
                                                            onDeleteBudget = { viewModel.deleteBudget(it) },
                                                            onDeleteGoal = {},
                                                            currencySymbol = currencySymbol
                                                        )
                                                    }
                                                    2 -> {
                                                        BudgetsAndGoalsListSubScreen(
                                                            transactions = convertedTransactions,
                                                            budgets = emptyList(),
                                                            goals = filteredSavingGoals,
                                                            onAddBudgetClick = {},
                                                            onAddGoalClick = { showAddGoalDialog = true },
                                                            onDepositGoalClick = { showDepositGoalDialog = it },
                                                            onDeleteBudget = {},
                                                            onDeleteGoal = { viewModel.deleteSavingGoal(it) },
                                                            currencySymbol = currencySymbol
                                                        )
                                                    }
                                                    3 -> {
                                                        BillsScreen(
                                                            billReminders = filteredBillReminders,
                                                            onAddBillClick = { showAddBillDialog = true },
                                                            onTogglePaid = { viewModel.toggleBillPaid(it) },
                                                            onSimulateAlert = { viewModel.simulateBillNotification(it) },
                                                            onDeleteBill = { viewModel.deleteBillReminder(it) },
                                                            currencySymbol = currencySymbol
                                                        )
                                                    }
                                                    else -> {
                                                        LedgerListSubScreen(
                                                            transactions = filteredTransactions,
                                                            members = members,
                                                            onDeleteClick = { viewModel.deleteTransaction(it) },
                                                            currencySymbol = currencySymbol
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(80.dp))
                                    }
                                }
                            }
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = inAppNotification != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(99f)
                .padding(16.dp)
        ) {
            inAppNotification?.let { msg ->
                NotificationToastBanner(msg) { viewModel.dismissNotification() }
            }
        }
    }

    if (showNotificationsDialog) {
        Dialog(onDismissRequest = { showNotificationsDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = OpaqueDialogBg),
                border = BorderStroke(1.dp, TranslucentCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notifications & Logs",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = { showNotificationsDialog = false }) {
                            Icon(Icons.Default.Close, "Close", tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (notificationsList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No active alerts or logs.",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(notificationsList) { alert ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = TranslucentCardBg),
                                    border = BorderStroke(1.dp, TranslucentCardBorder),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(BrandLime)
                                        )
                                        Text(
                                            text = alert.message,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.clearAllNotifications()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear All", color = Color(0xFFF87171))
                        }
                        Button(
                            onClick = { showNotificationsDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandLime),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Dismiss", color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    if (showProfileSetupDialog) {
        ProfileSetupDialog(
            onDismiss = {
                viewModel.completeOnboarding()
                showProfileSetupDialog = false
            },
            onSave = { name, targetGoalTitle, targetGoalAmount ->
                viewModel.setupPrimaryProfile(name, targetGoalTitle, targetGoalAmount)
                showProfileSetupDialog = false
            },
            onSkip = {
                viewModel.completeOnboarding()
                showProfileSetupDialog = false
            }
        )
    }

    if (showAddMemberDialog) {
        AddMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onSave = { name, role, col ->
                viewModel.addMember(name, col, role)
                showAddMemberDialog = false
            }
        )
    }

    if (showAddBudgetDialog) {
        AddBudgetDialog(
            onDismiss = { showAddBudgetDialog = false },
            onSave = { category, limit ->
                viewModel.addBudget(category, limit)
                showAddBudgetDialog = false
            },
            currencySymbol = currencySymbol
        )
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onSave = { title, target, current, date ->
                viewModel.addSavingGoal(title, target, current, date)
                showAddGoalDialog = false
            },
            currencySymbol = currencySymbol
        )
    }

    if (showAddTxDialog) {
        AddTransactionDialog(
            members = members,
            onDismiss = { showAddTxDialog = false },
            onSave = { amount, cat, desc, mId, shared, isExp, isEss ->
                val adjustedAmount = if (isExp) -kotlin.math.abs(amount) else kotlin.math.abs(amount)
                viewModel.addTransaction(adjustedAmount, cat, desc, mId, shared, isEss)
                showAddTxDialog = false
            },
            currencySymbol = currencySymbol,
            initialIsExpense = txDialogTypeIsExpense
        )
    }

    if (showSendMoneyDialog) {
        SendMoneyDialog(
            members = members,
            onDismiss = { showSendMoneyDialog = false },
            onSend = { amount, memberId ->
                val recipientName = members.find { it.id == memberId }?.name ?: "Member"
                viewModel.addTransaction(
                    amount = -amount,
                    category = "Transfer",
                    description = "Sent money to $recipientName",
                    memberId = memberId,
                    isShared = false
                )
                showSendMoneyDialog = false
            },
            currencySymbol = currencySymbol
        )
    }

    if (showAddBillDialog) {
        AddBillDialog(
            onDismiss = { showAddBillDialog = false },
            onSave = { title, amount, date, category ->
                viewModel.addBillReminder(title, amount, date, category)
                showAddBillDialog = false
            },
            currencySymbol = currencySymbol
        )
    }

    showDepositGoalDialog?.let { goal ->
        DepositGoalDialog(
            goal = goal,
            onDismiss = { showDepositGoalDialog = null },
            onDeposit = { money ->
                viewModel.updateSavingProgress(goal, money)
                showDepositGoalDialog = null
            },
            currencySymbol = currencySymbol
        )
    }

    showMemberProfileManageDialog?.let { member ->
        MemberProfileDetailDialog(
            member = member,
            transactionsCount = transactions.count { it.memberId == member.id },
            onDismiss = { showMemberProfileManageDialog = null },
            onDelete = {
                viewModel.deleteMember(member)
                showMemberProfileManageDialog = null
            }
        )
    }
}

@Composable
fun FloatingDockItem(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .clickable(onClick = onSelect),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color(0xFFA1A1AA),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DashboardCircleAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0x24FFFFFF))
            .border(1.dp, Color(0x1AFFFFFF), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow_walkthrough")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // UNIFIED SMOOTH ENTRY ANIMATIONS
    val titleAlpha = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(35f) }
    val descAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val springSpec = spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
        // High-performance quick reveal block
        launch { titleAlpha.animateTo(1f, tween(600, easing = EaseOutCubic)) }
        launch { titleOffset.animateTo(0f, springSpec) }
        
        delay(250)
        descAlpha.animateTo(1f, tween(800, easing = EaseInOutSine))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(color = Color(0xFF09080E)) // Pitch black background #09080E

                if (size.width > 0f) {
                    // Glowing sunset orange and deep violet ambient lights
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFC06240).copy(alpha = 0.16f * pulseGlow), Color.Transparent),
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.15f),
                            radius = size.width * 1.3f
                        ),
                        radius = size.width * 1.3f,
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.15f)
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.16f * (1.4f - pulseGlow)), Color.Transparent),
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.85f),
                            radius = size.width * 1.3f
                        ),
                        radius = size.width * 1.3f,
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.85f)
                    )
                }
            }
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Next-Gen status header chip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .graphicsLayer(
                            alpha = titleAlpha.value,
                            translationY = titleOffset.value
                        )
                        .background(Color(0xFF14131A), RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0x13FFFFFF), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFFE2F163), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SPENDLY QUANTUM ACCELERATOR ACTIVE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f),
                        letterSpacing = 1.3.sp
                    )
                }
            }

            // 3D floating graphic (featuring premium translucent live card + concentric rotating rings)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f),
                contentAlignment = Alignment.Center
            ) {
                FloatingGraphicCanvas()
            }

            // Typography and Swipe Control
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.1f)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // High-End Typography with Neon Accent Layout & NO FULL STOPS
                Column(
                    modifier = Modifier.graphicsLayer(
                        alpha = titleAlpha.value,
                        translationY = titleOffset.value
                    )
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Color.White)) {
                                append("Track   Budget\nSave   ")
                            }
                            withStyle(style = SpanStyle(color = Color(0xFFE2F163))) {
                                append("Together")
                            }
                        },
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        lineHeight = 46.sp,
                        letterSpacing = (-1).sp
                    )
                }

                Text(
                    text = "Keep track of daily transactions, active budgets, and savings targets in one beautiful space.",
                    fontSize = 15.sp,
                    color = Color(0xFFA1A1AA),
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .graphicsLayer(alpha = descAlpha.value)
                        .padding(top = 14.dp)
                )

                Spacer(modifier = Modifier.height(34.dp))

                // Premium Swipe Slider Pill with Micro-Animations
                val context = androidx.compose.ui.platform.LocalContext.current
                var dragAmount by remember { mutableStateOf(0f) }
                var isDragging by remember { mutableStateOf(false) }

                // Text continuous shimmer
                val shimmerAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1250, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "text_shimmer"
                )

                // Drag spring spec to have zero drag-lag but gorgeous release snap-back bounce
                val dragSpringSpec: AnimationSpec<Float> = if (isDragging) {
                    snap<Float>()
                } else {
                    spring<Float>(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                }

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp) // Height slightly increased to accommodate expanding handle beautifully
                        .clip(RoundedCornerShape(36.dp))
                        .background(Color(0xE6121118)) // dark tray
                        .border(1.dp, Color(0x13FFFFFF), RoundedCornerShape(36.dp))
                        .padding(5.dp)
                        .testTag("onboarding_swipe_pill")
                ) {
                    val widthPx = with(androidx.compose.ui.platform.LocalDensity.current) { maxWidth.toPx() }
                    // Base size of 56.dp in calculations avoids jitter from handle size changes
                    val handleSizePx = with(androidx.compose.ui.platform.LocalDensity.current) { 56.dp.toPx() }
                    val maxDragDistance = (widthPx - handleSizePx - with(androidx.compose.ui.platform.LocalDensity.current) { 10.dp.toPx() }).coerceAtLeast(1.0f)

                    // Snaps or springs the drag distance
                    val animatedDragDistance by animateFloatAsState(
                        targetValue = if (isDragging) dragAmount else 0f,
                        animationSpec = dragSpringSpec,
                        label = "drag_snapping"
                    )

                    val rawProgress = animatedDragDistance / maxDragDistance
                    val progress = if (rawProgress.isNaN()) 0f else rawProgress.coerceIn(0f, 1f)

                    // Text inside the slider tract (fades out as handle slides closer)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val fadeAlpha = (1f - progress).coerceIn(0.0f, 1f)
                        Text(
                            text = "Swipe to manage expenses",
                            color = Color.White.copy(alpha = shimmerAlpha * fadeAlpha),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Neon Lime Draggable Handle with dynamic scale sizes (56.dp to 62.dp)
                    val currentMaxDragDistance by rememberUpdatedState(maxDragDistance)
                    val currentOnGetStarted by rememberUpdatedState(onGetStarted)

                    val handleSize by animateDpAsState(
                        targetValue = if (isDragging) 62.dp else 56.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "handle_size"
                    )

                    val rawAlpha = 0.25f + progress * 0.75f
                    val glowAlpha = if (rawAlpha.isNaN()) 0.25f else rawAlpha.coerceIn(0f, 1f)
                    val glowColor = Color(0xFFE2F163).copy(alpha = glowAlpha)

                    Box(
                        modifier = Modifier
                            .offset(
                                x = with(androidx.compose.ui.platform.LocalDensity.current) {
                                    animatedDragDistance.toDp()
                                }
                            )
                            .size(handleSize)
                            .align(Alignment.CenterStart)
                            .clip(CircleShape)
                            .background(Color(0xFFE2F163)) // neon lime
                            .border(width = 2.dp, color = glowColor, shape = CircleShape)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { isDragging = true },
                                    onDragEnd = {
                                        isDragging = false
                                        val limit = currentMaxDragDistance
                                        if (dragAmount >= limit * 0.95f) {
                                            dragAmount = limit
                                            
                                            // Trigger tactile haptics/vibration
                                            try {
                                                val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                                                if (vibrator != null && vibrator.hasVibrator()) {
                                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                        vibrator.vibrate(
                                                            android.os.VibrationEffect.createOneShot(
                                                                80L,
                                                                android.os.VibrationEffect.DEFAULT_AMPLITUDE
                                                            )
                                                        )
                                                    } else {
                                                        @Suppress("DEPRECATION")
                                                        vibrator.vibrate(80L)
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                // Graces build environments or test suites safely
                                            }

                                            currentOnGetStarted()
                                        } else {
                                            dragAmount = 0f
                                        }
                                    },
                                    onDragCancel = {
                                        isDragging = false
                                        dragAmount = 0f
                                    },
                                    onDrag = { change, dragAmountOffset ->
                                        change.consume()
                                        dragAmount = (dragAmount + dragAmountOffset.x).coerceIn(0f, currentMaxDragDistance)
                                    }
                                )
                            }
                            .testTag("swipe_handle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Swipe Arrow",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupDialog(
    onDismiss: () -> Unit,
    onSave: (String, String?, Double?) -> Unit,
    onSkip: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var goalTitle by remember { mutableStateOf("") }
    var targetAmountStr by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("profile_setup_dialog_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.OpaqueDialogBg),
            border = BorderStroke(1.dp, Color(0x33FFFFFF))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Personalize Spendly",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Configure your profile to start tracking",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Your Name input
                Text(
                    text = "YOUR NAME *",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (showError && name.isBlank()) Color.Red else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.Start),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        if (it.isNotBlank()) showError = false
                    },
                    placeholder = { Text("e.g. Akshit", color = Color.Gray, fontSize = 14.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFDDF247),
                        unfocusedBorderColor = if (showError && name.isBlank()) Color.Red else Color(0x2BFFFFFF),
                        focusedContainerColor = Color(0x0CFFFFFF),
                        unfocusedContainerColor = Color(0x05FFFFFF)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("setup_name_input")
                )
                if (showError && name.isBlank()) {
                    Text(
                        text = "Name cannot be empty",
                        color = Color.Red,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Savings Goal Title
                Text(
                    text = "SAVINGS GOAL TITLE (OPTIONAL)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.Start),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = goalTitle,
                    onValueChange = { goalTitle = it },
                    placeholder = { Text("e.g. Wedding Fund", color = Color.Gray, fontSize = 14.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFC06240),
                        unfocusedBorderColor = Color(0x2BFFFFFF),
                        focusedContainerColor = Color(0x0CFFFFFF),
                        unfocusedContainerColor = Color(0x05FFFFFF)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("setup_goal_title_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Target Saving Amount
                Text(
                    text = "TARGET SAVING AMOUNT (OPTIONAL)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.Start),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = targetAmountStr,
                    onValueChange = { targetAmountStr = it },
                    placeholder = { Text("e.g. 100000", color = Color.Gray, fontSize = 14.sp) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color(0x2BFFFFFF),
                        focusedContainerColor = Color(0x0CFFFFFF),
                        unfocusedContainerColor = Color(0x05FFFFFF)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("setup_goal_amount_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            showError = true
                        } else {
                            val targetAmt = targetAmountStr.toDoubleOrNull()
                            onSave(name.trim(), goalTitle.trim().takeIf { it.isNotEmpty() }, targetAmt)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDDF247),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("setup_create_profile_btn")
                ) {
                    Text(
                        text = "Create Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.testTag("setup_skip_btn")
                ) {
                    Text(
                        text = "Skip Setup",
                        color = Color(0xFFDDF247),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeScreen(
    members: List<Member>,
    currencySymbol: String,
    onBack: () -> Unit,
    onExchangeCompleted: (fromMemberId: Int, toMemberId: Int, amount: Double) -> Unit
) {
    var rawAmount by remember { mutableStateOf("") }
    var selectedFromMemberId by remember { mutableIntStateOf(members.firstOrNull()?.id ?: 1) }
    var selectedToMemberId by remember { mutableIntStateOf(members.getOrNull(1)?.id ?: (members.firstOrNull()?.id ?: 1)) }

    var expandedFromMenu by remember { mutableStateOf(false) }
    var expandedToMenu by remember { mutableStateOf(false) }

    val fromMemberName = members.find { it.id == selectedFromMemberId }?.name ?: "Source"
    val toMemberName = members.find { it.id == selectedToMemberId }?.name ?: "Recipient"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF110E1A),
                            Color(0xFF09080E)
                        )
                    )
                )
            }
            .safeDrawingPadding()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x3BFFFFFF))
                ) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Text(
                    text = "Exchange",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x3D1F222C)),
                border = BorderStroke(1.dp, Color(0x19FFFFFF))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Send", color = Color(0xFFA1A1AA), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x2BFFFFFF))
                                    .clickable { expandedFromMenu = true }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(fromMemberName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = expandedFromMenu,
                                onDismissRequest = { expandedFromMenu = false },
                                modifier = Modifier.background(Color(0xFF22252C))
                            ) {
                                members.forEach { m ->
                                    DropdownMenuItem(
                                        text = { Text(m.name, color = Color.White) },
                                        onClick = {
                                            selectedFromMemberId = m.id
                                            expandedFromMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        TextField(
                            value = rawAmount,
                            onValueChange = { rawAmount = it },
                            placeholder = { Text("0.00", color = Color.Gray, fontSize = 24.sp, fontWeight = FontWeight.Black, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End) },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.End
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(160.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Balance: Available",
                        fontSize = 11.sp,
                        color = Color(0xFFA1A1AA)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        val temp = selectedFromMemberId
                        selectedFromMemberId = selectedToMemberId
                        selectedToMemberId = temp
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(BrandLime)
                        .border(4.dp, Color(0xFF09080E), CircleShape)
                ) {
                    Icon(Icons.Default.SwapVert, "Toggle Swap", tint = Color.Black)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x3D1F222C)),
                border = BorderStroke(1.dp, Color(0x19FFFFFF))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Receive", color = Color(0xFFA1A1AA), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x2BFFFFFF))
                                    .clickable { expandedToMenu = true }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(toMemberName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = expandedToMenu,
                                onDismissRequest = { expandedToMenu = false },
                                modifier = Modifier.background(Color(0xFF22252C))
                            ) {
                                members.forEach { m ->
                                    DropdownMenuItem(
                                        text = { Text(m.name, color = Color.White) },
                                        onClick = {
                                            selectedToMemberId = m.id
                                            expandedToMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (rawAmount.isEmpty()) "0.00" else rawAmount,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.End
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Instantly credited to wallet",
                        fontSize = 11.sp,
                        color = Color(0xFFA1A1AA)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    val amountVal = rawAmount.toDoubleOrNull() ?: 0.0
                    if (amountVal > 0 && selectedFromMemberId != selectedToMemberId) {
                        onExchangeCompleted(selectedFromMemberId, selectedToMemberId, amountVal)
                    }
                },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                enabled = rawAmount.isNotEmpty() && selectedFromMemberId != selectedToMemberId
            ) {
                Text("Exchange", fontSize = 16.sp, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                colors = CardDefaults.cardColors(containerColor = TranslucentCardBg),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, TranslucentCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Rate", color = Color(0xFFA1A1AA), fontSize = 13.sp)
                        Text("1 $currencySymbol = 1.00 $currencySymbol", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Price impact", color = Color(0xFFA1A1AA), fontSize = 13.sp)
                        Text("0.05%", color = BrandLime, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Liquidity Provider Fee", color = Color(0xFFA1A1AA), fontSize = 13.sp)
                        Text("Free Sync Transfer", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BudgetsAndGoalsListSubScreen(
    transactions: List<Transaction>,
    budgets: List<Budget>,
    goals: List<SavingGoal>,
    onAddBudgetClick: () -> Unit,
    onAddGoalClick: () -> Unit,
    onDepositGoalClick: (SavingGoal) -> Unit,
    onDeleteBudget: (Budget) -> Unit,
    onDeleteGoal: (SavingGoal) -> Unit,
    currencySymbol: String = "$"
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (budgets.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Category Limit Thresholds", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    IconButton(onClick = onAddBudgetClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.AddCircle, null, tint = BrandLime)
                    }
                }
            }

            items(budgets) { budget ->
                val spent = remember(transactions, budget) {
                    val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                    transactions
                        .filter { 
                            it.category.equals(budget.category, ignoreCase = true) && 
                            it.amount < 0 && 
                            sdf.format(Date(it.date)) == budget.monthYear
                        }
                        .sumOf { -it.amount }
                }
                val ratio = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit).toFloat().coerceIn(0f, 1f) else 0f
                val color = if (ratio > 0.85f) Color(0xFFF87171) else BrandLime

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(onClick = {}, onLongClick = { onDeleteBudget(budget) }),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = TranslucentCardBg),
                    border = BorderStroke(1.dp, TranslucentCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(budget.category, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${currencySymbol}${String.format("%.0f", spent)} / ${currencySymbol}${String.format("%.0f", budget.monthlyLimit)}", color = Color.White, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { onDeleteBudget(budget) },
                                    modifier = Modifier.size(24.dp).testTag("delete_budget_bin_${budget.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Budget Limit",
                                        tint = Color(0xFFF43F5E),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = ratio,
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = color,
                            trackColor = Color(0x33FFFFFF)
                        )
                    }
                }
            }
        }

        if (goals.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Savings Vault Target", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    IconButton(onClick = onAddGoalClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.AddCircle, null, tint = BrandLime)
                    }
                }
            }

            items(goals) { goal ->
                val ratio = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onDepositGoalClick(goal) },
                            onLongClick = { onDeleteGoal(goal) }
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = TranslucentCardBg),
                    border = BorderStroke(1.dp, TranslucentCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(goal.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Target Date: ${goal.targetDate} (Tap to deposit cash)", color = Color(0xFFA1A1AA), fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${currencySymbol}${String.format("%.0f", goal.currentAmount)} / ${currencySymbol}${String.format("%.0f", goal.targetAmount)}", color = Color.White, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { onDeleteGoal(goal) },
                                    modifier = Modifier.size(24.dp).testTag("delete_goal_bin_${goal.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Saving Goal",
                                        tint = Color(0xFFF43F5E),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = ratio,
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = Color(0xFF10B981),
                            trackColor = Color(0x33FFFFFF)
                        )
                    }
                }
            }
        }

        if (budgets.isEmpty() && goals.isEmpty()) {
            item {
                CardEmptyPlaceholder("No Limits Configured", "Record dynamic category limits or savings goals in profile vault.")
            }
        }
    }
}

@Composable
fun CardEmptyPlaceholder(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 20.dp),
        colors = CardDefaults.cardColors(containerColor = TranslucentCardBg),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, TranslucentCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.AccountBalanceWallet, null, tint = BrandLime, modifier = Modifier.size(44.dp))
            Spacer(modifier = Modifier.height(14.dp))
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(subtitle, color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
        }
    }
}

@Composable
fun NotificationToastBanner(message: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = TranslucentCardBg),
        border = BorderStroke(1.dp, BrandLime.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Info, null, tint = BrandLime)
                Spacer(modifier = Modifier.width(10.dp))
                Text(message, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}
@Composable
fun MemberAvatarItem(
    displayName: String,
    initials: String,
    signatureColor: Color,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onManage: () -> Unit,
    isAllShared: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onSelect)
            .padding(vertical = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(62.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) BrandLime.copy(alpha = 0.13f) else Color.Transparent)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) BrandLime else TranslucentCardBorder,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isAllShared) Color(0x38FFFFFF) else signatureColor.copy(alpha = 0.28f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isAllShared) {
                        Icon(
                            imageVector = Icons.Default.AllInclusive,
                            contentDescription = null,
                            tint = BrandLime,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = initials,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = signatureColor,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }
            }

            if (!isAllShared) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 2.dp, end = 2.dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22252D))
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        .clickable { onManage() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Manage",
                        tint = signatureColor,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = displayName,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 64.dp)
        )
    }
}

fun getCategoryIcon(cat: String): ImageVector {
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

fun parseHexColor(hex: String, default: Color = Color.Gray): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        default
    }
}
