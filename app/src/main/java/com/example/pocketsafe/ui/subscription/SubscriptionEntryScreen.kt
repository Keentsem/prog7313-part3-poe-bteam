package com.example.pocketsafe.ui.subscription

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketsafe.R
import com.example.pocketsafe.data.RenewalPeriod
import com.example.pocketsafe.data.Subscription
import com.example.pocketsafe.ui.viewmodel.SubscriptionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionEntryScreen(
    subscriptionId: Long = 0L,
    onNavigateBack: () -> Unit,
    viewModel: SubscriptionViewModel
) {
    // Pixel-retro theme colors
    val pixelFont = FontFamily(Font(R.font.pixel_game))
    val goldColor = Color(0xFFF3C34E)
    val brownColor = Color(0xFF5B3F2C)
    
    // State for form fields
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(0) }
    var selectedFrequency by remember { mutableStateOf(RenewalPeriod.MONTHLY) }
    var nextDueDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var isActive by remember { mutableStateOf(true) }
    
    // Tracks whether this is an edit (existing subscription) or a new subscription
    var isEditMode by remember { mutableStateOf(false) }
    
    // Load existing subscription data if we're editing
    LaunchedEffect(subscriptionId) {
        if (subscriptionId > 0) {
            isEditMode = true
            val subscription = viewModel.getSubscriptionById(subscriptionId)
            subscription?.let {
                name = it.name
                amount = it.amount.toString()
                description = it.description ?: ""
                paymentMethod = it.paymentMethod ?: ""
                selectedCategory = it.categoryId
                selectedFrequency = it.renewalPeriod
                nextDueDate = it.nextDueDate
                isActive = it.activeStatus
            }
        }
    }
    
    // Formatting dates
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (isEditMode) "Edit Subscription" else "New Subscription",
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
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brownColor),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sub_active),
                    contentDescription = "Subscription Icon",
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Fit
                )
            }
            
            // Form fields
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { 
                    Text(
                        "Subscription Name",
                        fontFamily = pixelFont
                    ) 
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = goldColor,
                    unfocusedBorderColor = brownColor,
                    cursorColor = goldColor
                )
            )
            
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { 
                    Text(
                        "Amount",
                        fontFamily = pixelFont
                    ) 
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = goldColor,
                    unfocusedBorderColor = brownColor,
                    cursorColor = goldColor
                )
            )
            
            // Frequency dropdown
            Column {
                Text(
                    "Renewal Period",
                    fontFamily = pixelFont,
                    color = brownColor
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RenewalPeriod.values().forEach { period ->
                        Button(
                            onClick = { selectedFrequency = period },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedFrequency == period) goldColor else Color.LightGray,
                                contentColor = brownColor
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = period.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontFamily = pixelFont,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            
            // Next due date picker
            Column {
                Text(
                    "Next Due Date",
                    fontFamily = pixelFont,
                    color = brownColor
                )
                
                Button(
                    onClick = { 
                        // In a real app, show a date picker dialog here
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = goldColor,
                        contentColor = brownColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select Date"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateFormatter.format(Date(nextDueDate)),
                        fontFamily = pixelFont
                    )
                }
            }
            
            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { 
                    Text(
                        "Description (Optional)",
                        fontFamily = pixelFont
                    ) 
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = goldColor,
                    unfocusedBorderColor = brownColor,
                    cursorColor = goldColor
                )
            )
            
            // Payment method
            OutlinedTextField(
                value = paymentMethod,
                onValueChange = { paymentMethod = it },
                label = { 
                    Text(
                        "Payment Method (Optional)",
                        fontFamily = pixelFont
                    ) 
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = goldColor,
                    unfocusedBorderColor = brownColor,
                    cursorColor = goldColor
                )
            )
            
            // Active status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = goldColor,
                        checkedTrackColor = brownColor,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.LightGray
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Active Subscription",
                    fontFamily = pixelFont,
                    color = brownColor
                )
            }
            
            // Save button
            Button(
                onClick = {
                    // Validate inputs
                    if (name.isBlank() || amount.isBlank()) {
                        return@Button
                    }
                    
                    try {
                        val amountValue = amount.toDoubleOrNull() ?: 0.0
                        
                        val subscription = Subscription(
                            id = if (isEditMode) subscriptionId else 0L,
                            name = name.trim(),
                            amount = amountValue,
                            frequency = selectedFrequency.name,
                            nextDueDate = nextDueDate,
                            activeStatus = isActive,
                            categoryId = selectedCategory,
                            description = description.trim().ifBlank { null },
                            paymentMethod = paymentMethod.trim().ifBlank { null }
                        )
                        
                        viewModel.saveSubscription(subscription)
                        onNavigateBack()
                    } catch (e: Exception) {
                        // In a real app, show an error message
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = goldColor,
                    contentColor = brownColor
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Subscription",
                    fontFamily = pixelFont,
                    fontSize = 16.sp
                )
            }
        }
    }
}
