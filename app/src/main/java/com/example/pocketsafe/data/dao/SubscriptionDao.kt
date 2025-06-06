package com.example.pocketsafe.data.dao

import androidx.room.*
import com.example.pocketsafe.data.Subscription
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY nextDueDate ASC")
    fun getAllSubscriptions(): Flow<List<Subscription>>

    // Using active_status column name as per updated entity
    @Query("SELECT * FROM subscriptions WHERE active_status = 1 ORDER BY nextDueDate ASC")
    fun getActiveSubscriptions(): Flow<List<Subscription>>

    // Using active_status column name as per updated entity
    @Query("SELECT * FROM subscriptions WHERE active_status = 1 ORDER BY nextDueDate ASC")
    suspend fun getActiveSubscriptionsList(): List<Subscription>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getSubscriptionById(id: Long): Subscription?

    // Get subscriptions due soon (next 7 days)
    @Query("SELECT * FROM subscriptions WHERE active_status = 1 AND nextDueDate <= :dueDate ORDER BY nextDueDate ASC")
    suspend fun getSubscriptionsDueSoon(dueDate: Long): List<Subscription>

    // Get subscriptions by category
    @Query("SELECT * FROM subscriptions WHERE categoryId = :categoryId AND active_status = 1")
    suspend fun getSubscriptionsByCategory(categoryId: Int): List<Subscription>

    @Insert
    suspend fun insertSubscription(subscription: Subscription): Long

    @Update
    suspend fun updateSubscription(subscription: Subscription)

    @Delete
    suspend fun deleteSubscription(subscription: Subscription)

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteSubscriptionById(id: Long)

    // Update subscription status
    @Query("UPDATE subscriptions SET active_status = :isActive WHERE id = :id")
    suspend fun updateSubscriptionStatus(id: Long, isActive: Boolean)

    // Update next due date (for recurring subscriptions)
    @Query("UPDATE subscriptions SET nextDueDate = :nextDueDate WHERE id = :id")
    suspend fun updateNextDueDate(id: Long, nextDueDate: Long)

    // Get total monthly subscription cost
    @Query("SELECT SUM(amount) FROM subscriptions WHERE active_status = 1 AND frequency = 'MONTHLY'")
    suspend fun getTotalMonthlySubscriptionCost(): Double?

    // Get total yearly subscription cost
    @Query("SELECT SUM(amount) FROM subscriptions WHERE active_status = 1 AND frequency = 'YEARLY'")
    suspend fun getTotalYearlySubscriptionCost(): Double?
}