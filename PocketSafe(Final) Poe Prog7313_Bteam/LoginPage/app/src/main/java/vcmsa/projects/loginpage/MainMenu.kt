package vcmsa.projects.loginpage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import vcmsa.projects.loginpage.SubscriptionTracker.SubscriptionViewModel
import vcmsa.projects.loginpage.ui.theme.LoginPageTheme

class MainMenu : ComponentActivity() {

    private val subscriptionViewModel: SubscriptionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginPageTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF8B5E3C)
                ) {
                    val context = LocalContext.current
                    val sharedPref = context.getSharedPreferences("GoalPrefs", Context.MODE_PRIVATE)

                    val minGoal = remember { mutableStateOf(0) }
                    val maxGoal = remember { mutableStateOf(0) }
                    val income = remember { mutableStateOf(0) }
                    val totalExpenses = remember { mutableStateOf(0) }

                    val statusMessage = remember { mutableStateOf("") }
                    val statusColor = remember { mutableStateOf(Color.Gray) }
                    val isRefreshing = remember { mutableStateOf(false) }

                    fun updateFinancialData() {
                        minGoal.value = sharedPref.getString("minGoal", "0")?.toIntOrNull() ?: 0
                        maxGoal.value = sharedPref.getString("maxGoal", "0")?.toIntOrNull() ?: 0
                        income.value = sharedPref.getString("income", "0")?.toIntOrNull() ?: 0
                        totalExpenses.value = sharedPref.getInt("totalExpenses", 0)

                        val remainder = income.value - totalExpenses.value
                        statusMessage.value = when {
                            remainder > maxGoal.value -> "🎯 Goal Achieved"
                            remainder > minGoal.value -> "⚠️ Partially Achieved"
                            else -> " Not Achieved"
                        }

                        statusColor.value = when (statusMessage.value) {
                            "🎯 Goal Achieved" -> Color(0xFF4CAF50)
                            "⚠️ Partially Achieved" -> Color(0xFFFFC107)
                            else -> Color(0xFF808080)
                        }
                    }

                    LaunchedEffect(Unit) {
                        updateFinancialData()
                    }

                    LaunchedEffect(isRefreshing.value) {
                        if (isRefreshing.value) {
                            updateFinancialData()
                            delay(1000)
                            isRefreshing.value = false
                        }
                    }

                    // Collect subscriptions StateFlow from ViewModel
                    val subscriptions by subscriptionViewModel.subscriptions.collectAsState()

                    // Calendar info
                    val calendar = java.util.Calendar.getInstance()
                    val today = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                    val currentMonthIndex = calendar.get(java.util.Calendar.MONTH)
                    val monthNames = listOf(
                        "January", "February", "March", "April", "May", "June",
                        "July", "August", "September", "October", "November", "December"
                    )

                    // Find next upcoming subscriptions sorted by days until due
                    val upcomingSubscriptions = subscriptions
                        .filter { it.nextBillingDate.toIntOrNull() != null }
                        .sortedBy {
                            val billingDay = it.nextBillingDate.toInt()
                            if (billingDay >= today) billingDay - today else billingDay + 30 - today
                        }
                        .take(2)

                    val nextSubscription = upcomingSubscriptions.firstOrNull()

                    val notificationBackgroundColor = Color(0xFFD6C5B1) // Cream brown

                    Box(modifier = Modifier.fillMaxSize()) {
                        SwipeRefresh(
                            state = rememberSwipeRefreshState(isRefreshing.value),
                            onRefresh = { isRefreshing.value = true },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                // Back button with expanded clickable area
                                Text(
                                    text = "Log out",
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier
                                        .padding(bottom = 16.dp)
                                        .fillMaxWidth()
                                        .clickable {
                                            val intent = Intent(context, MainActivity::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            context.startActivity(intent)
                                            (context as? ComponentActivity)?.finish()
                                        }
                                        .padding(16.dp)
                                )

                                Text(
                                    text = "Welcome to the Main Menu!",
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Text(
                                    text = "Personal Budget Goals",
                                    color = Color(0xFFF9D29D),
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF6D4C41)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "🏁 Goal Ranges",
                                            color = Color(0xFFFFE0B2),
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        Text(
                                            text = "• Minimum Goal: R ${minGoal.value}",
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "• Maximum Goal: R ${maxGoal.value}",
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyLarge
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "💼 Financial Snapshot",
                                            color = Color(0xFFFFE0B2),
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        Text(
                                            text = "• Monthly Income: R ${income.value}",
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "• Total Expenses: R ${totalExpenses.value}",
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyLarge
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "📊 Status: ${statusMessage.value}",
                                            color = statusColor.value,
                                            style = MaterialTheme.typography.headlineSmall
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Surface(
                                    modifier = Modifier
                                        .weight(3.2f)
                                        .fillMaxWidth(),
                                    color = Color(0xFF5D4037)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "My Pocket Expenses",
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        val intent = Intent(context, ExpenseEntry::class.java)
                                                        context.startActivity(intent)
                                                    }
                                            )

                                            Text(
                                                text = "My Pocket Goals",
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        val intent = Intent(context, vcmsa.projects.loginpage.BudgetGoals.Goals::class.java)
                                                        context.startActivity(intent)
                                                    }
                                            )

                                            Text(
                                                text = "Subscription Tracker",
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        val intent = Intent(context, vcmsa.projects.loginpage.SubscriptionTracker.SubscriptionTrackerActivity::class.java)
                                                        context.startActivity(intent)
                                                    }
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))  // Add vertical space between rows

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "My Pocket Expense Splitter",
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        val intent = Intent(context, vcmsa.projects.loginpage.ExpenseSplitActivity::class.java)
                                                        context.startActivity(intent)
                                                    }
                                            )

                                            Spacer(modifier = Modifier.weight(3f))  // fills remaining space to keep button size consistent
                                        }
                                    }
                                }


                                Spacer(modifier = Modifier.height(56.dp)) // Leave space for notification box
                            }
                        }

                        // Permanent notification box at bottom center
                        if (nextSubscription != null) {
                            val billingDay = nextSubscription.nextBillingDate.toInt()
                            val showNextMonth = billingDay < today
                            val displayMonthIndex = if (showNextMonth) (currentMonthIndex + 1) % 12 else currentMonthIndex
                            val displayMonth = monthNames[displayMonthIndex]

                            Surface(
                                color = notificationBackgroundColor,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Next subscription due: ${nextSubscription.name} on $billingDay $displayMonth",
                                    color = Color(0xFF5D4037),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
