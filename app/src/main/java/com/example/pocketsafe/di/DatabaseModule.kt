package com.example.pocketsafe.di

/**
 * TEMPORARILY DISABLED
 * 
 * This Hilt module is disabled because Hilt has been commented out in the build.gradle.kts file.
 * Instead, we're using the MainApplication singleton pattern to provide database access.
 * This file is kept for reference when Hilt is re-enabled.
 */

/*
import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.pocketsafe.data.AppDatabase
import com.example.pocketsafe.data.dao.AccountDao
import com.example.pocketsafe.data.dao.BillReminderDao
import com.example.pocketsafe.data.dao.BudgetGoalDao
import com.example.pocketsafe.data.dao.CategoryDao
import com.example.pocketsafe.data.dao.ExpenseDao
import com.example.pocketsafe.data.dao.SubscriptionDao
import com.example.pocketsafe.data.dao.UserDao
import com.example.pocketsafe.data.dao.SavingsGoalDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val TAG = "DatabaseModule"

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        try {
            Log.d(TAG, "Getting AppDatabase using singleton")
            return com.example.pocketsafe.MainApplication.getDatabase(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting AppDatabase: ${e.message}")
            // Last resort fallback - create a new in-memory database
            return Room.inMemoryDatabaseBuilder(
                context,
                AppDatabase::class.java
            ).build()
        }
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    fun provideSavingsGoalDao(database: AppDatabase): SavingsGoalDao = database.savingsGoalDao()

    @Provides
    fun provideBudgetGoalDao(database: AppDatabase): BudgetGoalDao = database.budgetGoalDao()

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideExpenseDao(database: AppDatabase): ExpenseDao = database.expenseDao()

    @Provides
    fun provideSubscriptionDao(database: AppDatabase): SubscriptionDao = database.subscriptionDao()
    
    @Provides
    fun provideBillReminderDao(database: AppDatabase): BillReminderDao = database.billReminderDao()
    
    @Provides
    fun provideAccountDao(database: AppDatabase): AccountDao = database.accountDao()
}
*/