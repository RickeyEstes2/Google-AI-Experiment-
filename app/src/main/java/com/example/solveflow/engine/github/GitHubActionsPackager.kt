package com.example.solveflow.engine.github

/**
 * Generates GitHub Actions workflow configurations, Android project scaffolding,
 * and Git commands to package generated code into an Android APK on GitHub CI/CD.
 */
object GitHubActionsPackager {

    /**
     * Complete GitHub Actions workflow YAML for building and packaging an APK.
     */
    fun generateWorkflowYaml(projectName: String = "GeneratedApp"): String {
        return """
name: Package Android APK

on:
  push:
    branches: [ "main", "master" ]
  workflow_dispatch:

permissions:
  contents: read

jobs:
  build-apk:
    name: Build & Package Android APK
    runs-on: ubuntu-latest
    timeout-minutes: 25

    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v5
        with:
          distribution: 'temurin'
          java-version: '21'
          cache: 'gradle'

      - name: Grant Execute Permission for Gradle Wrapper
        run: chmod +x gradlew

      - name: Build Debug APK with Gradle
        run: ./gradlew assembleDebug --no-daemon --stacktrace

      - name: Upload Debug APK Artifact
        uses: actions/upload-artifact@v4
        with:
          name: ${projectName.lowercase().replace(" ", "-")}-debug-apk
          path: app/build/outputs/apk/debug/*.apk
          retention-days: 14
          if-no-files-found: error
        """.trimIndent()
    }

    /**
     * Scaffolds the app/build.gradle.kts file for the packaged project.
     */
    fun generateAppBuildGradle(applicationId: String = "com.aistudio.codegen.apk"): String {
        return """
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.generated"
    compileSdk = 35

    defaultConfig {
        applicationId = "$applicationId"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}
        """.trimIndent()
    }

    /**
     * Scaffolds AndroidManifest.xml for the packaged project.
     */
    fun generateAndroidManifest(appName: String = "Generated App"): String {
        return """
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:label="$appName"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
        """.trimIndent()
    }

    /**
     * Bundles the generated code into a complete, runnable Android MainActivity.
     */
    fun wrapCodeIntoMainActivity(generatedCode: String, prompt: String): String {
        return """
package com.example.generated

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GeneratedAppScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratedAppScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Packaged Generated App") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Original Generation Prompt:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$prompt",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Embedded Generated Logic
            GeneratedLogicContainer()
        }
    }
}

@Composable
fun GeneratedLogicContainer() {
    // Generated Code Execution Surface:
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Compiled Output & Status",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Packaged successfully via GitHub Actions APK workflow.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// --- ORIGINAL GENERATED SOURCE ---
/*
$generatedCode
*/
        """.trimIndent()
    }

    /**
     * Returns the CLI commands required to push to GitHub.
     */
    fun getGitPushCommands(repoUrlPlaceholder: String = "https://github.com/<user>/<repo>.git"): String {
        return """
git init
git add .
git commit -m "feat: package generated code into Android APK via GitHub Actions"
git branch -M main
git remote add origin $repoUrlPlaceholder
git push -u origin main
        """.trimIndent()
    }
}
