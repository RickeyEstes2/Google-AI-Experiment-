package com.example.solveflow.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solveflow.data.model.FlowchartData
import com.example.solveflow.data.model.NodeType
import com.example.solveflow.ui.components.DiagnosticRunnerDialog
import com.example.solveflow.ui.components.EditFlowchartDialog
import com.example.solveflow.ui.components.GitHubActionsDialog
import com.example.solveflow.ui.theme.Amber500
import com.example.solveflow.ui.theme.Emerald400
import com.example.solveflow.ui.theme.Emerald500
import com.example.solveflow.ui.theme.Violet400
import com.example.solveflow.ui.viewmodel.FlowchartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: FlowchartViewModel) {
    val context = LocalContext.current
    val flowcharts by viewModel.filteredFlowcharts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var activeRunnerFlowchart by remember { mutableStateOf<FlowchartData?>(null) }
    var activeEditingFlowchart by remember { mutableStateOf<FlowchartData?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showGitHubActionsDialog by remember { mutableStateOf(false) }
    var inspectedFlowchart by remember { mutableStateOf<FlowchartData?>(null) }
    var showTopMenu by remember { mutableStateOf(false) }

    val categories = listOf(
        "All",
        "IT & Infrastructure",
        "Software & SRE",
        "Root Cause Analysis",
        "Hardware & Electronics",
        "Operations & Support"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Flowchart",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Emerald400.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "CI & PDF/HTML READY",
                                    color = Emerald400,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Problem Solving Flowcharts & Diagnostics",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showGitHubActionsDialog = true },
                        modifier = Modifier.testTag("ci_hub_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "GitHub Actions APK Packaging",
                            tint = Emerald500
                        )
                    }

                    Box {
                        IconButton(onClick = { showTopMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = showTopMenu,
                            onDismissRequest = { showTopMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Reset Preloaded Templates") },
                                onClick = {
                                    viewModel.resetTemplates()
                                    showTopMenu = false
                                    Toast.makeText(context, "Templates reset to defaults", Toast.LENGTH_SHORT).show()
                                },
                                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("GitHub CI/CD Hub") },
                                onClick = {
                                    showGitHubActionsDialog = true
                                    showTopMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Emerald500,
                contentColor = Color(0xFF022C22),
                modifier = Modifier.testTag("create_flowchart_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Flowchart")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearch(it) },
                placeholder = { Text("Search problems, symptoms, or actions...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Category Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Emerald400 else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable { viewModel.setCategory(category) }
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Emerald400 else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        Text(
                            text = category,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF022C22) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Flowchart list
            if (flowcharts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No flowcharts found matching filter",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = {
                            viewModel.setSearch("")
                            viewModel.setCategory("All")
                        }) {
                            Text("Clear Filters")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(flowcharts, key = { it.id }) { flowchart ->
                        FlowchartCard(
                            flowchart = flowchart,
                            onSolveClick = { activeRunnerFlowchart = flowchart },
                            onInspectClick = { inspectedFlowchart = flowchart },
                            onEditClick = { activeEditingFlowchart = flowchart },
                            onDeleteClick = { viewModel.deleteFlowchart(flowchart.id) },
                            onExportHtml = { viewModel.exportAndShareHtml(context, flowchart) },
                            onExportPdf = { viewModel.exportAndSharePdf(context, flowchart) }
                        )
                    }
                }
            }
        }
    }

    // Active Diagnostic Runner Dialog
    if (activeRunnerFlowchart != null) {
        DiagnosticRunnerDialog(
            flowchart = activeRunnerFlowchart!!,
            onDismiss = { activeRunnerFlowchart = null },
            onSaveSession = { session ->
                viewModel.saveDiagnosticRun(session)
                Toast.makeText(context, "Session saved to audit history", Toast.LENGTH_SHORT).show()
            },
            onExportHtml = { session ->
                viewModel.exportAndShareHtml(context, activeRunnerFlowchart!!, session)
            },
            onExportPdf = { session ->
                viewModel.exportAndSharePdf(context, activeRunnerFlowchart!!, session)
            }
        )
    }

    // Inspect flowchart nodes dialog
    if (inspectedFlowchart != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { inspectedFlowchart = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxSize(0.92f)
                    .clip(RoundedCornerShape(24.dp)),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = inspectedFlowchart!!.category.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald400
                            )
                            Text(
                                text = inspectedFlowchart!!.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { inspectedFlowchart = null }) {
                            Icon(Icons.Default.Delete, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    com.example.solveflow.ui.components.FlowchartListView(
                        flowchart = inspectedFlowchart!!,
                        activeNodeId = null,
                        onNodeClick = {},
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            activeRunnerFlowchart = inspectedFlowchart
                            inspectedFlowchart = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald500,
                            contentColor = Color(0xFF022C22)
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF022C22))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Interactive Solver", color = Color(0xFF022C22), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Edit Flowchart Dialog
    if (activeEditingFlowchart != null) {
        EditFlowchartDialog(
            initialFlowchart = activeEditingFlowchart,
            onDismiss = { activeEditingFlowchart = null },
            onSave = { updated ->
                viewModel.saveFlowchart(updated)
                activeEditingFlowchart = null
                Toast.makeText(context, "Flowchart updated", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Create New Flowchart Dialog
    if (showCreateDialog) {
        EditFlowchartDialog(
            initialFlowchart = null,
            onDismiss = { showCreateDialog = false },
            onSave = { newFlowchart ->
                viewModel.saveFlowchart(newFlowchart)
                showCreateDialog = false
                Toast.makeText(context, "New flowchart created", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // GitHub Actions Hub Dialog
    if (showGitHubActionsDialog) {
        GitHubActionsDialog(
            onDismiss = { showGitHubActionsDialog = false }
        )
    }
}

@Composable
fun FlowchartCard(
    flowchart: FlowchartData,
    onSolveClick: () -> Unit,
    onInspectClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onExportHtml: () -> Unit,
    onExportPdf: () -> Unit
) {
    val decisions = flowchart.nodes.count { it.type == NodeType.DECISION }
    val actions = flowchart.nodes.count { it.type == NodeType.ACTION }
    val solutions = flowchart.nodes.count { it.type == NodeType.OUTCOME_SUCCESS }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Category badge & template pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Emerald400.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = flowchart.category,
                        color = Emerald400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (flowchart.isTemplate) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Emerald500.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "TEMPLATE",
                            color = Emerald500,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (!flowchart.isTemplate) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = flowchart.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = flowchart.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stats chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatPill("Nodes", "${flowchart.nodes.size}", Color(0xFFA1A1AA))
                StatPill("Decisions", "$decisions", Violet400)
                StatPill("Actions", "$actions", Amber500)
                StatPill("Resolved", "$solutions", Emerald500)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSolveClick,
                    modifier = Modifier.weight(1.3f).testTag("solve_button_${flowchart.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald500,
                        contentColor = Color(0xFF022C22)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFF022C22),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Solve / Run",
                        color = Color(0xFF022C22),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = onInspectClick,
                    modifier = Modifier.weight(1f).testTag("steps_button_${flowchart.id}"),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Steps", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onExportHtml,
                    modifier = Modifier.weight(1f).testTag("export_html_button_${flowchart.id}"),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(14.dp), tint = Emerald400)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("HTML", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onExportPdf,
                    modifier = Modifier.weight(1f).testTag("export_pdf_button_${flowchart.id}"),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp), tint = Emerald500)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("PDF", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun StatPill(label: String, count: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = count,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
