package com.example.pocketsafe.data.dao

import androidx.room.*
import com.example.pocketsafe.data.BillReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface BillReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillReminder(billReminder: BillReminder)

    @Query("SELECT * FROM bill_reminders ORDER BY dueDate ASC")
    fun getAllBillReminders(): Flow<List<BillReminder>>

    @Query("SELECT * FROM bill_reminders WHERE paid = 0 ORDER BY dueDate ASC")
    fun getUnpaidBillReminders(): Flow<List<BillReminder>>
    
    @Query("SELECT * FROM bill_reminders WHERE paid = 0 ORDER BY dueDate ASC")
    suspend fun getUnpaidBills(): List<BillReminder>

    @Query("SELECT * FROM bill_reminders WHERE id = :id")
    suspend fun getBillReminderById(id: String): BillReminder?

    @Delete
    suspend fun deleteBillReminder(billReminder: BillReminder)

    @Update
    suspend fun updateBillReminder(billReminder: BillReminder)
    
    @Query("UPDATE bill_reminders SET paid = :paid WHERE id = :id")
    suspend fun updatePaidStatus(id: String, paid: Boolean)
}
