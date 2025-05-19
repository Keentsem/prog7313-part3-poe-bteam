package com.example.pocketsafe.data

import androidx.room.*

@Entity(tableName = "budget_goals")
data class BudgetGoal(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "current_amount")
    val currentAmount: Double = 0.0,

    @ColumnInfo(name = "description", defaultValue = "NULL")
    val description: String? = null,

    @ColumnInfo(name = "target_date")
    val targetDate: Long
)
