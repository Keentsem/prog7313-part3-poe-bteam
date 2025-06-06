package com.example.pocketsafe.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketsafe.MainApplication
import com.example.pocketsafe.R
import com.example.pocketsafe.ui.expense.ExpenseViewModel
import com.example.pocketsafe.ui.viewmodel.SubscriptionViewModel
import java.text.SimpleDateFormat
import java.util.*

// Define pixel-retro theme colors
val goldColor = Color(0xFFF3C34E)
val brownColor = Color(0xFF5B3F2C)
val pixelGameFont = FontFamily(Font(R.font.pixel_game))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToSubscriptions: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    // Create ViewModels using remember instead of viewModel() to avoid dependency issues
    val expenseViewModel = remember {
        ExpenseViewModel.Factory(MainApplication.instance).create(ExpenseViewModel::class.java)
    }
    
    val subscriptionViewModel = remember {
        SubscriptionViewModel.Factory(MainApplication.instance).create(SubscriptionViewModel::class.java)
    }
    
    // Observe data
    val expenses by expenseViewModel.recentExpenses.observeAsState(initial = emptyList())
    val subscriptions by subscriptionViewModel.upcomingSubscriptions.observeAsState(initial = emptyList())
    val monthlySubscriptionCost by subscriptionViewModel.monthlySubscriptionCost.observeAsState(initial = 0.0)
    
    val pixelFont = FontFamily(Font(R.font.pixel_game))
    val goldColor = Color(0xFFF3C34E)
    val brownColor = Color(0xFF5B3F2C)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "PocketSafe Dashboard", 
                        fontFamily = pixelFont,
                        color = brownColor
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = goldColor
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = brownColor
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF9C4))
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Main App Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = goldColor
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.pocketsafe),
                        contentDescription = "PocketSafe Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "Welcome to PocketSafe",
                            fontFamily = pixelFont,
                            fontSize = 24.sp,
                            color = brownColor
                        )
                        Text(
                            text = "Your pixel-themed budget tracker",
                            fontFamily = pixelFont,
                            fontSize = 14.sp,
                            color = brownColor
                        )
                    }
                }
            }
            
            // Quick Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Subscriptions Stats
                StatCard(
                    title = "Subscriptions",
                    value = "$${String.format("%.2f", monthlySubscriptionCost)}/mo",
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFFE6F5E6),
                    iconRes = R.drawable.sub_active,
                    pixelFont = pixelFont,
                    brownColor = brownColor,
                    onClick = onNavigateToSubscriptions
                )
                
                // Expenses Stats
                StatCard(
                    title = "Recent Expenses",
                    value = "${expenses.size} items",
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFFFFECB3),
                    iconRes = R.drawable.pocketsafeexpense,
                    pixelFont = pixelFont,
                    brownColor = brownColor,
                    onClick = onNavigateToExpenses
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Upcoming Subscriptions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Upcoming Subscriptions",
                            fontFamily = pixelFont,
                            fontSize = 18.sp,
                            color = brownColor
                        )
                        
                        TextButton(onClick = onNavigateToSubscriptions) {
                            Text(
                                text = "View All",
                                fontFamily = pixelFont,
                                color = goldColor
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (subscriptions.isNotEmpty()) {
                        subscriptions.take(3).forEach { subscription ->
                            val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
                            val renewalDate = formatter.format(Date(subscription.renewalDate))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Status icon
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(goldColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.sub_active),
                                        contentDescription = "Active Subscription",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = subscription.name,
                                        fontFamily = pixelFont,
                                        fontSize = 16.sp,
                                        color = brownColor
                                    )
                                    Text(
                                        text = "Due: $renewalDate",
                                        fontFamily = pixelFont,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                
                                Text(
                                    text = "$${String.format("%.2f", subscription.amount)}",
                                    fontFamily = pixelFont,
                                    fontSize = 16.sp,
                                    color = brownColor
                                )
                            }
                            
                            if (subscription != subscriptions.take(3).lastOrNull()) {
                                Divider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = Color.LightGray.copy(alpha = 0.5f)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No upcoming subscriptions",
                                fontFamily = pixelFont,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Recent Expenses
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Expenses",
                            fontFamily = pixelFont,
                            fontSize = 18.sp,
                            color = brownColor
                        )
                        
                        TextButton(onClick = onNavigateToExpenses) {
                            Text(
                                text = "View All",
                                fontFamily = pixelFont,
                                color = goldColor
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (expenses.isNotEmpty()) {
                        expenses.take(3).forEach { expense ->
                            val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
                            val expenseDate = formatter.format(Date(expense.date))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category icon
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(goldColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // This would ideally use a category-specific icon
                                    Text(
                                        text = "${expense.categoryId + 1}",
                                        fontFamily = pixelFont,
                                        color = brownColor
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = expense.description ?: "Expense",
                                        fontFamily = pixelFont,
                                        fontSize = 16.sp,
                                        color = brownColor
                                    )
                                    Text(
                                        text = expenseDate,
                                        fontFamily = pixelFont,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                
                                Text(
                                    text = "$${String.format("%.2f", expense.amount)}",
                                    fontFamily = pixelFont,
                                    fontSize = 16.sp,
                                    color = brownColor
                                )
                            }
                            
                            if (expense != expenses.take(3).lastOrNull()) {
                                Divider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = Color.LightGray.copy(alpha = 0.5f)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No recent expenses",
                                fontFamily = pixelFont,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    iconRes: Int,
    pixelFont: FontFamily,
    brownColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            
            // Stats
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = value,
                    fontFamily = pixelFont,
                    fontSize = 18.sp,
                    color = brownColor
                )
                
                Text(
                    text = title,
                    fontFamily = pixelFont,
                    fontSize = 12.sp,
                    color = brownColor
                )
            }
        }
    }
}
