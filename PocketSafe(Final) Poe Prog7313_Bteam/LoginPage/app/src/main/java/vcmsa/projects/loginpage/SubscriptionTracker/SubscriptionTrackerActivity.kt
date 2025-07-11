package vcmsa.projects.loginpage.SubscriptionTracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel

class SubscriptionTrackerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: SubscriptionViewModel = viewModel()
            SubscriptionTracker(viewModel = viewModel)
        }
    }
}
