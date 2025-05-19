package com.example.pocketsafe.ui.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketsafe.data.Expense
import com.example.pocketsafe.data.PocketSafeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: PocketSafeRepository
) : ViewModel() {
    
    val allExpenses: Flow<List<Expense>> = repository.getAllExpenses()

    fun getExpensesByCategory(categoryId: Int): Flow<List<Expense>> =
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

    fun getExpensesByDateRange(startDate: String, endDate: String): Flow<List<Expense>> =
        repository.getExpensesByDateRange(startDate, endDate)

    fun getExpensesByCategoryAndDateRange(categoryId: Int, startDate: String, endDate: String): Flow<List<Expense>> =
        repository.getExpensesByCategoryAndDateRange(categoryId, startDate, endDate)
} 