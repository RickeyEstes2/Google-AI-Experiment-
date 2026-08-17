package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.crypto.CryptoManager

/**
 * Room entity representing an encrypted saved web page or article.
 */
@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val title: String,
    val sourceDomain: String,
    val thumbnailUrl: String,
    val category: String,
    val hashtagsJson: String, // JSON or comma-separated list of hashtags
    val contentEncrypted: String, // Encrypted article text
    val summaryEncrypted: String, // Encrypted AI summary
    val readingTimeMinutes: Int,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val equationsJsonEncrypted: String = "", // Encrypted JSON of extracted physics equations
    val nlpJsonEncrypted: String = "" // Encrypted JSON of NLP analysis cache
) {
    fun toDomain(): Article {
        return Article(
            id = id,
            url = url,
            title = title,
            sourceDomain = sourceDomain,
            thumbnailUrl = thumbnailUrl,
            category = category,
            hashtags = parseHashtags(hashtagsJson),
            content = CryptoManager.decrypt(contentEncrypted),
            summary = CryptoManager.decrypt(summaryEncrypted),
            readingTimeMinutes = readingTimeMinutes,
            isFavorite = isFavorite,
            isArchived = isArchived,
            createdTimestamp = createdTimestamp,
            equationsEncrypted = equationsJsonEncrypted,
            nlpEncrypted = nlpJsonEncrypted
        )
    }

    companion object {
        fun fromDomain(article: Article): ArticleEntity {
            return ArticleEntity(
                id = article.id,
                url = article.url,
                title = article.title,
                sourceDomain = article.sourceDomain,
                thumbnailUrl = article.thumbnailUrl,
                category = article.category,
                hashtagsJson = article.hashtags.joinToString(","),
                contentEncrypted = CryptoManager.encrypt(article.content),
                summaryEncrypted = CryptoManager.encrypt(article.summary),
                readingTimeMinutes = article.readingTimeMinutes,
                isFavorite = article.isFavorite,
                isArchived = article.isArchived,
                createdTimestamp = article.createdTimestamp,
                equationsJsonEncrypted = article.equationsEncrypted,
                nlpJsonEncrypted = article.nlpEncrypted
            )
        }

        private fun parseHashtags(raw: String): List<String> {
            if (raw.isBlank()) return emptyList()
            return raw.split(",")
                .map { it.trim().removePrefix("#") }
                .filter { it.isNotBlank() }
                .map { "#$it" }
        }
    }
}

/**
 * Clean domain representation of an article for UI.
 */
data class Article(
    val id: Long = 0,
    val url: String,
    val title: String,
    val sourceDomain: String,
    val thumbnailUrl: String,
    val category: String,
    val hashtags: List<String>,
    val content: String,
    val summary: String,
    val readingTimeMinutes: Int,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val equationsEncrypted: String = "",
    val nlpEncrypted: String = ""
)

/**
 * Fundamental forces of physics for color-coding equations.
 */
enum class FundamentalForce(
    val displayName: String,
    val forceName: String,
    val hexColor: Long, // Green = Strong, Blue = Weak, Red = EM, Brown = Gravity
    val carrierBoson: String,
    val description: String
) {
    STRONG_NUCLEAR(
        displayName = "Strong Nuclear Force",
        forceName = "Strong Force",
        hexColor = 0xFF16A34A, // Green
        carrierBoson = "Gluon (g) / Color Charge",
        description = "Binds quarks into hadrons and holds atomic nuclei together via gluon field exchange."
    ),
    WEAK_NUCLEAR(
        displayName = "Weak Nuclear Force",
        forceName = "Weak Force",
        hexColor = 0xFF2563EB, // Blue
        carrierBoson = "W⁺, W⁻, Z⁰ Bosons",
        description = "Responsible for radioactive decay, flavor changes in subatomic particles, and neutrino interactions."
    ),
    ELECTROMAGNETIC(
        displayName = "Electromagnetic Force",
        forceName = "Electromagnetism",
        hexColor = 0xFFDC2626, // Red
        carrierBoson = "Photon (γ)",
        description = "Governs interactions between electrically charged particles, atomic structure, and radiation."
    ),
    GRAVITY(
        displayName = "Gravity",
        forceName = "Gravitational Force",
        hexColor = 0xFF854D0E, // Brown (#854D0E / #78350F)
        carrierBoson = "Graviton / Spacetime Curvature (g_μν)",
        description = "Universal attraction between masses resulting from the curvature of spacetime."
    ),
    GENERAL_UNIFIED(
        displayName = "Grand Unified / Mathematical",
        forceName = "Unified / Math",
        hexColor = 0xFF6366F1, // Indigo
        carrierBoson = "Unified Field",
        description = "Fundamental mathematical symmetry and unified field relationships."
    )
}

/**
 * Mathematical / Physical Equation item.
 */
data class EquationItem(
    val id: String,
    val title: String,
    val rawFormula: String,
    val primaryForce: FundamentalForce,
    val termsBreakdown: List<EquationTerm>,
    val conceptualExplanation: String,
    val stepByStepSolution: List<SolutionStep>,
    val variableNames: Map<String, String>, // e.g. "G" -> "Gravitational Constant", "M" -> "Mass"
    val defaultValues: Map<String, Double> = emptyMap(),
    val unit: String = ""
)

data class EquationTerm(
    val symbol: String,
    val name: String,
    val force: FundamentalForce,
    val explanation: String
)

data class SolutionStep(
    val stepNumber: Int,
    val stepTitle: String,
    val formulaState: String,
    val explanation: String
)

/**
 * Natural Language Processing (NLP) linguistic breakdown data classes.
 */
data class NLPAnalysisResult(
    val totalWords: Int,
    val uniqueWords: Int,
    val readabilityScore: Double,
    val termFrequencies: List<TermFreqItem>,
    val namedEntities: List<NamedEntityItem>,
    val verbToVerbs: List<VerbToVerbItem>,
    val nounPhrases: List<NounPhraseItem>,
    val bigrams: List<BigramItem>,
    val trigrams: List<TrigramItem>,
    val posTaggedTerms: List<PosTagItem>
)

data class TermFreqItem(
    val term: String,
    val count: Int,
    val percentage: Float
)

data class NamedEntityItem(
    val entity: String,
    val type: String, // PERSON, ORG, LOCATION, CONCEPT, EVENT
    val count: Int
)

data class VerbToVerbItem(
    val phrase: String, // e.g. "aim to discover", "want to understand"
    val verb1: String,
    val verb2: String,
    val count: Int
)

data class NounPhraseItem(
    val phrase: String, // e.g. "quantum chromodynamics", "strong nuclear force"
    val count: Int
)

data class BigramItem(
    val bigram: String,
    val count: Int
)

data class TrigramItem(
    val trigram: String,
    val count: Int
)

data class PosTagItem(
    val term: String,
    val tag: String, // NN, VB, JJ, RB, IN, etc.
    val tagDescription: String,
    val count: Int
)

/**
 * AI Chat message.
 */
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: ChatSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isThinking: Boolean = false,
    val thoughtProcess: String? = null
)

enum class ChatSender {
    USER,
    AI
}

/**
 * Google Drive Sync State.
 */
data class GoogleDriveSyncState(
    val isConnected: Boolean = true,
    val isSyncing: Boolean = false,
    val accountEmail: String = "database.mastermind@drive.google.com",
    val lastSyncTimestamp: Long = System.currentTimeMillis() - (1000 * 60 * 14),
    val totalBackupsCount: Int = 12,
    val isE2EEActive: Boolean = true,
    val cloudBackupSizeBytes: Long = 428000
)
