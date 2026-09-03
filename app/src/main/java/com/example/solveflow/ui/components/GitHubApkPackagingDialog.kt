package com.example.solveflow.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Terminal
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.solveflow.data.model.GenerationRecord
import com.example.solveflow.engine.github.GitHubActionsPackager

@Composable
fun GitHubApkPackagingDialog(
    record: GenerationRecord?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var repoUrlInput by remember { mutableStateOf("https://github.com/myusername/generated-app.git") }

    val workflowYaml = remember { GitHubActionsPackager.generateWorkflowYaml("GeneratedApp") }
    val gitCommands = remember(repoUrlInput) { GitHubActionsPackager.getGitPushCommands(repoUrlInput) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Package Code into APK (GitHub Actions)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Explanatory Banner
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Automated Cloud APK Compilation",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Push this repository to GitHub. The included GitHub Actions workflow will automatically compile your code using Java 21 & Gradle, and output an installable Android APK artifact in under 3 minutes.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Repository URL input for customized Git commands
                    OutlinedTextField(
                        value = repoUrlInput,
                        onValueChange = { repoUrlInput = it },
                        label = { Text("Your GitHub Repository URL") },
                        placeholder = { Text("https://github.com/username/repo.git") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Step 1: Git Commands
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF89B4FA), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Terminal Push Commands",
                                        color = Color(0xFFCDD6F4),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Git Commands", gitCommands))
                                        Toast.makeText(context, "Git commands copied!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF89B4FA), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy Commands", color = Color(0xFF89B4FA), fontSize = 11.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = gitCommands,
                                color = Color(0xFFA6ADC8),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Step 2: GitHub Actions Workflow YAML preview
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ".github/workflows/build_apk.yml",
                                    color = Color(0xFFBAC2DE),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                TextButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Workflow YAML", workflowYaml))
                                        Toast.makeText(context, "Workflow YAML copied!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF89B4FA), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy YAML", color = Color(0xFF89B4FA), fontSize = 11.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = workflowYaml,
                                color = Color(0xFFA6ADC8),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                maxLines = 15
                            )
                        }
                    }

                    // Instructions for downloading the compiled APK
                    Column {
                        Text(
                            text = "How to Download your APK from GitHub:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "1. Run the terminal push commands above.\n" +
                                    "2. Go to your GitHub repository in your browser and click on the 'Actions' tab.\n" +
                                    "3. Click on the latest workflow run ('Package Android APK').\n" +
                                    "4. Scroll down to the 'Artifacts' section and click on 'debug-apk' to download your compiled APK file.",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) {
                        Text("Done")
                    }
                }
            }
        }
    }
}
