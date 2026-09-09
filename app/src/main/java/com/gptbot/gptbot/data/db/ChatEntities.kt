package com.gptbot.gptbot.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gptbot.gptbot.data.model.ChatMode
import kotlinx.serialization.Serializable

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdatedAt: Long = System.currentTimeMillis(),
    val initialMode: String = ChatMode.GENERAL_COT.name,
    val totalIterations: Int = 1
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val role: String, // "USER", "ASSISTANT", "SYSTEM"
    val content: String,
    val chainOfThoughtJson: String? = null,
    val searchResultsJson: String? = null,
    val contradictionReportJson: String? = null,
    val mathSolutionJson: String? = null,
    val codeBlocksJson: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val iterationCount: Int = 1,
    val mode: String = ChatMode.GENERAL_COT.name,
    val attachedSnippetTitle: String? = null
)

@Entity(tableName = "saved_logs")
data class SavedLogEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val source: String, // e.g. "Android Logcat", "K8s Microservice", "Custom"
    val rawText: String,
    val lineCount: Int,
    val errorCount: Int,
    val warnCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_pdf_docs")
data class SavedPdfEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val pageCount: Int,
    val summary: String,
    val fullContentJson: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_math_problems")
data class SavedMathEntity(
    @PrimaryKey
    val id: String,
    val problem: String,
    val domain: String,
    val latexFormula: String,
    val solutionJson: String,
    val timestamp: Long = System.currentTimeMillis()
)
