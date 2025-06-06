package com.example.pocketsafe.data.dao

import androidx.room.*
import com.example.pocketsafe.data.BudgetGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetGoalDao {
    // FIXED: Column names match entity properties (camelCase, not snake_case)
    @Query("SELECT * FROM budget_goals ORDER BY id DESC")
    fun getAllBudgetGoals(): Flow<List<BudgetGoal>>

    @Query("SELECT * FROM budget_goals WHERE id = :id")
    suspend fun getBudgetGoalById(id: Long): BudgetGoal?

    // FIXED: Column names now match your actual BudgetGoal entity exactly
    @Query("SELECT * FROM budget_goals WHERE categoryId = :categoryId")
    suspend fun getBudgetGoalsByCategory(categoryId: Int): List<BudgetGoal>

    @Query("SELECT * FROM budget_goals WHERE month = :month AND year = :year")
    suspend fun getBudgetGoalsForMonth(month: Int, year: Int): List<BudgetGoal>

    @Insert
    suspend fun insertBudgetGoal(budgetGoal: BudgetGoal): Long

    @Update
    suspend fun updateBudgetGoal(budgetGoal: BudgetGoal)

    @Delete
    suspend fun deleteBudgetGoal(budgetGoal: BudgetGoal)

    @Query("DELETE FROM budget_goals WHERE id = :id")
    suspend fun deleteBudgetGoalById(id: Long)

    // Helper query to get total target amount for a category
    @Query("SELECT SUM(targetAmount) FROM budget_goals WHERE categoryId = :categoryId")
    suspend fun getTotalTargetForCategory(categoryId: Int): Double?

    // Helper query to get current total spending vs budget
    @Query("SELECT SUM(currentAmount) FROM budget_goals WHERE categoryId = :categoryId")
    suspend fun getCurrentSpendingForCategory(categoryId: Int): Double?
}