// app/src/main/java/com/example/pocketsafe/HiltApplication.kt
package com.example.pocketsafe  // ← Must match your namespace

import android.app.Application
import dagger.hilt.android.HiltAndroidApp  // ← Import will work after gradle sync

@HiltAndroidApp  // ← This enables Hilt
class PocketSafeApplication : Application()