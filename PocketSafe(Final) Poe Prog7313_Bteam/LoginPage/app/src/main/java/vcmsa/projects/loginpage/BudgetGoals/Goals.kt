package vcmsa.projects.loginpage.BudgetGoals

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import vcmsa.projects.loginpage.MainMenu
import vcmsa.projects.loginpage.ui.theme.LoginPageTheme

@OptIn(ExperimentalMaterial3Api::class)
class Goals : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginPageTheme {
                GoalsScreen()
            }
        }
    }
}

data class ExpenseCategoryData(
    val category: String,
    val totalAmount: Double
)

enum class TimeFilter(val display: String) {
    CURRENT_MONTH("Current Month"),
    LAST_MONTH("Last Month"),
    THIS_YEAR("This Year")
}

@Preview(showBackground = true)
@Composable
fun GoalsScreen() {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("GoalPrefs", Context.MODE_PRIVATE)

    var minGoal by remember { mutableStateOf("") }
    var maxGoal by remember { mutableStateOf("") }
    var income by remember { mutableStateOf("") }

    var currentMin by remember { mutableStateOf("") }
    var currentMax by remember { mutableStateOf("") }
    var currentIncome by remember { mutableStateOf("") }

    var totalExpenses by remember { mutableStateOf(0.0) }
    var goalStatus by remember { mutableStateOf("") }
    var goalColor by remember { mutableStateOf(Color.Transparent) }

    var categoryTotals by remember { mutableStateOf<List<ExpenseCategoryData>>(emptyList()) }
    var selectedTimeFilter by remember { mutableStateOf(TimeFilter.CURRENT_MONTH) }

    val firestore = FirebaseFirestore.getInstance()

    fun fetchAndFilterData(timeFilter: TimeFilter) {
        firestore.collection("expenses")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val filtered = querySnapshot.documents.mapNotNull { doc ->
                    try {
                        val amount = doc.getDouble("amount") ?: 0.0
                        val startDateStr = doc.getString("startDate") ?: return@mapNotNull null
                        val category = doc.getString("category") ?: "Uncategorized"
                        val date = formatter.parse(startDateStr) ?: return@mapNotNull null

                        val match = when (timeFilter) {
                            TimeFilter.CURRENT_MONTH -> isInCurrentMonth(date)
                            TimeFilter.LAST_MONTH -> isInLastMonth(date)
                            TimeFilter.THIS_YEAR -> isInCurrentYear(date)
                        }

                        if (match) Pair(category, amount) else null
                    } catch (e: Exception) {
                        null
                    }
                }

                totalExpenses = filtered.sumOf { it.second }
                val totalsByCategory = filtered.groupBy({ it.first }, { it.second })
                    .map { ExpenseCategoryData(it.key, it.value.sum()) }
                    .sortedByDescending { it.totalAmount }

                categoryTotals = totalsByCategory
                sharedPref.edit().putInt("totalExpenses", totalExpenses.toInt()).apply()

                val (status, color) = getGoalStatus(currentMin, currentMax, currentIncome, totalExpenses)
                goalStatus = status
                goalColor = color
            }
            .addOnFailureListener {
                totalExpenses = 0.0
                goalStatus = "Failed to load expenses"
                goalColor = Color.Red
            }
    }

    LaunchedEffect(selectedTimeFilter) {
        currentMin = sharedPref.getString("minGoal", "") ?: ""
        currentMax = sharedPref.getString("maxGoal", "") ?: ""
        currentIncome = sharedPref.getString("income", "") ?: ""
        fetchAndFilterData(selectedTimeFilter)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color(0xFF8B5E3C))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Back",
            modifier = Modifier
                .clickable {
                    val intent = Intent(context, MainMenu::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    (context as? ComponentActivity)?.finish()

                }
                .padding(16.dp),
            color = Color(0xFFF5F5DC),
            style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.Underline)
        )

        Text("Current Min Goal: $currentMin", color = Color(0xFFF5F5DC))
        Text("Current Max Goal: $currentMax", color = Color(0xFFF5F5DC))
        Text("Current Monthly Income: $currentIncome", color = Color(0xFFF5F5DC))
        Text("Total Expenses: $totalExpenses", color = Color(0xFFF5F5DC))

        if (goalStatus.isNotEmpty()) {
            Text(goalStatus, color = goalColor, style = MaterialTheme.typography.titleMedium)
        }

        var expanded by remember { mutableStateOf(false) }
        Box {
            Text(
                text = "View: ${selectedTimeFilter.display}",
                modifier = Modifier
                    .clickable { expanded = true }
                    .background(Color(0xFFD2B29D), RoundedCornerShape(4.dp))
                    .padding(12.dp),
                color = Color(0xFF5C4033)
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                TimeFilter.values().forEach { filter ->
                    DropdownMenuItem(
                        text = { Text(filter.display) },
                        onClick = {
                            selectedTimeFilter = filter
                            expanded = false
                        }
                    )
                }
            }
        }

        if (categoryTotals.isNotEmpty()) {
            Text(
                "Expenses by Category",
                color = Color(0xFFF5F5DC),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            ExpenseCategoryBarGraph(categoryTotals)
            Spacer(modifier = Modifier.height(16.dp))
            MinMaxGoalBarGraph(currentMin, currentMax, currentIncome, totalExpenses)
        }

        OutlinedTextField(
            value = minGoal,
            onValueChange = { input -> minGoal = input.filter { it.isDigit() } },
            label = { Text("Min Goal") },
            modifier = Modifier.fillMaxWidth(),
            colors = goalTextFieldColors(),
            singleLine = true
        )

        OutlinedTextField(
            value = maxGoal,
            onValueChange = { input -> maxGoal = input.filter { it.isDigit() } },
            label = { Text("Max Goal") },
            modifier = Modifier.fillMaxWidth(),
            colors = goalTextFieldColors(),
            singleLine = true
        )

        OutlinedTextField(
            value = income,
            onValueChange = { input -> income = input.filter { it.isDigit() } },
            label = { Text("Monthly Income") },
            modifier = Modifier.fillMaxWidth(),
            colors = goalTextFieldColors(),
            singleLine = true
        )

        Button(
            onClick = {
                val finalMin = if (minGoal.isNotBlank()) minGoal else currentMin
                val finalMax = if (maxGoal.isNotBlank()) maxGoal else currentMax
                val finalIncome = if (income.isNotBlank()) income else currentIncome

                saveGoalsToPreferences(context, finalMin, finalMax, finalIncome)

                currentMin = finalMin
                currentMax = finalMax
                currentIncome = finalIncome

                val (status, color) = getGoalStatus(finalMin, finalMax, finalIncome, totalExpenses)
                goalStatus = status
                goalColor = color

                minGoal = ""
                maxGoal = ""
                income = ""
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD2B29D))
        ) {
            Text("Set", color = Color(0xFF5C4033))
        }
    }
}

@Composable
fun ExpenseCategoryBarGraph(data: List<ExpenseCategoryData>) {
    val maxAmount = data.maxOfOrNull { it.totalAmount } ?: 1.0
    val selectedCategory = remember { mutableStateOf<ExpenseCategoryData?>(null) }

    Box { // Wrapper for popup positioning
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp)
                .padding(horizontal = 4.dp)
        ) {
            data.forEach { categoryData ->
                val barWidthFraction = (categoryData.totalAmount / maxAmount).toFloat()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .clickable { selectedCategory.value = categoryData }
                ) {
                    Text(
                        text = categoryData.category,
                        color = Color(0xFFF5F5DC),
                        modifier = Modifier.width(100.dp),
                        maxLines = 1,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .fillMaxWidth(barWidthFraction)
                            .background(Color(0xFFDEB887), shape = RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "%.2f".format(categoryData.totalAmount),
                            color = Color(0xFF5C4033),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }
            }
        }

        // Popup showing the selected value
        val selected = selectedCategory.value
        if (selected != null) {
            Popup(
                onDismissRequest = { selectedCategory.value = null },
                alignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Value for ${selected.category}: %.2f".format(selected.totalAmount),
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}


@Composable
fun MinMaxGoalBarGraph(min: String, max: String, income: String, expenses: Double) {
    val parsedMin = min.toDoubleOrNull() ?: 0.0
    val parsedMax = max.toDoubleOrNull() ?: 0.0
    val parsedIncome = income.toDoubleOrNull() ?: 0.0
    val remainder = parsedIncome - expenses

    val maxValue = listOf(parsedMin, parsedMax, remainder).maxOrNull()?.takeIf { it > 0 } ?: 1.0

    val data = listOf(
        Triple("Min Goal", parsedMin, Color(0xFF8FBC8F)),
        Triple("Max Goal", parsedMax, Color(0xFF4682B4)),
        Triple("Remainder", remainder, Color(0xFFFFA500))
    )

    val selectedLabel = remember { mutableStateOf<Triple<String, Double, Color>?>(null) }

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            data.forEach { (label, value, color) ->
                val fraction = (value / maxValue).toFloat()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .clickable { selectedLabel.value = Triple(label, value, color) }
                ) {
                    Text(
                        text = label,
                        color = Color.White,
                        modifier = Modifier.width(100.dp),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .fillMaxWidth(fraction)
                            .background(color, shape = RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "%.2f".format(value),
                            color = Color.Black,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }
            }
        }

        // Popup with selected value
        val selected = selectedLabel.value
        if (selected != null) {
            Popup(
                onDismissRequest = { selectedLabel.value = null },
                alignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "${selected.first} value: %.2f".format(selected.second),
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun goalTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFFDEB887),
    unfocusedBorderColor = Color(0xFFDEB887),
    focusedLabelColor = Color(0xFFDEB887),
    unfocusedLabelColor = Color(0xFFDEB887),
    focusedTextColor = Color(0xFFDEB887),
    unfocusedTextColor = Color(0xFFDEB887),
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    cursorColor = Color(0xFFDEB887)
)

fun isInCurrentMonth(date: Date): Boolean {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    calendar.time = date
    return calendar.get(Calendar.MONTH) == currentMonth && calendar.get(Calendar.YEAR) == currentYear
}

fun isInLastMonth(date: Date): Boolean {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, -1)
    val lastMonth = calendar.get(Calendar.MONTH)
    val lastYear = calendar.get(Calendar.YEAR)
    calendar.time = date
    return calendar.get(Calendar.MONTH) == lastMonth && calendar.get(Calendar.YEAR) == lastYear
}

fun isInCurrentYear(date: Date): Boolean {
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    calendar.time = date
    return calendar.get(Calendar.YEAR) == currentYear
}

fun saveGoalsToPreferences(context: Context, min: String, max: String, income: String) {
    val sharedPref = context.getSharedPreferences("GoalPrefs", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putString("minGoal", min)
        putString("maxGoal", max)
        putString("income", income)
        apply()
    }
}

fun getGoalStatus(min: String, max: String, income: String, totalExpenses: Double): Pair<String, Color> {
    return try {
        val minVal = min.toDouble()
        val maxVal = max.toDouble()
        val incomeVal = income.toDouble()
        val remainder = incomeVal - totalExpenses

        when {
            remainder > maxVal -> "Monthly Goal has been reached" to Color(0xFFDEB887)
            remainder > minVal -> "Monthly goal Partially achieved" to Color(0xFFFFA000)
            else -> "Monthly Goal not achieved" to Color.Gray
        }
    } catch (e: Exception) {
        "" to Color.Transparent
    }
}
