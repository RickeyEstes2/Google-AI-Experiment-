package com.gptbot.gptbot.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptbot.gptbot.data.db.ChatMessageEntity
import com.gptbot.gptbot.data.model.ChainOfThought
import com.gptbot.gptbot.data.model.ChatMode
import com.gptbot.gptbot.data.model.SearchResultItem
import com.gptbot.gptbot.ui.components.*
import com.gptbot.gptbot.ui.theme.*
import com.gptbot.gptbot.ui.viewmodel.ChatViewModel
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotMainScreen(
    viewModel: ChatViewModel,
    onNavigateToLegacyCodeGen: () -> Unit = {}
) {
    val messages by viewModel.currentMessages.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val activeMode by viewModel.activeMode.collectAsState()
    val isSearchEnabled by viewModel.isSearchEnabled.collectAsState()
    val attachedContext by viewModel.attachedContext.collectAsState()
    val attachedTitle by viewModel.attachedTitle.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generationStep by viewModel.generationStep.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Modals visibility states
    var showMathModal by remember { mutableStateOf(false) }
    var showLogModal by remember { mutableStateOf(false) }
    var showPdfModal by remember { mutableStateOf(false) }
    var showContradictionModal by remember { mutableStateOf(false) }
    var showSessionsDrawer by remember { mutableStateOf(false) }

    val json = remember { Json { ignoreUnknownKeys = true } }

    // Auto-scroll on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = Zinc950,
        topBar = {
            Surface(
                color = Zinc900,
                border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
            ) {
                Column(modifier = Modifier.statusBarsPadding()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        // Title and Session Drawer toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showSessionsDrawer = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Emerald500.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Psychology,
                                    contentDescription = "Menu",
                                    tint = Emerald400,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "GPT-Bot",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFAFAFA)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Color(0xFFA1A1AA),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "Self-Improving CoT • LaTeX • Search • STEM",
                                    fontSize = 11.sp,
                                    color = Emerald400
                                )
                            }
                        }

                        // Tool modal quick triggers
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showMathModal = true }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Functions, contentDescription = "Math Solver", tint = Emerald400, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { showLogModal = true }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Article, contentDescription = "Log Search", tint = Emerald400, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { showPdfModal = true }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Extractor", tint = Rose400, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { showContradictionModal = true }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Balance, contentDescription = "Contradictions", tint = Amber400, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { viewModel.createNewSession() }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.AddComment, contentDescription = "New Chat", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    // Mode Chips Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        items(ChatMode.values()) { mode ->
                            val isSelected = activeMode == mode
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) Emerald500 else Zinc800,
                                modifier = Modifier
                                    .clickable { viewModel.setMode(mode) }
                                    .border(1.dp, if (isSelected) Emerald400 else Zinc700, RoundedCornerShape(16.dp))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    val icon = when (mode) {
                                        ChatMode.GENERAL_COT -> Icons.Default.Psychology
                                        ChatMode.BRAINSTORM -> Icons.Default.Lightbulb
                                        ChatMode.REVERSE_BRAINSTORM -> Icons.Default.Autorenew
                                        ChatMode.MATH_SOLVER -> Icons.Default.Functions
                                        ChatMode.CONTRADICTION_DETECTOR -> Icons.Default.Balance
                                        ChatMode.LOG_ANALYZER -> Icons.Default.Article
                                        ChatMode.PDF_EXTRACTOR -> Icons.Default.PictureAsPdf
                                        ChatMode.CODE_GEN -> Icons.Default.Code
                                    }
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFF022C22) else Color(0xFFA1A1AA),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = mode.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF022C22) else Color(0xFFE4E4E7)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = Zinc900,
                border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800),
                modifier = Modifier.imePadding().navigationBarsPadding()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    // Context attachment pill (if any)
                    attachedTitle?.let { title ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Zinc800,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .border(1.dp, Emerald500.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = Emerald400, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Attached: $title",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Emerald400,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(onClick = { viewModel.clearAttachedContext() }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFFA1A1AA), modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    // Mode Presets Prompt Bar
                    val presets = getQuickPresetsForMode(activeMode)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        items(presets) { preset ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Zinc800.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .clickable { inputText = preset }
                                    .border(1.dp, Zinc700, RoundedCornerShape(12.dp))
                            ) {
                                Text(
                                    text = preset,
                                    fontSize = 11.sp,
                                    color = Color(0xFFD4D4D8),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Input bar row
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Google Search Toggle
                        IconButton(
                            onClick = { viewModel.toggleSearch() },
                            modifier = Modifier
                                .size(40.dp)
                                .background(if (isSearchEnabled) Emerald500.copy(alpha = 0.2f) else Zinc800, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = "Toggle Search",
                                tint = if (isSearchEnabled) Emerald400 else Color(0xFFA1A1AA),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Text input field
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    text = getPlaceholderForMode(activeMode),
                                    fontSize = 12.sp,
                                    color = Color(0xFFA1A1AA)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald500,
                                unfocusedBorderColor = Zinc700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Send button
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank() && !isGenerating) {
                                    val textToSend = inputText
                                    inputText = ""
                                    viewModel.sendMessage(textToSend)
                                }
                            },
                            enabled = inputText.isNotBlank() && !isGenerating,
                            modifier = Modifier
                                .size(44.dp)
                                .background(if (inputText.isNotBlank() && !isGenerating) Emerald500 else Zinc800, CircleShape)
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    color = Emerald400,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = if (inputText.isNotBlank()) Color(0xFF022C22) else Color(0xFFA1A1AA),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatMessageBubble(
                        message = message,
                        onSelfImprove = { viewModel.refineAndImprove(message) }
                    )
                }

                // Ongoing Generation status indicator
                if (isGenerating) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Zinc900,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Emerald500.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = Emerald400,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = generationStep.ifBlank { "Executing 5-Stage Chain of Thought..." },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Emerald400
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modals
    if (showMathModal) {
        MathSolverModal(
            onDismiss = { showMathModal = false },
            onSolveInChat = { problem ->
                viewModel.setMode(ChatMode.MATH_SOLVER)
                viewModel.sendMessage(problem)
            }
        )
    }

    if (showLogModal) {
        LogSearchModal(
            onDismiss = { showLogModal = false },
            onAnalyzeInChat = { logText ->
                viewModel.setMode(ChatMode.LOG_ANALYZER)
                viewModel.sendMessage(logText)
            }
        )
    }

    if (showPdfModal) {
        PdfExtractorModal(
            onDismiss = { showPdfModal = false },
            onAskInChat = { pdfText ->
                viewModel.setMode(ChatMode.PDF_EXTRACTOR)
                viewModel.sendMessage(pdfText)
            }
        )
    }

    if (showContradictionModal) {
        ContradictionCheckerModal(
            onDismiss = { showContradictionModal = false },
            onDiscussInChat = { contradictionPrompt ->
                viewModel.setMode(ChatMode.CONTRADICTION_DETECTOR)
                viewModel.sendMessage(contradictionPrompt)
            }
        )
    }

    if (showSessionsDrawer) {
        SessionsHistoryDrawer(
            sessions = sessions,
            currentSessionId = currentSessionId,
            onSelectSession = { id ->
                viewModel.selectSession(id)
                showSessionsDrawer = false
            },
            onDeleteSession = { id -> viewModel.deleteSession(id) },
            onNewSession = {
                viewModel.createNewSession()
                showSessionsDrawer = false
            },
            onDismiss = { showSessionsDrawer = false },
            onLegacyCodeGenClick = {
                showSessionsDrawer = false
                onNavigateToLegacyCodeGen()
            }
        )
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessageEntity,
    onSelfImprove: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    val json = remember { Json { ignoreUnknownKeys = true } }

    val cot = remember(message.chainOfThoughtJson) {
        message.chainOfThoughtJson?.let { runCatching { json.decodeFromString<ChainOfThought>(it) }.getOrNull() }
    }

    val searchResults = remember(message.searchResultsJson) {
        message.searchResultsJson?.let { runCatching { json.decodeFromString<List<SearchResultItem>>(it) }.getOrNull() } ?: emptyList()
    }

    val isUser = message.role == "USER"
    val isSystem = message.role == "SYSTEM"

    Column(
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Role & Mode Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = when {
                    isUser -> "You"
                    isSystem -> "System Protocol"
                    else -> "GPT-Bot (CoT)"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUser) Sky400 else Emerald400
            )

            if (!isUser && !isSystem) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Emerald500.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Iteration #${message.iterationCount}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Emerald400,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        // Message Surface
        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            ),
            color = when {
                isUser -> Emerald500.copy(alpha = 0.15f)
                isSystem -> Zinc900
                else -> Zinc900
            },
            modifier = Modifier
                .widthIn(max = 620.dp)
                .border(
                    1.dp,
                    when {
                        isUser -> Emerald500.copy(alpha = 0.4f)
                        isSystem -> Zinc800
                        else -> Zinc700
                    },
                    RoundedCornerShape(14.dp)
                )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Attached snippet marker
                message.attachedSnippetTitle?.let { title ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Zinc800,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "Context: $title",
                            fontSize = 10.sp,
                            color = Emerald400,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // 5-Stage Chain of Thought Card (if present)
                if (cot != null) {
                    ChainOfThoughtCard(
                        cot = cot,
                        currentIteration = message.iterationCount,
                        onSelfImproveClick = onSelfImprove,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                // Search Grounding citations card (if present)
                if (searchResults.isNotEmpty()) {
                    SearchGroundingCard(
                        results = searchResults,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                // Render Message Body with LaTeX Formula Parsing
                RenderRichMessageContent(content = message.content)

                // Footer Actions for Assistant messages
                if (!isUser && !isSystem) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Zinc800, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(message.content))
                                    copied = true
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy Response",
                                    tint = if (copied) Emerald400 else Color(0xFFA1A1AA),
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            if (copied) {
                                Text("Copied!", fontSize = 10.sp, color = Emerald400)
                            }
                        }

                        // One-tap Refine / Self-Improve
                        OutlinedButton(
                            onClick = onSelfImprove,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Emerald400),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Emerald500.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Self-Improve (Pass #${message.iterationCount + 1})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Parses markdown, code blocks, and $$ LaTeX $$ display formulas.
 */
@Composable
fun RenderRichMessageContent(content: String) {
    val parts = remember(content) {
        splitContentIntoBlocks(content)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        parts.forEach { block ->
            when (block) {
                is ContentBlock.LatexDisplay -> {
                    DisplayMathBlock(rawFormula = block.formula)
                }
                is ContentBlock.CodeSnippet -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Zinc950,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Zinc800, RoundedCornerShape(8.dp))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = block.language.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald400,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            SelectionContainer {
                                Text(
                                    text = block.code,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFE4E4E7),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
                is ContentBlock.RegularText -> {
                    SelectionContainer {
                        Text(
                            text = block.text,
                            fontSize = 13.sp,
                            color = Color(0xFFFAFAFA),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

sealed class ContentBlock {
    data class LatexDisplay(val formula: String) : ContentBlock()
    data class CodeSnippet(val language: String, val code: String) : ContentBlock()
    data class RegularText(val text: String) : ContentBlock()
}

fun splitContentIntoBlocks(raw: String): List<ContentBlock> {
    val blocks = mutableListOf<ContentBlock>()
    val lines = raw.lines()
    var currentText = StringBuilder()
    var inCodeBlock = false
    var codeLanguage = ""
    var currentCode = StringBuilder()

    var i = 0
    while (i < lines.size) {
        val line = lines[i]

        if (line.trim().startsWith("```")) {
            if (inCodeBlock) {
                blocks.add(ContentBlock.CodeSnippet(codeLanguage, currentCode.toString().trimEnd()))
                currentCode.clear()
                inCodeBlock = false
            } else {
                if (currentText.isNotBlank()) {
                    blocks.add(ContentBlock.RegularText(currentText.toString().trim()))
                    currentText.clear()
                }
                codeLanguage = line.trim().removePrefix("```").trim().ifBlank { "kotlin" }
                inCodeBlock = true
            }
            i++
            continue
        }

        if (inCodeBlock) {
            currentCode.appendLine(line)
            i++
            continue
        }

        // Check for $$ display math block
        if (line.trim().startsWith("$$") && line.trim().endsWith("$$") && line.trim().length > 4) {
            if (currentText.isNotBlank()) {
                blocks.add(ContentBlock.RegularText(currentText.toString().trim()))
                currentText.clear()
            }
            val formula = line.trim().removePrefix("$$").removeSuffix("$$").trim()
            blocks.add(ContentBlock.LatexDisplay(formula))
            i++
            continue
        }

        currentText.appendLine(line)
        i++
    }

    if (inCodeBlock && currentCode.isNotBlank()) {
        blocks.add(ContentBlock.CodeSnippet(codeLanguage, currentCode.toString()))
    }
    if (currentText.isNotBlank()) {
        blocks.add(ContentBlock.RegularText(currentText.toString().trim()))
    }

    return blocks
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsHistoryDrawer(
    sessions: List<com.gptbot.gptbot.data.db.ChatSessionEntity>,
    currentSessionId: String,
    onSelectSession: (String) -> Unit,
    onDeleteSession: (String) -> Unit,
    onNewSession: () -> Unit,
    onDismiss: () -> Unit,
    onLegacyCodeGenClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Zinc900,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Zinc700) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Conversation Sessions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFAFAFA)
                )

                Button(
                    onClick = onNewSession,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = Color(0xFF022C22)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Switch to Legacy Code Studio
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Zinc800,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLegacyCodeGenClick() }
                    .border(1.dp, Emerald500.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = Emerald400, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Code Studio & DBSCAN / RL Benchmarks",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFAFAFA)
                        )
                        Text(
                            text = "Inspect raw code generator modules, snippets & policies",
                            fontSize = 11.sp,
                            color = Color(0xFFA1A1AA)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
            ) {
                items(sessions, key = { it.id }) { s ->
                    val isCurrent = s.id == currentSessionId
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCurrent) Emerald500.copy(alpha = 0.2f) else Zinc800,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectSession(s.id) }
                            .border(1.dp, if (isCurrent) Emerald400 else Zinc700, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = s.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCurrent) Emerald400 else Color(0xFFFAFAFA),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Mode: ${s.initialMode}",
                                    fontSize = 10.sp,
                                    color = Color(0xFFA1A1AA)
                                )
                            }

                            IconButton(
                                onClick = { onDeleteSession(s.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Rose400, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun getQuickPresetsForMode(mode: ChatMode): List<String> {
    return when (mode) {
        ChatMode.MATH_SOLVER -> listOf(
            "\\int x \\cdot e^{2x} \\, dx",
            "Derivative of f(x) = x^3 \\cdot \\sin(x)",
            "Solve quadratic 2x^2 - 7x + 3 = 0",
            "Prove identity \\sin(2\\theta)/(1+\\cos(2\\theta))"
        )
        ChatMode.BRAINSTORM -> listOf(
            "Decentralized edge AI inference orchestration",
            "Zero-alloc Kotlin high-throughput event bus",
            "Antifragile distributed consensus protocols"
        )
        ChatMode.REVERSE_BRAINSTORM -> listOf(
            "How to guarantee total Android app deadlock",
            "How to completely sabotage a distributed database",
            "Worst practices in cryptography and key management"
        )
        ChatMode.LOG_ANALYZER -> listOf(
            "Find root cause of NullPointerException in Coroutine worker",
            "Isolate deadlock in chat_sessions relation 16401",
            "Analyze Redis timeout and GPU throttling"
        )
        ChatMode.CONTRADICTION_DETECTOR -> listOf(
            "Sub-millisecond latency over global WAN",
            "Pure stateless function mutating global state",
            "Zero loss delivery over unreliable UDP"
        )
        ChatMode.PDF_EXTRACTOR -> listOf(
            "Extract Fundamental Theorem of Calculus from PDF",
            "Summarize Byzantine fault tolerance quorum rules",
            "List formulas from Chapter 2"
        )
        ChatMode.CODE_GEN -> listOf(
            "Generate thread-safe LRU Cache in Kotlin",
            "Fast Fourier Transform with O(N log N) complexity",
            "Trie autocomplete engine with memory compaction"
        )
        ChatMode.GENERAL_COT -> listOf(
            "Explain quantum superposition with mathematical axioms",
            "Deconstruct CAP theorem with network partition proofs",
            "Compare Dijkstra vs A* heuristic convergence"
        )
    }
}

private fun getPlaceholderForMode(mode: ChatMode): String {
    return when (mode) {
        ChatMode.MATH_SOLVER -> "Enter equation to solve (e.g. \\int x e^2x dx or 2x^2 - 7x + 3 = 0)..."
        ChatMode.BRAINSTORM -> "Enter topic or goal to brainstorm..."
        ChatMode.REVERSE_BRAINSTORM -> "Enter goal to reverse-brainstorm & invert..."
        ChatMode.LOG_ANALYZER -> "Paste log stack trace or ask about error logs..."
        ChatMode.PDF_EXTRACTOR -> "Ask a question about extracted PDF document..."
        ChatMode.CONTRADICTION_DETECTOR -> "Enter propositions to test for contradictions..."
        ChatMode.CODE_GEN -> "Describe algorithm or feature to generate code for..."
        ChatMode.GENERAL_COT -> "Ask any question for deep 5-stage Chain of Thought reasoning..."
    }
}
