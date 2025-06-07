package vcmsa.projects.loginpage.data

import android.content.Context

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = AppDatabase.getDatabase(context.applicationContext)
            INSTANCE = instance
            instance
        }
    }
}
