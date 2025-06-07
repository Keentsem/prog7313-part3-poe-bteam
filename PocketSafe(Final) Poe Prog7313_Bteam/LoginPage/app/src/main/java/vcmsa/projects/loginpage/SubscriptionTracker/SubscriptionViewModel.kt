package vcmsa.projects.loginpage.SubscriptionTracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SubscriptionViewModel : ViewModel() {
    private val repo = SubscriptionFirebaseRepository()
    private val _subscriptions = MutableStateFlow<List<Subscription>>(emptyList())
    val subscriptions: StateFlow<List<Subscription>> = _subscriptions.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAllSubscriptions().collectLatest {
                _subscriptions.value = it
            }
        }
    }

    fun add(subscription: Subscription) = viewModelScope.launch {
        repo.addSubscription(subscription)
    }

    fun delete(id: String) = viewModelScope.launch {
        repo.deleteSubscription(id)
    }

    fun update(subscription: Subscription) = viewModelScope.launch {
        repo.updateSubscription(subscription)
    }
}
