package vcmsa.projects.loginpage.user

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import vcmsa.projects.loginpage.RegisterActivity
import vcmsa.projects.loginpage.User
import vcmsa.projects.loginpage.UserDatabase
import vcmsa.projects.loginpage.ui.theme.LoginPageTheme

class UserList : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userDao = UserDatabase.getDatabase(this).userDao()
        val usersState = mutableStateListOf<User>()

        lifecycleScope.launch {
            usersState.clear()
            usersState.addAll(userDao.getAllUsers())
        }

        setContent {
            LoginPageTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    UserListScreen(
                        users = usersState,
                        onBackClick = {
                            startActivity(Intent(this@UserList, RegisterActivity::class.java))
                            finish()
                        },
                        onDeleteClick = { user ->
                            lifecycleScope.launch {
                                userDao.deleteUser(user)
                                usersState.remove(user)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UserListScreen(
    users: List<User>,
    onBackClick: () -> Unit,
    onDeleteClick: (User) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("User List", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = onBackClick) {
                Text("Back")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(users) { user ->
                UserItem(user = user, onDeleteClick = onDeleteClick)
                Divider()
            }
        }
    }
}

@Composable
fun UserItem(user: User, onDeleteClick: (User) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text("Full Name: ${user.fullName}", style = MaterialTheme.typography.bodyLarge)
        Text("Email: ${user.email}", style = MaterialTheme.typography.bodyMedium)
        Text("Password: ${user.password}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = { onDeleteClick(user) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Delete", color = MaterialTheme.colorScheme.onError)
        }
    }
}
