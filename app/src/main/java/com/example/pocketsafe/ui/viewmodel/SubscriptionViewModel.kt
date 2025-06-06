package com.example.pocketsafe.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.pocketsafe.data.Subscription
import com.example.pocketsafe.data.repository.SubscriptionRepository
import com.example.pocketsafe.data.RenewalPeriod
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar
import android.app.Application
import com.example.pocketsafe.MainApplication

/**
 * ViewModel for managing subscription data
 * Uses Factory pattern instead of Hilt to avoid crashes
 */
class SubscriptionViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {
    
    /**
     * Factory for creating SubscriptionViewModel with the repository
     */
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SubscriptionViewModel::class.java)) {
                // Create repository here without Hilt
                val database = MainApplication.getDatabase(application)
                val repository = SubscriptionRepository(database.subscriptionDao())
                return SubscriptionViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    // Error handling
    private val _error = MutableLiveData<String>("")
    val error: LiveData<String> = _error

    // LiveData for currently displayed subscriptions
    private val _subscriptions = MutableLiveData<List<Subscription>>()
    val subscriptions: LiveData<List<Subscription>> = _subscriptions

    // Get all subscriptions
    val allSubscriptions: LiveData<List<Subscription>> = repository.getAllSubscriptions()
        .asLiveData(viewModelScope.coroutineContext)
        
    // Get active subscriptions
    val activeSubscriptions: LiveData<List<Subscription>> = repository.getActiveSubscriptions()
        .asLiveData(viewModelScope.coroutineContext)
        
    // Get upcoming subscriptions due in the next 7 days
    val upcomingSubscriptions: LiveData<List<Subscription>> = repository.getActiveSubscriptions()
        .map { subscriptions ->
            val calendar = Calendar.getInstance()
            val currentTime = calendar.timeInMillis
            
            // Calculate timestamp for 7 days from now
            calendar.add(Calendar.DAY_OF_YEAR, 7)
            val sevenDaysLater = calendar.timeInMillis
            
            // Filter subscriptions due in the next 7 days
            subscriptions.filter { subscription ->
                subscription.renewalDate in (currentTime + 1)..sevenDaysLater
            }
        }
        .asLiveData(viewModelScope.coroutineContext)
        
    // Get total monthly subscription cost
    val monthlySubscriptionCost: LiveData<Double> = repository.getActiveSubscriptions()
        .map { subscriptions ->
            subscriptions.sumOf { subscription ->
                when (subscription.renewalPeriod) {
                    RenewalPeriod.DAILY -> subscription.amount * 30
                    RenewalPeriod.WEEKLY -> subscription.amount * 4
                    RenewalPeriod.MONTHLY -> subscription.amount
                    RenewalPeriod.QUARTERLY -> subscription.amount / 3
                    RenewalPeriod.YEARLY -> subscription.amount / 12
                }
            }
        }
        .asLiveData(viewModelScope.coroutineContext)
        
    // Add/update subscription
    fun saveSubscription(subscription: Subscription) {
        viewModelScope.launch {
            try {
                repository.saveSubscription(subscription)
                _error.value = "" // Clear any previous errors
            } catch (e: Exception) {
                _error.value = "Failed to save subscription: ${e.message}"
            }
        }
    }
    
    // Delete subscription
    fun deleteSubscription(subscription: Subscription) {
        viewModelScope.launch {
            try {
                repository.deleteSubscription(subscription)
                _error.value = "" // Clear any previous errors
            } catch (e: Exception) {
                _error.value = "Failed to delete subscription: ${e.message}"
            }
        }
    }
    
    // Toggle subscription active status
    fun toggleSubscriptionStatus(subscription: Subscription) {
        viewModelScope.launch {
            val updatedSubscription = subscription.copy(activeStatus = !subscription.activeStatus)
            repository.updateSubscription(updatedSubscription)
        }
    }
    
    // Sync subscriptions with Firebase
    fun syncSubscriptions() {
        viewModelScope.launch {
            repository.syncSubscriptions()
        }
    }
    
    // Get all subscriptions and update the LiveData
    fun getAllSubscriptions() {
        viewModelScope.launch {
            try {
                _subscriptions.value = repository.getAllSubscriptions().first()
                _error.value = "" // Clear any previous errors
            } catch (e: Exception) {
                _error.value = "Failed to load subscriptions: ${e.message}"
                _subscriptions.value = emptyList() // Provide empty list as fallback
            }
        }
    }
    
    // Get active subscriptions and update the LiveData
    fun getActiveSubscriptions() {
        viewModelScope.launch {
            try {
                _subscriptions.value = repository.getActiveSubscriptions().first()
                _error.value = "" // Clear any previous errors
            } catch (e: Exception) {
                _error.value = "Failed to load active subscriptions: ${e.message}"
                _subscriptions.value = emptyList() // Provide empty list as fallback
            }
        }
    }
    
    // Get upcoming subscriptions and update the LiveData
    fun getUpcomingSubscriptions() {
        viewModelScope.launch {
            try {
                val activeSubscriptions = repository.getActiveSubscriptions().first()
                val calendar = Calendar.getInstance()
                val currentTime = calendar.timeInMillis
                
                // Calculate timestamp for 7 days from now
                calendar.add(Calendar.DAY_OF_YEAR, 7)
                val sevenDaysLater = calendar.timeInMillis
                
                // Filter subscriptions due in the next 7 days
                _subscriptions.value = activeSubscriptions.filter { subscription ->
                    subscription.renewalDate in (currentTime + 1)..sevenDaysLater
                }
                _error.value = "" // Clear any previous errors
            } catch (e: Exception) {
                _error.value = "Failed to load upcoming subscriptions: ${e.message}"
                _subscriptions.value = emptyList() // Provide empty list as fallback
            }
        }
    }
    
    // Get a specific subscription by ID
    suspend fun getSubscriptionById(id: Long): Subscription? {
        return repository.getSubscriptionById(id)
    }
    
    // Update an existing subscription
    fun updateSubscription(subscription: Subscription) {
        viewModelScope.launch {
            try {
                repository.updateSubscription(subscription)
                _error.value = "" // Clear any previous errors
            } catch (e: Exception) {
                _error.value = "Failed to update subscription: ${e.message}"
            }
        }
    }
    
    // Save all subscriptions - used by the save button in the navigation bar
    fun saveAllSubscriptions() {
        viewModelScope.launch {
            try {
                // Get current list of subscriptions and save them all
                val currentSubscriptions = _subscriptions.value ?: return@launch
                
                // Update lastUpdated timestamp for all subscriptions
                val updatedSubscriptions = currentSubscriptions.map { 
                    it.copy(lastUpdated = System.currentTimeMillis())
                }
                
                // Save all subscriptions
                updatedSubscriptions.forEach { subscription ->
                    repository.updateSubscription(subscription)
                }
                
                _error.value = "" // Clear any previous errors
            } catch (e: Exception) {
                _error.value = "Failed to save all subscriptions: ${e.message}"
            }
        }
    }
    
    // Add a new subscription
    fun addSubscription(subscription: Subscription) {
        viewModelScope.launch {
            try {
                repository.saveSubscription(subscription)
                _error.value = "" // Clear any previous errors
            } catch (e: Exception) {
                _error.value = "Failed to add subscription: ${e.message}"
            }
        }
    }
}
