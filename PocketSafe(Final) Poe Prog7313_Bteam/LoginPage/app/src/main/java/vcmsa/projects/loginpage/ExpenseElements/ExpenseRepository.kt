package vcmsa.projects.loginpage.ExpenseElements

import vcmsa.projects.loginpage.CategoryElements.Category
import vcmsa.projects.loginpage.CategoryElements.CategoryDao
import vcmsa.projects.loginpage.data.ExpenseDao

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao
) {

    suspend fun insertExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
    }

    suspend fun getAllExpenses(): List<Expense> {
        return expenseDao.getAllExpenses()
    }

    suspend fun getAllCategories(): List<Category> {
        return categoryDao.getAllCategories()
    }

    suspend fun getDistinctExpenseCategories(): List<String> {
        return expenseDao.getAllExpenseCategories()
    }
}
