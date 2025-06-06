package com.example.pocketsafe.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import android.app.Application
import com.example.pocketsafe.MainApplication
import com.example.pocketsafe.data.BillReminder
import com.example.pocketsafe.data.repository.BillReminderRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ViewModel for managing bill reminders
 * Converted to use factory pattern instead of Hilt to avoid crashes
 */
class BillReminderViewModel(
    private val repository: BillReminderRepository
) : ViewModel() {
    
    /**
     * Factory for creating BillReminderViewModel with the repository
     */
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BillReminderViewModel::class.java)) {
                // Create repository here without Hilt
                val database = MainApplication.getDatabase(application)
                val repository = BillReminderRepository(database.billReminderDao())
                return BillReminderViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    // LiveData for currently displayed bill reminders
    private val _billReminders = MutableLiveData<List<BillReminder>>()
    val billReminders: LiveData<List<BillReminder>> = _billReminders

    // Get all bill reminders sorted by due date
    val allBillReminders: LiveData<List<BillReminder>> = repository.getAllBillReminders()
        .asLiveData(viewModelScope.coroutineContext)
        
    // Get unpaid bill reminders
    val unpaidBillReminders: LiveData<List<BillReminder>> = repository.getUnpaidBillReminders()
        .asLiveData(viewModelScope.coroutineContext)
        
    // Get overdue bill reminders
    val overdueBillReminders: LiveData<List<BillReminder>> = repository.getUnpaidBillReminders()
        .map { bills ->
            val currentTime = System.currentTimeMillis()
            bills.filter { bill -> 
                bill.dueDate < currentTime && !bill.paid
            }
        }
        .asLiveData(viewModelScope.coroutineContext)
        
    // Get upcoming bills due in the next 7 days (and not yet paid)
    val upcomingBillReminders: LiveData<List<BillReminder>> = repository.getUnpaidBillReminders()
        .map { bills ->
            val calendar = Calendar.getInstance()
            val currentTime = calendar.timeInMillis
            
            // Calculate timestamp for 7 days from now
            calendar.add(Calendar.DAY_OF_YEAR, 7)
            val sevenDaysLater = calendar.timeInMillis
            
            // Filter bills due in the next 7 days and not yet paid
            bills.filter { bill ->
                bill.dueDate in (currentTime + 1)..sevenDaysLater && !bill.paid
            }
        }
        .asLiveData(viewModelScope.coroutineContext)
        
    // Get total amount due for unpaid bills
    val totalAmountDue: LiveData<Double> = repository.getUnpaidBillReminders()
        .map { bills ->
            bills.sumOf { it.amount }
        }
        .asLiveData(viewModelScope.coroutineContext)
        
    // Add/update bill reminder
    fun saveBillReminder(billReminder: BillReminder) {
        viewModelScope.launch {
            repository.saveBillReminder(billReminder)
        }
    }
    
    // Mark bill as paid or unpaid
    fun markBillAsPaid(billId: String, paid: Boolean = true) {
        viewModelScope.launch {
            repository.markBillAsPaid(billId, paid)
        }
    }
    
    // Delete bill reminder
    fun deleteBillReminder(billReminder: BillReminder) {
        viewModelScope.launch {
            repository.deleteBillReminder(billReminder)
        }
    }
    
    // Sync bill reminders with Firebase
    fun syncBillReminders() {
        viewModelScope.launch {
            repository.syncBillReminders()
        }
    }
    
    // Get all bill reminders and update the LiveData
    fun getAllBillReminders() {
        viewModelScope.launch {
            _billReminders.value = repository.getAllBillReminders().first()
        }
    }
    
    // Get upcoming bill reminders and update the LiveData
    fun getUpcomingBillReminders() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val currentTime = calendar.timeInMillis
            
            // Calculate timestamp for 7 days from now
            calendar.add(Calendar.DAY_OF_YEAR, 7)
            val sevenDaysLater = calendar.timeInMillis
            
            // Filter bills due in the next 7 days and not yet paid
            val upcoming = repository.getUnpaidBillReminders().first().filter { bill ->
                bill.dueDate in (currentTime + 1)..sevenDaysLater && !bill.paid
            }
            
            _billReminders.value = upcoming
        }
    }
    
    // Get paid bill reminders and update the LiveData
    fun getPaidBillReminders() {
        viewModelScope.launch {
            _billReminders.value = repository.getAllBillReminders().first().filter { it.paid }
        }
    }
    
    // Get a specific bill reminder by ID
    fun getBillReminderById(id: String): LiveData<BillReminder?> {
        val result = MutableLiveData<BillReminder?>()
        viewModelScope.launch {
            val billReminder = repository.getAllBillReminders().first().find { it.id == id }
            result.value = billReminder
        }
        return result
    }
    
    // Update an existing bill reminder
    fun updateBillReminder(billReminder: BillReminder) {
        viewModelScope.launch {
            repository.updateBillReminder(billReminder)
        }
    }
    
    // Add a new bill reminder
    fun addBillReminder(billReminder: BillReminder) {
        viewModelScope.launch {
            repository.saveBillReminder(billReminder)
        }
    }
}
