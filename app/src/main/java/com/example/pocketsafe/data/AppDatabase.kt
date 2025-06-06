package com.example.pocketsafe.data

import android.content.Context
import androidx.room.*
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.pocketsafe.data.dao.*
import java.util.*

// FIXED: Renamed to avoid conflict with existing Converters class
class DatabaseConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromIconType(iconType: IconType): String = iconType.name

    @TypeConverter
    fun toIconType(value: String): IconType = IconType.valueOf(value)
}

@Database(
    entities = [
        Expense::class,
        Category::class,
        BudgetGoal::class,
        Subscription::class,
        User::class,
        BillReminder::class,
        SavingsGoal::class,
        Account::class
    ],
    version = 11,  // Increased version for complete rebuild
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {

    // Core DAOs that definitely exist
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao

    // Required DAOs for application functionality
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun billReminderDao(): BillReminderDao
    
    // Additional DAOs for full functionality
    abstract fun budgetGoalDao(): BudgetGoalDao
    abstract fun userDao(): UserDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pocket_safe_database_v11"  // FIXED: Underscore instead of camelCase
                )
                    .fallbackToDestructiveMigration() // Recreate tables on schema changes
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Helper method to reset database during development
        fun resetDatabase(context: Context) {
            synchronized(this) {
                INSTANCE?.close()
                context.deleteDatabase("pocket_safe_database_v11")
                INSTANCE = null
            }
        }
    }
}