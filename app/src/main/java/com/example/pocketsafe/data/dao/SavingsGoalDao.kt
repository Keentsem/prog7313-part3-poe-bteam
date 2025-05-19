package com.example.pocketsafe.data.dao

import androidx.room.*
import com.example.pocketsafe.data.SavingsGoal

@Dao
interface SavingsGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: SavingsGoal)

    @Query("SELECT * FROM savings_goals ORDER BY id DESC LIMIT 1")
    suspend fun getCurrentGoal(): SavingsGoal?

    @Query("UPDATE savings_goals SET current_amount = :newAmount WHERE id = :goalId")
    suspend fun updateSavings(goalId: Int, newAmount: Double)
} 