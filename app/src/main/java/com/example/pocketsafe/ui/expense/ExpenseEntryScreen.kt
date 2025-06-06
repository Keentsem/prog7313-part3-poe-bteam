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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocketsafe.MainApplication
import coil.compose.rememberAsyncImagePainter
import com.example.pocketsafe.R
import com.example.pocketsafe.data.Expense
import com.example.pocketsafe.data.CategoryType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEntryScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExpenseViewModel = viewModel(factory = ExpenseViewModel.Factory(MainApplication.instance))
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    
    // Pixel-retro theme colors
    val pixelFont = FontFamily(Font(R.font.pixel_game))
    val goldColor = Color(0xFFF3C34E)
    val brownColor = Color(0xFF5B3F2C)
    
    val currentTime = System.currentTimeMillis()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val currentDate = dateFormat.format(Date(currentTime))
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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
                painter = painterResource(id = R.drawable.expenseentry),
                contentDescription = "Expense Header",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
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
                color = brownColor
            )
            
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount", fontFamily = pixelFont) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = goldColor,
                    unfocusedBorderColor = brownColor,
                    cursorColor = goldColor,
                    focusedLabelColor = brownColor
                )
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description", fontFamily = pixelFont) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = goldColor,
                    unfocusedBorderColor = brownColor,
                    cursorColor = goldColor,
                    focusedLabelColor = brownColor
                )
            )
            
            // Category Dropdown
            var expanded by remember { mutableStateOf(false) }
            val categories = CategoryType.values().toList()
            val selectedCategory = selectedCategoryId?.let { id -> 
                categories.getOrNull(id) ?: CategoryType.OTHER 
            }
            
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = brownColor
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(brownColor))
                ) {
                    Text(
                        text = selectedCategory?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Select Category",
                        fontFamily = pixelFont,
                        color = brownColor
                    )
                }
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEachIndexed { index, category ->
                        DropdownMenuItem(
                            text = { Text(category.name.lowercase().replaceFirstChar { it.uppercase() }, fontFamily = pixelFont) },
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
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = goldColor,
                    contentColor = brownColor
                )
            ) {
                Text(
                    "Add Photo", 
                    fontFamily = pixelFont
                )
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
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                enabled = amount.isNotBlank() && selectedCategoryId != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = goldColor,
                    contentColor = brownColor,
                    disabledContainerColor = Color.LightGray,
                    disabledContentColor = Color.Gray
                )
            ) {
                Text(
                    "Save Expense", 
                    fontFamily = pixelFont,
                    fontSize = 16.sp
                )
            }
        }
    }
} 