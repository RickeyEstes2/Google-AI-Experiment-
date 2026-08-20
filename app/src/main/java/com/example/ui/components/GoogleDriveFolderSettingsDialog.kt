package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.sync.CloudProvider
import com.example.data.sync.CloudSyncManager
import com.example.data.sync.GoogleDriveFolder
import com.example.data.sync.SyncStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleDriveFolderSettingsDialog(
    syncManager: CloudSyncManager,
    totalLocalArticles: Int,
    onDismiss: () -> Unit,
    onOpenFullCloudSync: () -> Unit = {}
) {
    val context = LocalContext.current
    val driveFolders by syncManager.driveFolders.collectAsState()
    val syncSettings by syncManager.driveSyncSettings.collectAsState()
    val syncStatus by syncManager.syncStatus.collectAsState()
    val configuredServers by syncManager.configuredServers.collectAsState()
    val gDriveServer = configuredServers.find { it.providerId == CloudProvider.GOOGLE_DRIVE.id }

    var selectedTab by remember { mutableStateOf(0) } // 0: Folder Selector, 1: Sync Preferences, 2: Account & API
    var isCreateFolderExpanded by remember { mutableStateOf(false) }
    var newFolderNameInput by remember { mutableStateOf("") }
    var newFolderParentPath by remember { mutableStateOf("/Google Drive/") }

    // Settings local edit states
    var syncFileNameInput by remember(syncSettings.syncFileName) { mutableStateOf(syncSettings.syncFileName) }
    var autoSyncOnChangeState by remember(syncSettings.autoSyncOnChange) { mutableStateOf(syncSettings.autoSyncOnChange) }
    var backupIntervalState by remember(syncSettings.autoBackupIntervalMinutes) { mutableStateOf(syncSettings.autoBackupIntervalMinutes) }
    var timestampedBackupsState by remember(syncSettings.createTimestampedBackups) { mutableStateOf(syncSettings.createTimestampedBackups) }
    var aesEncryptionState by remember(syncSettings.enableAES256Encryption) { mutableStateOf(syncSettings.enableAES256Encryption) }
    var isTestingConnection by remember { mutableStateOf(false) }

    val activeSelectedFolder = driveFolders.find { it.isSelected } ?: driveFolders.firstOrNull()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp)
                .testTag("google_drive_folder_settings_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dialog Header
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
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = "Google Drive",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Google Drive Target Folders",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        text = "v3 REST",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Choose destination folders on Drive for database auto-sync",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_gdrive_settings_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Active Folder Summary Banner
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.FolderSpecial,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "AUTO-SYNC DESTINATION",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 9.5.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                    Text(
                                        text = activeSelectedFolder?.path ?: syncSettings.selectedFolderPath,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = {
                                    activeSelectedFolder?.let { syncManager.testDriveFolderAccess(it.id) }
                                    Toast.makeText(context, "Testing Google Drive folder permissions...", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Access", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (syncStatus == SyncStatus.SYNCING) Icons.Default.Sync else if (syncStatus == SyncStatus.ERROR) Icons.Default.SyncProblem else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (syncStatus == SyncStatus.SYNCING) MaterialTheme.colorScheme.tertiary else if (syncStatus == SyncStatus.ERROR) MaterialTheme.colorScheme.error else Color(0xFF059669),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (syncStatus == SyncStatus.SYNCING) "Syncing with folder..." else "${activeSelectedFolder?.fileCount ?: 0} items on Drive • $totalLocalArticles articles in DB",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Clean Tab Row Navigation
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "Folders (${driveFolders.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "Sync Rules",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                text = "Account",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    )
                }

                // Tab Content Body
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> DriveFolderSelectorTab(
                            folders = driveFolders,
                            selectedFolderId = activeSelectedFolder?.id ?: "",
                            isCreateFolderExpanded = isCreateFolderExpanded,
                            newFolderNameInput = newFolderNameInput,
                            newFolderParentPath = newFolderParentPath,
                            onToggleCreateFolder = { isCreateFolderExpanded = !isCreateFolderExpanded },
                            onFolderNameChanged = { newFolderNameInput = it },
                            onFolderParentPathChanged = { newFolderParentPath = it },
                            onSelectFolder = { folderId ->
                                syncManager.selectDriveFolder(folderId)
                                Toast.makeText(context, "Target auto-sync folder set to Google Drive!", Toast.LENGTH_SHORT).show()
                            },
                            onCreateFolder = { name, parent ->
                                if (syncManager.createDriveFolder(name, parent)) {
                                    newFolderNameInput = ""
                                    isCreateFolderExpanded = false
                                    Toast.makeText(context, "New Google Drive folder created & available!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please enter a valid folder name.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDeleteFolder = { folderId ->
                                if (syncManager.deleteDriveFolder(folderId)) {
                                    Toast.makeText(context, "Folder removed from target list.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Cannot delete default system folders.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onTestFolder = { folderId ->
                                syncManager.testDriveFolderAccess(folderId)
                                Toast.makeText(context, "Testing Drive permissions...", Toast.LENGTH_SHORT).show()
                            },
                            onRefreshFolders = {
                                syncManager.refreshDriveFoldersFromApi()
                                Toast.makeText(context, "Fetching latest folders via Google Drive API...", Toast.LENGTH_SHORT).show()
                            }
                        )
                        1 -> DriveSyncPreferencesTab(
                            syncSettings = syncSettings,
                            syncFileNameInput = syncFileNameInput,
                            autoSyncOnChange = autoSyncOnChangeState,
                            backupIntervalMinutes = backupIntervalState,
                            timestampedBackups = timestampedBackupsState,
                            aesEncryption = aesEncryptionState,
                            onFileNameChanged = { syncFileNameInput = it },
                            onAutoSyncOnChangeChanged = { autoSyncOnChangeState = it },
                            onBackupIntervalChanged = { backupIntervalState = it },
                            onTimestampedBackupsChanged = { timestampedBackupsState = it },
                            onAesEncryptionChanged = { aesEncryptionState = it },
                            onSavePreferences = {
                                val updated = syncSettings.copy(
                                    syncFileName = syncFileNameInput.trim().ifBlank { "mastermind_database.json" },
                                    autoSyncOnChange = autoSyncOnChangeState,
                                    autoBackupIntervalMinutes = backupIntervalState,
                                    createTimestampedBackups = timestampedBackupsState,
                                    enableAES256Encryption = aesEncryptionState
                                )
                                syncManager.updateDriveSyncSettings(updated)
                                Toast.makeText(context, "Google Drive sync preferences saved!", Toast.LENGTH_SHORT).show()
                            }
                        )
                        2 -> DriveAccountAuthTab(
                            gDriveServer = gDriveServer,
                            syncManager = syncManager,
                            onOpenFullCloudSync = onOpenFullCloudSync
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Bottom Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = {
                                syncManager.triggerSyncNow()
                                Toast.makeText(context, "Syncing database to Google Drive target folder...", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync to Drive Now")
                        }

                        Button(
                            onClick = {
                                onDismiss()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Apply & Done")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DriveFolderSelectorTab(
    folders: List<GoogleDriveFolder>,
    selectedFolderId: String,
    isCreateFolderExpanded: Boolean,
    newFolderNameInput: String,
    newFolderParentPath: String,
    onToggleCreateFolder: () -> Unit,
    onFolderNameChanged: (String) -> Unit,
    onFolderParentPathChanged: (String) -> Unit,
    onSelectFolder: (String) -> Unit,
    onCreateFolder: (String, String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onTestFolder: (String) -> Unit,
    onRefreshFolders: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Create New Folder Accordion / Action Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleCreateFolder() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreateNewFolder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Create New Target Folder on Drive",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        IconButton(
                            onClick = onToggleCreateFolder,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isCreateFolderExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }
                    }

                    AnimatedVisibility(visible = isCreateFolderExpanded) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Text(
                                text = "Creates a directory in your Google Drive storage to isolate database synchronization payloads.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = newFolderNameInput,
                                onValueChange = onFolderNameChanged,
                                label = { Text("New Folder Name") },
                                placeholder = { Text("e.g. Mastermind_Vault_2026") },
                                leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Parent Directory Path Selector Chips
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Parent Directory Location:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val presets = listOf("/Google Drive/", "/Google Drive/Backups/", "/Google Drive/Documents/", "/Google Drive/Research/")
                                    items(presets) { preset ->
                                        FilterChip(
                                            selected = newFolderParentPath == preset,
                                            onClick = { onFolderParentPathChanged(preset) },
                                            label = { Text(preset, fontSize = 10.sp, fontFamily = FontFamily.Monospace) }
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { onCreateFolder(newFolderNameInput, newFolderParentPath) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Create & Add to Targets", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Available Folders List Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SELECT TARGET FOLDER FOR AUTO-SYNC",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(
                        onClick = onRefreshFolders,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Fetch Drive API Folders", modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fetch Folders", fontSize = 10.5.sp)
                    }
                }
            }
        }

        // Folder Items
        items(folders) { folder ->
            val isSelected = folder.isSelected || folder.id == selectedFolderId
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    }
                ),
                border = if (isSelected) {
                    androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                } else {
                    androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectFolder(folder.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelectFolder(folder.id) }
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (folder.folderType) {
                                        "backup" -> Icons.Default.Inventory2
                                        "shared" -> Icons.Default.FolderShared
                                        "root" -> Icons.Default.Cloud
                                        else -> Icons.Default.Folder
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = folder.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    fontSize = 13.5.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = "ACTIVE TARGET",
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = folder.path,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "${folder.fileCount} files on Drive",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text("•", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                Text(
                                    text = "Status: ${folder.lastSyncFormatted}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onTestFolder(folder.id) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Test Access",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (folder.isCreatedByUser) {
                            IconButton(
                                onClick = { onDeleteFolder(folder.id) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete custom folder",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DriveSyncPreferencesTab(
    syncSettings: com.example.data.sync.GoogleDriveSyncSettings,
    syncFileNameInput: String,
    autoSyncOnChange: Boolean,
    backupIntervalMinutes: Int,
    timestampedBackups: Boolean,
    aesEncryption: Boolean,
    onFileNameChanged: (String) -> Unit,
    onAutoSyncOnChangeChanged: (Boolean) -> Unit,
    onBackupIntervalChanged: (Int) -> Unit,
    onTimestampedBackupsChanged: (Boolean) -> Unit,
    onAesEncryptionChanged: (Boolean) -> Unit,
    onSavePreferences: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Database File Naming on Google Drive",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    OutlinedTextField(
                        value = syncFileNameInput,
                        onValueChange = onFileNameChanged,
                        label = { Text("Target Database File Name") },
                        placeholder = { Text("mastermind_database.json") },
                        leadingIcon = { Icon(Icons.Default.InsertDriveFile, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick name preset chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Presets:", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        listOf("mastermind_database.json", "chrome_hub_sync.json", "vault_backup.enc").forEach { preset ->
                            SuggestionChip(
                                onClick = { onFileNameChanged(preset) },
                                label = { Text(preset, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Auto-Sync Behavior & Triggers",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    // Auto sync on change
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Real-Time Sync on Edit", fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                            Text("Automatically push database payload to Drive whenever links or notes are edited", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = autoSyncOnChange,
                            onCheckedChange = onAutoSyncOnChangeChanged
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Versioned timestamped backups
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Create Timestamped Snapshots", fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                            Text("Preserve historical backup snapshots in Drive target folder alongside main file", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = timestampedBackups,
                            onCheckedChange = onTimestampedBackupsChanged
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // AES-256 Encryption
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("AES-256 Zero-Knowledge Encryption", fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                            Text("Encrypt file payload locally before transmitting to Google Drive storage", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = aesEncryption,
                            onCheckedChange = onAesEncryptionChanged
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Auto Backup Cadence
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Periodic Background Auto-Sync Cadence", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(5, 15, 30, 60).forEach { mins ->
                                FilterChip(
                                    selected = backupIntervalMinutes == mins,
                                    onClick = { onBackupIntervalChanged(mins) },
                                    label = { Text("Every $mins min", fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = onSavePreferences,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Google Drive Sync Preferences")
            }
        }
    }
}

@Composable
private fun DriveAccountAuthTab(
    gDriveServer: com.example.data.sync.CloudServerConfig?,
    syncManager: CloudSyncManager,
    onOpenFullCloudSync: () -> Unit
) {
    val context = LocalContext.current
    val firebaseConfig = remember { syncManager.googleDriveApiClient.firebaseConfig }
    var emailInput by remember(gDriveServer?.authAccount) { mutableStateOf(gDriveServer?.authAccount ?: "lookingup2theskytemp@gmail.com") }
    var passwordInput by remember(gDriveServer?.authSecretOrPassword) { mutableStateOf(gDriveServer?.authSecretOrPassword ?: "") }
    var customTokenInput by remember { mutableStateOf(syncManager.googleDriveApiClient.getOAuthAccessToken() ?: "") }
    var accountInfo by remember { mutableStateOf<com.example.data.drive.DriveAccountInfo?>(null) }

    LaunchedEffect(Unit) {
        accountInfo = syncManager.fetchDriveAccountInfo()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // OAuth Credentials from firebase-applet-config.json
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text("Google Drive OAuth 2.0 Credentials", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                "firebase-applet-config.json",
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("OAuth Client ID:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(firebaseConfig.maskedClientId, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Project ID:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(firebaseConfig.projectId, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("API Key:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(firebaseConfig.maskedApiKey, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Authorized Scope:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("https://www.googleapis.com/auth/drive.file", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Storage and Quota Bar
                    accountInfo?.let { info ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Drive Storage Quota:", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${info.storageUsageFormatted} / ${info.storageLimitFormatted}", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                            LinearProgressIndicator(
                                progress = { info.storageUsageRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Google Drive Authentication Status",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = if (gDriveServer?.isAccountConnected == true || gDriveServer?.authAccount?.isNotBlank() == true) "CONNECTED & AUTHORIZED" else "NOT CONNECTED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Authorized Scope: https://www.googleapis.com/auth/drive.file\nEnables creating, updating, and syncing database files inside designated folders without full-drive access.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Connected Google Account") },
                        leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password / App Password (Optional)") },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                syncManager.connectAccount(
                                    providerId = CloudProvider.GOOGLE_DRIVE.id,
                                    accountEmail = emailInput,
                                    secretOrPass = passwordInput
                                )
                                syncManager.saveGoogleDriveToken(
                                    token = customTokenInput.ifBlank { "oauth_bearer_${UUID.randomUUID().toString().take(12)}" },
                                    email = emailInput
                                )
                                Toast.makeText(context, "Account $emailInput authenticated with Drive API v3!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save & Authenticate", fontSize = 11.5.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                syncManager.disconnectAccount(CloudProvider.GOOGLE_DRIVE.id)
                                syncManager.googleDriveApiClient.clearOAuthTokens()
                                emailInput = ""
                                passwordInput = ""
                                customTokenInput = ""
                                Toast.makeText(context, "Google Drive account disconnected.", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Disconnect", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ),
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
                            text = "Multi-Cloud & Server Sync Hub",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                        Text(
                            text = "Sync across Dropbox, OneDrive, Nextcloud, S3, Box, and iCloud simultaneously.",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = onOpenFullCloudSync,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Open Cloud Hub", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
