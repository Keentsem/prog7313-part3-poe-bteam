package com.example.pocketsafe.data.repository

import com.example.pocketsafe.data.Subscription
import com.example.pocketsafe.data.dao.SubscriptionDao
import com.example.pocketsafe.firebase.FirebaseService
import kotlinx.coroutines.flow.Flow

/**
 * Repository that handles subscription data operations with both local database and Firebase
 * Modified to work without Hilt to prevent app crashes
 */
class SubscriptionRepository(
    private val subscriptionDao: SubscriptionDao
) {
    // Get Firebase service using singleton pattern
    private val firebaseService: FirebaseService = FirebaseService.getInstance()
    // Local database operations
    fun getAllSubscriptions(): Flow<List<Subscription>> = 
        subscriptionDao.getAllSubscriptions()
        
    fun getActiveSubscriptions(): Flow<List<Subscription>> =
        subscriptionDao.getActiveSubscriptions()
        
    suspend fun getSubscriptionById(id: Long): Subscription? =
        subscriptionDao.getSubscriptionById(id)
        
    // Save to both local and Firebase
    suspend fun saveSubscription(subscription: Subscription) {
        // Save to Firebase first
        val subscriptionId = firebaseService.saveSubscription(subscription)
        
        // If ID was 0, we got a new ID from Firebase
        val finalSubscription = if (subscription.id == 0L) {
            subscription.copy(id = subscriptionId.toLong())
        } else {
            subscription
        }
        
        // Save to local database
        subscriptionDao.insertSubscription(finalSubscription)
    }
    
    suspend fun updateSubscription(subscription: Subscription) {
        // Update in Firebase
        firebaseService.updateSubscription(subscription)
        
        // Update in local database
        subscriptionDao.updateSubscription(subscription)
    }
    
    suspend fun deleteSubscription(subscription: Subscription) {
        // Delete from Firebase
        firebaseService.deleteSubscription(subscription.id.toString())
        
        // Delete from local database
        subscriptionDao.deleteSubscription(subscription)
    }
    
    // Sync data from Firebase to local database
    suspend fun syncSubscriptions() {
        try {
            val subscriptions = firebaseService.fetchAllSubscriptions()
            for (subscription in subscriptions) {
                subscriptionDao.insertSubscription(subscription)
            }
        } catch (e: Exception) {
            // Handle errors - maybe log or show a message
        }
    }
}
