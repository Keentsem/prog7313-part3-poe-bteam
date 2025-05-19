package com.example.pocketsafe

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import com.example.pocketsafe.ui.theme.PocketSafeTheme
import kotlinx.coroutines.launch

class MainMenu : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("PocketSafePrefs", MODE_PRIVATE)

        setContent {
            PocketSafeTheme {
                MainMenuContent(sharedPreferences, this)
            }
        }
    }
}

@Composable
fun MainMenuContent(sharedPreferences: SharedPreferences, activity: ComponentActivity) {
    var minGoal by remember { mutableStateOf(sharedPreferences.getFloat("minGoal", 0f)) }
    var maxGoal by remember { mutableStateOf(sharedPreferences.getFloat("maxGoal", 0f)) }
    var income by remember { mutableStateOf(sharedPreferences.getFloat("income", 0f)) }
    var totalExpenses by remember { mutableStateOf(0.0) }
    var statusMessage by remember { mutableStateOf("") }
    var statusColor by remember { mutableStateOf(Color.Green) }
    val scope = rememberCoroutineScope()
    val swipeRefreshState = rememberSwipeRefreshState(false)
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        updateFinancialData(sharedPreferences, minGoal, maxGoal, income, totalExpenses) { newTotalExpenses ->
            totalExpenses = newTotalExpenses
            updateStatusMessage(minGoal, maxGoal, income, newTotalExpenses) { message, color ->
                statusMessage = message
                statusColor = color
            }
        }
    }

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            scope.launch {
                updateFinancialData(sharedPreferences, minGoal, maxGoal, income, totalExpenses) { newTotalExpenses ->
                    totalExpenses = newTotalExpenses
                    updateStatusMessage(minGoal, maxGoal, income, newTotalExpenses) { message, color ->
                        statusMessage = message
                        statusColor = color
                    }
                }
                swipeRefreshState.isRefreshing = false
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Financial Overview",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B5E3C)
            )

            Spacer(modifier = Modifier.height(16.dp))

            FinancialCard(
                title = "Minimum Savings Goal",
                amount = minGoal,
                onEditClick = { activity.startActivity(Intent(activity, BudgetGoals.Goals::class.java)) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            FinancialCard(
                title = "Maximum Savings Goal",
                amount = maxGoal,
                onEditClick = { activity.startActivity(Intent(activity, BudgetGoals.Goals::class.java)) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            FinancialCard(
                title = "Monthly Income",
                amount = income,
                onEditClick = { activity.startActivity(Intent(activity, BudgetGoals.Goals::class.java)) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            FinancialCard(
                title = "Total Expenses",
                amount = totalExpenses.toFloat(),
                onEditClick = { activity.startActivity(Intent(activity, ViewExpensesActivity::class.java)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = statusMessage,
                color = statusColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MenuButton(
                    text = "View Expenses",
                    onClick = { activity.startActivity(Intent(activity, ViewExpensesActivity::class.java)) }
                )

                MenuButton(
                    text = "Add Expense",
                    onClick = { activity.startActivity(Intent(activity, ExpenseEntry::class.java)) }
                )

                MenuButton(
                    text = "Set Goals",
                    onClick = { activity.startActivity(Intent(activity, BudgetGoals.Goals::class.java)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    sharedPreferences.edit().clear().apply()
                    activity.startActivity(Intent(activity, MainActivity::class.java))
                    activity.finish()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5E3C))
            ) {
                Text("Logout")
            }
        }
    }
}

@Composable
fun FinancialCard(
    title: String,
    amount: Float,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD2B48C))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                color = Color(0xFF8B5E3C)
            )
            Text(
                text = "$${String.format("%.2f", amount)}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B5E3C)
            )
            TextButton(onClick = onEditClick) {
                Text("Edit")
            }
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5E3C)),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(text)
    }
}

private fun updateFinancialData(
    sharedPreferences: SharedPreferences,
    minGoal: Float,
    maxGoal: Float,
    income: Float,
    totalExpenses: Double,
    onUpdate: (Double) -> Unit
) {
    // Update financial data from shared preferences
    val newMinGoal = sharedPreferences.getFloat("minGoal", minGoal)
    val newMaxGoal = sharedPreferences.getFloat("maxGoal", maxGoal)
    val newIncome = sharedPreferences.getFloat("income", income)

    // Calculate total expenses (you'll need to implement this based on your database)
    val newTotalExpenses = totalExpenses // Replace with actual calculation

    onUpdate(newTotalExpenses)
}

private fun updateStatusMessage(
    minGoal: Float,
    maxGoal: Float,
    income: Float,
    totalExpenses: Double,
    onUpdate: (String, Color) -> Unit
) {
    val savings = income - totalExpenses
    val message = when {
        savings < minGoal -> "You're below your minimum savings goal!"
        savings > maxGoal -> "You're exceeding your maximum savings goal!"
        else -> "You're within your savings goals!"
    }
    val color = when {
        savings < minGoal -> Color.Red
        savings > maxGoal -> Color.Yellow
        else -> Color.Green
    }
    onUpdate(message, color)
} 