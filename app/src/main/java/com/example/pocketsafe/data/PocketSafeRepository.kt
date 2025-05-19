package com.example.pocketsafe.data

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import com.example.pocketsafe.data.dao.CategoryDao
import com.example.pocketsafe.data.dao.ExpenseDao
import com.example.pocketsafe.data.dao.BudgetGoalDao
import com.example.pocketsafe.data.Category
import com.example.pocketsafe.data.Expense
import com.example.pocketsafe.data.BudgetGoal

class PocketSafeRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val budgetGoalDao: BudgetGoalDao
) {
    // Category operations
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    
    suspend fun insertCategory(category: Category) = categoryDao.insertCategory(category)
    
    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)
    
    suspend fun getCategoryById(id: Int): Category? = categoryDao.getCategoryById(id)

    // Expense operations
    fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()
    
    fun getExpensesByCategory(categoryId: Int): Flow<List<Expense>> = 
        expenseDao.getExpensesByCategory(categoryId)
    
    suspend fun getAllExpenseCategories() = expenseDao.getAllExpenseCategories()
    
    fun getExpensesByDateRange(startDate: String, endDate: String): Flow<List<Expense>> =
        expenseDao.getExpensesByDateRange(startDate, endDate)
    
    fun getExpensesByCategoryAndDateRange(categoryId: Int, startDate: String, endDate: String): Flow<List<Expense>> =
        expenseDao.getExpensesByCategoryAndDateRange(categoryId, startDate, endDate)
    
    suspend fun insertExpense(expense: Expense) = expenseDao.insertExpense(expense)
    
    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)
    
    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)
    
    suspend fun getExpenseById(id: Int): Expense? = expenseDao.getExpenseById(id)

    suspend fun getTotalExpensesByCategory(categoryId: Int): Double =
        expenseDao.getTotalExpensesByCategory(categoryId)

    // Budget goal operations
    fun getAllBudgetGoals(): Flow<List<BudgetGoal>> = budgetGoalDao.getAllBudgetGoals()
    
    suspend fun insertBudgetGoal(goal: BudgetGoal) = budgetGoalDao.insertBudgetGoal(goal)

    suspend fun deleteBudgetGoal(goal: BudgetGoal) = budgetGoalDao.deleteBudgetGoal(goal)

    suspend fun getBudgetGoalById(id: Int): BudgetGoal? = budgetGoalDao.getBudgetGoalById(id)

    suspend fun getCurrentBudgetGoal(date: Long): BudgetGoal? = budgetGoalDao.getCurrentBudgetGoal(date)
} 