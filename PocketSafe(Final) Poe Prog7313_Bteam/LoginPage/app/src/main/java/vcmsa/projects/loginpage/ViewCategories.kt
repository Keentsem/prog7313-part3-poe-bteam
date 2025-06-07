package vcmsa.projects.loginpage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import vcmsa.projects.loginpage.CategoryElements.Category
import vcmsa.projects.loginpage.ui.theme.LoginPageTheme
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import kotlinx.coroutines.launch
import vcmsa.projects.loginpage.data.AppDatabase

class ViewCategories : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginPageTheme {
                CategoryListScreen(this) // pass activity reference
            }
        }
    }
}

@Composable
fun CategoryListScreen(activity: ComponentActivity) {
    var categoryList by remember { mutableStateOf<List<Category>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) } // State to control the dialog visibility
    var categoryIdToDelete by remember { mutableStateOf<Int?>(null) } // To store the category ID to delete
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val db = AppDatabase.getDatabase(activity.applicationContext)
        categoryList = db.categoryDao().getAllCategories()
    }

    // Function to delete category by ID
    fun deleteCategory(categoryId: Int) {
        coroutineScope.launch {
            val db = AppDatabase.getDatabase(activity.applicationContext)
            db.categoryDao().deleteCategoryById(categoryId)
            categoryList = categoryList.filter { it.id != categoryId } // Update the category list after deletion
        }
    }

    val scrollState = rememberScrollState()

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF994F31)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top-left Back text (stays fixed)
            Text(
                text = "Back",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .clickable { activity.finish() }
                    .padding(bottom = 16.dp)
                    .align(Alignment.Start)
            )

            // Scrollable content starts here
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "Category List",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFFEEBA86),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (categoryList.isEmpty()) {
                    Text("No categories found.", color = Color.White)
                } else {
                    categoryList.forEach { category ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEEBA86))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Name: ${category.name}", color = Color.Black)
                                Text("Total: ${category.total}", color = Color.Black)

                                Spacer(modifier = Modifier.height(8.dp))

                                // Smaller delete button
                                Button(
                                    onClick = {
                                        // Show the confirmation dialog
                                        categoryIdToDelete = category.id
                                        showDialog = true
                                    },
                                    modifier = Modifier
                                        .width(120.dp) // Set the width to make the button smaller
                                        .height(40.dp) // Set a fixed height
                                        .padding(4.dp), // Optional: Adjust padding
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                ) {
                                    Text("Delete", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for delete action
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false // Close the dialog when dismissing
            },
            title = { Text("Are you sure?") },
            text = { Text("Do you really want to delete this category?") },
            confirmButton = {
                TextButton(onClick = {
                    categoryIdToDelete?.let {
                        deleteCategory(it) // Delete the category
                    }
                    showDialog = false // Close the dialog after action
                }) {
                    Text("Yes", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false // Close the dialog without doing anything
                }) {
                    Text("No")
                }
            }
        )
    }
}

//