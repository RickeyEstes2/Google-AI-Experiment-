package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sync.CloudProvider
import com.example.data.sync.CloudSyncManager
import com.example.data.sync.SyncStatus
import java.text.SimpleDateFormat
import java.util.*

/**
 * Modern, polished Visual Sync Status Indicator for Google Drive integration.
 * Compact, responsive, and adheres to Material Design 3 guidelines.
 */
@Composable
fun GoogleDriveSyncStatusCard(
    syncManager: CloudSyncManager,
    onOpenFolderSettings: () -> Unit,
    onOpenCloudSync: () -> Unit,
    modifier: Modifier = Modifier,
    totalLocalArticles: Int = 0
) {
    val syncStatus by syncManager.syncStatus.collectAsState()
    val lastSyncTime by syncManager.lastSyncTimestamp.collectAsState()
    val pendingChanges by syncManager.pendingChanges.collectAsState()
    val driveSettings by syncManager.driveSyncSettings.collectAsState()
    val driveFolders by syncManager.driveFolders.collectAsState()
    val configuredServers by syncManager.configuredServers.collectAsState()
    val autoSyncEnabled by syncManager.autoSyncEnabled.collectAsState()
    val syncInterval by syncManager.syncIntervalSeconds.collectAsState()

    var isExpanded by remember { mutableStateOf(false) }

    val gDriveServer = remember(configuredServers) {
        configuredServers.find { it.providerId == CloudProvider.GOOGLE_DRIVE.id }
    }
    val isDriveEnabled = gDriveServer?.isEnabled ?: true

    val selectedFolder = remember(driveFolders, driveSettings) {
        driveFolders.find { it.id == driveSettings.selectedFolderId }
            ?: driveFolders.find { it.isSelected }
    }
    val folderName = selectedFolder?.name ?: "Mastermind_Database"
    val folderPath = selectedFolder?.path ?: driveSettings.selectedFolderPath

    // Animation for syncing state
    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    val relativeSyncText = remember(lastSyncTime, syncStatus) {
        formatRelativeTime(lastSyncTime)
    }

    // Modern M3 color tokens
    val statusColor = when {
        !isDriveEnabled -> Color(0xFF64748B)
        syncStatus == SyncStatus.SYNCING -> Color(0xFF0284C7)
        syncStatus == SyncStatus.ERROR -> Color(0xFFDC2626)
        pendingChanges > 0 || syncStatus == SyncStatus.PENDING -> Color(0xFFD97706)
        else -> Color(0xFF059669)
    }

    val statusBadgeBg = when {
        !isDriveEnabled -> MaterialTheme.colorScheme.surfaceVariant
        syncStatus == SyncStatus.SYNCING -> Color(0xFFE0F2FE)
        syncStatus == SyncStatus.ERROR -> Color(0xFFFEE2E2)
        pendingChanges > 0 || syncStatus == SyncStatus.PENDING -> Color(0xFFFEF3C7)
        else -> Color(0xFFD1FAE5)
    }

    val statusIcon: ImageVector = when {
        !isDriveEnabled -> Icons.Default.CloudOff
        syncStatus == SyncStatus.SYNCING -> Icons.Default.Sync
        syncStatus == SyncStatus.ERROR -> Icons.Default.Warning
        pendingChanges > 0 || syncStatus == SyncStatus.PENDING -> Icons.Default.CloudUpload
        else -> Icons.Default.CloudDone
    }

    val statusLabel = when {
        !isDriveEnabled -> "Sync Paused"
        syncStatus == SyncStatus.SYNCING -> "Syncing..."
        syncStatus == SyncStatus.ERROR -> "Sync Error"
        pendingChanges > 0 || syncStatus == SyncStatus.PENDING -> "$pendingChanges Pending"
        else -> "In Sync"
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("gdrive_sync_status_bar")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main Compact Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Google Drive Icon & Folder Info
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = statusBadgeBg,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = statusLabel,
                                tint = statusColor,
                                modifier = Modifier
                                    .size(18.dp)
                                    .then(
                                        if (syncStatus == SyncStatus.SYNCING) Modifier.rotate(spinAngle)
                                        else Modifier
                                    )
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Google Drive",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            // Status Chip
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = statusBadgeBg,
                                modifier = Modifier.padding(start = 2.dp)
                            ) {
                                Text(
                                    text = statusLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.5.sp,
                                        color = statusColor
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Target folder breadcrumb
                        Text(
                            text = "📁 $folderName • $relativeSyncText",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right: Quick Sync Button & Expand Chevron
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { syncManager.triggerSyncNow() },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("gdrive_quick_sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync Now",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(18.dp)
                                .then(
                                    if (syncStatus == SyncStatus.SYNCING) Modifier.rotate(spinAngle)
                                    else Modifier
                                )
                        )
                    }

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("gdrive_sync_expand_toggle")
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Expanded Details Panel
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Destination Path & File Card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "DESTINATION FOLDER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )

                                TextButton(
                                    onClick = onOpenFolderSettings,
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("Change Folder", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Text(
                                text = folderPath,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "File: ${driveSettings.syncFileName}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = "$totalLocalArticles articles in DB",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Auto-sync Switch & Cadence Row
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Auto-Sync with Google Drive",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.5.sp
                                        )
                                    )
                                    Text(
                                        text = "Syncs immediately on every edit & at set intervals",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }

                                Switch(
                                    checked = autoSyncEnabled,
                                    onCheckedChange = { syncManager.setAutoSyncEnabled(it) },
                                    modifier = Modifier.testTag("gdrive_autosync_switch")
                                )
                            }

                            if (autoSyncEnabled) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Interval:",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )

                                    listOf(5, 15, 30, 60).forEach { seconds ->
                                        val isSelected = syncInterval == seconds
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            border = BorderStroke(
                                                0.5.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                            ),
                                            modifier = Modifier.clickable { syncManager.setSyncInterval(seconds) }
                                        ) {
                                            Text(
                                                text = "${seconds}s",
                                                fontSize = 10.5.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenFolderSettings,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Drive Folders", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { syncManager.triggerSyncNow() },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(15.dp)
                                    .then(
                                        if (syncStatus == SyncStatus.SYNCING) Modifier.rotate(spinAngle)
                                        else Modifier
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (syncStatus == SyncStatus.SYNCING) "Syncing..." else "Sync Now", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact Top App Bar Status Pill for Google Drive Sync
 */
@Composable
fun GoogleDriveSyncHeaderChip(
    syncManager: CloudSyncManager,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val syncStatus by syncManager.syncStatus.collectAsState()
    val pendingChanges by syncManager.pendingChanges.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "header_sync_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    val (chipBg, chipContentColor, label) = when {
        syncStatus == SyncStatus.SYNCING -> Triple(Color(0xFFE0F2FE), Color(0xFF0284C7), "Syncing")
        syncStatus == SyncStatus.ERROR -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), "Error")
        pendingChanges > 0 || syncStatus == SyncStatus.PENDING -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "Pending")
        else -> Triple(Color(0xFFD1FAE5), Color(0xFF059669), "Synced")
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = chipBg,
        modifier = modifier
            .testTag("gdrive_sync_header_chip")
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (syncStatus == SyncStatus.SYNCING) Icons.Default.Sync else Icons.Default.CloudDone,
                contentDescription = label,
                tint = chipContentColor,
                modifier = Modifier
                    .size(13.dp)
                    .then(if (syncStatus == SyncStatus.SYNCING) Modifier.rotate(spinAngle) else Modifier)
            )
            Text(
                text = "Drive $label",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = chipContentColor
                )
            )
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    if (timestamp <= 0) return "Never synced"
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 10_000L -> "Just now"
        diff < 60_000L -> "${diff / 1000}s ago"
        diff < 3600_000L -> "${diff / 60_000}m ago"
        diff < 86400_000L -> {
            val df = SimpleDateFormat("h:mm a", Locale.getDefault())
            "Today at ${df.format(Date(timestamp))}"
        }
        else -> {
            val df = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            df.format(Date(timestamp))
        }
    }
}
