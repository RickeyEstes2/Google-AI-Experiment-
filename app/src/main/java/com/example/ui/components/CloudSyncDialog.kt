package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.sync.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CloudSyncDialog(
    syncManager: CloudSyncManager,
    currentArticleCount: Int,
    onDismiss: () -> Unit,
    onCreateSnapshot: (note: String) -> Unit,
    onRestoreSnapshot: (snapshot: CloudSnapshot) -> Unit,
    onExportBackup: () -> String,
    onImportBackup: (json: String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val syncStatus by syncManager.syncStatus.collectAsState()
    val lastSync by syncManager.lastSyncTimestamp.collectAsState()
    val pendingCount by syncManager.pendingChanges.collectAsState()
    val autoSyncEnabled by syncManager.autoSyncEnabled.collectAsState()
    val syncInterval by syncManager.syncIntervalSeconds.collectAsState()
    val configuredServers by syncManager.configuredServers.collectAsState()
    val logs by syncManager.syncLogs.collectAsState()
    val snapshots by syncManager.snapshots.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var snapshotNoteInput by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }

    // State for editing a server's folder & configuration
    var editingServerConfig by remember { mutableStateOf<CloudServerConfig?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val timeFormatter = remember { SimpleDateFormat("MMM d, yyyy 'at' h:mm:ss a", Locale.getDefault()) }
    val formattedLastSync = remember(lastSync) {
        if (lastSync == 0L) "Never" else timeFormatter.format(Date(lastSync))
    }

    val activeServerCount = remember(configuredServers) {
        configuredServers.count { it.isEnabled }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Cloud Auto Sync & Storage",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "$activeServerCount active cloud destination${if (activeServerCount != 1) "s" else ""}",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Status Banner
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when (syncStatus) {
                        SyncStatus.SYNCED -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        SyncStatus.SYNCING -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        SyncStatus.PENDING -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        SyncStatus.ERROR -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        SyncStatus.OFFLINE -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = when (syncStatus) {
                                    SyncStatus.SYNCED -> Icons.Default.CloudDone
                                    SyncStatus.SYNCING -> Icons.Default.Sync
                                    SyncStatus.PENDING -> Icons.Default.CloudQueue
                                    SyncStatus.ERROR -> Icons.Default.CloudOff
                                    SyncStatus.OFFLINE -> Icons.Default.CloudOff
                                },
                                contentDescription = null,
                                tint = when (syncStatus) {
                                    SyncStatus.SYNCED -> MaterialTheme.colorScheme.primary
                                    SyncStatus.SYNCING -> MaterialTheme.colorScheme.secondary
                                    SyncStatus.PENDING -> MaterialTheme.colorScheme.tertiary
                                    SyncStatus.ERROR -> MaterialTheme.colorScheme.error
                                    SyncStatus.OFFLINE -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .then(if (syncStatus == SyncStatus.SYNCING) Modifier.rotate(rotationAngle) else Modifier)
                            )
                            Column {
                                Text(
                                    text = when (syncStatus) {
                                        SyncStatus.SYNCED -> "Synchronized with $activeServerCount Server${if (activeServerCount != 1) "s" else ""}"
                                        SyncStatus.SYNCING -> "Auto-Syncing to Cloud Servers..."
                                        SyncStatus.PENDING -> "$pendingCount change${if (pendingCount > 1) "s" else ""} queued for sync"
                                        SyncStatus.ERROR -> "Sync Connection Error"
                                        SyncStatus.OFFLINE -> "Auto-Sync is Paused"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                                Text(
                                    text = "Last synced: $formattedLastSync",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { syncManager.triggerSyncNow() },
                            shape = RoundedCornerShape(10.dp),
                            enabled = syncStatus != SyncStatus.SYNCING,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("sync_now_button")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync All", fontSize = 12.sp)
                        }
                    }
                }

                // Tabs: Cloud Servers, Settings, Snapshots, Logs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Cloud Servers ($activeServerCount)", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Sync Settings", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Snapshots (${snapshots.size})", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Event Logs", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                // ==========================================
                // TAB 0: Cloud Servers & Target Folders
                // ==========================================
                if (selectedTab == 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select 1 or more cloud servers to auto-sync with:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        configuredServers.forEach { server ->
                            val provider = CloudProvider.values().find { it.id == server.providerId }
                            val isEnabled = server.isEnabled

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isEnabled) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Row 1: Icon, Name, Protocol, and Enable Switch
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = getProviderIcon(server.providerId),
                                                        contentDescription = null,
                                                        tint = if (isEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            Column {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = server.displayName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.5.sp
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                                                    ) {
                                                        Text(
                                                            text = provider?.protocolType ?: "Cloud API",
                                                            fontSize = 9.5.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = provider?.description ?: "Remote cloud storage",
                                                    fontSize = 10.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Switch(
                                            checked = isEnabled,
                                            onCheckedChange = { checked ->
                                                syncManager.toggleServerEnabled(server.providerId, checked)
                                            },
                                            modifier = Modifier.testTag("server_toggle_${server.providerId}")
                                        )
                                    }

                                    // Row 2: Target Sync Folder Display & Edit Action
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.FolderOpen,
                                                    contentDescription = "Target Folder",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Column {
                                                    Text(
                                                        text = "Auto-Sync Folder:",
                                                        fontSize = 9.5.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = server.targetFolder,
                                                        fontSize = 11.5.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }

                                            FilledTonalButton(
                                                onClick = { editingServerConfig = server },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("Change Folder", fontSize = 10.5.sp)
                                            }
                                        }
                                    }

                                    // Row 3: Action Buttons & Status Badge
                                    if (isEnabled) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = server.statusText,
                                                    fontSize = 10.5.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                OutlinedButton(
                                                    onClick = { syncManager.testServerConnection(server.providerId) },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(11.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Test Connection", fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // TAB 1: Sync Settings & Controls
                // ==========================================
                if (selectedTab == 1) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Auto Sync Master Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Continuous Auto-Sync Engine", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        "Automatically syncs bookmarks, notes, formulas, charts & Venn diagrams across all selected cloud servers",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = autoSyncEnabled,
                                    onCheckedChange = { syncManager.setAutoSyncEnabled(it) },
                                    modifier = Modifier.testTag("auto_sync_switch")
                                )
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Sync Interval selector
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Auto-Sync Frequency Interval", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(15 to "15s", 30 to "30s", 60 to "1m", 300 to "5m").forEach { (sec, label) ->
                                        FilterChip(
                                            selected = syncInterval == sec,
                                            onClick = { syncManager.setSyncInterval(sec) },
                                            label = { Text(label, fontSize = 11.sp) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Backup & Restore actions
                            Text("Full JSON Data Portability", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val backupJson = onExportBackup()
                                        clipboardManager.setText(AnnotatedString(backupJson))
                                        Toast.makeText(context, "Full dataset backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Export JSON", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = { showImportDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Import JSON", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // TAB 2: Cloud Snapshots & Restore Points
                // ==========================================
                if (selectedTab == 2) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = snapshotNoteInput,
                                onValueChange = { snapshotNoteInput = it },
                                placeholder = { Text("Snapshot label (e.g., Before research notes)", fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    onCreateSnapshot(snapshotNoteInput)
                                    snapshotNoteInput = ""
                                    Toast.makeText(context, "Cloud snapshot saved!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save", fontSize = 12.sp)
                            }
                        }

                        if (snapshots.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No saved snapshots yet. Save a snapshot to create a restore point across your cloud servers.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            snapshots.forEach { snapshot ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = snapshot.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "${snapshot.itemCount} links • ${timeFormatter.format(Date(snapshot.timestamp))}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        FilledTonalButton(
                                            onClick = {
                                                onRestoreSnapshot(snapshot)
                                                Toast.makeText(context, "Restored snapshot: ${snapshot.name}", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Restore", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // TAB 3: Multi-Server Event Logs
                // ==========================================
                if (selectedTab == 3) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Live Multi-Server Sync Logs",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${logs.size} events",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            val logTimeFormatter = remember { SimpleDateFormat("h:mm:ss a", Locale.getDefault()) }

                            if (logs.isEmpty()) {
                                Text(
                                    text = "No sync activity recorded yet.",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            } else {
                                logs.take(20).forEach { log ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = logTimeFormatter.format(Date(log.timestamp)),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = log.message,
                                            fontSize = 11.sp,
                                            color = when (log.status) {
                                                SyncStatus.SYNCED -> MaterialTheme.colorScheme.primary
                                                SyncStatus.SYNCING -> MaterialTheme.colorScheme.secondary
                                                SyncStatus.PENDING -> MaterialTheme.colorScheme.tertiary
                                                SyncStatus.ERROR -> MaterialTheme.colorScheme.error
                                                SyncStatus.OFFLINE -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ========================================================
    // Edit Target Folder & Server Configuration Dialog
    // ========================================================
    editingServerConfig?.let { targetServer ->
        var folderInput by remember { mutableStateOf(targetServer.targetFolder) }
        var serverUrlInput by remember { mutableStateOf(targetServer.serverUrl) }
        var accountInput by remember { mutableStateOf(targetServer.authAccount) }
        var selectedDirection by remember {
            mutableStateOf(
                try {
                    SyncDirection.valueOf(targetServer.syncDirection)
                } catch (_: Exception) {
                    SyncDirection.TWO_WAY
                }
            )
        }

        Dialog(onDismissRequest = { editingServerConfig = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = getProviderIcon(targetServer.providerId),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Configure ${targetServer.displayName}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        IconButton(onClick = { editingServerConfig = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    // Target Folder Input
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Auto-Sync Target Folder Path",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        OutlinedTextField(
                            value = folderInput,
                            onValueChange = { folderInput = it },
                            placeholder = { Text("e.g. /Documents/Mastermind_Notes/") },
                            leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Quick Folder Presets
                        Text(
                            text = "Quick Folder Presets:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "/Mastermind/AutoSync/",
                                "/Documents/ResearchNotes/",
                                "/CloudVault/2026/",
                                "/Notes/Mastermind/",
                                "/Backups/ArticleBookmarks/"
                            ).forEach { preset ->
                                SuggestionChip(
                                    onClick = { folderInput = preset },
                                    label = { Text(preset, fontSize = 10.5.sp) }
                                )
                            }
                        }
                    }

                    // Optional server URL for custom or Nextcloud
                    if (targetServer.providerId == CloudProvider.NEXTCLOUD_WEBDAV.id ||
                        targetServer.providerId == CloudProvider.CUSTOM_WEBDAV_SERVER.id) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("WebDAV / Server URL Endpoint", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                            OutlinedTextField(
                                value = serverUrlInput,
                                onValueChange = { serverUrlInput = it },
                                placeholder = { Text("https://cloud.myserver.com/remote.php/webdav/") },
                                leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Account / Username
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Account Handle / Bucket Name (Optional)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                        OutlinedTextField(
                            value = accountInput,
                            onValueChange = { accountInput = it },
                            placeholder = { Text("e.g. user@gmail.com or my-notes-bucket") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Sync Direction Behavior
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Sync Direction Behavior", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                        SyncDirection.values().forEach { dir ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedDirection = dir }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RadioButton(
                                    selected = selectedDirection == dir,
                                    onClick = { selectedDirection = dir }
                                )
                                Text(text = dir.label, fontSize = 12.5.sp)
                            }
                        }
                    }

                    // Save and Test Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                syncManager.testServerConnection(targetServer.providerId)
                                Toast.makeText(context, "Testing connection...", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                syncManager.updateServerDetails(
                                    providerId = targetServer.providerId,
                                    targetFolder = folderInput,
                                    serverUrl = serverUrlInput,
                                    authAccount = accountInput,
                                    direction = selectedDirection
                                )
                                editingServerConfig = null
                                Toast.makeText(context, "Target folder updated for ${targetServer.displayName}!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Changes", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // JSON Import Dialog
    if (showImportDialog) {
        Dialog(onDismissRequest = { showImportDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Import JSON Backup",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Paste full dataset JSON exported from another cloud instance or device:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("[{\"id\":\"...\",\"title\":\"...\"}]") },
                        minLines = 4,
                        maxLines = 8,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showImportDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (importJsonText.isNotBlank()) {
                                    onImportBackup(importJsonText)
                                    showImportDialog = false
                                    Toast.makeText(context, "Importing dataset...", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("Import & Merge")
                        }
                    }
                }
            }
        }
    }
}

private fun getProviderIcon(providerId: String): ImageVector {
    return when (providerId) {
        CloudProvider.GOOGLE_DRIVE.id -> Icons.Default.CloudUpload
        CloudProvider.DROPBOX.id -> Icons.Default.Inventory2
        CloudProvider.MICROSOFT_ONEDRIVE.id -> Icons.Default.Cloud
        CloudProvider.NEXTCLOUD_WEBDAV.id -> Icons.Default.Hub
        CloudProvider.AMAZON_S3.id -> Icons.Default.Storage
        CloudProvider.BOX.id -> Icons.Default.FolderShared
        CloudProvider.APPLE_ICLOUD.id -> Icons.Default.CloudQueue
        CloudProvider.CUSTOM_WEBDAV_SERVER.id -> Icons.Default.Dns
        else -> Icons.Default.CloudSync
    }
}
