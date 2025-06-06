package com.example.pocketsafe.data.repository

import com.example.pocketsafe.data.BillReminder
import com.example.pocketsafe.data.dao.BillReminderDao
import com.example.pocketsafe.firebase.FirebaseService
import kotlinx.coroutines.flow.Flow

/**
 * Repository that handles bill reminder data operations with both local database and Firebase
 * Modified to work without Hilt to prevent app crashes
 */
class BillReminderRepository(
    private val billReminderDao: BillReminderDao
) {
    // Get Firebase service using singleton pattern
    private val firebaseService: FirebaseService = FirebaseService.getInstance()
    // Local database operations
    fun getAllBillReminders(): Flow<List<BillReminder>> = 
        billReminderDao.getAllBillReminders()
        
    fun getUnpaidBillReminders(): Flow<List<BillReminder>> =
        billReminderDao.getUnpaidBillReminders()
        
    suspend fun getBillReminderById(id: String): BillReminder? =
        billReminderDao.getBillReminderById(id)
        
    // Save to both local and Firebase
    suspend fun saveBillReminder(billReminder: BillReminder) {
        // Save to Firebase first
        val billId = firebaseService.saveBillReminder(billReminder)
        
        // If ID was empty, we got a new ID from Firebase
        val finalBillReminder = if (billReminder.id.isEmpty()) {
            billReminder.copy(id = billId)
        } else {
            billReminder
        }
        
        // Save to local database
        billReminderDao.insertBillReminder(finalBillReminder)
    }
    
    suspend fun updateBillReminder(billReminder: BillReminder) {
        // Update in Firebase
        firebaseService.updateBillReminder(billReminder)
        
        // Update in local database
        billReminderDao.updateBillReminder(billReminder)
    }
    
    suspend fun markBillAsPaid(billId: String, paid: Boolean = true) {
        // Update in Firebase
        firebaseService.markBillAsPaid(billId, paid)
        
        // Update in local database
        billReminderDao.updatePaidStatus(billId, paid)
    }
    
    suspend fun deleteBillReminder(billReminder: BillReminder) {
        // Delete from Firebase
        firebaseService.deleteBillReminder(billReminder.id)
        
        // Delete from local database
        billReminderDao.deleteBillReminder(billReminder)
    }
    
    // Sync data from Firebase to local database
    suspend fun syncBillReminders() {
        try {
            val billReminders = firebaseService.fetchAllBillReminders()
            for (billReminder in billReminders) {
                billReminderDao.insertBillReminder(billReminder)
            }
        } catch (e: Exception) {
            // Handle errors - maybe log or show a message
        }
    }
}
