package com.example.pocketsafe.ui.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import com.example.pocketsafe.MainApplication
import com.example.pocketsafe.R
import com.example.pocketsafe.data.Expense
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onNavigateToAddExpense: () -> Unit,
    viewModel: ExpenseViewModel = viewModel(factory = ExpenseViewModel.Factory(MainApplication.instance))
) {
    val expenses = viewModel.allExpenses.observeAsState(initial = emptyList())
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    val pixelFont = FontFamily(Font(R.font.pixel_game))
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddExpense,
                containerColor = Color(0xFFA89D92)
            ) {
                Icon(Icons.Default.Add, "Add Expense")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF9C4))
                .padding(padding)
        ) {
            Text(
                text = "Expenses",
                fontFamily = pixelFont,
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp),
                color = Color(0xFF2C1810)
            )
            
            // Category filter dropdown will be implemented here
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(expenses.value) { expense ->
                    ExpenseCard(
                        expense = expense,
                        onDelete = { viewModel.deleteExpense(expense) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseCard(
    expense: Expense,
    onDelete: () -> Unit
) {
    val pixelFont = FontFamily(Font(R.font.pixel_game))
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "₱${expense.amount}",
                    fontFamily = pixelFont,
                    fontSize = 20.sp,
                    color = Color(0xFF2C1810)
                )
                if (expense.description != null) {
                    Text(
                        text = expense.description,
                        fontFamily = pixelFont,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = dateFormat.format(Date(expense.date)),
                    fontFamily = pixelFont,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFA89D92)
                )
            }
        }
    }
} 