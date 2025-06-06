package com.example.pocketsafe.data.dao

import androidx.room.*
import com.example.pocketsafe.data.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses")
    fun getAllExpenses(): List<Expense>

    @Query("""
        SELECT DISTINCT c.name 
        FROM expenses e
        INNER JOIN categories c ON e.categoryId = c.id
    """)
    fun getAllExpenseCategories(): List<String>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId")
    fun getExpensesByCategory(categoryId: Int): List<Expense>

    @Insert
    suspend fun insertExpense(expense: Expense): Long

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalExpenses(): Double
} 