ST10275475
ST10296904
ST10284789
ST10391223
Keentse, Mojalefa, JT Gounder, Keanan
The B Team (group 2) 
Willander

Youtube:
GitHub:

PocketSafe Android App

Project Overview
PocketSafe is a modern Android application developed using Kotlin, Jetpack Compose, Room DB, and Navigation-Compose. It provides user management, budgeting, and expense tracking features with a Compose-first UI architecture.

Tech Stack

| Feature               | Technology/Library                          |
|-----------------------|---------------------------------------------|
| UI                    | Jetpack Compose + Material 3                |
| Navigation            | navigation-compose                          |
| Local DB              | Room + Kotlin Kapt                          |
| Refresh UX            | accompanist-swiperefresh                    |
| Lifecycle             | lifecycle-runtime-ktx                       |
| Compatibility         | AppCompat                                   |
| Min SDK               | 24                                          |
| Target/Compile SDK    | 35                                          |
| JVM Target            | 11                                          |

Project Structure

| Module                      | Description                                 |
|-----------------------------|---------------------------------------------|
| MainActivity                | Entry point and launcher activity           |
| RegisterActivity            | Handles user registration                   |
| UserList                    | Displays all registered users               |
| MainMenu                    | Central navigation menu                     |
| Goals                       | Handles user budget goals                   |
| AddCategory, ViewCategories | Manage expense categories                   |
| ExpenseEntry, ViewExpensesActivity | Input and display of expenses     |


 Plugins Used
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt") // For Room annotation processing
    id("org.jetbrains.kotlin.kapt")
}
```

Dependencies Highlights

Jetpack Compose UI
```kotlin
implementation("androidx.compose.material3:material3:1.0.0")
implementation(libs.androidx.ui)
implementation(libs.androidx.ui.graphics)
implementation(libs.androidx.ui.tooling.preview)
```

 Room Database
```kotlin
implementation("androidx.room:room-runtime:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
```

 Navigation & Lifecycle
```kotlin
implementation("androidx.navigation:navigation-compose:2.7.7")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
```

Swipe Refresh
```kotlin
implementation("com.google.accompanist:accompanist-swiperefresh:0.33.2-alpha")
```

 Testing Support

- junit for unit testing
- espresso-core for UI testing
- Compose UI Test libraries included for instrumentation

Running the App

1. Open the project in Android Studio Giraffe or newer.
2. Sync Gradle.
3. Run on device/emulator (API 24+).

Notes

- We are using both kotlin-kapt and org.jetbrains.kotlin.kapt. These can be redundant; We will consider using only one to prevent possible annotation processing conflicts.
- Scoped storage is recommended over READ/WRITE_EXTERNAL_STORAGE for newer Android versions upon research.

####################################################################################################################################

Updated Features:

Main Change: Migration to online Firebase and Authentication
Visuals: Added Graphs showing goals visually

Added Features:

My Subscription Tracker: Track your subscriptions by adding and viewing
My Pocket Splitter a fun way to distribute a budget amongst a group
##########################################################################################################################################
