package com.example.data.drive

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface GoogleDriveApiService {

    /**
     * Lists or searches files and folders in the user's Google Drive.
     * Query example: "mimeType = 'application/vnd.google-apps.folder' and trashed = false"
     */
    @GET("drive/v3/files")
    suspend fun listFiles(
        @Query("q") query: String? = null,
        @Query("fields") fields: String = "nextPageToken, files(id, name, mimeType, parents, size, createdTime, modifiedTime, webViewLink, shared, trashed, description)",
        @Query("orderBy") orderBy: String = "folder,modifiedTime desc,name",
        @Query("pageSize") pageSize: Int = 100,
        @Query("spaces") spaces: String = "drive",
        @Header("Authorization") authorization: String? = null,
        @Query("key") apiKey: String? = null
    ): Response<DriveFileListResponse>

    /**
     * Retrieves file or folder metadata.
     */
    @GET("drive/v3/files/{fileId}")
    suspend fun getFileMetadata(
        @Path("fileId") fileId: String,
        @Query("fields") fields: String = "id, name, mimeType, parents, size, createdTime, modifiedTime, webViewLink, shared, trashed, description",
        @Header("Authorization") authorization: String? = null,
        @Query("key") apiKey: String? = null
    ): Response<DriveFileItem>

    /**
     * Creates a new folder or file metadata entry.
     */
    @POST("drive/v3/files")
    suspend fun createFolder(
        @Body request: DriveFolderCreateRequest,
        @Header("Authorization") authorization: String? = null,
        @Query("key") apiKey: String? = null
    ): Response<DriveFileItem>

    /**
     * Creates a new file metadata record.
     */
    @POST("drive/v3/files")
    suspend fun createFileMetadata(
        @Body request: DriveFileMetadataRequest,
        @Header("Authorization") authorization: String? = null,
        @Query("key") apiKey: String? = null
    ): Response<DriveFileItem>

    /**
     * Deletes a file or folder permanently.
     */
    @DELETE("drive/v3/files/{fileId}")
    suspend fun deleteFile(
        @Path("fileId") fileId: String,
        @Header("Authorization") authorization: String? = null,
        @Query("key") apiKey: String? = null
    ): Response<Unit>

    /**
     * Downloads file content (e.g. database json).
     */
    @GET("drive/v3/files/{fileId}")
    @Streaming
    suspend fun downloadFileContent(
        @Path("fileId") fileId: String,
        @Query("alt") alt: String = "media",
        @Header("Authorization") authorization: String? = null,
        @Query("key") apiKey: String? = null
    ): Response<ResponseBody>

    /**
     * Retrieves information about the user, the user's Drive, and system capabilities.
     */
    @GET("drive/v3/about")
    suspend fun getAbout(
        @Query("fields") fields: String = "user, storageQuota, importFormats",
        @Header("Authorization") authorization: String? = null,
        @Query("key") apiKey: String? = null
    ): Response<DriveAboutResponse>

    /**
     * Direct upload of JSON content via Google Drive upload endpoint.
     */
    @POST("upload/drive/v3/files?uploadType=multipart")
    suspend fun uploadFileMultipart(
        @Body body: RequestBody,
        @Header("Authorization") authorization: String? = null,
        @Query("key") apiKey: String? = null
    ): Response<DriveFileItem>
}
