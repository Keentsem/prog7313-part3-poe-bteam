// FILE: Account.kt
package vcmsa.projects.loginpage.AccountElements

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    val fullName: String,
    @PrimaryKey val email: String,
    val password: String,
    val confirmPassword: String
)
