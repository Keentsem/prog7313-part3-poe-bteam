package com.example.pocketsafe.ui.expense

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.pocketsafe.R
import com.example.pocketsafe.data.Expense
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEntryScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    
    val pixelFont = FontFamily(Font(R.font.pixel_game))
    val currentTime = System.currentTimeMillis()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val currentDate = dateFormat.format(Date(currentTime))
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF9C4))
            .verticalScroll(rememberScrollState())
    ) {
        // Header Card with Image
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.pocketsafeexpense),
                contentDescription = "Expense Header",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // Form Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "New Expense",
                fontFamily = pixelFont,
                fontSize = 24.sp,
                color = Color(0xFF2C1810)
            )
            
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount", fontFamily = pixelFont) },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description", fontFamily = pixelFont) },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Category Dropdown
            var expanded by remember { mutableStateOf(false) }
            val categories = listOf("Food", "Transport", "Entertainment", "Bills", "Shopping")
            
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedCategoryId?.let { categories[it] } ?: "Select Category",
                        fontFamily = pixelFont
                    )
                }
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEachIndexed { index, category ->
                        DropdownMenuItem(
                            text = { Text(category, fontFamily = pixelFont) },
                            onClick = {
                                selectedCategoryId = index
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            // Photo Preview
            photoUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Expense Photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Photo Capture Button
            Button(
                onClick = { /* TODO: Implement photo capture */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Photo", fontFamily = pixelFont)
            }
            
            // Save Button
            Button(
                onClick = {
                    if (amount.isNotBlank() && selectedCategoryId != null) {
                        val expense = Expense(
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            categoryId = selectedCategoryId!!,
                            description = description,
                            photoUri = photoUri?.toString(),
                            date = currentTime,
                            startDate = currentDate,
                            endDate = currentDate
                        )
                        viewModel.addExpense(expense)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.isNotBlank() && selectedCategoryId != null
            ) {
                Text("Save Expense", fontFamily = pixelFont)
            }
        }
    }
} 