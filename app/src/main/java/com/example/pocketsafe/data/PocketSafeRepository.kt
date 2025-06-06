package com.example.pocketsafe.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.collect
import androidx.lifecycle.LiveData
import com.example.pocketsafe.data.dao.*
import com.example.pocketsafe.data.*
import com.example.pocketsafe.firebase.FirebaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository that handles expense, category and budget goal operations
 * Modified to work without Hilt to prevent app crashes
 * Maintains the pixel-retro theme styling with gold (#f3c34e) and brown (#5b3f2c) colors
 */
class PocketSafeRepository(database: AppDatabase) {
    private val categoryDao: CategoryDao = database.categoryDao()
    private val expenseDao: ExpenseDao = database.expenseDao()
    private val budgetGoalDao: BudgetGoalDao = database.budgetGoalDao()
    private val subscriptionDao: SubscriptionDao = database.subscriptionDao()
    private val billReminderDao: BillReminderDao = database.billReminderDao()
    private val savingsGoalDao: SavingsGoalDao = database.savingsGoalDao()
    private val accountDao: AccountDao = database.accountDao()
    private val firebaseService: FirebaseService = FirebaseService.getInstance()

    // Category operations
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    
    suspend fun insertCategory(category: Category) = categoryDao.insertCategory(category)
    
    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)
    
    suspend fun getCategoryById(id: Int): Category? = categoryDao.getAllCategories().firstOrNull()?.find { it.id == id }

    // Expense operations
    fun getAllExpenses(): List<Expense> = expenseDao.getAllExpenses()
    
    fun getExpensesByCategory(categoryId: Int): List<Expense> = 
        expenseDao.getExpensesByCategory(categoryId)
    
    suspend fun getAllExpenseCategories() = expenseDao.getAllExpenseCategories()
    
    // Add implementation for these methods
    fun getExpensesByDateRange(startDate: String, endDate: String): List<Expense> {
        // Filter expenses by date range manually if DAO doesn't have this method
        val allExpenses = expenseDao.getAllExpenses()
        val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        
        val start = format.parse(startDate)?.time ?: 0L
        val end = format.parse(endDate)?.time ?: System.currentTimeMillis()
        
        return allExpenses.filter { expense ->
            val expenseDate = expense.date
            expenseDate in start..end
        }
    }
    
    fun getExpensesByCategoryAndDateRange(categoryId: Int, startDate: String, endDate: String): List<Expense> {
        // Filter expenses by category and date range manually
        val categoryExpenses = expenseDao.getExpensesByCategory(categoryId)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        
        val start = format.parse(startDate)?.time ?: 0L
        val end = format.parse(endDate)?.time ?: System.currentTimeMillis()
        
        return categoryExpenses.filter { expense ->
            val expenseDate = expense.date
            expenseDate in start..end
        }
    }
    
    suspend fun insertExpense(expense: Expense) = expenseDao.insertExpense(expense)
    
    suspend fun updateExpense(expense: Expense) = expenseDao.deleteExpense(expense).also {
        expenseDao.insertExpense(expense)
    }
    
    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)
    
    fun getExpenseById(id: Int): Expense? {
        return expenseDao.getAllExpenses().find { it.id.toInt() == id }
    }

    fun getTotalExpensesByCategory(categoryId: Int): Double {
        val expenses = expenseDao.getExpensesByCategory(categoryId)
        return expenses.sumOf { it.amount }
    }

    // Budget goal operations
    // Budget goal operations must return LiveData or immediate results
    fun getAllBudgetGoals(): Flow<List<BudgetGoal>> = budgetGoalDao.getAllBudgetGoals()
    
    suspend fun insertBudgetGoal(goal: BudgetGoal) = budgetGoalDao.insertBudgetGoal(goal)

    suspend fun deleteBudgetGoal(goal: BudgetGoal) = budgetGoalDao.deleteBudgetGoal(goal)

    suspend fun getBudgetGoalById(id: Long): BudgetGoal? = budgetGoalDao.getBudgetGoalById(id)

    suspend fun getCurrentBudgetGoal(date: Long): BudgetGoal? {
        val goals = budgetGoalDao.getAllBudgetGoals().first()
        // Convert the date to month and year
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = date
        val month = calendar.get(java.util.Calendar.MONTH) + 1 // Calendar months are 0-based
        val year = calendar.get(java.util.Calendar.YEAR)
        
        // Find a goal for this month/year
        val matchGoal = goals.find { goal -> goal.month == month && goal.year == year }
        if (matchGoal != null) {
            return matchGoal
        }
        
        // Fall back to the goal with the highest ID if no exact match
        return if (goals.isNotEmpty()) {
            goals.maxByOrNull { it.id }
        } else {
            null
        }
    }

    // Subscription operations
    fun getAllSubscriptions(): Flow<List<Subscription>> = subscriptionDao.getAllSubscriptions()
    
    fun getActiveSubscriptions(): Flow<List<Subscription>> = subscriptionDao.getActiveSubscriptions()
    
    // Get active subscriptions as a direct list
    suspend fun getActiveSubscriptionsList(): List<Subscription> = subscriptionDao.getActiveSubscriptionsList()
    
    suspend fun insertSubscription(subscription: Subscription): Long = subscriptionDao.insertSubscription(subscription)
    
    suspend fun updateFirebaseSubscription(id: Long, subscription: Subscription) {
        // Here we would update a remote Firebase subscription
        // The FirebaseService handles ID conversion internally
        // We need to update the subscription's ID before passing it to the service
        val updatedSubscription = if (subscription.firebaseId.isBlank()) {
            subscription.copy(id = id) // Ensure the subscription has the right ID
        } else {
            subscription
        }
        firebaseService.updateSubscription(updatedSubscription)
    }
    
    suspend fun updateSubscription(subscription: Subscription) = subscriptionDao.updateSubscription(subscription)
    
    suspend fun deleteSubscription(subscription: Subscription) = subscriptionDao.deleteSubscription(subscription)
    
    suspend fun getSubscriptionById(id: Long): Subscription? = subscriptionDao.getSubscriptionById(id)

    // Bill reminder operations
    fun getAllBillReminders(): Flow<List<BillReminder>> = billReminderDao.getAllBillReminders()
    
    suspend fun getUnpaidBills(): List<BillReminder> {
        return withContext(Dispatchers.IO) {
            // Use the direct DAO method instead of filtering Flow
            billReminderDao.getUnpaidBills()
        }
    }
    
    suspend fun insertBillReminder(bill: BillReminder) { billReminderDao.insertBillReminder(bill) }
    
    suspend fun updateBillReminder(bill: BillReminder) = billReminderDao.updateBillReminder(bill)
    
    suspend fun deleteBillReminder(bill: BillReminder) = billReminderDao.deleteBillReminder(bill)
    
    suspend fun getBillReminderById(id: String): BillReminder? = billReminderDao.getBillReminderById(id)

    // Savings goal operations
    fun getAllSavingsGoals(): Flow<List<SavingsGoal>> = savingsGoalDao.getAllGoals()
    
    suspend fun insertSavingsGoal(goal: SavingsGoal): Long = savingsGoalDao.insert(goal)
    
    suspend fun updateSavingsGoal(goal: SavingsGoal) { savingsGoalDao.updateGoal(goal) }
    
    suspend fun deleteSavingsGoal(goal: SavingsGoal) { savingsGoalDao.deleteGoal(goal) }
    
    suspend fun getSavingsGoalById(id: Int): SavingsGoal? {
        return savingsGoalDao.getGoalById(id)
    }
} 