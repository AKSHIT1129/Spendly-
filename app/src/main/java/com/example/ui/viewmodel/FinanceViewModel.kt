package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Member
import com.example.data.model.Transaction
import com.example.data.model.Budget
import com.example.data.model.SavingGoal
import com.example.data.model.BillReminder
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class EssentialsRatioBreakdown(
    val totalIncome: Double,
    val totalExpense: Double,
    val essentialExpense: Double,
    val discretionaryExpense: Double,
    val savings: Double,
    val essentialPercent: Float,
    val discretionaryPercent: Float,
    val savingsPercent: Float
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository
    
    private val sharedPrefs = application.getSharedPreferences("spendly_prefs", android.content.Context.MODE_PRIVATE)
    
    private val _showOnboarding = MutableStateFlow(!sharedPrefs.getBoolean("onboarding_completed", false))
    val showOnboarding = _showOnboarding.asStateFlow()
    
    fun completeOnboarding() {
        sharedPrefs.edit().putBoolean("onboarding_completed", true).apply()
        _showOnboarding.value = false
    }

    // Native reactive StateFlows from database
    val members: StateFlow<List<Member>>
    val transactions: StateFlow<List<Transaction>>
    val budgets: StateFlow<List<Budget>>
    val savingGoals: StateFlow<List<SavingGoal>>
    val billReminders: StateFlow<List<BillReminder>>

    // UI state states
    private val _selectedMemberId = MutableStateFlow<Int?>(null) // null means "All Members"
    val selectedMemberId = _selectedMemberId.asStateFlow()

    private val _notification = MutableStateFlow<String?>(null)
    val notification = _notification.asStateFlow()

    private val _notificationsList = MutableStateFlow<List<AppNotification>>(
        listOf(
            AppNotification(message = "System initialized and ready for Akshit. Double-check your active budgets below.")
        )
    )
    val notificationsList = _notificationsList.asStateFlow()

    fun clearAllNotifications() {
        _notificationsList.value = emptyList()
    }

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // Currency configuration flow
    private val _currency = MutableStateFlow("INR")
    val currency = _currency.asStateFlow()

    val currencySymbol = _currency.map { cur ->
        when (cur) {
            "USD" -> "$"
            "EUR" -> "€"
            else -> "₹"
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = "₹"
    )

    private val _exchangeRates = MutableStateFlow<Map<String, Double>>(
        mapOf(
            "USD" to 1.0 / 95.85,
            "EUR" to 1.0 / 111.49,
            "INR" to 1.0
        )
    )
    val exchangeRates = _exchangeRates.asStateFlow()

    val currencyRate = combine(_currency, _exchangeRates) { cur, rates ->
        rates[cur] ?: when (cur) {
            "USD" -> 1.0 / 95.85
            "EUR" -> 1.0 / 111.49
            else -> 1.0
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 1.0
    )

    fun setCurrency(newCurrency: String) {
        _currency.value = newCurrency
        showInAppNotification("💱 Currency converted to $newCurrency!")
    }

    fun getCurrencySymbol(): String {
        return when (_currency.value) {
            "USD" -> "$"
            "EUR" -> "€"
            else -> "₹"
        }
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FinanceRepository(database.financeDao(), application)

        members = repository.allMembers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        transactions = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        budgets = repository.allBudgets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        savingGoals = repository.allSavingGoals.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        billReminders = repository.allBillReminders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed default starter data if database is empty only if onboarding is completed
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val onboardingCompleted = sharedPrefs.getBoolean("onboarding_completed", false)
            if (onboardingCompleted) {
                val currentMembers = repository.allMembers.first()
                if (currentMembers.isEmpty()) {
                    seedDatabase("You")
                }
            }
        }
        fetchLiveExchangeRates()
    }

    private suspend fun seedDatabase(primaryName: String) {
        // Create 3 initial members (including Primary)
        val m1 = Member(name = primaryName, colorHex = "#10B981", role = "Primary")
        val m2 = Member(name = "Sarah", colorHex = "#6366F1", role = "Partner")
        val m3 = Member(name = "Akshit", colorHex = "#EC4899", role = "Family")

        repository.insertMember(m1)
        repository.insertMember(m2)
        repository.insertMember(m3)

        // Add pre-configured category budgets
        val curMonth = getCurrentYearMonth()
        repository.insertBudget(Budget(category = "Food", monthlyLimit = 15000.0, monthYear = curMonth))
        repository.insertBudget(Budget(category = "Rent", monthlyLimit = 40000.0, monthYear = curMonth))
        repository.insertBudget(Budget(category = "Shopping", monthlyLimit = 12000.0, monthYear = curMonth))
        repository.insertBudget(Budget(category = "Entertainment", monthlyLimit = 6000.0, monthYear = curMonth))
        repository.insertBudget(Budget(category = "Utilities", monthlyLimit = 8000.0, monthYear = curMonth))

        // Add Saving Goals
        repository.insertSavingGoal(SavingGoal(title = "Emergency Fund", targetAmount = 250000.0, currentAmount = 100000.0, targetDate = "Dec 2026"))
        repository.insertSavingGoal(SavingGoal(title = "Japan Summer Trip", targetAmount = 500000.0, currentAmount = 150000.0, targetDate = "Aug 2026"))

        // Add Bill Reminders
        val now = System.currentTimeMillis()
        repository.insertBillReminder(BillReminder(title = "Netflix Premium", amount = 649.00, dueDate = now + 2 * 24 * 60 * 60 * 1000, isPaid = false, category = "Entertainment"))
        repository.insertBillReminder(BillReminder(title = "Residential Rent", amount = 35000.00, dueDate = now + 5 * 24 * 60 * 60 * 1000, isPaid = false, category = "Rent"))
        repository.insertBillReminder(BillReminder(title = "Fiber Optic Internet", amount = 999.00, dueDate = now + 10 * 24 * 60 * 60 * 1000, isPaid = true, category = "Utilities"))

        // Add initial structured ledger transactions (seeding 1-based member IDs)
        // Note: First inserted members will get autogenerated IDs 1, 2, 3 in order
        repository.insertTransaction(Transaction(amount = 120000.0, category = "Salary", description = "$primaryName Monthly Base", date = now - 5 * 24 * 60 * 60 * 1000, memberId = 1, isShared = false, isEssential = false))
        repository.insertTransaction(Transaction(amount = -35000.0, category = "Rent", description = "Monthly Apart. Base", date = now - 4 * 24 * 60 * 60 * 1000, memberId = 1, isShared = true, isEssential = true))
        repository.insertTransaction(Transaction(amount = -3500.50, category = "Food", description = "Whole Foods Organic Groceries", date = now - 3 * 24 * 60 * 60 * 1000, memberId = 2, isShared = true, isEssential = true))
        repository.insertTransaction(Transaction(amount = -850.00, category = "Entertainment", description = "Cinema Standard Tickets", date = now - 2 * 24 * 60 * 60 * 1000, memberId = 3, isShared = true, isEssential = false))
        repository.insertTransaction(Transaction(amount = -2500.00, category = "Shopping", description = "Winter Warm Jacket", date = now - 1 * 24 * 60 * 60 * 1000, memberId = 2, isShared = false, isEssential = false))
        repository.insertTransaction(Transaction(amount = -1800.00, category = "Utilities", description = "Clean water & Power bill", date = now - 8 * 60 * 60 * 1000, memberId = 1, isShared = true, isEssential = true))
        repository.insertTransaction(Transaction(amount = 15000.0, category = "Salary", description = "Mobile Consulting Freelance", date = now - 2 * 60 * 60 * 1000, memberId = 1, isShared = false, isEssential = false))
    }

    fun setupPrimaryProfile(name: String, targetGoalTitle: String?, targetGoalAmount: Double?) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val resolvedName = if (name.isBlank()) "You" else name.trim()
            
            // Seed database using the resolved user's name
            seedDatabase(resolvedName)
            
            // Insert custom saving goal if provided
            if (!targetGoalTitle.isNullOrBlank() && targetGoalAmount != null && targetGoalAmount > 0.0) {
                val rate = currencyRate.value
                val baseTarget = targetGoalAmount / rate
                val cap = SavingGoal(
                    title = targetGoalTitle.trim(),
                    targetAmount = baseTarget,
                    currentAmount = 0.0,
                    targetDate = "Dec 2026"
                )
                repository.insertSavingGoal(cap)
            }
            
            // Complete onboarding setup and hide state
            completeOnboarding()
        }
    }

    // --- Member Interface ---
    fun selectMember(memberId: Int?) {
        _selectedMemberId.value = memberId
    }

    fun addMember(name: String, colorHex: String, role: String) {
        viewModelScope.launch {
            val count = members.value.size
            if (count >= 10) {
                showInAppNotification("⚠️ Profile Limit Reached: Safe max capacity is 10 shared accounts.")
                return@launch
            }
            repository.insertMember(Member(name = name, colorHex = colorHex, role = role))
            showInAppNotification("🎉 Sourced profile for '$name' as $role!")
        }
    }

    fun deleteMember(member: Member) {
        viewModelScope.launch {
            if (member.role == "Primary") {
                showInAppNotification("👮 Cannot dismantle the primary administrative profile.")
                return@launch
            }
            repository.deleteMember(member)
            showInAppNotification("🗑️ Account and profile database entry for ${member.name} removed.")
        }
    }

    // --- Transaction Interface ---
    fun addTransaction(
        amount: Double,
        category: String,
        description: String,
        memberId: Int,
        isShared: Boolean,
        isEssential: Boolean = isCategoryEssentialByDefault(category)
    ) {
        viewModelScope.launch {
            val rate = currencyRate.value
            val baseAmount = amount / rate
            val tx = Transaction(
                amount = baseAmount,
                category = category,
                description = description,
                date = System.currentTimeMillis(),
                memberId = memberId,
                isShared = isShared,
                isEssential = isEssential
            )
            repository.insertTransaction(tx)

            // Trigger proactive notification checking if they crossed a budget
            checkBudgetsForCrossing(category, baseAmount)

            showInAppNotification(
                if (amount > 0) "💰 Streamed revenue input: +${getCurrencySymbol()}${String.format("%.2f", amount)}"
                else "📉 Expense authorized: -${getCurrencySymbol()}${String.format("%.2f", -amount)}"
            )
        }
    }

    private fun checkBudgetsForCrossing(category: String, amount: Double) {
        if (amount >= 0) return
        val expenseVal = -amount

        viewModelScope.launch {
            val currentMonth = getCurrentYearMonth()
            // Find active budget limits for that category for the current month
            val budgetList = budgets.value
            val targetBudget = budgetList.find { 
                it.category.equals(category, ignoreCase = true) && it.monthYear == currentMonth 
            } ?: return@launch

            // Compute current aggregate spent for the current month
            val allTx = transactions.value
            val currentSpent = allTx
                .filter { 
                    it.category.equals(category, ignoreCase = true) && 
                    it.amount < 0 && 
                    isTimestampInMonth(it.date, currentMonth)
                }
                .sumOf { -it.amount }

            val projectedSpent = currentSpent + expenseVal
            val previousRatio = if (targetBudget.monthlyLimit > 0) currentSpent / targetBudget.monthlyLimit else 0.0
            val newRatio = if (targetBudget.monthlyLimit > 0) projectedSpent / targetBudget.monthlyLimit else 0.0

            if (projectedSpent > targetBudget.monthlyLimit && previousRatio <= 1.0) {
                showInAppNotification("⚠️ Budget Alert: Limit of ${getCurrencySymbol()}${String.format("%.2f", targetBudget.monthlyLimit)} exceeded for '$category' category!")
            } else if (newRatio >= 0.9 && previousRatio < 0.9) {
                showInAppNotification("⚠️ Warning: Spend has reached ${String.format("%.0f", newRatio * 100)}% of your monthly budget limit for '$category'!")
            } else if (newRatio >= 0.8 && previousRatio < 0.8) {
                showInAppNotification("⚠️ Notice: Spend has reached ${String.format("%.0f", newRatio * 100)}% of your monthly budget limit for '$category'!")
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            showInAppNotification("🗑️ Entry deleted.")
        }
    }

    // --- Budget Interface ---
    fun addBudget(category: String, limit: Double) {
        viewModelScope.launch {
            val baseLimit = limit / currencyRate.value
            repository.insertBudget(Budget(category = category, monthlyLimit = baseLimit, monthYear = getCurrentYearMonth()))
            showInAppNotification("📊 Created monthly budget limit of ${getCurrencySymbol()}${limit} for $category.")
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
            showInAppNotification("🗑️ Budget limit dismissed.")
        }
    }

    // --- Saving Goals Interface ---
    fun addSavingGoal(title: String, targetAmount: Double, currentAmount: Double, targetDate: String) {
        viewModelScope.launch {
            val baseTarget = targetAmount / currencyRate.value
            val baseCurrent = currentAmount / currencyRate.value
            val goal = SavingGoal(title = title, targetAmount = baseTarget, currentAmount = baseCurrent, targetDate = targetDate)
            repository.insertSavingGoal(goal)
            showInAppNotification("🎯 Launched saving goal: '$title' to secure ${getCurrencySymbol()}${targetAmount}!")
        }
    }

    fun updateSavingProgress(goal: SavingGoal, addAmount: Double) {
        viewModelScope.launch {
            val rate = currencyRate.value
            val baseAddAmount = addAmount / rate
            val dbGoal = repository.allSavingGoals.first().find { it.id == goal.id } ?: goal
            val updatedAmount = (dbGoal.currentAmount + baseAddAmount).coerceIn(0.0, dbGoal.targetAmount)
            val updatedGoal = dbGoal.copy(currentAmount = updatedAmount)
            repository.insertSavingGoal(updatedGoal)

            val activeTarget = dbGoal.targetAmount * rate
            val activeUpdated = updatedAmount * rate
            if (updatedAmount >= dbGoal.targetAmount) {
                showInAppNotification("🏆 Target Reached! Saved ${getCurrencySymbol()}${String.format("%.2f", activeTarget)} for '${dbGoal.title}'!")
            } else {
                showInAppNotification("💰 Seeded ${getCurrencySymbol()}${addAmount} to '${dbGoal.title}'. Saved: ${getCurrencySymbol()}${String.format("%.2f", activeUpdated)}/${getCurrencySymbol()}${String.format("%.2f", activeTarget)}")
            }
        }
    }

    fun deleteSavingGoal(goal: SavingGoal) {
        viewModelScope.launch {
            repository.deleteSavingGoal(goal)
            showInAppNotification("🗑️ Savings Goal deleted.")
        }
    }

    // --- Bill Reminders Interface ---
    fun addBillReminder(title: String, amount: Double, dueDate: Long, category: String) {
        viewModelScope.launch {
            val baseAmount = amount / currencyRate.value
            val bill = BillReminder(title = title, amount = baseAmount, dueDate = dueDate, isPaid = false, category = category)
            repository.insertBillReminder(bill)
            showInAppNotification("📅 Calendar bill scheduled: '$title' (${getCurrencySymbol()}${amount})")
        }
    }

    fun toggleBillPaid(bill: BillReminder) {
        viewModelScope.launch {
            val dbBill = repository.allBillReminders.first().find { it.id == bill.id } ?: bill
            val updated = dbBill.copy(isPaid = !dbBill.isPaid)
            repository.insertBillReminder(updated)
            showInAppNotification(if (updated.isPaid) "✅ Marked bill '${bill.title}' as paid!" else "📅 Bill marked as outstanding.")
        }
    }

    fun simulateBillNotification(bill: BillReminder) {
        showInAppNotification("🔔 REMINDER: '${bill.title}' bill of ${getCurrencySymbol()}${String.format("%.2f", bill.amount)} is due shortly!")
    }

    fun deleteBillReminder(bill: BillReminder) {
        viewModelScope.launch {
            repository.deleteBillReminder(bill)
            showInAppNotification("🗑️ Bill reminder deleted.")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllFinancialData()
            showInAppNotification("🧹 Complete Wipeout: Clear all database records successfully!")
        }
    }

    // --- Common Notification alert Banner logic ---
    fun showInAppNotification(message: String) {
        viewModelScope.launch {
            _notification.value = message
            val newItem = AppNotification(message = message)
            _notificationsList.value = listOf(newItem) + _notificationsList.value
        }
    }

    fun dismissNotification() {
        _notification.value = null
    }

    fun getCurrentYearMonth(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date())
    }

    fun isTimestampInMonth(timestamp: Long, monthYear: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date(timestamp)) == monthYear
    }

    fun isCategoryEssentialByDefault(category: String): Boolean {
        return when (category.trim().lowercase(Locale.getDefault())) {
            "rent", "food", "groceries", "utilities", "bills", "healthcare", "medical", "insurance", "transport", "emi", "education" -> true
            else -> false
        }
    }

    fun getEssentialsBreakdown(monthYear: String? = getCurrentYearMonth()): EssentialsRatioBreakdown {
        val txs = transactions.value.filter {
            if (monthYear != null) isTimestampInMonth(it.date, monthYear) else true
        }
        val income = txs.filter { it.amount > 0 }.sumOf { it.amount }
        val expenseTxs = txs.filter { it.amount < 0 }
        val totalExpense = expenseTxs.sumOf { -it.amount }
        val essential = expenseTxs.filter { it.isEssential || isCategoryEssentialByDefault(it.category) }.sumOf { -it.amount }
        val discretionary = (totalExpense - essential).coerceAtLeast(0.0)
        val savings = (income - totalExpense).coerceAtLeast(0.0)
        val baseForPercent = if (income > 0) income else totalExpense.coerceAtLeast(1.0)
        val essentialPct = ((essential / baseForPercent) * 100).toFloat().coerceIn(0f, 100f)
        val discretionaryPct = ((discretionary / baseForPercent) * 100).toFloat().coerceIn(0f, 100f)
        val savingsPct = ((savings / baseForPercent) * 100).toFloat().coerceIn(0f, 100f)

        return EssentialsRatioBreakdown(
            totalIncome = income,
            totalExpense = totalExpense,
            essentialExpense = essential,
            discretionaryExpense = discretionary,
            savings = savings,
            essentialPercent = essentialPct,
            discretionaryPercent = discretionaryPct,
            savingsPercent = savingsPct
        )
    }

    private fun fetchLiveExchangeRates() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val moshi = com.squareup.moshi.Moshi.Builder()
                    .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                    .build()
                val retrofit = retrofit2.Retrofit.Builder()
                    .baseUrl("https://open.er-api.com/")
                    .addConverterFactory(retrofit2.converter.moshi.MoshiConverterFactory.create(moshi))
                    .build()
                val api = retrofit.create(com.example.data.api.CurrencyApi::class.java)
                val response = api.getLatestRates()
                if (response.result == "success" && response.rates.isNotEmpty()) {
                    val usd = response.rates["USD"]
                    val eur = response.rates["EUR"]
                    val inr = response.rates["INR"] ?: 1.0
                    if (usd != null && eur != null) {
                        _exchangeRates.value = mapOf(
                            "USD" to usd,
                            "EUR" to eur,
                            "INR" to inr
                        )
                        android.util.Log.d("FinanceViewModel", "Live rates fetched: USD $usd, EUR $eur")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "Failed to fetch live exchange rates, falling back to static config", e)
            }
        }
    }
}
