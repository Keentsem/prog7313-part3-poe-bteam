package com.example.pocketsafe.data.dao

import androidx.room.*
import com.example.pocketsafe.data.BudgetGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetGoalDao {
    @Query("SELECT * FROM budget_goals ORDER BY target_date DESC")
    fun getAllBudgetGoals(): Flow<List<BudgetGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetGoal(goal: BudgetGoal)

    @Delete
    suspend fun deleteBudgetGoal(goal: BudgetGoal)

    @Query("SELECT * FROM budget_goals WHERE id = :id")
    suspend fun getBudgetGoalById(id: Int): BudgetGoal?

    @Query("SELECT * FROM budget_goals WHERE :date <= target_date")
    suspend fun getCurrentBudgetGoal(date: Long): BudgetGoal?
} 