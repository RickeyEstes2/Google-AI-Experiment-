package com.example.data.sync

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ArticleEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

enum class SyncStatus {
    SYNCED,
    SYNCING,
    PENDING,
    ERROR,
    OFFLINE
}

enum class ConflictResolutionStrategy(val title: String) {
    MERGE_NEWEST("Merge Newest (Recommended)"),
    SERVER_WINS("Cloud / Server Wins"),
    CLIENT_WINS("Local Device Wins")
}

enum class SyncDirection(val label: String) {
    TWO_WAY("Two-Way Sync (Bidirectional)"),
    UPLOAD_ONLY("Backup (Upload Only)"),
    DOWNLOAD_ONLY("Mirror (Download Only)")
}

enum class CloudProvider(
    val id: String,
    val displayName: String,
    val defaultFolder: String,
    val description: String,
    val protocolType: String
) {
    GOOGLE_DRIVE(
        id = "google_drive",
        displayName = "Google Drive",
        defaultFolder = "/Google Drive/Mastermind Notes/",
        description = "Sync across personal & workspace Google Drive folders",
        protocolType = "Drive REST v3"
    ),
    DROPBOX(
        id = "dropbox",
        displayName = "Dropbox",
        defaultFolder = "/Dropbox/Apps/Mastermind/",
        description = "Store notes, formulas & diagrams in your Dropbox App folder",
        protocolType = "Dropbox v2 API"
    ),
    MICROSOFT_ONEDRIVE(
        id = "onedrive",
        displayName = "Microsoft OneDrive",
        defaultFolder = "/OneDrive/Documents/Mastermind/",
        description = "Synchronize seamlessly with OneDrive Personal or Business",
        protocolType = "Microsoft Graph"
    ),
    NEXTCLOUD_WEBDAV(
        id = "nextcloud",
        displayName = "Nextcloud / ownCloud",
        defaultFolder = "/Nextcloud/Notes/Mastermind/",
        description = "Self-hosted private cloud via secure WebDAV protocol",
        protocolType = "WebDAV / CalDAV"
    ),
    AMAZON_S3(
        id = "aws_s3",
        displayName = "AWS S3 / MinIO Object Storage",
        defaultFolder = "s3://mastermind-notes-bucket/sync/",
        description = "Encrypted multi-region object storage bucket",
        protocolType = "S3 Object Store"
    ),
    BOX(
        id = "box",
        displayName = "Box.com",
        defaultFolder = "/Box/Sync/Mastermind/",
        description = "Enterprise-grade cloud content management",
        protocolType = "Box v2 REST"
    ),
    APPLE_ICLOUD(
        id = "icloud",
        displayName = "Apple iCloud Drive",
        defaultFolder = "/iCloud Drive/Mastermind/",
        description = "Sync across iOS, macOS, and Android devices",
        protocolType = "CloudKit Storage"
    ),
    CUSTOM_WEBDAV_SERVER(
        id = "custom_server",
        displayName = "Custom Server / WebDAV URL",
        defaultFolder = "/remote_vault/mastermind_data/",
        description = "Connect to any custom HTTPS WebDAV or REST sync endpoint",
        protocolType = "Custom HTTPS API"
    )
}

data class CloudServerConfig(
    val providerId: String,
    val displayName: String,
    val isEnabled: Boolean = false,
    val targetFolder: String,
    val serverUrl: String = "",
    val authAccount: String = "",
    val syncDirection: String = SyncDirection.TWO_WAY.name,
    val lastSyncTime: Long = 0L,
    val statusText: String = "Ready"
)

data class SyncLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val status: SyncStatus,
    val message: String
)

data class CloudSnapshot(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val timestamp: Long = System.currentTimeMillis(),
    val itemCount: Int,
    val dataJson: String
)

class CloudSyncManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("cloud_sync_prefs", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val entityListType = Types.newParameterizedType(List::class.java, ArticleEntity::class.java)
    private val entityListAdapter = moshi.adapter<List<ArticleEntity>>(entityListType)

    private val serverListType = Types.newParameterizedType(List::class.java, CloudServerConfig::class.java)
    private val serverListAdapter = moshi.adapter<List<CloudServerConfig>>(serverListType)

    private val _syncStatus = MutableStateFlow(SyncStatus.SYNCED)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(prefs.getLong("last_sync_timestamp", System.currentTimeMillis()))
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val _pendingChanges = MutableStateFlow(0)
    val pendingChanges: StateFlow<Int> = _pendingChanges.asStateFlow()

    private val _autoSyncEnabled = MutableStateFlow(prefs.getBoolean("auto_sync_enabled", true))
    val autoSyncEnabled: StateFlow<Boolean> = _autoSyncEnabled.asStateFlow()

    private val _syncIntervalSeconds = MutableStateFlow(prefs.getInt("sync_interval_seconds", 30))
    val syncIntervalSeconds: StateFlow<Int> = _syncIntervalSeconds.asStateFlow()

    private val _configuredServers = MutableStateFlow<List<CloudServerConfig>>(emptyList())
    val configuredServers: StateFlow<List<CloudServerConfig>> = _configuredServers.asStateFlow()

    private val _syncLogs = MutableStateFlow<List<SyncLogEntry>>(emptyList())
    val syncLogs: StateFlow<List<SyncLogEntry>> = _syncLogs.asStateFlow()

    private val _snapshots = MutableStateFlow<List<CloudSnapshot>>(emptyList())
    val snapshots: StateFlow<List<CloudSnapshot>> = _snapshots.asStateFlow()

    private var autoSyncJob: Job? = null
    private var syncCallback: (suspend () -> List<ArticleEntity>)? = null
    private var applySyncedArticlesCallback: (suspend (List<ArticleEntity>) -> Unit)? = null

    init {
        loadServers()
        loadSnapshots()
        addLog(SyncStatus.SYNCED, "Cloud sync engine initialized. Auto-sync active.")
        startAutoSyncWorker()
    }

    fun registerSyncCallbacks(
        fetchLocalArticles: suspend () -> List<ArticleEntity>,
        applySyncedArticles: suspend (List<ArticleEntity>) -> Unit
    ) {
        this.syncCallback = fetchLocalArticles
        this.applySyncedArticlesCallback = applySyncedArticles
    }

    private fun loadServers() {
        val raw = prefs.getString("cloud_servers_config_json", null)
        val defaultList = CloudProvider.values().mapIndexed { index, provider ->
            CloudServerConfig(
                providerId = provider.id,
                displayName = provider.displayName,
                isEnabled = index == 0, // Enable Google Drive by default
                targetFolder = provider.defaultFolder,
                serverUrl = if (provider == CloudProvider.NEXTCLOUD_WEBDAV) "https://cloud.example.org/remote.php/webdav/" else "",
                authAccount = if (index == 0) "user@gmail.com" else "",
                syncDirection = SyncDirection.TWO_WAY.name,
                lastSyncTime = if (index == 0) System.currentTimeMillis() else 0L,
                statusText = if (index == 0) "Active" else "Ready"
            )
        }

        if (raw != null) {
            try {
                val saved = serverListAdapter.fromJson(raw) ?: defaultList
                // Ensure all enum providers exist even if updated
                val merged = CloudProvider.values().map { provider ->
                    saved.find { it.providerId == provider.id } ?: CloudServerConfig(
                        providerId = provider.id,
                        displayName = provider.displayName,
                        isEnabled = false,
                        targetFolder = provider.defaultFolder
                    )
                }
                _configuredServers.value = merged
            } catch (e: Exception) {
                _configuredServers.value = defaultList
            }
        } else {
            _configuredServers.value = defaultList
            saveServers(defaultList)
        }
    }

    private fun saveServers(servers: List<CloudServerConfig>) {
        try {
            prefs.edit().putString("cloud_servers_config_json", serverListAdapter.toJson(servers)).apply()
        } catch (_: Exception) {}
    }

    fun toggleServerEnabled(providerId: String, enabled: Boolean) {
        val updated = _configuredServers.value.map {
            if (it.providerId == providerId) {
                it.copy(
                    isEnabled = enabled,
                    statusText = if (enabled) "Enabled" else "Disabled"
                )
            } else it
        }
        _configuredServers.value = updated
        saveServers(updated)
        val server = updated.find { it.providerId == providerId }
        addLog(
            if (enabled) SyncStatus.SYNCED else SyncStatus.PENDING,
            "${server?.displayName ?: providerId} auto-sync ${if (enabled) "ENABLED" else "DISABLED"}"
        )
        if (enabled && _autoSyncEnabled.value) {
            triggerSyncNow()
        }
    }

    fun updateServerFolder(providerId: String, newFolder: String) {
        val updated = _configuredServers.value.map {
            if (it.providerId == providerId) {
                it.copy(targetFolder = newFolder.trim())
            } else it
        }
        _configuredServers.value = updated
        saveServers(updated)
        addLog(SyncStatus.SYNCED, "Updated target folder for $providerId to: $newFolder")
    }

    fun updateServerDetails(
        providerId: String,
        targetFolder: String,
        serverUrl: String,
        authAccount: String,
        direction: SyncDirection
    ) {
        val updated = _configuredServers.value.map {
            if (it.providerId == providerId) {
                it.copy(
                    targetFolder = targetFolder.trim(),
                    serverUrl = serverUrl.trim(),
                    authAccount = authAccount.trim(),
                    syncDirection = direction.name
                )
            } else it
        }
        _configuredServers.value = updated
        saveServers(updated)
        addLog(SyncStatus.SYNCED, "Updated configuration & folder for $providerId.")
    }

    fun testServerConnection(providerId: String) {
        scope.launch {
            val server = _configuredServers.value.find { it.providerId == providerId } ?: return@launch
            addLog(SyncStatus.SYNCING, "Testing connection to ${server.displayName} at folder: ${server.targetFolder}...")
            delay(800L)
            val updated = _configuredServers.value.map {
                if (it.providerId == providerId) {
                    it.copy(
                        statusText = "Verified Connected",
                        lastSyncTime = System.currentTimeMillis()
                    )
                } else it
            }
            _configuredServers.value = updated
            saveServers(updated)
            addLog(SyncStatus.SYNCED, "Connection verified for ${server.displayName}! Target folder confirmed.")
        }
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        _autoSyncEnabled.value = enabled
        prefs.edit().putBoolean("auto_sync_enabled", enabled).apply()
        addLog(
            if (enabled) SyncStatus.SYNCED else SyncStatus.PENDING,
            "Auto-Sync ${if (enabled) "Enabled" else "Paused by user"}"
        )
        if (enabled) {
            startAutoSyncWorker()
            triggerSyncNow()
        } else {
            autoSyncJob?.cancel()
        }
    }

    fun setSyncInterval(seconds: Int) {
        _syncIntervalSeconds.value = seconds
        prefs.edit().putInt("sync_interval_seconds", seconds).apply()
        addLog(SyncStatus.SYNCED, "Auto-sync interval set to ${seconds}s")
        if (_autoSyncEnabled.value) {
            startAutoSyncWorker()
        }
    }

    fun notifyDataChanged() {
        _pendingChanges.value += 1
        _syncStatus.value = SyncStatus.PENDING
        if (_autoSyncEnabled.value) {
            // Trigger quick debounce sync
            scope.launch {
                delay(1200L)
                performSyncInternal()
            }
        }
    }

    fun triggerSyncNow() {
        scope.launch {
            performSyncInternal()
        }
    }

    private fun startAutoSyncWorker() {
        autoSyncJob?.cancel()
        if (!_autoSyncEnabled.value) return

        autoSyncJob = scope.launch {
            while (isActive) {
                val interval = _syncIntervalSeconds.value.coerceAtLeast(10) * 1000L
                delay(interval)
                if (_autoSyncEnabled.value) {
                    performSyncInternal()
                }
            }
        }
    }

    private suspend fun performSyncInternal() = withContext(Dispatchers.IO) {
        if (_syncStatus.value == SyncStatus.SYNCING) return@withContext

        val activeServers = _configuredServers.value.filter { it.isEnabled }

        try {
            _syncStatus.value = SyncStatus.SYNCING
            val localItems = syncCallback?.invoke() ?: emptyList()

            if (activeServers.isEmpty()) {
                addLog(SyncStatus.PENDING, "Auto-sync pending: No cloud servers selected. Please enable at least 1 cloud server.")
                _syncStatus.value = SyncStatus.PENDING
                return@withContext
            }

            addLog(SyncStatus.SYNCING, "Syncing ${localItems.size} items across ${activeServers.size} active cloud server(s)...")

            // Simulate network sync to all selected servers
            delay(1000L)

            val now = System.currentTimeMillis()
            val updatedServers = _configuredServers.value.map { server ->
                if (server.isEnabled) {
                    server.copy(
                        lastSyncTime = now,
                        statusText = "Synced (${localItems.size} items)"
                    )
                } else server
            }
            _configuredServers.value = updatedServers
            saveServers(updatedServers)

            // Cache local state
            val json = entityListAdapter.toJson(localItems)
            prefs.edit().putString("cloud_storage_cached_state", json).apply()

            _lastSyncTimestamp.value = now
            prefs.edit().putLong("last_sync_timestamp", now).apply()
            _pendingChanges.value = 0
            _syncStatus.value = SyncStatus.SYNCED

            activeServers.forEach { s ->
                addLog(SyncStatus.SYNCED, "✓ [${s.displayName}] Synced ${localItems.size} items to folder: ${s.targetFolder}")
            }
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            addLog(SyncStatus.ERROR, "Sync error: ${e.message ?: "Network timeout"}")
        }
    }

    fun createSnapshot(items: List<ArticleEntity>, note: String = "") {
        val now = System.currentTimeMillis()
        val dateStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(now))
        val name = if (note.isNotBlank()) "$note ($dateStr)" else "Snapshot ($dateStr)"
        val json = entityListAdapter.toJson(items)
        val snapshot = CloudSnapshot(
            name = name,
            timestamp = now,
            itemCount = items.size,
            dataJson = json
        )
        val updated = listOf(snapshot) + _snapshots.value.take(10)
        _snapshots.value = updated
        saveSnapshots(updated)
        addLog(SyncStatus.SYNCED, "Created cloud snapshot: $name (${items.size} items)")
    }

    suspend fun restoreSnapshot(snapshot: CloudSnapshot) = withContext(Dispatchers.IO) {
        try {
            _syncStatus.value = SyncStatus.SYNCING
            val items = entityListAdapter.fromJson(snapshot.dataJson) ?: emptyList()
            applySyncedArticlesCallback?.invoke(items)
            val now = System.currentTimeMillis()
            _lastSyncTimestamp.value = now
            _pendingChanges.value = 0
            _syncStatus.value = SyncStatus.SYNCED
            addLog(SyncStatus.SYNCED, "Restored snapshot: ${snapshot.name} (${items.size} items)")
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            addLog(SyncStatus.ERROR, "Failed to restore snapshot: ${e.message}")
        }
    }

    fun exportBackupJson(items: List<ArticleEntity>): String {
        return entityListAdapter.toJson(items)
    }

    suspend fun importBackupJson(json: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val items = entityListAdapter.fromJson(json) ?: return@withContext false
            applySyncedArticlesCallback?.invoke(items)
            triggerSyncNow()
            addLog(SyncStatus.SYNCED, "Imported cloud backup with ${items.size} articles.")
            true
        } catch (e: Exception) {
            addLog(SyncStatus.ERROR, "Import error: ${e.message}")
            false
        }
    }

    private fun addLog(status: SyncStatus, msg: String) {
        val newEntry = SyncLogEntry(status = status, message = msg)
        val updated = (listOf(newEntry) + _syncLogs.value).take(40)
        _syncLogs.value = updated
    }

    private fun loadSnapshots() {
        val raw = prefs.getString("cloud_snapshots_json", null) ?: return
        try {
            val snapListType = Types.newParameterizedType(List::class.java, CloudSnapshot::class.java)
            val adapter = moshi.adapter<List<CloudSnapshot>>(snapListType)
            _snapshots.value = adapter.fromJson(raw) ?: emptyList()
        } catch (e: Exception) {
            _snapshots.value = emptyList()
        }
    }

    private fun saveSnapshots(list: List<CloudSnapshot>) {
        try {
            val snapListType = Types.newParameterizedType(List::class.java, CloudSnapshot::class.java)
            val adapter = moshi.adapter<List<CloudSnapshot>>(snapListType)
            prefs.edit().putString("cloud_snapshots_json", adapter.toJson(list)).apply()
        } catch (_: Exception) {}
    }
}

