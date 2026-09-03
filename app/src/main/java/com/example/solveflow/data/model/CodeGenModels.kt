package com.example.solveflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Supported programming languages (both built-in and user-defined custom languages).
 */
@Serializable
@Entity(tableName = "languages")
data class ProgrammingLanguage(
    @PrimaryKey val id: String,
    val name: String,
    val extension: String,
    val paradigm: String,
    val sampleBoilerplate: String,
    val syntaxKeywords: String, // Comma-separated keywords
    val isCustom: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)

/**
 * User-provided and fine-tuned code snippets stored for RAG and DBSCAN clustering.
 */
@Serializable
@Entity(tableName = "code_snippets")
data class CodeSnippet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val languageId: String,
    val code: String,
    val tags: String, // Comma-separated tags
    val category: String, // e.g., Network, Database, Algorithm, UI, Security, Utility
    val explanation: String = "",
    val isFineTunedExemplar: Boolean = false,
    val errorMitigationNote: String? = null,
    val dateAdded: Long = System.currentTimeMillis()
)

/**
 * Retrievable domain knowledge, patterns, API specs, and architecture rules for generation.
 */
@Serializable
@Entity(tableName = "knowledge_items")
data class KnowledgeItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val topic: String, // e.g., Architecture, Best Practices, Security, Performance, Error Prevention
    val languageScope: String, // "All" or specific language id
    val content: String,
    val tags: String,
    val dateAdded: Long = System.currentTimeMillis()
)

/**
 * Reinforcement Learning strategy for code generation.
 */
@Serializable
enum class RlStrategy(
    val title: String,
    val description: String,
    val badgeColorHex: Long
) {
    CONCISE(
        title = "Concise & Idiomatic",
        description = "Minimal boilerplate, clean high-level syntax, direct functional expressions",
        badgeColorHex = 0xFF0284C7
    ),
    ROBUST_DEFENSIVE(
        title = "Robust & Defensive",
        description = "Exhaustive error handling, input validation, try-catch guards, safe nullability",
        badgeColorHex = 0xFF059669
    ),
    ENTERPRISE_MODULAR(
        title = "Enterprise & Modular",
        description = "Clean architecture, interface segregation, type safety, detailed documentation",
        badgeColorHex = 0xFF7C3AED
    ),
    HIGH_PERFORMANCE(
        title = "High Performance",
        description = "Optimized algorithms, memory efficiency, concurrent/asynchronous execution",
        badgeColorHex = 0xFFD97706
    ),
    TEST_DRIVEN(
        title = "Test-Driven & Assertive",
        description = "Includes integrated verification assertions, edge-case unit test coverage",
        badgeColorHex = 0xFFE11D48
    )
}

/**
 * Records of past generations, user feedback, error mitigation edits, and fine-tuning history.
 */
@Serializable
@Entity(tableName = "generation_records")
data class GenerationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prompt: String,
    val languageId: String,
    val generatedCode: String,
    val explanation: String,
    val strategy: RlStrategy,
    val clusterId: Int = -1,
    val clusterLabel: String = "Unclustered",
    val userFeedback: Int = 0, // -1 = negative, 0 = neutral, +1 = positive
    val editedCode: String? = null,
    val isFineTuned: Boolean = false,
    val errorMitigationType: String? = null,
    val errorMitigationNotes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Reinforcement Learning Q-table state-action policy values.
 */
@Serializable
@Entity(tableName = "rl_policy", primaryKeys = ["stateKey", "strategyName"])
data class RlPolicyEntry(
    val stateKey: String, // e.g. "kotlin:network", "python:algorithm", "general:default"
    val strategyName: String,
    val qValue: Double = 0.0,
    val updateCount: Int = 0,
    val totalReward: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
)
