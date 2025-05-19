package com.example.pocketsafe.di

import android.content.Context
import androidx.room.Room
import com.example.pocketsafe.data.AppDatabase
import com.example.pocketsafe.data.UserDatabase
import com.example.pocketsafe.data.dao.BudgetGoalDao
import com.example.pocketsafe.data.dao.CategoryDao
import com.example.pocketsafe.data.dao.ExpenseDao
import com.example.pocketsafe.data.dao.UserDao
import com.example.pocketsafe.data.dao.AccountDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): UserDatabase = Room.databaseBuilder(
        context,
        UserDatabase::class.java,
        "user_database"
    ).build()

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "app_database"
    ).build()

    @Provides
    fun provideUserDao(database: UserDatabase): UserDao = database.userDao()

    @Provides
    fun provideSavingsGoalDao(database: UserDatabase) = database.savingsGoalDao()

    @Provides
    fun provideAccountDao(database: UserDatabase): AccountDao = database.accountDao()

    @Provides
    fun provideBudgetGoalDao(database: UserDatabase) = database.budgetGoalDao()

    @Provides
    fun provideCategoryDao(database: UserDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideExpenseDao(database: UserDatabase) = database.expenseDao()
} 