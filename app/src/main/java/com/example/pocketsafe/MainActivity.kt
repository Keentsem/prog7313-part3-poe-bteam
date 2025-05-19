package com.example.pocketsafe

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.pocketsafe.ui.theme.PocketSafeTheme
import com.example.pocketsafe.data.UserDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PocketSafeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFF994F31)
                ) { innerPadding ->
                    LoginPageLayout(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LoginPageLayout(modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginMessage by remember { mutableStateOf("") }
    var messageColor by remember { mutableStateOf(Color.Red) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color(0xFF994F31), shape = RoundedCornerShape(8.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PocketSafe",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFFEEBA86),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Email input field
        BasicTextField(
            value = email,
            onValueChange = { email = it },
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(8.dp)
                        .background(Color(0xFFF1E1C6), shape = RoundedCornerShape(8.dp))
                        .padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (email.isEmpty()) {
                        Text("Email", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password input field
        BasicTextField(
            value = password,
            onValueChange = { password = it },
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(8.dp)
                        .background(Color(0xFFF1E1C6), shape = RoundedCornerShape(8.dp))
                        .padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (password.isEmpty()) {
                        Text("Password", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    val userDao = UserDatabase.getDatabase(context).userDao()
                    val user = userDao.getUserByEmail(email)

                    if (user != null && user.password == password) {
                        loginMessage = "Login Successful"
                        messageColor = Color.Green
                        val intent = Intent(context, MainMenu::class.java)
                        context.startActivity(intent)
                    } else {
                        loginMessage = "Invalid credentials"
                        messageColor = Color.Gray
                        Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Login", color = Color.White)
        }

        Text(
            text = loginMessage,
            color = messageColor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .align(Alignment.Start)
        ) {
            Text(
                text = "No account? Register",
                color = Color(0xFFEEBA86),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable {
                    val intent = Intent(context, RegisterActivity::class.java)
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPagePreview() {
    PocketSafeTheme {
        LoginPageLayout()
    }
} 