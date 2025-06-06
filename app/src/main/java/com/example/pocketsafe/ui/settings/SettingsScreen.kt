package com.example.pocketsafe.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketsafe.R
import com.example.pocketsafe.MainApplication
import com.example.pocketsafe.ui.theme.AppColors
import com.example.pocketsafe.util.PreferenceHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val pixelFont = FontFamily(Font(R.font.pixel_game))
    // Use the centralized app colors
    val goldColor = AppColors.Gold
    val brownColor = AppColors.Brown
    
    // Preferences
    val preferenceHelper = MainApplication.getPreferenceHelper(MainApplication.instance)
    var darkMode by remember { mutableStateOf(preferenceHelper.getDarkMode()) }
    var notificationsEnabled by remember { mutableStateOf(preferenceHelper.getNotificationsEnabled()) }
    var syncEnabled by remember { mutableStateOf(preferenceHelper.getSyncEnabled()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Settings",
                        fontFamily = pixelFont,
                        color = brownColor
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = brownColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = goldColor
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF9C4))
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // App Logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pocketsafe),
                    contentDescription = "PocketSafe Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.FillBounds
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Dark Mode Switch
                    SettingSwitch(
                        title = "Dark Mode",
                        description = "Enable dark theme for the app",
                        checked = darkMode,
                        onCheckedChange = { isChecked ->
                            darkMode = isChecked
                            preferenceHelper.setDarkMode(isChecked)
                        }
                    )
                    
                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Color.LightGray.copy(alpha = 0.5f)
                    )
                    
                    // Notifications Switch
                    SettingSwitch(
                        title = "Notifications",
                        description = "Enable bill and subscription reminders",
                        checked = notificationsEnabled,
                        onCheckedChange = { isChecked ->
                            notificationsEnabled = isChecked
                            preferenceHelper.setNotificationsEnabled(isChecked)
                        }
                    )
                    
                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Color.LightGray.copy(alpha = 0.5f)
                    )
                    
                    // Cloud Sync Switch
                    SettingSwitch(
                        title = "Cloud Sync",
                        description = "Sync data with Firebase",
                        checked = syncEnabled,
                        onCheckedChange = { isChecked ->
                            syncEnabled = isChecked
                            preferenceHelper.setSyncEnabled(isChecked)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // About Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = goldColor
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "About PocketSafe",
                        fontFamily = pixelFont,
                        fontSize = 18.sp,
                        color = brownColor
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Version 1.0.0",
                        fontFamily = pixelFont,
                        fontSize = 14.sp,
                        color = brownColor
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "A pixel-themed budgeting app with subscription tracking, bill reminders, and expense management.",
                        fontFamily = pixelFont,
                        fontSize = 14.sp,
                        color = brownColor,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    pixelFont: FontFamily = FontFamily(Font(R.font.pixel_game)),
    brownColor: Color = AppColors.Brown,
    goldColor: Color = AppColors.Gold
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontFamily = pixelFont,
                fontSize = 18.sp,
                color = brownColor
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                fontFamily = pixelFont,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = brownColor,
                checkedTrackColor = goldColor,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}
