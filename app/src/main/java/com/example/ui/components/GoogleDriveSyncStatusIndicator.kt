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
import androidx.compose.ui.graphics.Brush
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
 * Visual Sync Status Indicator for Google Drive integration.
 * Reflects whether the local database is currently in sync with the selected Google Drive folder.
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

    var isExpanded by remember { mutableStateOf(false) }

    val gDriveServer = remember(configuredServers) {
        configuredServers.find { it.providerId == CloudProvider.GOOGLE_DRIVE.id }
    }
    val isDriveEnabled = gDriveServer?.isEnabled ?: true
    val accountEmail = gDriveServer?.authAccount?.ifBlank { "lookingup2theskytemp@gmail.com" } ?: "lookingup2theskytemp@gmail.com"

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
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    // Pulse animation for active sync dot
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Formatted relative timestamp
    val relativeSyncText = remember(lastSyncTime, syncStatus) {
        formatRelativeTime(lastSyncTime)
    }

    // Color definitions based on Google Drive sync state
    val statusColor = when {
        !isDriveEnabled -> Color(0xFF94A3B8)
        syncStatus == SyncStatus.SYNCING -> Color(0xFF0284C7) // Sky Blue
        syncStatus == SyncStatus.ERROR -> Color(0xFFEF4444) // Red
        pendingChanges > 0 || syncStatus == SyncStatus.PENDING -> Color(0xFFF59E0B) // Amber
        else -> Color(0xFF10B981) // Emerald Green
    }

    val statusContainerColor = when {
        !isDriveEnabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        syncStatus == SyncStatus.SYNCING -> Color(0xFFE0F2FE)
        syncStatus == SyncStatus.ERROR -> Color(0xFFFEE2E2)
        pendingChanges > 0 || syncStatus == SyncStatus.PENDING -> Color(0xFFFEF3C7)
        else -> Color(0xFFDCFCE7)
    }

    val statusIcon: ImageVector = when {
        !isDriveEnabled -> Icons.Default.CloudOff
        syncStatus == SyncStatus.SYNCING -> Icons.Default.Sync
        syncStatus == SyncStatus.ERROR -> Icons.Default.Warning
        pendingChanges > 0 || syncStatus == SyncStatus.PENDING -> Icons.Default.CloudUpload
        else -> Icons.Default.CheckCircle
    }

    val statusTitle = when {
        !isDriveEnabled -> "Google Drive Sync Paused"
        syncStatus == SyncStatus.SYNCING -> "Syncing with Google Drive..."
        syncStatus == SyncStatus.ERROR -> "Google Drive Sync Warning"
        pendingChanges > 0 || syncStatus == SyncStatus.PENDING -> "$pendingChanges Unsynced Local Change${if (pendingChanges > 1) "s" else ""}"
        else -> "Database in Sync with Google Drive"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.5.dp, statusColor.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("gdrive_sync_status_bar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Main Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Icon + Title + Target Folder
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Badge Icon with glowing container
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = statusContainerColor,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = statusTitle,
                                tint = statusColor,
                                modifier = Modifier
                                    .size(22.dp)
                                    .then(
                                        if (syncStatus == SyncStatus.SYNCING) Modifier.rotate(spinAngle)
                                        else Modifier
                                    )
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Pulsing Dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (syncStatus == SyncStatus.SYNCING) statusColor.copy(alpha = pulseAlpha)
                                        else statusColor
                                    )
                            )

                            Text(
                                text = statusTitle,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.5.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Target folder breadcrumb
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = folderPath,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.5.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Right side: Quick Action Button ("Sync Now" or Spinner)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = { syncManager.triggerSyncNow() },
                        enabled = syncStatus != SyncStatus.SYNCING,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (pendingChanges > 0) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (pendingChanges > 0) Color(0xFF92400E) else MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("gdrive_sync_now_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync Now",
                            modifier = Modifier
                                .size(14.dp)
                                .then(
                                    if (syncStatus == SyncStatus.SYNCING) Modifier.rotate(spinAngle)
                                    else Modifier
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (syncStatus == SyncStatus.SYNCING) "Syncing..." else if (pendingChanges > 0) "Sync Now" else "Sync",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Expand / Collapse Chevron Button
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("gdrive_sync_expand_button")
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Show Less" else "Show Details",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Sync metrics summary row (always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Last Sync Timestamp
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Last Synced: $relativeSyncText",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Right: Target database file badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = driveSettings.syncFileName,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Expanded In-Depth Sync & Google Drive Folder Inspection
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )

                    // 4 Key Metric Tiles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Tile 1: Database Records
                        MetricTile(
                            title = "Local Database",
                            value = "$totalLocalArticles Articles",
                            subtext = "SQLite / Room",
                            icon = Icons.Default.Storage,
                            modifier = Modifier.weight(1f)
                        )

                        // Tile 2: Target Drive Folder
                        MetricTile(
                            title = "Drive Folder",
                            value = folderName,
                            subtext = "${selectedFolder?.fileCount ?: 24} cloud files",
                            icon = Icons.Default.FolderSpecial,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Tile 3: Google Account
                        MetricTile(
                            title = "OAuth Account",
                            value = accountEmail.takeWhile { it != '@' },
                            subtext = accountEmail,
                            icon = Icons.Default.AccountCircle,
                            modifier = Modifier.weight(1f)
                        )

                        // Tile 4: Sync Protocol
                        MetricTile(
                            title = "API Protocol",
                            value = "Drive REST v3",
                            subtext = if (driveSettings.enableAES256Encryption) "AES-256 Enabled" else "JSON Mirror",
                            icon = Icons.Default.Security,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Auto-Sync Control & Cadence Row
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (autoSyncEnabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, if (autoSyncEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
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
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (autoSyncEnabled) Icons.Default.SyncLock else Icons.Default.SyncDisabled,
                                        contentDescription = null,
                                        tint = if (autoSyncEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = if (autoSyncEnabled) "Google Drive Auto-Sync: ACTIVE" else "Google Drive Auto-Sync: PAUSED",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (autoSyncEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = if (autoSyncEnabled) "Syncs on every database edit & every ${syncManager.syncIntervalSeconds.collectAsState().value}s" else "Manual sync only",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
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
                                        text = "Sync Cadence:",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )

                                    val syncInterval by syncManager.syncIntervalSeconds.collectAsState()
                                    listOf(5, 15, 30, 60).forEach { seconds ->
                                        val isSelected = syncInterval == seconds
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(
                                                0.5.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                            ),
                                            modifier = Modifier.clickable { syncManager.setSyncInterval(seconds) }
                                        ) {
                                            Text(
                                                text = "${seconds}s",
                                                fontSize = 10.5.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Action buttons: Folder Settings & Full Cloud Sync
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenFolderSettings,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("gdrive_open_folder_settings_button")
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Manage Folders", fontSize = 11.5.sp)
                        }

                        OutlinedButton(
                            onClick = onOpenCloudSync,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("gdrive_open_cloud_sync_button")
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("All Cloud Servers", fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact Top Bar Header Pill for Google Drive Sync.
 * Perfect for placement in navigation headers and toolbars.
 */
@Composable
fun GoogleDriveSyncHeaderChip(
    syncManager: CloudSyncManager,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val syncStatus by syncManager.syncStatus.collectAsState()
    val pendingChanges by syncManager.pendingChanges.collectAsState()
    val driveSettings by syncManager.driveSyncSettings.collectAsState()
    val driveFolders by syncManager.driveFolders.collectAsState()

    val selectedFolder = remember(driveFolders, driveSettings) {
        driveFolders.find { it.id == driveSettings.selectedFolderId } ?: driveFolders.firstOrNull()
    }
    val folderName = selectedFolder?.name ?: "Mastermind_Database"

    // Animation for spinning icon
    val infiniteTransition = rememberInfiniteTransition(label = "header_sync_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "header_spin"
    )

    val (chipBg, chipBorder, chipContentColor, statusLabel) = when {
        syncStatus == SyncStatus.SYNCING -> {
            Quad(
                Color(0xFFE0F2FE),
                Color(0xFF38BDF8),
                Color(0xFF0369A1),
                "Syncing..."
            )
        }
        pendingChanges > 0 || syncStatus == SyncStatus.PENDING -> {
            Quad(
                Color(0xFFFEF3C7),
                Color(0xFFFBBF24),
                Color(0xFFB45309),
                "$pendingChanges pending"
            )
        }
        syncStatus == SyncStatus.ERROR -> {
            Quad(
                Color(0xFFFEE2E2),
                Color(0xFFF87171),
                Color(0xFFB91C1C),
                "Sync Alert"
            )
        }
        else -> {
            Quad(
                Color(0xFFDCFCE7),
                Color(0xFF4ADE80),
                Color(0xFF15803D),
                "Drive In Sync"
            )
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = chipBg,
        border = BorderStroke(1.dp, chipBorder.copy(alpha = 0.6f)),
        modifier = modifier
            .testTag("gdrive_sync_header_chip")
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = when {
                    syncStatus == SyncStatus.SYNCING -> Icons.Default.Sync
                    pendingChanges > 0 -> Icons.Default.CloudUpload
                    syncStatus == SyncStatus.ERROR -> Icons.Default.Warning
                    else -> Icons.Default.CloudDone
                },
                contentDescription = statusLabel,
                tint = chipContentColor,
                modifier = Modifier
                    .size(15.dp)
                    .then(
                        if (syncStatus == SyncStatus.SYNCING) Modifier.rotate(spinAngle)
                        else Modifier
                    )
            )

            Text(
                text = statusLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = chipContentColor,
                    fontSize = 10.5.sp
                )
            )
        }
    }
}

/**
 * Metric tile helper for expanded sync status inspection.
 */
@Composable
private fun MetricTile(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 9.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private fun formatRelativeTime(timestamp: Long): String {
    if (timestamp <= 0) return "Never"
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 10_000L -> "Just now"
        diff < 60_000L -> "${diff / 1000}s ago"
        diff < 3600_000L -> "${diff / 60_000}m ago"
        diff < 86400_000L -> {
            val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
            "Today at ${formatter.format(Date(timestamp))}"
        }
        else -> {
            val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
    }
}
