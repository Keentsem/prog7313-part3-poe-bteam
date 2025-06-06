package com.example.pocketsafe.ui.subscription

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketsafe.R
import com.example.pocketsafe.data.Subscription
import com.example.pocketsafe.data.RenewalPeriod
import com.example.pocketsafe.ui.viewmodel.SubscriptionViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.Currency

// Extension functions for formatting
fun Long.formatDateDisplay(): String {
    val date = Date(this)
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(date)
}

fun Double.toCurrencyString(): String {
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
    format.currency = Currency.getInstance(Locale.getDefault())
    return format.format(this)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionDetailScreen(
    subscriptionId: Long,
    onNavigateBack: () -> Unit,
    onEditSubscription: (Long) -> Unit = {},
    viewModel: SubscriptionViewModel
) {
    val subscriptionState = remember { mutableStateOf<Subscription?>(null) }
    val pixelFont = FontFamily(Font(R.font.pixel_game))
    val goldColor = Color(0xFFF3C34E)
    val brownColor = Color(0xFF5B3F2C)
    
    // Fetch subscription details
    LaunchedEffect(subscriptionId) {
        val subscription = viewModel.getSubscriptionById(subscriptionId)
        subscriptionState.value = subscription
    }
    
    val subscription = subscriptionState.value
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = subscription?.name ?: "Subscription Details",
                        fontFamily = pixelFont,
                        color = brownColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = brownColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = goldColor
                ),
                actions = {
                    IconButton(onClick = { subscription?.let { onEditSubscription(it.id) } }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = brownColor
                        )
                    }
                    IconButton(onClick = {
                        subscription?.let { viewModel.deleteSubscription(it) }
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = brownColor
                        )
                    }
                }
            )
        }
    ) { padding ->
        subscription?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFF9C4))
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Subscription Icon
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (it.activeStatus) goldColor else Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            id = if (it.activeStatus) R.drawable.sub_active else R.drawable.sub_due_alert
                        ),
                        contentDescription = "Subscription Icon",
                        modifier = Modifier.size(80.dp),
                        contentScale = ContentScale.FillBounds
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Subscription Name
                Text(
                    text = it.name,
                    fontFamily = pixelFont,
                    fontSize = 24.sp,
                    color = brownColor
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Amount
                Text(
                    text = "$${String.format("%.2f", it.amount)} / ${it.renewalPeriod.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    fontFamily = pixelFont,
                    fontSize = 20.sp,
                    color = brownColor
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                        DetailRow(
                            label = "Description",
                            value = it.description ?: "No description",
                            pixelFont = pixelFont,
                            brownColor = brownColor
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        DetailRow(
                            label = "Status",
                            value = if (it.activeStatus) "Active" else "Inactive",
                            pixelFont = pixelFont,
                            brownColor = brownColor
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        DetailRow(
                            label = "Next Renewal",
                            value = it.renewalDate.formatDateDisplay(),
                            pixelFont = pixelFont,
                            brownColor = brownColor
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        DetailRow(
                            label = "Payment Method",
                            value = it.paymentMethod ?: "Not specified",
                            pixelFont = pixelFont,
                            brownColor = brownColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Toggle Status Button
                Button(
                    onClick = { 
                        viewModel.toggleSubscriptionStatus(it)
                        subscriptionState.value = it.copy(activeStatus = !it.activeStatus)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = goldColor,
                        contentColor = brownColor
                    )
                ) {
                    Text(
                        text = if (it.activeStatus) "Mark as Inactive" else "Mark as Active",
                        fontFamily = pixelFont
                    )
                }
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF9C4))
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Subscription not found",
                fontFamily = pixelFont,
                color = brownColor,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    pixelFont: FontFamily,
    brownColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = pixelFont,
            fontSize = 14.sp,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            fontFamily = pixelFont,
            fontSize = 16.sp,
            color = brownColor
        )
    }
}
