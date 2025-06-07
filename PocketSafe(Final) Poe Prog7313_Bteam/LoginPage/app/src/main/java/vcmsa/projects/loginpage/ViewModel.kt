package vcmsa.projects.loginpage.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import vcmsa.projects.loginpage.ExpenseElements.Expense
import vcmsa.projects.loginpage.ExpenseElements.ExpenseFirebaseRepository

class ExpenseViewModel(private val expenseRepository: ExpenseFirebaseRepository) : ViewModel() {

    private val _filteredExpenses = MutableLiveData<List<Expense>>()
    val filteredExpenses: LiveData<List<Expense>> get() = _filteredExpenses

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.addExpenseToFirebase(expense)
        }
    }

    fun filterExpenses(category: String, startDate: String, endDate: String) {
        viewModelScope.launch {
            val expenses = expenseRepository.getFilteredExpensesFirebase(category, startDate, endDate)
            _filteredExpenses.postValue(expenses)
        }
    }
}
