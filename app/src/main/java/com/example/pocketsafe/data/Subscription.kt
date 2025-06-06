package com.example.pocketsafe.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Ignore
import androidx.room.ColumnInfo

@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    @ColumnInfo(name = "frequency")
    val frequency: String, // "MONTHLY", "YEARLY", "WEEKLY"
    @ColumnInfo(name = "nextDueDate")
    val nextDueDate: Long,
    @ColumnInfo(name = "active_status")
    val activeStatus: Boolean = true,
    val categoryId: Int,
    val description: String? = null,
    val paymentMethod: String? = null,
    val lastUpdated: Long = System.currentTimeMillis(),
    // Firebase document ID for cloud synchronization
    @ColumnInfo(name = "firebaseId")
    val firebaseId: String = ""
) {
    /**
     * Compatibility properties for legacy code that uses different field names
     */
    // Cannot use @Ignore on computed properties (no backing field)
    val renewalDate: Long
        get() = nextDueDate
    
    // Compatibility property for code expecting 'isActive'
    val isActive: Boolean
        get() = activeStatus
        
    // Compatibility property for code expecting 'active'
    val active: Boolean
        get() = activeStatus

    val renewalPeriod: RenewalPeriod
        get() = try { RenewalPeriod.valueOf(frequency) } catch (e: Exception) { RenewalPeriod.MONTHLY }
    
    val category: Int
        get() = categoryId
        
    /**
     * Default notification preferences since they're referenced in various places
     */
    @JvmField
    var notificationEnabled: Boolean = true
    
    @JvmField
    var notificationDays: Int = 3
}