package com.example.pocketsafe.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pocketsafe.data.dao.BudgetGoalDao
import com.example.pocketsafe.data.dao.CategoryDao
import com.example.pocketsafe.data.dao.ExpenseDao
import com.example.pocketsafe.data.dao.SavingsGoalDao
import com.example.pocketsafe.data.dao.UserDao
import com.example.pocketsafe.data.dao.AccountDao

@Database(
    entities = [
        User::class,
        SavingsGoal::class,
        Account::class,
        BudgetGoal::class,
        Category::class,
        Expense::class
    ],
    version = 1
)
@TypeConverters(IconTypeConverter::class)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetGoalDao(): BudgetGoalDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: UserDatabase? = null

        fun getDatabase(context: Context): UserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "user_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 