package vcmsa.projects.loginpage

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import vcmsa.projects.loginpage.ui.theme.LoginPageTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginPageTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFF994F31)
                ) { innerPadding ->
                    LoginScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginMessage by remember { mutableStateOf("") }
    var messageColor by remember { mutableStateOf(Color.Red) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "PocketSafe",
            color = Color(0xFFEEBA86),
            fontSize = 30.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        val fieldColors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color(0xFFEEBA86),
            unfocusedIndicatorColor = Color(0xFFEEBA86),
            cursorColor = Color.White,
            focusedLabelColor = Color(0xFFEEBA86),
            unfocusedLabelColor = Color(0xFF4E342E),
            focusedTextColor = Color(0xFF4E342E),

            unfocusedTextColor = Color(0xFF6D4C41),
            unfocusedContainerColor = Color(0xFFFFF8E1), // Creamy white
            focusedContainerColor = Color(0xFFFFF8E1)    // Creamy white
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it.filterNot { ch -> ch.isWhitespace() } },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it.filterNot { ch -> ch.isWhitespace() } },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors
        )


        if (loginMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = loginMessage,
                color = messageColor,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isEmpty() || password.isEmpty()) {
                    loginMessage = "Please enter both email and password."
                    messageColor = Color.Red
                } else {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                                context.startActivity(Intent(context, MainMenu::class.java))
                            } else {
                                loginMessage = task.exception?.message ?: "Login failed."
                                messageColor = Color.Red
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEEBA86),
                contentColor = Color.Black
            )
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Don't have an account? Register",
            color = Color.White,
            modifier = Modifier.clickable {
                context.startActivity(Intent(context, RegisterActivity::class.java))
            }
        )
    }
}
