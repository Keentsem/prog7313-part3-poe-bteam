package com.example.pocketsafe.ui.subscription

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketsafe.R
import com.example.pocketsafe.data.Subscription
import com.example.pocketsafe.ui.viewmodel.SubscriptionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionListScreen(
    onNavigateToSubscriptionDetail: (Long) -> Unit,
    onNavigateToAddSubscription: () -> Unit,
    viewModel: SubscriptionViewModel
) {
    val subscriptions by viewModel.allSubscriptions.observeAsState(initial = emptyList())
    val pixelFont = FontFamily(Font(R.font.pixel_game))
    
    val activeSubscriptions by viewModel.activeSubscriptions.observeAsState(initial = emptyList())
    val monthlyTotal by viewModel.monthlySubscriptionCost.observeAsState(initial = 0.0)
    
    val goldColor = Color(0xFFF3C34E)
    val brownColor = Color(0xFF5B3F2C)
    
    LaunchedEffect(Unit) {
        viewModel.getAllSubscriptions()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Subscriptions", 
                        fontFamily = pixelFont,
                        color = brownColor
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = goldColor
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddSubscription,
                containerColor = goldColor,
                contentColor = brownColor
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Subscription")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF9C4))
                .padding(padding)
        ) {
            // Monthly total card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = goldColor,
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Monthly Subscriptions",
                        fontFamily = pixelFont,
                        fontSize = 18.sp,
                        color = brownColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$${String.format("%.2f", monthlyTotal)}",
                        fontFamily = pixelFont,
                        fontSize = 24.sp,
                        color = brownColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${activeSubscriptions.size} active subscriptions",
                        fontFamily = pixelFont,
                        color = brownColor
                    )
                }
            }
            
            // Subscription list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(subscriptions) { subscription ->
                    SubscriptionCard(
                        subscription = subscription,
                        onClick = { onNavigateToSubscriptionDetail(subscription.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SubscriptionCard(
    subscription: Subscription,
    onClick: () -> Unit
) {
    val pixelFont = FontFamily(Font(R.font.pixel_game))
    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
    val renewalDate = formatter.format(Date(subscription.renewalDate))
    val goldColor = Color(0xFFF3C34E)
    val brownColor = Color(0xFF5B3F2C)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (subscription.active) Color(0xFFE6F5E6) else Color(0xFFFAFAFA)
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (subscription.active) goldColor else Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        id = if (subscription.active) R.drawable.sub_active else R.drawable.sub_due_alert
                    ),
                    contentDescription = if (subscription.active) "Active" else "Inactive",
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.FillBounds
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Subscription info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = subscription.name,
                    fontFamily = pixelFont,
                    fontSize = 18.sp,
                    color = brownColor
                )
                Text(
                    text = subscription.description ?: "",
                    fontFamily = pixelFont,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Renews: $renewalDate",
                    fontFamily = pixelFont,
                    fontSize = 12.sp,
                    color = brownColor
                )
            }
            
            // Price
            Text(
                text = "$${String.format("%.2f", subscription.amount)}",
                fontFamily = pixelFont,
                fontSize = 18.sp,
                color = brownColor
            )
        }
    }
}
