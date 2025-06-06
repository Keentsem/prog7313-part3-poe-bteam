package com.example.pocketsafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.example.pocketsafe.navigation.PocketSafeNavHost
import com.example.pocketsafe.ui.theme.PocketSafeTheme

class NavigationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            PocketSafeTheme {
                // Gold (#f3c34e) background color for the app's pixel-retro theme
                val backgroundColor = Color(0xFFFFF9C4) 
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = backgroundColor
                ) { innerPadding ->
                    val navController = rememberNavController()
                    
                    // Use our new PocketSafeNavHost
                    PocketSafeNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
