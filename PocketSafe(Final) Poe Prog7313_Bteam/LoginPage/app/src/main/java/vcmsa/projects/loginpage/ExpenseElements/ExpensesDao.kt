package vcmsa.projects.loginpage.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import vcmsa.projects.loginpage.ExpenseElements.Expense

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expense WHERE id = :expenseId")
    suspend fun getExpenseById(expenseId: Long): Expense?

    @Query("SELECT * FROM expense")
    suspend fun getAllExpenses(): List<Expense>

    @Query("SELECT * FROM expense WHERE categoryName = :categoryName")
    suspend fun getExpensesByCategory(categoryName: String): List<Expense>
    @Query("SELECT * FROM expense WHERE strftime('%Y-%m', startDate) = :yearMonth")
    suspend fun getExpensesForMonth(yearMonth: String): List<Expense>

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT DISTINCT categoryName FROM expense")
    suspend fun getAllExpenseCategories(): List<String>

    // 👇 Add this delete by ID
    @Query("DELETE FROM expense WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: Long)
}
