package com.example.pocketsafe.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses")
    fun getAllExpenses(): List<Expense>

    @Query("SELECT DISTINCT categoryName FROM expenses")
    fun getAllExpenseCategories(): List<String>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId")
    fun getExpensesByCategory(categoryId: Int): List<Expense>

    @Insert
    suspend fun insertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalExpenses(): Double
} 