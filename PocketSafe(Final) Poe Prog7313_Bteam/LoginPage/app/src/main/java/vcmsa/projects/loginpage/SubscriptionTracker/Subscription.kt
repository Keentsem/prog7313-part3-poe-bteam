package vcmsa.projects.loginpage.SubscriptionTracker

data class Subscription(
    val id: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val recurrence: String = "",
    val nextBillingDate: String = "",
    val userId: String = "" // optional if using FirebaseAuth
)
