package com.example.pocketsafe.ui.expense

import android.app.Application
import androidx.lifecycle.*
import androidx.lifecycle.viewModelScope
import com.example.pocketsafe.MainApplication
import com.example.pocketsafe.data.Expense
import com.example.pocketsafe.data.PocketSafeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*
import java.text.SimpleDateFormat

/**
 * ViewModel for managing expenses
 * Converted to use factory pattern instead of Hilt to prevent app crashes
 * Maintains pixel-retro theme styling for expense UI elements
 */
class ExpenseViewModel(
    private val repository: PocketSafeRepository
) : ViewModel() {
    
    /**
     * Factory for creating ExpenseViewModel with the repository
     */
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
                // Create repository here without Hilt
                val database = MainApplication.getDatabase(application)
                val repository = PocketSafeRepository(database)
                return ExpenseViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
    
    // Convert to LiveData for better compose integration
    private val _allExpenses = MutableLiveData<List<Expense>>()
    val allExpenses: LiveData<List<Expense>> = _allExpenses
    
    // Recent expenses for dashboard
    private val _recentExpenses = MutableLiveData<List<Expense>>()
    val recentExpenses: LiveData<List<Expense>> = _recentExpenses
    
    init {
        // Load expenses on initialization
        loadAllExpenses()
        loadRecentExpenses()
    }
    
    // Update expenses from repository - changed to List instead of Flow
    fun loadAllExpenses() = viewModelScope.launch {
        _allExpenses.value = repository.getAllExpenses()
    }
    
    // Load recent expenses from last 30 days
    fun loadRecentExpenses() = viewModelScope.launch {
        val cal = Calendar.getInstance()
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(cal.timeInMillis))
        cal.add(Calendar.DAY_OF_MONTH, -30)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(cal.timeInMillis))
        
        _recentExpenses.value = repository.getExpensesByDateRange(startDate, endDate)
    }
    
    fun getExpensesByCategory(categoryId: Int): List<Expense> =
        repository.getExpensesByCategory(categoryId)

    suspend fun getExpenseById(id: Int): Expense? =
        repository.getExpenseById(id)

    fun addExpense(expense: Expense) = viewModelScope.launch {
        repository.insertExpense(expense)
    }

    fun updateExpense(expense: Expense) = viewModelScope.launch {
        repository.updateExpense(expense)
    }

    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        repository.deleteExpense(expense)
    }

    suspend fun getTotalExpensesByCategory(categoryId: Int): Double =
        repository.getTotalExpensesByCategory(categoryId)

    suspend fun getAllExpenseCategories() = repository.getAllExpenseCategories()

    fun getExpensesByDateRange(startDate: String, endDate: String): List<Expense> =
        repository.getExpensesByDateRange(startDate, endDate)

    fun getExpensesByCategoryAndDateRange(categoryId: Int, startDate: String, endDate: String): List<Expense> =
        repository.getExpensesByCategoryAndDateRange(categoryId, startDate, endDate)
} 