package com.example.data.drive

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DriveFileListResponse(
    @param:Json(name = "kind") val kind: String? = null,
    @param:Json(name = "nextPageToken") val nextPageToken: String? = null,
    @param:Json(name = "incompleteSearch") val incompleteSearch: Boolean = false,
    @param:Json(name = "files") val files: List<DriveFileItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DriveFileItem(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "name") val name: String,
    @param:Json(name = "mimeType") val mimeType: String = "application/vnd.google-apps.folder",
    @param:Json(name = "parents") val parents: List<String>? = null,
    @param:Json(name = "size") val size: String? = null,
    @param:Json(name = "createdTime") val createdTime: String? = null,
    @param:Json(name = "modifiedTime") val modifiedTime: String? = null,
    @param:Json(name = "webViewLink") val webViewLink: String? = null,
    @param:Json(name = "iconLink") val iconLink: String? = null,
    @param:Json(name = "shared") val shared: Boolean = false,
    @param:Json(name = "trashed") val trashed: Boolean = false,
    @param:Json(name = "description") val description: String? = null
) {
    val isFolder: Boolean
        get() = mimeType == "application/vnd.google-apps.folder"
}

@JsonClass(generateAdapter = true)
data class DriveFolderCreateRequest(
    @param:Json(name = "name") val name: String,
    @param:Json(name = "mimeType") val mimeType: String = "application/vnd.google-apps.folder",
    @param:Json(name = "parents") val parents: List<String>? = null,
    @param:Json(name = "description") val description: String? = null
)

@JsonClass(generateAdapter = true)
data class DriveFileMetadataRequest(
    @param:Json(name = "name") val name: String,
    @param:Json(name = "mimeType") val mimeType: String = "application/json",
    @param:Json(name = "parents") val parents: List<String>? = null,
    @param:Json(name = "description") val description: String? = null
)

@JsonClass(generateAdapter = true)
data class DriveAboutResponse(
    @param:Json(name = "user") val user: DriveUserInfo? = null,
    @param:Json(name = "storageQuota") val storageQuota: DriveStorageQuota? = null,
    @param:Json(name = "importFormats") val importFormats: Map<String, List<String>>? = null
)

@JsonClass(generateAdapter = true)
data class DriveUserInfo(
    @param:Json(name = "displayName") val displayName: String? = null,
    @param:Json(name = "emailAddress") val emailAddress: String? = null,
    @param:Json(name = "photoLink") val photoLink: String? = null,
    @param:Json(name = "permissionId") val permissionId: String? = null
)

@JsonClass(generateAdapter = true)
data class DriveStorageQuota(
    @param:Json(name = "limit") val limit: String? = null,
    @param:Json(name = "usage") val usage: String? = null,
    @param:Json(name = "usageInDrive") val usageInDrive: String? = null,
    @param:Json(name = "usageInDriveTrash") val usageInDriveTrash: String? = null
) {
    val formattedLimit: String
        get() {
            val bytes = limit?.toLongOrNull() ?: return "Unlimited (Workspace)"
            return formatBytes(bytes)
        }

    val formattedUsage: String
        get() {
            val bytes = usage?.toLongOrNull() ?: return "0 MB"
            return formatBytes(bytes)
        }

    val usagePercentage: Float
        get() {
            val l = limit?.toFloatOrNull() ?: return 0.05f
            val u = usage?.toFloatOrNull() ?: return 0f
            if (l <= 0) return 0.05f
            return (u / l).coerceIn(0f, 1f)
        }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format("%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format("%.1f MB", mb)
        val gb = mb / 1024.0
        return String.format("%.2f GB", gb)
    }
}

data class DriveAccountInfo(
    val email: String,
    val displayName: String,
    val storageUsageFormatted: String,
    val storageLimitFormatted: String,
    val storageUsageRatio: Float,
    val projectId: String,
    val oAuthClientId: String,
    val isConnected: Boolean,
    val lastVerified: Long = System.currentTimeMillis()
)
