package com.example.pocketsafe.data

import androidx.room.*

@Entity(tableName = "budget_goals")
data class BudgetGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Int,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val month: Int,
    val year: Int,
    val description: String? = null
)

