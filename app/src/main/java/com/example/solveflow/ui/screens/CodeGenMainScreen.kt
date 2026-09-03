package com.example.solveflow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solveflow.ui.components.*
import com.example.solveflow.ui.viewmodel.CodeGenTab
import com.example.solveflow.ui.viewmodel.CodeGenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeGenMainScreen(
    viewModel: CodeGenViewModel
) {
    val activeTab by viewModel.activeTab.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val languages by viewModel.languages.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val currentRecord by viewModel.currentRecord.collectAsState()

    // Dialog states
    val showFineTuningDialog by viewModel.showFineTuningDialog.collectAsState()
    val showNewLanguageDialog by viewModel.showNewLanguageDialog.collectAsState()
    val showNewSnippetDialog by viewModel.showNewSnippetDialog.collectAsState()
    val showNewKnowledgeDialog by viewModel.showNewKnowledgeDialog.collectAsState()
    val showGitHubApkDialog by viewModel.showGitHubApkDialog.collectAsState()
    var showGeminiKeyDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Code Generator",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 19.sp
                            )
                            Text(
                                text = "DBSCAN • Reinforcement Learning • CI/CD APK",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showGeminiKeyDialog = true }) {
                        Icon(Icons.Default.Key, contentDescription = "Gemini API Key")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = activeTab == CodeGenTab.GENERATE,
                    onClick = { viewModel.setActiveTab(CodeGenTab.GENERATE) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                    label = { Text("Generate", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == CodeGenTab.SNIPPETS,
                    onClick = { viewModel.setActiveTab(CodeGenTab.SNIPPETS) },
                    icon = { Icon(Icons.Default.DataObject, contentDescription = null) },
                    label = { Text("Snippets", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == CodeGenTab.KNOWLEDGE,
                    onClick = { viewModel.setActiveTab(CodeGenTab.KNOWLEDGE) },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                    label = { Text("Knowledge", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == CodeGenTab.LANGUAGES,
                    onClick = { viewModel.setActiveTab(CodeGenTab.LANGUAGES) },
                    icon = { Icon(Icons.Default.Code, contentDescription = null) },
                    label = { Text("Languages", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == CodeGenTab.DBSCAN_RL,
                    onClick = { viewModel.setActiveTab(CodeGenTab.DBSCAN_RL) },
                    icon = { Icon(Icons.Default.Hub, contentDescription = null) },
                    label = { Text("DBSCAN/RL", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == CodeGenTab.GITHUB_APK,
                    onClick = { viewModel.setActiveTab(CodeGenTab.GITHUB_APK) },
                    icon = { Icon(Icons.Default.Build, contentDescription = null) },
                    label = { Text("Build APK", fontSize = 10.sp) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (activeTab) {
                CodeGenTab.GENERATE -> GenerateScreen(viewModel = viewModel)
                CodeGenTab.SNIPPETS -> SnippetsScreen(viewModel = viewModel)
                CodeGenTab.KNOWLEDGE -> KnowledgeScreen(viewModel = viewModel)
                CodeGenTab.LANGUAGES -> LanguagesScreen(viewModel = viewModel)
                CodeGenTab.DBSCAN_RL -> DbscanRlScreen(viewModel = viewModel)
                CodeGenTab.GITHUB_APK -> GitHubApkScreen(viewModel = viewModel)
            }
        }
    }

    // Modal Dialogs
    if (showFineTuningDialog && currentRecord != null) {
        ErrorMitigationDialog(
            record = currentRecord!!,
            onDismiss = { viewModel.closeFineTuningDialog() },
            onApplyFineTune = { editedCode, mitigationType, notes, saveAsExemplar ->
                viewModel.applyErrorMitigationAndFineTune(
                    editedCode = editedCode,
                    mitigationType = mitigationType,
                    notes = notes,
                    saveAsGoldenExemplar = saveAsExemplar
                )
            }
        )
    }

    if (showNewLanguageDialog) {
        NewLanguageDialog(
            onDismiss = { viewModel.closeNewLanguageDialog() },
            onAddLanguage = { name, extension, paradigm, boilerplate, keywords ->
                viewModel.addCustomLanguage(name, extension, paradigm, boilerplate, keywords)
            }
        )
    }

    if (showNewSnippetDialog) {
        NewSnippetDialog(
            languages = languages,
            currentLanguageId = selectedLanguage?.id,
            onDismiss = { viewModel.closeNewSnippetDialog() },
            onAddSnippet = { title, languageId, code, tags, category, explanation ->
                viewModel.addSnippet(title, languageId, code, tags, category, explanation)
            }
        )
    }

    if (showNewKnowledgeDialog) {
        NewKnowledgeDialog(
            onDismiss = { viewModel.closeNewKnowledgeDialog() },
            onAddKnowledge = { title, topic, languageScope, content, tags ->
                viewModel.addKnowledge(title, topic, languageScope, content, tags)
            }
        )
    }

    if (showGitHubApkDialog) {
        GitHubApkPackagingDialog(
            record = currentRecord,
            onDismiss = { viewModel.closeGitHubApkDialog() }
        )
    }

    // Gemini API Key Dialog
    if (showGeminiKeyDialog) {
        val currentKey by viewModel.geminiApiKey.collectAsState()
        var tempKey by remember { mutableStateOf(currentKey) }

        AlertDialog(
            onDismissRequest = { showGeminiKeyDialog = false },
            title = { Text("Gemini AI Integration Key") },
            text = {
                Column {
                    Text(
                        text = "Optionally provide your Gemini API key to activate cloud LLM code synthesis with DBSCAN cluster augmentation and RL policy modulation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempKey,
                        onValueChange = { tempKey = it },
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("AIzaSy...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "When left blank, CodeCraft runs entirely offline using its built-in algorithmic multi-language engine.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setGeminiApiKey(tempKey.trim())
                        showGeminiKeyDialog = false
                    }
                ) {
                    Text("Save Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGeminiKeyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
