package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.example.data.model.Member
import com.example.data.model.Transaction
import com.example.data.model.Budget
import com.example.data.model.SavingGoal
import com.example.data.model.BillReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {

    // --- Member Room Operations ---
    @Query("SELECT * FROM members ORDER BY id ASC")
    fun getAllMembers(): Flow<List<Member>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member)

    @Delete
    suspend fun deleteMember(member: Member)

    // --- Transaction Room Operations ---
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)

    // --- Budget Room Operations ---
    @Query("SELECT * FROM budgets ORDER BY id DESC")
    fun getAllBudgets(): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)

    // --- Saving Goal Room Operations ---
    @Query("SELECT * FROM saving_goals ORDER BY id DESC")
    fun getAllSavingGoals(): Flow<List<SavingGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingGoal(goal: SavingGoal)

    @Delete
    suspend fun deleteSavingGoal(goal: SavingGoal)

    // --- Bill Reminder Room Operations ---
    @Query("SELECT * FROM bill_reminders ORDER BY dueDate ASC")
    fun getAllBillReminders(): Flow<List<BillReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillReminder(bill: BillReminder)

    @Delete
    suspend fun deleteBillReminder(bill: BillReminder)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Query("DELETE FROM budgets")
    suspend fun clearBudgets()

    @Query("DELETE FROM saving_goals")
    suspend fun clearSavingGoals()

    @Query("DELETE FROM bill_reminders")
    suspend fun clearBillReminders()
}
