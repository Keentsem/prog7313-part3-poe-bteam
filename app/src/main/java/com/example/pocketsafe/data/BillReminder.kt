package com.example.pocketsafe.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Ignore

/**
 * Represents a bill reminder with due date and payment status
 * Uses the pixel-retro theme with gold (#f3c34e) and brown (#5b3f2c) colors
 */
@Entity(tableName = "bill_reminders")
data class BillReminder(
    @PrimaryKey
    val id: String = "", // String ID for Firebase compatibility
    val title: String = "",
    val amount: Double = 0.0,
    val dueDate: Long = 0L,
    val paid: Boolean = false,
    val category: String = "",
    val categoryId: Int = 0, // For category filtering
    val paymentMethod: String = "",
    val notes: String = "",
    val recurring: Boolean = false,
    val recurringPeriod: Int = 0, // In days
    val notificationEnabled: Boolean = true,
    val notificationDays: Int = 3, // Days before due date to notify
    val lastUpdated: Long = System.currentTimeMillis()
)