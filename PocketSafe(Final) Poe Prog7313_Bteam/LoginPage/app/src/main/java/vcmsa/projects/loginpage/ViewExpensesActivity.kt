@file:OptIn(ExperimentalMaterial3Api::class)
package vcmsa.projects.loginpage

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import vcmsa.projects.loginpage.ExpenseElements.Expense
import vcmsa.projects.loginpage.ExpenseElements.ExpenseFirebaseRepository
import java.text.SimpleDateFormat
import java.util.*

class ViewExpensesActivity : ComponentActivity() {

    private val firebaseRepo = ExpenseFirebaseRepository()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ViewExpensesScreen()
        }
    }

    @Composable
    fun ViewExpensesScreen() {
        var selectedCategory by remember { mutableStateOf("All Categories") }
        var selectedStartDate by remember { mutableStateOf<Date?>(null) }
        var selectedEndDate by remember { mutableStateOf<Date?>(null) }

        var allExpenses by remember { mutableStateOf(listOf<Expense>()) }
        var filteredExpenses by remember { mutableStateOf(listOf<Expense>()) }
        var categories by remember { mutableStateOf(listOf("All Categories")) }
        var totalAmount by remember { mutableStateOf(0.0) }

        // Fetch expenses and categories from Firebase once on composition
        LaunchedEffect(Unit) {
            try {
                val firebaseExpenses = firebaseRepo.getAllExpensesFromFirebase()
                allExpenses = firebaseExpenses
                categories = listOf("All Categories") + firebaseExpenses.map { it.categoryName }.distinct()
                // Initially show all expenses
                filteredExpenses = firebaseExpenses
                totalAmount = firebaseExpenses.sumOf { it.amount }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun filterExpenses() {
            filteredExpenses = allExpenses.filter { expense ->
                // Category filter
                val categoryMatches = selectedCategory == "All Categories" || expense.categoryName == selectedCategory

                // Parse dates safely
                val expenseStart = try { dateFormat.parse(expense.startDate) } catch (e: Exception) { null }
                val expenseEnd = try { dateFormat.parse(expense.endDate) } catch (e: Exception) { null }

                if (expenseStart == null || expenseEnd == null) return@filter false

                // Date range overlap check
                val dateMatches = when {
                    selectedStartDate != null && selectedEndDate != null ->
                        !expenseEnd.before(selectedStartDate) && !expenseStart.after(selectedEndDate)
                    selectedStartDate != null ->
                        !expenseEnd.before(selectedStartDate)
                    selectedEndDate != null ->
                        !expenseStart.after(selectedEndDate)
                    else -> true
                }

                categoryMatches && dateMatches
            }
            totalAmount = filteredExpenses.sumOf { it.amount }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // Category Dropdown
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                TextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) }, onClick = {
                            selectedCategory = cat
                            expanded = false
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date Pickers
            DatePickerButton("Start Date", selectedStartDate) { selectedStartDate = it }
            Spacer(modifier = Modifier.height(8.dp))
            DatePickerButton("End Date", selectedEndDate) { selectedEndDate = it }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Button to apply filters
            Button(onClick = {
                filterExpenses()
            }) {
                Text("Search")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Expenses", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredExpenses.isEmpty()) {
                Text(
                    "No expenses found",
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFD2B48C))
                        .padding(16.dp),
                    color = Color(0xFF8B5E3C),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredExpenses) { expense ->
                        ExpenseItem(expense)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Total Amount: $totalAmount", style = MaterialTheme.typography.headlineSmall)
        }
    }

    @Composable
    fun DatePickerButton(label: String, date: Date?, onDateSelected: (Date) -> Unit) {
        val context = this@ViewExpensesActivity
        Button(onClick = {
            val calendar = Calendar.getInstance()
            date?.let { calendar.time = it }

            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val newDate = Calendar.getInstance()
                    newDate.set(year, month, dayOfMonth)
                    onDateSelected(newDate.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }) {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            Text(if (date == null) label else "$label: ${format.format(date)}")
        }
    }

    @Composable
    fun ExpenseItem(expense: Expense) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFD2B48C), shape = MaterialTheme.shapes.medium)
                .padding(16.dp)
        ) {
            Text("Category: ${expense.categoryName}", color = Color(0xFF8B5E3C))
            Text("Amount: ${expense.amount}", color = Color(0xFF8B5E3C))
            Text("Start: ${expense.startDate}", color = Color(0xFF8B5E3C))
            Text("End: ${expense.endDate}", color = Color(0xFF8B5E3C))
            Text("Description: ${expense.description}", color = Color(0xFF8B5E3C))
        }
    }
}
