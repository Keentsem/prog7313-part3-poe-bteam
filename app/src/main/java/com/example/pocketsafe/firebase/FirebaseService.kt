package com.example.pocketsafe.firebase

import android.util.Log
import com.example.pocketsafe.data.BillReminder
import com.example.pocketsafe.data.Expense
import com.example.pocketsafe.data.SavingsGoal
import com.example.pocketsafe.data.Subscription
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Service to handle all Firebase operations in a centralized place
 * Uses Firestore for data storage
 * Converted to singleton pattern without Hilt to avoid crashes
 */
class FirebaseService private constructor() {
    
    companion object {
        private const val TAG = "FirebaseService"
        @Volatile
        private var instance: FirebaseService? = null
        
        fun getInstance(): FirebaseService {
            return instance ?: synchronized(this) {
                instance ?: FirebaseService().also { instance = it }
            }
        }
    }
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    // Collection references
    private val usersCollection = db.collection("users")
    
    // Get current user ID
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    }
    
    private fun getUserDocRef() = usersCollection.document(getCurrentUserId())
    
    // Expense Operations
    suspend fun saveExpense(expense: Expense): Long {
        // For Firebase, convert the Long ID to a document ID
        val documentId = expense.id.toString()
        // We don't modify the original expense id since it's a Long
        
        getUserDocRef()
            .collection("expenses")
            .document(documentId)
            .set(expense)
            .await()
            
        return expense.id
    }
    
    suspend fun fetchExpensesByCategory(categoryId: Int): List<Expense> {
        return getUserDocRef()
            .collection("expenses")
            .whereEqualTo("categoryId", categoryId)
            .get()
            .await()
            .toObjects(Expense::class.java)
    }
    
    suspend fun fetchExpensesByCategoryAndDateRange(categoryId: Int, startTimestamp: Long, endTimestamp: Long): List<Expense> {
        // First fetch by category
        val expenses = fetchExpensesByCategory(categoryId)
        
        // Then filter by date range - note that expense.date is already a Long timestamp
        return expenses.filter { expense ->
            // The expense.date is already a timestamp in milliseconds
            expense.date >= startTimestamp && expense.date <= endTimestamp
        }
    }
    
    suspend fun fetchAllExpenses(): List<Expense> {
        return getUserDocRef()
            .collection("expenses")
            .get()
            .await()
            .toObjects(Expense::class.java)
    }
    
    suspend fun deleteExpense(expenseId: String) {
        getUserDocRef()
            .collection("expenses")
            .document(expenseId)
            .delete()
            .await()
    }
    
    // Savings Goal Operations
    suspend fun saveUserGoal(goal: SavingsGoal): Int {
        // Use firebaseId if it exists, otherwise generate one from the local ID
        val firebaseId = if (goal.firebaseId.isNotBlank()) goal.firebaseId else UUID.randomUUID().toString()
        
        // Create an updated copy with the Firebase ID and current timestamp
        val updatedGoal = goal.copy(
            firebaseId = firebaseId,
            lastUpdated = System.currentTimeMillis()
        )
        
        getUserDocRef()
            .collection("savingsGoals") // Using consistent collection name
            .document(firebaseId)
            .set(updatedGoal)
            .await()
            
        return goal.id
    }
    
    suspend fun fetchUserGoals(): List<SavingsGoal> {
        try {
            return getUserDocRef()
                .collection("savingsGoals") // Using consistent collection name
                .get()
                .await()
                .toObjects(SavingsGoal::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error fetching savings goals: ${e.message}")
            return emptyList() // Return empty list instead of crashing
        }
    }
    
    /**
     * Update a savings goal's progress with current amount saved
     * @param goalId Firebase document ID for the goal
     * @param currentAmount Current amount saved toward the goal
     */
    suspend fun updateGoalProgress(goalId: String, currentAmount: Double) {
        try {
            getUserDocRef()
                .collection("savingsGoals")
                .document(goalId)
                .update(
                    mapOf(
                        "currentAmount" to currentAmount,
                        "current_amount" to currentAmount, // For backward compatibility
                        "lastUpdated" to System.currentTimeMillis()
                    )
                )
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error updating goal progress: ${e.message}")
            throw e
        }
    }
    
    /**
     * Delete a savings goal using its Firebase ID
     * @param goalId Firebase document ID for the goal
     */
    suspend fun deleteSavingsGoal(goalId: String) {
        try {
            getUserDocRef()
                .collection("savingsGoals")
                .document(goalId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error deleting savings goal: ${e.message}")
            throw e
        }
    }
    
    /**
     * Delete a savings goal using its Room ID (int)
     * @param id Room database ID for the goal
     */
    suspend fun deleteSavingsGoalById(id: Int) {
        try {
            // First fetch the goal to get the Firebase document ID
            val goals = fetchUserGoals().filter { it.id == id }
            if (goals.isNotEmpty()) {
                val goal = goals.first()
                val docId = if (goal.firebaseId.isNotBlank()) goal.firebaseId else id.toString()
                deleteSavingsGoal(docId)
            }
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error deleting savings goal by ID: ${e.message}")
            throw e
        }
    }
    
    // Subscription Operations
    suspend fun saveSubscription(subscription: Subscription): Long {
        try {
            // Generate a Firebase document ID if one doesn't exist
            val firebaseId = if (subscription.firebaseId.isEmpty()) UUID.randomUUID().toString() 
                else subscription.firebaseId
            
            // Create a copy with the Firebase ID
            val subscriptionWithId = subscription.copy(firebaseId = firebaseId)
            
            // Save to Firebase
            getUserDocRef()
                .collection("subscriptions")
                .document(firebaseId)
                .set(subscriptionWithId)
                .await()
                
            return subscription.id
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error saving subscription: ${e.message}")
            throw e
        }
    }
    
    suspend fun fetchAllSubscriptions(): List<Subscription> {
        try {
            return getUserDocRef()
                .collection("subscriptions")
                .get()
                .await()
                .toObjects(Subscription::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error fetching subscriptions: ${e.message}")
            return emptyList() // Return empty list instead of crashing
        }
    }
    
    suspend fun updateSubscription(subscription: Subscription) {
        // Use firebase ID if available, otherwise use the local ID
        val docId = if (subscription.firebaseId.isNotBlank()) {
            subscription.firebaseId
        } else {
            subscription.id.toString()
        }
        
        // Ensure we have a proper document ID
        if (docId.isBlank()) {
            throw IllegalArgumentException("Subscription ID cannot be empty for updates")
        }
        
        getUserDocRef()
            .collection("subscriptions")
            .document(docId)
            .set(subscription)
            .await()
    }
    
    /**
     * Delete a subscription using either its Firebase document ID (string) or numeric Room ID
     * @param subscriptionId Firebase document ID or Room local ID as a string
     */
    suspend fun deleteSubscription(subscriptionId: String) {
        getUserDocRef()
            .collection("subscriptions")
            .document(subscriptionId)
            .delete()
            .await()
    }
    
    /**
     * Delete a subscription by its Room ID (long)
     */
    suspend fun deleteSubscriptionById(id: Long) {
        // First fetch the subscription to get the Firebase document ID
        val subscriptions = fetchAllSubscriptions().filter { it.id == id }
        if (subscriptions.isNotEmpty()) {
            val subscription = subscriptions.first()
            val docId = if (subscription.firebaseId.isNotBlank()) subscription.firebaseId else id.toString()
            deleteSubscription(docId)
        }
    }
    
    // Bill Reminder Operations
    suspend fun saveBillReminder(billReminder: BillReminder): String {
        try {
            // Generate a document ID if one doesn't exist
            val billId = if (billReminder.id.isEmpty()) UUID.randomUUID().toString() else billReminder.id
            
            // The updated BillReminder entity already uses String IDs for Firebase compatibility
            val billWithId = billReminder.copy(id = billId, lastUpdated = System.currentTimeMillis())
            
            getUserDocRef()
                .collection("billReminders")
                .document(billId)
                .set(billWithId)
                .await()
                
            return billId
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error saving bill reminder: ${e.message}")
            throw e
        }
    }
    
    suspend fun fetchAllBillReminders(): List<BillReminder> {
        try {
            return getUserDocRef()
                .collection("billReminders") // Changed collection name for consistency
                .get()
                .await()
                .toObjects(BillReminder::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error fetching bill reminders: ${e.message}")
            return emptyList() // Return empty list instead of crashing
        }
    }
    
    suspend fun updateBillReminder(billReminder: BillReminder) {
        if (billReminder.id.isBlank()) {
            throw IllegalArgumentException("Bill reminder ID cannot be empty for updates")
        }
        
        // Update with current timestamp
        val updatedBill = billReminder.copy(lastUpdated = System.currentTimeMillis())
        
        getUserDocRef()
            .collection("billReminders")
            .document(billReminder.id)
            .set(updatedBill)
            .await()
    }
    
    suspend fun markBillAsPaid(billId: String, paid: Boolean = true) {
        getUserDocRef()
            .collection("billReminders")
            .document(billId)
            .update("paid", paid)
            .await()
    }
    
    suspend fun deleteBillReminder(billId: String) {
        getUserDocRef()
            .collection("billReminders")
            .document(billId)
            .delete()
            .await()
    }
}
