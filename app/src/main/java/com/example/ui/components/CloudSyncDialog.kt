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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.sync.CloudSnapshot
import com.example.data.sync.CloudSyncManager
import com.example.data.sync.SyncStatus
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
    val logs by syncManager.syncLogs.collectAsState()
    val snapshots by syncManager.snapshots.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var snapshotNoteInput by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }

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

    val timeFormatter = remember { SimpleDateFormat("MMM d, yyyy 'at' hh:mm:ss a", Locale.getDefault()) }
    val formattedLastSync = remember(lastSync) {
        if (lastSync == 0L) "Never" else timeFormatter.format(Date(lastSync))
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(12.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "Cloud Auto Sync",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Status Banner
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when (syncStatus) {
                        SyncStatus.SYNCED -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        SyncStatus.SYNCING -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        SyncStatus.PENDING -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                        SyncStatus.ERROR -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                        SyncStatus.OFFLINE -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                            SyncStatus.SYNCED -> "Cloud Fully Synced"
                                            SyncStatus.SYNCING -> "Syncing with Cloud..."
                                            SyncStatus.PENDING -> "$pendingCount Pending Change${if (pendingCount > 1) "s" else ""}"
                                            SyncStatus.ERROR -> "Sync Connection Error"
                                            SyncStatus.OFFLINE -> "Auto-Sync Paused"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
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
                                Text("Sync Now", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Tabs: Settings, Snapshots, Logs
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Settings", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Snapshots (${snapshots.size})", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Sync Logs", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                // TAB 0: Settings & Controls
                if (selectedTab == 0) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Auto Sync Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Automatic Cloud Sync", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        "Continuously syncs bookmarks, notes, charts & formulas",
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
                                Text("Sync Interval Frequency", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
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
                            Text("Cloud Backup & Export", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val backupJson = onExportBackup()
                                        clipboardManager.setText(AnnotatedString(backupJson))
                                        Toast.makeText(context, "Cloud backup JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
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

                // TAB 1: Cloud Snapshots
                if (selectedTab == 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Create Snapshot form
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = snapshotNoteInput,
                                onValueChange = { snapshotNoteInput = it },
                                placeholder = { Text("Snapshot label (e.g., Before research)", fontSize = 12.sp) },
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
                                    text = "No saved snapshots yet. Save a snapshot to create a restore point.",
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

                // TAB 2: Sync Logs
                if (selectedTab == 2) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val logTimeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
                        if (logs.isEmpty()) {
                            Text("No sync activity recorded.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            logs.take(25).forEach { entry ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = logTimeFormatter.format(Date(entry.timestamp)),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = entry.message,
                                        fontSize = 11.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = when (entry.status) {
                                            SyncStatus.ERROR -> MaterialTheme.colorScheme.error
                                            SyncStatus.SYNCING -> MaterialTheme.colorScheme.secondary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Import Dialog
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
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Import Cloud Backup",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        label = { Text("Paste JSON Backup") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showImportDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onImportBackup(importJsonText)
                                showImportDialog = false
                                Toast.makeText(context, "Backup imported!", Toast.LENGTH_SHORT).show()
                            },
                            enabled = importJsonText.isNotBlank()
                        ) {
                            Text("Import")
                        }
                    }
                }
            }
        }
    }
}
