package vcmsa.projects.loginpage.CategoryElements

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import vcmsa.projects.loginpage.data.AppDatabase
import vcmsa.projects.loginpage.ui.theme.LoginPageTheme

class AddCategory : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginPageTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                var categoryName by remember { mutableStateOf("") }
                var isButtonDisabled by remember { mutableStateOf(false) }

                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF994F31)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Back Text
                        Text(
                            text = "Back",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .clickable { finish() }
                                .padding(bottom = 16.dp)
                        )

                        // Add vcmsa.projects.loginpage.AddExpense.Expense Title
                        Text(
                            text = "Add Category",
                            color = Color(0xFFEEBA86),
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Subtext
                        Text(
                            text = "Add your category details below.",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Category Name TextField
                        OutlinedTextField(
                            value = categoryName,
                            onValueChange = { categoryName = it },
                            label = { Text("Category Name") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White,
                                cursorColor = Color.White
                            )
                        )

                        // Confirm Button
                        Button(
                            onClick = {
                                if (categoryName.isNotBlank()) {
                                    val db = AppDatabase.getDatabase(applicationContext)
                                    val newCategory = Category(name = categoryName, total = 0.0)

                                    lifecycleScope.launch {
                                        // Disable the button to prevent multiple clicks
                                        isButtonDisabled = true

                                        val exists = db.categoryDao().countByName(categoryName) > 0
                                        if (exists) {
                                            snackbarHostState.showSnackbar("Category already exists.")
                                        } else {
                                            db.categoryDao().insertCategory(newCategory)
                                            snackbarHostState.showSnackbar("Category successfully added.")
                                            categoryName = "" // Reset the input field
                                        }

                                        // Re-enable the button after snackbar feedback
                                        isButtonDisabled = false
                                    }
                                } else {
                                    lifecycleScope.launch {
                                        snackbarHostState.showSnackbar("Please enter a category name.")
                                    }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 16.dp),
                            enabled = !isButtonDisabled, // Disable the button when adding
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEEBA86),
                                contentColor = Color.Black
                            )
                        ) {
                            Text(text = "Confirm")
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Snackbar Host
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}
