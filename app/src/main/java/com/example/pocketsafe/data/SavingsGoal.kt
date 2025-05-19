package com.example.pocketsafe.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val target_amount: Double,
    val current_amount: Double = 0.0,
    val target_date: Long,
    val description: String? = null
) 