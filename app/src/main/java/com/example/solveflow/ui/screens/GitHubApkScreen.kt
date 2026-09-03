package com.example.solveflow.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solveflow.engine.github.GitHubActionsPackager
import com.example.solveflow.ui.viewmodel.CodeGenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitHubApkScreen(
    viewModel: CodeGenViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentRecord by viewModel.currentRecord.collectAsState()

    var repoUrl by remember { mutableStateOf("https://github.com/myusername/my-codegen-app.git") }
    var selectedFileTab by remember { mutableIntStateOf(0) } // 0 = build_apk.yml, 1 = MainActivity.kt, 2 = build.gradle.kts

    val workflowYaml = remember { GitHubActionsPackager.generateWorkflowYaml("GeneratedApp") }
    val mainActivityCode = remember(currentRecord) {
        GitHubActionsPackager.wrapCodeIntoMainActivity(
            currentRecord?.editedCode ?: currentRecord?.generatedCode ?: "// No code generated yet",
            currentRecord?.prompt ?: "Custom Prompt"
        )
    }
    val buildGradleCode = remember { GitHubActionsPackager.generateAppBuildGradle() }
    val gitCommands = remember(repoUrl) { GitHubActionsPackager.getGitPushCommands(repoUrl) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "GitHub Actions APK Packaging Pipeline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Package your generated multi-language or Android Kotlin code into an installable APK using GitHub Actions CI/CD with automated Java 21 & Gradle compilation.",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )
            }
        }

        // Repository Config
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "1. Target GitHub Repository:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = repoUrl,
                    onValueChange = { repoUrl = it },
                    label = { Text("Remote Git URL") },
                    placeholder = { Text("https://github.com/<user>/<repo>.git") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // Terminal CLI Push Commands
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF89B4FA), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "2. Terminal Push Commands",
                            color = Color(0xFFCDD6F4),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Git Push Commands", gitCommands))
                            Toast.makeText(context, "Copied all push commands!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF89B4FA)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF1E1E2E), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Commands", color = Color(0xFF1E1E2E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = gitCommands,
                    color = Color(0xFFA6ADC8),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // Project Scaffolding Previews
        Column {
            Text(
                text = "3. Scaffolding Files Generated for APK Build:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            TabRow(selectedTabIndex = selectedFileTab) {
                Tab(
                    selected = selectedFileTab == 0,
                    onClick = { selectedFileTab = 0 },
                    text = { Text("build_apk.yml", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedFileTab == 1,
                    onClick = { selectedFileTab = 1 },
                    text = { Text("MainActivity.kt", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedFileTab == 2,
                    onClick = { selectedFileTab = 2 },
                    text = { Text("build.gradle.kts", fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val currentFileContent = when (selectedFileTab) {
                0 -> workflowYaml
                1 -> mainActivityCode
                else -> buildGradleCode
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("File Content", currentFileContent))
                                Toast.makeText(context, "Copied file content!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF89B4FA), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy File", color = Color(0xFF89B4FA), fontSize = 11.sp)
                        }
                    }

                    Text(
                        text = currentFileContent,
                        color = Color(0xFFCDD6F4),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        maxLines = 18
                    )
                }
            }
        }

        // Instructions
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "How to Collect Your Downloadable APK:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Once pushed, GitHub Actions triggers the '.github/workflows/build_apk.yml' job.\n" +
                            "• The runner executes with Temurin JDK 21 and compiles the APK with Gradle.\n" +
                            "• On completion, open your repository's 'Actions' tab and download the 'debug-apk' artifact directly.",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
