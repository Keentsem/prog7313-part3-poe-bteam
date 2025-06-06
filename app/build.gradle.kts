plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    // Temporarily disable Hilt to fix crashing issues
    // id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.pocketsafe"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.pocketsafe"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments += mapOf(
            "runnerBuilder" to "de.mannodermaus.junit5.AndroidJUnit5Builder"
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    // Image loading and manipulation
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:ksp:4.16.0")
    
    // Permission handling
    implementation("com.github.permissions-dispatcher:permissionsdispatcher:4.9.2")
    ksp("com.github.permissions-dispatcher:permissionsdispatcher-processor:4.9.2")

    // Hilt - Disabled due to crashing issues
    // implementation("com.google.dagger:hilt-android:2.48")
    // ksp("com.google.dagger:hilt-android-compiler:2.48")
    // implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    // implementation("androidx.hilt:hilt-navigation-fragment:1.1.0")

    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.runtime:runtime")
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Navigation for Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.navigation:navigation-runtime-ktx:2.7.7") 
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // SwipeRefresh
    implementation("com.google.accompanist:accompanist-swiperefresh:0.32.0")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    
    // Work Manager for scheduling notifications
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}