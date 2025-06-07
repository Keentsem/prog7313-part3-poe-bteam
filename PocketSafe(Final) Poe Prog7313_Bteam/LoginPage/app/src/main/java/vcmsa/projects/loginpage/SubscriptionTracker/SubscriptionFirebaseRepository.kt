package vcmsa.projects.loginpage.SubscriptionTracker

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

class SubscriptionFirebaseRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val subscriptionsRef = firestore.collection("subscriptions")

    suspend fun addSubscription(subscription: Subscription) {
        val docRef = subscriptionsRef.document()
        val withId = subscription.copy(id = docRef.id)
        docRef.set(withId).await()
    }

    suspend fun updateSubscription(subscription: Subscription) {
        subscriptionsRef.document(subscription.id).set(subscription).await()
    }

    suspend fun deleteSubscription(subscriptionId: String) {
        subscriptionsRef.document(subscriptionId).delete().await()
    }

    fun getAllSubscriptions(): Flow<List<Subscription>> = callbackFlow {
        val listener = subscriptionsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { it.toObject(Subscription::class.java) }
            trySend(list ?: emptyList())
        }
        awaitClose { listener.remove() }
    }
}
