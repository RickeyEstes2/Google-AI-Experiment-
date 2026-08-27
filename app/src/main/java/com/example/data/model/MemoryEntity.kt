package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val appName: String = "System",
    val packageName: String = "",
    val appCategory: String = "General",
    val title: String = "",
    val tags: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val sourceType: String = "ACCESSIBILITY", // ACCESSIBILITY, NOTIFICATION, PROCESS_TEXT, SHARE_INTENT, CLIPBOARD, MANUAL
    val sentiment: String = "NEUTRAL", // POSITIVE, NEUTRAL, IMPORTANT, ACTION_ITEM, QUESTION, IDEA
    val wordCount: Int = 0,
    val isStarred: Boolean = false,
    val addendums: List<Addendum> = emptyList(),
    val extraContext: String = ""
)

data class Addendum(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MemorySourceType(val label: String, val iconName: String) {
    ACCESSIBILITY("Live Screen & Apps", "Screen"),
    NOTIFICATION("Notification", "Notifications"),
    PROCESS_TEXT("Text Selection", "Highlight"),
    SHARE_INTENT("Shared Link / Text", "Share"),
    CLIPBOARD("Clipboard Capture", "Clipboard"),
    MANUAL("Manual Note", "Edit")
}

enum class AppCategory(val displayName: String, val colorHex: Long) {
    BROWSING("Browsing & Web", 0xFF0284C7),
    MESSAGING("Chats & Messages", 0xFF10B981),
    SOCIAL("Social Media", 0xFF8B5CF6),
    PRODUCTIVITY("Productivity & Work", 0xFFF59E0B),
    READING("Reading & Articles", 0xFFEC4899),
    FINANCE("Finance & Shopping", 0xFF06B6D4),
    MEDIA("Media & Video", 0xFFEF4444),
    GENERAL("Notes & General", 0xFF64748B);

    companion object {
        fun fromPackageOrName(pkg: String, name: String): AppCategory {
            val lower = (pkg + " " + name).lowercase()
            return when {
                lower.contains("chrome") || lower.contains("browser") || lower.contains("firefox") || lower.contains("brave") || lower.contains("edge") || lower.contains("opera") || lower.contains("safari") -> BROWSING
                lower.contains("whatsapp") || lower.contains("telegram") || lower.contains("signal") || lower.contains("message") || lower.contains("sms") || lower.contains("mms") || lower.contains("chat") || lower.contains("slack") || lower.contains("discord") || lower.contains("teams") -> MESSAGING
                lower.contains("twitter") || lower.contains("x.com") || lower.contains("instagram") || lower.contains("reddit") || lower.contains("facebook") || lower.contains("threads") || lower.contains("tiktok") || lower.contains("linkedin") -> SOCIAL
                lower.contains("notion") || lower.contains("docs") || lower.contains("keep") || lower.contains("obsidian") || lower.contains("todo") || lower.contains("calendar") || lower.contains("trello") || lower.contains("jira") || lower.contains("word") || lower.contains("excel") -> PRODUCTIVITY
                lower.contains("medium") || lower.contains("substack") || lower.contains("kindle") || lower.contains("books") || lower.contains("news") || lower.contains("feedly") || lower.contains("pocket") || lower.contains("reader") -> READING
                lower.contains("bank") || lower.contains("amazon") || lower.contains("ebay") || lower.contains("pay") || lower.contains("wallet") || lower.contains("crypto") || lower.contains("shop") -> FINANCE
                lower.contains("youtube") || lower.contains("spotify") || lower.contains("netflix") || lower.contains("music") || lower.contains("podcast") || lower.contains("video") -> MEDIA
                else -> GENERAL
            }
        }
    }
}

data class AnswerResult(
    val query: String,
    val answer: String,
    val keyPoints: List<String>,
    val citedMemories: List<MemoryEntity>,
    val generatedAt: Long = System.currentTimeMillis()
)

data class SummaryResult(
    val title: String,
    val timeframe: String,
    val summary: String,
    val keyTakeaways: List<String>,
    val topApps: List<Pair<String, Int>>,
    val memoryCount: Int,
    val wordCount: Int,
    val generatedAt: Long = System.currentTimeMillis()
)

data class PersonalStats(
    val totalMemories: Int = 0,
    val totalWords: Int = 0,
    val uniqueAppsCount: Int = 0,
    val activeDaysCount: Int = 0,
    val starredCount: Int = 0,
    val appDistribution: List<AppUsageStat> = emptyList(),
    val categoryDistribution: List<CategoryStat> = emptyList(),
    val sentimentDistribution: Map<String, Int> = emptyMap(),
    val hourlyActivity: List<Int> = List(24) { 0 },
    val dailyActivity: List<Pair<String, Int>> = emptyList(),
    val keyThemes: List<String> = emptyList(),
    val actionItemsDetected: List<String> = emptyList(),
    val personalInsights: List<String> = emptyList()
)

data class AppUsageStat(
    val appName: String,
    val packageName: String,
    val count: Int,
    val percentage: Float
)

data class CategoryStat(
    val category: String,
    val count: Int,
    val percentage: Float
)
