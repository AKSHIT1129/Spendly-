package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val colorHex: String,
    val role: String, // e.g., "Primary", "Partner", "Family", "Friend"
    val userId: String = "family_akshit_2026",
    val isPendingSync: Boolean = false
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double, // positive for income, negative for expense
    val category: String, // e.g., "Food", "Rent", "Salary", "Shopping", "Entertainment"
    val description: String,
    val date: Long, // timestamp
    val memberId: Int, // associated member
    val isShared: Boolean = false, // shared or individual
    val isEssential: Boolean = false, // 50/30/20 rule: true for Needs/Essentials (Rent, Food, Utilities), false for Wants
    val userId: String = "family_akshit_2026",
    val isPendingSync: Boolean = false
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val monthlyLimit: Double,
    val monthYear: String, // e.g., "2026-05" (matching current year-month)
    val userId: String = "family_akshit_2026",
    val isPendingSync: Boolean = false
)

@Entity(tableName = "saving_goals")
data class SavingGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: String,
    val userId: String = "family_akshit_2026",
    val isPendingSync: Boolean = false
)

@Entity(tableName = "bill_reminders")
data class BillReminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val dueDate: Long,
    var isPaid: Boolean = false,
    val category: String,
    val userId: String = "family_akshit_2026",
    val isPendingSync: Boolean = false
)
