package com.example.pocketsafe.data.dao

import androidx.room.*
import com.example.pocketsafe.data.SavingsGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: SavingsGoal): Long

    @Query("SELECT * FROM savings_goals")
    fun getAllGoals(): Flow<List<SavingsGoal>>
    
    @Query("SELECT * FROM savings_goals ORDER BY id DESC LIMIT 1")
    suspend fun getCurrentGoal(): SavingsGoal?
    
    @Query("SELECT * FROM savings_goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: Int): SavingsGoal?

    @Query("UPDATE savings_goals SET current_amount = :newAmount WHERE id = :goalId")
    suspend fun updateSavings(goalId: Int, newAmount: Double)
    
    @Update
    suspend fun updateGoal(goal: SavingsGoal)
    
    @Delete
    suspend fun deleteGoal(goal: SavingsGoal)
}