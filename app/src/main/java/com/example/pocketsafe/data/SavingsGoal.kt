package com.example.pocketsafe.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Ignore

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "target_amount")
    val targetAmount: Double,
    @ColumnInfo(name = "current_amount")
    val currentAmount: Double = 0.0,
    @ColumnInfo(name = "target_date")
    val targetDate: Long,
    val description: String? = null,
    // Firebase-specific fields
    @ColumnInfo(name = "firebase_id")
    val firebaseId: String = "",
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "icon_type")
    val iconType: String = "PIXEL_MONEY_BAG"
) {
    /**
     * Compatibility properties to handle both naming conventions (camelCase and snake_case)
     * This helps with Kotlin code that might access using either style
     */
    // Cannot use @Ignore on computed properties (no backing field)
    val target_amount: Double get() = targetAmount
    val current_amount: Double get() = currentAmount
    val target_date: Long get() = targetDate
    
    /**
     * Get the Firebase document ID, using the firebaseId field if available, otherwise fallback to string ID
     */
    fun getDocumentId(): String {
        return if (firebaseId.isNotEmpty()) firebaseId else id.toString()
    }
}