package vcmsa.projects.loginpage.SubscriptionTracker

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.elevatedCardElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import vcmsa.projects.loginpage.MainMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionTracker(viewModel: SubscriptionViewModel) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var recurrence by remember { mutableStateOf("Monthly") }
    var nextDate by remember { mutableStateOf("") }

    var formExpanded by remember { mutableStateOf(false) }

    val subscriptions by viewModel.subscriptions.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val oakColor = Color(0xFFF0E6D2)

    val dateOptions = (1..30).map { it.toString() }
    var nextDateExpanded by remember { mutableStateOf(false) }

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val calendar = java.util.Calendar.getInstance()
    val today = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    val currentMonthIndex = calendar.get(java.util.Calendar.MONTH)

    val upcomingSubscriptions = subscriptions
        .filter { it.nextBillingDate.toIntOrNull() != null }
        .sortedBy {
            val billingDay = it.nextBillingDate.toInt()
            if (billingDay >= today) billingDay - today else billingDay + 30 - today
        }
        .take(2)

    val nextSubscription = upcomingSubscriptions.firstOrNull()

    LaunchedEffect(nextSubscription) {
        nextSubscription?.let {
            val billingDay = it.nextBillingDate.toInt()
            val showNextMonth = billingDay < today
            val displayMonthIndex = if (showNextMonth) (currentMonthIndex + 1) % 12 else currentMonthIndex
            val displayMonth = monthNames[displayMonthIndex]

            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    "Next subscription due: ${it.name} on $billingDay $displayMonth"
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
            .fillMaxSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Back",
                fontSize = 14.sp,
                color = oakColor,
                modifier = Modifier
                    .clickable {
                        val intent = Intent(context, MainMenu::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        (context as? ComponentActivity)?.finish()
                    }
                    .padding(end = 12.dp)
            )
            Text(
                text = "Subscription Tracker",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = oakColor
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        nextSubscription?.let { sub ->
            val billingDay = sub.nextBillingDate.toInt()
            val showNextMonth = billingDay < today
            val displayMonthIndex = if (showNextMonth) (currentMonthIndex + 1) % 12 else currentMonthIndex
            val displayMonth = monthNames[displayMonthIndex]

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4E342E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Next Subscription Due:", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(sub.name, color = Color.White)
                    Text("Due on: $billingDay $displayMonth", color = Color.White)
                    Text("Amount: R${sub.amount}", color = Color.White)
                }
            }
        }

        upcomingSubscriptions.forEach { sub ->
            val billingDay = sub.nextBillingDate.toInt()
            val showNextMonth = billingDay < today
            val displayMonthIndex = if (showNextMonth) (currentMonthIndex + 1) % 12 else currentMonthIndex
            val displayMonth = monthNames[displayMonthIndex]

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF6D4C41)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(sub.name, color = Color.White)
                    Text("Billing Day: ${sub.nextBillingDate} ($displayMonth) – R${sub.amount}", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Collapsible Form Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0C097))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { formExpanded = !formExpanded }
                ) {
                    Text(
                        text = if (formExpanded) "Hide Form" else "Add New Subscription",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (formExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.Black
                    )
                }

                if (formExpanded) {
                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Subscription Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount (R)") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = recurrence,
                        onValueChange = {},
                        label = { Text("Recurrence") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    ExposedDropdownMenuBox(
                        expanded = nextDateExpanded,
                        onExpandedChange = { nextDateExpanded = !nextDateExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = nextDate,
                            onValueChange = {},
                            label = { Text("Billing Date (Day of Month)") },
                            readOnly = true,
                            enabled = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = nextDateExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = nextDateExpanded,
                            onDismissRequest = { nextDateExpanded = false }
                        ) {
                            dateOptions.forEach { day ->
                                DropdownMenuItem(
                                    text = { Text(day, color = Color.Black) },
                                    onClick = {
                                        nextDate = day
                                        nextDateExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank() && amount.toDoubleOrNull() != null && nextDate.isNotBlank()) {
                                viewModel.add(
                                    Subscription(
                                        name = name,
                                        amount = amount.toDouble(),
                                        recurrence = recurrence,
                                        nextBillingDate = nextDate
                                    )
                                )
                                name = ""
                                amount = ""
                                recurrence = "Monthly"
                                nextDate = ""
                                formExpanded = false
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Please fill in all required fields.")
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth()
                    ) {
                        Text("Add Subscription")
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState)

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            items(subscriptions) { sub ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    elevation = elevatedCardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = sub.name, style = MaterialTheme.typography.titleLarge)
                        Text(text = "R${sub.amount}")
                        Text(text = "Recurs: ${sub.recurrence}")

                        val billingDay = sub.nextBillingDate.toIntOrNull()
                        val displayMonth = if (billingDay != null) {
                            val showNextMonth = billingDay < today
                            val targetMonthIndex = if (showNextMonth) (currentMonthIndex + 1) % 12 else currentMonthIndex
                            monthNames[targetMonthIndex]
                        } else {
                            "Invalid"
                        }

                        Text(text = "Next Billing: ${sub.nextBillingDate} ($displayMonth)")

                        Spacer(Modifier.height(4.dp))
                        Row {
                            TextButton(onClick = { viewModel.delete(sub.id) }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
