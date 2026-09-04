package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.data.db.FinanceDao
import com.example.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.IOException

class FinanceRepository(
    private val financeDao: FinanceDao,
    context: Context? = null
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isNetworkAvailable = false

    // Centralized active Family/User ID to differentiate families
    var activeUserId: String = "family_akshit_2026"

    val allMembers: Flow<List<Member>> = financeDao.getAllMembers()
    val allTransactions: Flow<List<Transaction>> = financeDao.getAllTransactions()
    val allBudgets: Flow<List<Budget>> = financeDao.getAllBudgets()
    val allSavingGoals: Flow<List<SavingGoal>> = financeDao.getAllSavingGoals()
    val allBillReminders: Flow<List<BillReminder>> = financeDao.getAllBillReminders()

    init {
        // Simple but powerful Network Connectivity Monitoring
        context?.let { ctx ->
            try {
                val connectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                if (connectivityManager != null) {
                    val activeNetwork = connectivityManager.activeNetwork
                    val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
                    isNetworkAvailable = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

                    val networkRequest = NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build()

                    connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: Network) {
                            Log.d("FinanceRepository", "Network restored! Commencing background sync.")
                            isNetworkAvailable = true
                            triggerBackgroundSync()
                        }

                        override fun onLost(network: Network) {
                            Log.d("FinanceRepository", "Network connection lost. Transitioned to local-only cache mode.")
                            isNetworkAvailable = false
                        }
                    })
                }
            } catch (e: Exception) {
                Log.e("FinanceRepository", "Failed to register network callback, default to online mode", e)
                isNetworkAvailable = true
            }
        } ?: run {
            // Default to true for unit tests or situations where context is missing
            isNetworkAvailable = true
        }

        // Trigger initial synchronization pass on launch
        triggerBackgroundSync()
    }

    // --- Core Cloud Database Client & Schema Mapping Configs ---
    // In production, configure your Supabase REST Headers or Firestore instance here:
    // val db = Firebase.firestore
    // OR Supabase client: val supabase = createSupabaseClient(...)
    private var cloudDbUrl = "https://your-project.supabase.co/rest/v1" 
    private var cloudApiKey = "your_supabase_anon_service_role"

    /**
     * Simulation of Online Database Upload.
     * In a live project, replace this helper with real Firestore or Retrofit/Ktor networking.
     */
    private suspend fun uploadToCloudDatabase(table: String, payload: Any): Boolean {
        return try {
            // Simulate cloud roundtrip latency
            delay(500)
            
            // To make this fully functional for users who connect their real services:
            // if (isFirestoreEnabled) { db.collection(table).document(id).set(payload).await() }
            // if (isSupabaseEnabled) { client.from(table).insert(payload) }
            
            Log.d("FinanceRepository", "Cloud database successfully committed entry to table '$table'.")
            true
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Failed uploading to cloud database on table '$table'", e)
            false
        }
    }

    /**
     * Simulation of Cloud Sync fetch. Downward sync from the Online Database.
     */
    suspend fun fetchCloudUpdates() {
        if (!isNetworkAvailable) return
        scope.launch {
            try {
                // Fetch cloud data for our specific userId to share across all family devices
                Log.d("FinanceRepository", "Fetching downward updates from Cloud Database for userId: $activeUserId")
                
                // Once entries are pulled from Firestore/Supabase for matching userId,
                // we merge them locally into Room using:
                // financeDao.insertTransaction(pulledTx.copy(isPendingSync = false))
            } catch (e: Exception) {
                Log.e("FinanceRepository", "Could not sync cloud updates downwards", e)
            }
        }
    }

    /**
     * Background Synchronization Worker. Parses local database for unsynced changes 
     * and uploads them as soon as network is confirmed online.
     */
    fun triggerBackgroundSync() {
        if (!isNetworkAvailable) {
            Log.d("FinanceRepository", "Sync triggered but network is offline. Maintaining local cache.")
            return
        }

        scope.launch {
            try {
                Log.d("FinanceRepository", "Network is ONLINE. Syncing family ledger with the Cloud...")
                
                // Synchronize Unsynced Members
                val unsyncedMembers = allMembers.first().filter { it.isPendingSync }
                for (member in unsyncedMembers) {
                    val ok = uploadToCloudDatabase("members", member)
                    if (ok) {
                        financeDao.insertMember(member.copy(isPendingSync = false, userId = activeUserId))
                    }
                }

                // Synchronize Unsynced Transactions
                val unsyncedTransactions = allTransactions.first().filter { it.isPendingSync }
                for (tx in unsyncedTransactions) {
                    val ok = uploadToCloudDatabase("transactions", tx)
                    if (ok) {
                        financeDao.insertTransaction(tx.copy(isPendingSync = false, userId = activeUserId))
                    }
                }

                // Synchronize Unsynced Budgets
                val unsyncedBudgets = allBudgets.first().filter { it.isPendingSync }
                for (budget in unsyncedBudgets) {
                    val ok = uploadToCloudDatabase("budgets", budget)
                    if (ok) {
                        financeDao.insertBudget(budget.copy(isPendingSync = false, userId = activeUserId))
                    }
                }

                // Synchronize Unsynced Saving Goals
                val unsyncedGoals = allSavingGoals.first().filter { it.isPendingSync }
                for (goal in unsyncedGoals) {
                    val ok = uploadToCloudDatabase("saving_goals", goal)
                    if (ok) {
                        financeDao.insertSavingGoal(goal.copy(isPendingSync = false, userId = activeUserId))
                    }
                }

                // Synchronize Unsynced Bill Reminders
                val unsyncedBills = allBillReminders.first().filter { it.isPendingSync }
                for (bill in unsyncedBills) {
                    val ok = uploadToCloudDatabase("bill_reminders", bill)
                    if (ok) {
                        financeDao.insertBillReminder(bill.copy(isPendingSync = false, userId = activeUserId))
                    }
                }

                Log.d("FinanceRepository", "Synchronisation operations completed successfully.")
            } catch (e: Exception) {
                Log.e("FinanceRepository", "Sync pipeline encountered unexpected exception", e)
            }
        }
    }

    // --- Member Operations ---
    suspend fun insertMember(member: Member) {
        val toSave = member.copy(userId = activeUserId, isPendingSync = true)
        financeDao.insertMember(toSave)
        triggerBackgroundSync()
    }

    suspend fun deleteMember(member: Member) {
        financeDao.deleteMember(member)
        // Also fire cloud deletion payload
        scope.launch { uploadToCloudDatabase("members_delete", member) }
    }

    // --- Transaction Operations ---
    suspend fun insertTransaction(transaction: Transaction) {
        val toSave = transaction.copy(userId = activeUserId, isPendingSync = true)
        financeDao.insertTransaction(toSave)
        triggerBackgroundSync()
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        financeDao.deleteTransaction(transaction)
        // Also fire cloud deletion payload
        scope.launch { uploadToCloudDatabase("transactions_delete", transaction) }
    }

    suspend fun deleteTransactionById(id: Int) {
        financeDao.deleteTransactionById(id)
        scope.launch { uploadToCloudDatabase("transactions_delete_id", id) }
    }

    // --- Budget Operations ---
    suspend fun insertBudget(budget: Budget) {
        val toSave = budget.copy(userId = activeUserId, isPendingSync = true)
        financeDao.insertBudget(toSave)
        triggerBackgroundSync()
    }

    suspend fun deleteBudget(budget: Budget) {
        financeDao.deleteBudget(budget)
        scope.launch { uploadToCloudDatabase("budgets_delete", budget) }
    }

    // --- Saving Goal Operations ---
    suspend fun insertSavingGoal(goal: SavingGoal) {
        val toSave = goal.copy(userId = activeUserId, isPendingSync = true)
        financeDao.insertSavingGoal(toSave)
        triggerBackgroundSync()
    }

    suspend fun deleteSavingGoal(goal: SavingGoal) {
        financeDao.deleteSavingGoal(goal)
        scope.launch { uploadToCloudDatabase("saving_goals_delete", goal) }
    }

    // --- Bill Reminder Operations ---
    suspend fun insertBillReminder(bill: BillReminder) {
        val toSave = bill.copy(userId = activeUserId, isPendingSync = true)
        financeDao.insertBillReminder(toSave)
        triggerBackgroundSync()
    }

    suspend fun deleteBillReminder(bill: BillReminder) {
        financeDao.deleteBillReminder(bill)
        scope.launch { uploadToCloudDatabase("bill_reminders_delete", bill) }
    }

    suspend fun clearAllFinancialData() {
        financeDao.clearTransactions()
        financeDao.clearBudgets()
        financeDao.clearSavingGoals()
        financeDao.clearBillReminders()
        scope.launch { uploadToCloudDatabase("clear_all", activeUserId) }
    }
}
