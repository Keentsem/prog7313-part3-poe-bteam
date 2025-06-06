package com.example.pocketsafe

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFF994F31)
                ) { innerPadding ->
                    MainMenuContent(sharedPreferences, this, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainMenuContent(sharedPreferences: SharedPreferences, activity: ComponentActivity, modifier: Modifier = Modifier) {
    var minGoal by remember { mutableStateOf(sharedPreferences.getFloat("minGoal", 0f)) }
    var maxGoal by remember { mutableStateOf(sharedPreferences.getFloat("maxGoal", 0f)) }
    var income by remember { mutableStateOf(sharedPreferences.getFloat("income", 0f)) }
    var totalExpenses by remember { mutableStateOf(0.0) }
    var statusMessage by remember { mutableStateOf("") }
    var statusColor by remember { mutableStateOf(Color.Green) }
    val scope = rememberCoroutineScope()
    val swipeRefreshState = rememberSwipeRefreshState(false)
    val context = LocalContext.current
    
    // Colors from the pixel-retro theme
    val goldColor = Color(0xFFF3C34E)
    val brownColor = Color(0xFF5B3F2C)
    val walletBrownColor = Color(0xFF994F31)
    
    // Wallet stitching style
    val stitchColor = Color(0xFFFFDA9E)
    val stitchEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    
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
        Box(
            modifier = modifier
                .fillMaxSize()
                .drawBehind {
                    // Draw wallet stitching around the edges
                    drawLine(
                        color = stitchColor,
                        start = Offset(15f, 15f),
                        end = Offset(size.width - 15f, 15f),
                        strokeWidth = 3f,
                        pathEffect = stitchEffect
                    )
                    drawLine(
                        color = stitchColor,
                        start = Offset(size.width - 15f, 15f),
                        end = Offset(size.width - 15f, size.height - 15f),
                        strokeWidth = 3f,
                        pathEffect = stitchEffect
                    )
                    drawLine(
                        color = stitchColor,
                        start = Offset(size.width - 15f, size.height - 15f),
                        end = Offset(15f, size.height - 15f),
                        strokeWidth = 3f,
                        pathEffect = stitchEffect
                    )
                    drawLine(
                        color = stitchColor,
                        start = Offset(15f, size.height - 15f),
                        end = Offset(15f, 15f),
                        strokeWidth = 3f,
                        pathEffect = stitchEffect
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PocketSafe",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldColor
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Safe dial image
                    Image(
                        painter = painterResource(id = R.drawable.dial),
                        contentDescription = "Safe Dial",
                        modifier = Modifier
                            .size(48.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Your Financial Vault",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = goldColor
                )

                Spacer(modifier = Modifier.height(24.dp))

                FinancialSummaryCard(
                    minGoal = minGoal,
                    maxGoal = maxGoal,
                    income = income,
                    totalExpenses = totalExpenses.toFloat(),
                    statusMessage = statusMessage,
                    statusColor = statusColor
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Quick Actions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = goldColor,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                PixelMenuGrid(
                    onViewExpensesClick = { activity.startActivity(Intent(activity, ViewExpensesActivity::class.java)) },
                    onAddExpenseClick = { activity.startActivity(Intent(activity, ExpenseEntry::class.java)) },
                    onSetGoalsClick = { activity.startActivity(Intent(activity, BudgetGoals.Goals::class.java)) },
                    onNavigateClick = { 
                        // Launch our new navigation activity with pixel-retro theme
                        activity.startActivity(Intent(activity, NavigationActivity::class.java)) 
                    },
                    onLogoutClick = {
                        sharedPreferences.edit().clear().apply()
                        activity.startActivity(Intent(activity, MainActivity::class.java))
                        activity.finish()
                    }
                )
            }
        }
    }
}

@Composable
fun FinancialSummaryCard(
    minGoal: Float,
    maxGoal: Float,
    income: Float,
    totalExpenses: Float,
    statusMessage: String,
    statusColor: Color
) {
    val goldColor = Color(0xFFF3C34E)
    val brownColor = Color(0xFF5B3F2C)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD2B48C)),
        border = BorderStroke(2.dp, brownColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            // Financial summary items
            FinancialDataRow(label = "Minimum Savings Goal", value = minGoal, labelColor = goldColor)
            Spacer(modifier = Modifier.height(8.dp))
            FinancialDataRow(label = "Maximum Savings Goal", value = maxGoal, labelColor = goldColor)
            Spacer(modifier = Modifier.height(8.dp))
            FinancialDataRow(label = "Monthly Income", value = income, labelColor = goldColor)
            Spacer(modifier = Modifier.height(8.dp))
            FinancialDataRow(label = "Total Expenses", value = totalExpenses, labelColor = goldColor)
            
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = brownColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = statusMessage,
                color = statusColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun FinancialDataRow(
    label: String,
    value: Float,
    labelColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = labelColor
        )
        Text(
            text = "$${String.format("%.2f", value)}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5B3F2C)
        )
    }
}

@Composable
fun PixelMenuGrid(
    onViewExpensesClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onSetGoalsClick: () -> Unit,
    onNavigateClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val goldColor = Color(0xFFF3C34E)
    val brownColor = Color(0xFF5B3F2C)
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PixelMenuButton(
                text = "View Expenses",
                icon = R.drawable.expenseentry,
                modifier = Modifier.weight(1f),
                onClick = onViewExpensesClick
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            PixelMenuButton(
                text = "Add Expense",
                icon = R.drawable.bill_clock,
                modifier = Modifier.weight(1f),
                onClick = onAddExpenseClick
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PixelMenuButton(
                text = "Set Goals",
                icon = R.drawable.goal,
                modifier = Modifier.weight(1f),
                onClick = onSetGoalsClick
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            PixelMenuButton(
                text = "Navigation",
                icon = R.drawable.expenseentry, // Reusing an existing icon
                modifier = Modifier.weight(1f),
                onClick = onNavigateClick
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onLogoutClick,
                colors = ButtonDefaults.buttonColors(containerColor = brownColor),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text(
                    text = "Logout",
                    color = goldColor
                )
            }
        }
    }
}

@Composable
fun PixelMenuButton(
    text: String,
    icon: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val goldColor = Color(0xFFF3C34E)
    val brownColor = Color(0xFF5B3F2C)
    
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD2B48C)),
        border = BorderStroke(2.dp, brownColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = text,
                fontSize = 14.sp,
                color = goldColor,
                fontWeight = FontWeight.Bold
            )
        }
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