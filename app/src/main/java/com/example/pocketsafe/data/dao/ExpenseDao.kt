package com.example.pocketsafe.data.dao

import androidx.room.*
import com.example.pocketsafe.data.Expense
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getExpensesByCategory(categoryId: Int): Flow<List<Expense>>

    @Query("SELECT DISTINCT categoryId FROM expenses")
    suspend fun getAllExpenseCategories(): List<Int>

    @Query("SELECT * FROM expenses WHERE startDate BETWEEN :startDate AND :endDate")
    fun getExpensesByDateRange(startDate: String, endDate: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId AND startDate BETWEEN :startDate AND :endDate")
    fun getExpensesByCategoryAndDateRange(categoryId: Int, startDate: String, endDate: String): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT SUM(amount) FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalExpensesBetweenDates(startDate: Long, endDate: Long): Flow<Double>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Int): Expense?

    @Query("SELECT SUM(amount) FROM expenses WHERE categoryId = :categoryId")
    suspend fun getTotalExpensesByCategory(categoryId: Int): Double
} 