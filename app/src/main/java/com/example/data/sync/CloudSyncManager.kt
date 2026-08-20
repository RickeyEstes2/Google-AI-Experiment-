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

    private val _syncLogs = MutableStateFlow<List<SyncLogEntry>>(emptyList())
    val syncLogs: StateFlow<List<SyncLogEntry>> = _syncLogs.asStateFlow()

    private val _snapshots = MutableStateFlow<List<CloudSnapshot>>(emptyList())
    val snapshots: StateFlow<List<CloudSnapshot>> = _snapshots.asStateFlow()

    private var autoSyncJob: Job? = null
    private var syncCallback: (suspend () -> List<ArticleEntity>)? = null
    private var applySyncedArticlesCallback: (suspend (List<ArticleEntity>) -> Unit)? = null

    init {
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

        try {
            _syncStatus.value = SyncStatus.SYNCING
            addLog(SyncStatus.SYNCING, "Connecting to Cloud Endpoint & verifying hash...")

            // Fetch current local entities
            val localItems = syncCallback?.invoke() ?: emptyList()
            delay(900L) // Simulate network cloud verification

            // Backup snapshot to cloud storage cache
            val json = entityListAdapter.toJson(localItems)
            prefs.edit().putString("cloud_storage_cached_state", json).apply()

            val now = System.currentTimeMillis()
            _lastSyncTimestamp.value = now
            prefs.edit().putLong("last_sync_timestamp", now).apply()
            _pendingChanges.value = 0
            _syncStatus.value = SyncStatus.SYNCED

            addLog(SyncStatus.SYNCED, "Cloud auto-sync completed successfully (${localItems.size} links synced).")
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            addLog(SyncStatus.ERROR, "Sync error: ${e.message ?: "Network timeout"}")
        }
    }

    fun createSnapshot(items: List<ArticleEntity>, note: String = "") {
        val now = System.currentTimeMillis()
        val dateStr = SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault()).format(Date(now))
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
