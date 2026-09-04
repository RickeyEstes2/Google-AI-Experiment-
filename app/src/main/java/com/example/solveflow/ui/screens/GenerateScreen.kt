package com.example.solveflow.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solveflow.data.model.ProgrammingLanguage
import com.example.solveflow.data.model.RlStrategy
import com.example.solveflow.ui.components.CodeViewDisplay
import com.example.solveflow.ui.viewmodel.CodeGenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateScreen(
    viewModel: CodeGenViewModel,
    modifier: Modifier = Modifier
) {
    val languages by viewModel.languages.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val promptText by viewModel.promptText.collectAsState()
    val selectedStrategy by viewModel.selectedStrategy.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val currentRecord by viewModel.currentRecord.collectAsState()

    var activeResultTab by remember { mutableIntStateOf(0) } // 0 = Code, 1 = Explanation

    val quickPrompts = listOf(
        "Safe Coroutine HTTP Fetcher with Retry" to "kotlin",
        "FastAPI Async Data Endpoint with Pydantic" to "python",
        "Thread-Safe In-Memory Cache with Arc & RwLock" to "rust",
        "Bounded Worker Pool with Channels & Context" to "go",
        "Resilient Fetch with AbortController Timeout" to "typescript",
        "Reactive Task Screen with Jetpack Compose" to "kotlin"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Prompt Input Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Describe the code or problem you want to solve:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = promptText,
                    onValueChange = { viewModel.setPromptText(it) },
                    placeholder = { Text("e.g., Write a resilient HTTP client with exponential backoff and JSON deserialization...") },
                    modifier = Modifier.fillMaxWidth().height(110.dp),
                    trailingIcon = {
                        if (promptText.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setPromptText("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Prompt Chips
                Text(
                    text = "Quick Examples:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickPrompts.forEach { (prompt, langId) ->
                        SuggestionChip(
                            onClick = {
                                viewModel.setPromptText(prompt)
                                languages.find { it.id == langId }?.let { viewModel.setSelectedLanguage(it) }
                            },
                            label = { Text(prompt, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // Language Selector Row
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Target Programming Language:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { viewModel.openNewLanguageDialog() }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Language", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                languages.forEach { lang ->
                    FilterChip(
                        selected = selectedLanguage?.id == lang.id,
                        onClick = { viewModel.setSelectedLanguage(lang) },
                        label = { Text(lang.name, fontSize = 12.sp) },
                        leadingIcon = if (selectedLanguage?.id == lang.id) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null
                    )
                }
            }
        }

        // Reinforcement Learning Strategy Selector
        Column {
            Text(
                text = "Reinforcement Learning Generation Policy:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Auto RL
                FilterChip(
                    selected = selectedStrategy == null,
                    onClick = { viewModel.setSelectedStrategy(null) },
                    label = { Text("🤖 Auto RL Policy (Q-Learning)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    leadingIcon = if (selectedStrategy == null) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null
                )

                RlStrategy.values().forEach { strategy ->
                    FilterChip(
                        selected = selectedStrategy == strategy,
                        onClick = { viewModel.setSelectedStrategy(strategy) },
                        label = { Text(strategy.title, fontSize = 12.sp) },
                        leadingIcon = if (selectedStrategy == strategy) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null
                    )
                }
            }
        }

        // Action Generate Button
        Button(
            onClick = { viewModel.generateCode() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = !isGenerating && promptText.isNotBlank() && selectedLanguage != null
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Synthesizing Code & Retrieving DBSCAN Clusters...")
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Code & Explain", fontWeight = FontWeight.Bold)
            }
        }

        // Output Section
        AnimatedVisibility(visible = currentRecord != null) {
            currentRecord?.let { record ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Context Badges Banner
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(record.strategy.badgeColorHex)
                                    ) {
                                        Text(
                                            text = record.strategy.title,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = selectedLanguage?.name ?: record.languageId,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "DBSCAN Context: ${record.clusterLabel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (record.isFineTuned) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF16A34A)
                                ) {
                                    Text(
                                        text = "Fine-Tuned",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Result Tabs (Code vs Explanation)
                    TabRow(selectedTabIndex = activeResultTab) {
                        Tab(
                            selected = activeResultTab == 0,
                            onClick = { activeResultTab = 0 },
                            text = { Text("Generated Code") },
                            icon = { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        Tab(
                            selected = activeResultTab == 1,
                            onClick = { activeResultTab = 1 },
                            text = { Text("Code Explanation") },
                            icon = { Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }

                    if (activeResultTab == 0) {
                        // Code Display
                        CodeViewDisplay(
                            code = record.editedCode ?: record.generatedCode,
                            languageName = selectedLanguage?.name ?: record.languageId,
                            onEditClicked = { viewModel.openFineTuningDialog() }
                        )
                    } else {
                        // Explanation Display
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = record.explanation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }

                    // Reinforcement Learning Feedback Bar
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Reinforcement Learning & Error Mitigation:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Thumbs Up / Down
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedButton(
                                        onClick = { viewModel.submitFeedback(1) },
                                        colors = if (record.userFeedback > 0) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors()
                                    ) {
                                        Icon(Icons.Default.ThumbUp, contentDescription = "Thumbs Up", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+1 Reward", fontSize = 11.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.submitFeedback(-1) },
                                        colors = if (record.userFeedback < 0) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer) else ButtonDefaults.outlinedButtonColors()
                                    ) {
                                        Icon(Icons.Default.ThumbDown, contentDescription = "Thumbs Down", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Penalize", fontSize = 11.sp)
                                    }
                                }

                                // Edit & Fine-Tune
                                Button(
                                    onClick = { viewModel.openFineTuningDialog() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit & Fine-Tune", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // GitHub Actions APK Packaging CTA Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = "Package into Android APK",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Generate GitHub Actions workflow to build and export an APK.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Button(
                                onClick = { viewModel.openGitHubApkDialog() }
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Package APK")
                            }
                        }
                    }
                }
            }
        }
    }
}
