package com.example.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.db.AppDatabase
import com.example.data.db.MemoryDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MemoryRepository(private val context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val dao: MemoryDao = database.memoryDao()

    val allMemories: Flow<List<MemoryEntity>> = dao.getAllMemoriesFlow()
    val distinctApps: Flow<List<String>> = dao.getDistinctAppNamesFlow()
    val memoriesCount: Flow<Int> = dao.getMemoriesCountFlow()

    suspend fun insertMemory(
        text: String,
        appName: String = "Notes",
        packageName: String = "",
        appCategory: String = "",
        title: String = "",
        tags: List<String> = emptyList(),
        sourceType: String = "MANUAL",
        sentiment: String = "",
        timestamp: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        val trimmedText = text.trim()
        if (trimmedText.isBlank()) return@withContext -1L

        val words = trimmedText.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val resolvedCategory = if (appCategory.isNotBlank()) appCategory else AppCategory.fromPackageOrName(packageName, appName).displayName
        val resolvedSentiment = if (sentiment.isNotBlank()) sentiment else detectSentiment(trimmedText)
        val extractedTags = if (tags.isNotEmpty()) tags else extractTagsFromText(trimmedText)
        val resolvedTitle = if (title.isNotBlank()) title else generateTitleSnippet(trimmedText, appName)

        val memory = MemoryEntity(
            text = trimmedText,
            appName = appName.ifBlank { "System" },
            packageName = packageName,
            appCategory = resolvedCategory,
            title = resolvedTitle,
            tags = extractedTags,
            timestamp = timestamp,
            sourceType = sourceType,
            sentiment = resolvedSentiment,
            wordCount = words,
            isStarred = false
        )
        dao.insertMemory(memory)
    }

    suspend fun updateMemory(memory: MemoryEntity) = withContext(Dispatchers.IO) {
        val words = memory.text.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        dao.updateMemory(memory.copy(wordCount = words))
    }

    suspend fun toggleStar(memory: MemoryEntity) = withContext(Dispatchers.IO) {
        dao.updateMemory(memory.copy(isStarred = !memory.isStarred))
    }

    suspend fun addAddendum(memoryId: Long, content: String): Boolean = withContext(Dispatchers.IO) {
        val existing = dao.getMemoryById(memoryId) ?: return@withContext false
        val newAddendum = Addendum(content = content.trim())
        dao.updateMemory(existing.copy(addendums = existing.addendums + newAddendum))
        true
    }

    suspend fun removeAddendum(memoryId: Long, addendumId: String): Boolean = withContext(Dispatchers.IO) {
        val existing = dao.getMemoryById(memoryId) ?: return@withContext false
        dao.updateMemory(existing.copy(addendums = existing.addendums.filterNot { it.id == addendumId }))
        true
    }

    suspend fun deleteMemory(memory: MemoryEntity) = withContext(Dispatchers.IO) {
        dao.deleteMemory(memory)
    }

    suspend fun deleteMemoryById(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteMemoryById(id)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        dao.clearAllMemories()
    }

    suspend fun getMemoryById(id: Long): MemoryEntity? = withContext(Dispatchers.IO) {
        dao.getMemoryById(id)
    }

    // -------------------------------------------------------------
    // CSV EXPORT ENGINE (RFC-4180 Compliant)
    // -------------------------------------------------------------
    suspend fun exportMemoriesToCsv(): Pair<File, Uri>? = withContext(Dispatchers.IO) {
        try {
            val memories = dao.getAllMemoriesList()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)

            val stringBuilder = StringBuilder()
            // CSV Header
            stringBuilder.append("ID,Timestamp_Formatted,Timestamp_ISO,Timestamp_Epoch,App_Name,Package_Name,Category,Title,Source_Type,Sentiment,Word_Count,Is_Starred,Tags,Addendums_Count,Text_Content,Addendums_Content\n")

            for (m in memories) {
                val formattedDate = dateFormat.format(Date(m.timestamp))
                val isoDate = isoFormat.format(Date(m.timestamp))
                val tagsStr = m.tags.joinToString("; ")
                val addendumsStr = m.addendums.joinToString(" | ") { "[${dateFormat.format(Date(it.timestamp))}] ${it.content}" }

                stringBuilder.append(escapeCsv(m.id.toString())).append(",")
                stringBuilder.append(escapeCsv(formattedDate)).append(",")
                stringBuilder.append(escapeCsv(isoDate)).append(",")
                stringBuilder.append(escapeCsv(m.timestamp.toString())).append(",")
                stringBuilder.append(escapeCsv(m.appName)).append(",")
                stringBuilder.append(escapeCsv(m.packageName)).append(",")
                stringBuilder.append(escapeCsv(m.appCategory)).append(",")
                stringBuilder.append(escapeCsv(m.title)).append(",")
                stringBuilder.append(escapeCsv(m.sourceType)).append(",")
                stringBuilder.append(escapeCsv(m.sentiment)).append(",")
                stringBuilder.append(escapeCsv(m.wordCount.toString())).append(",")
                stringBuilder.append(escapeCsv(if (m.isStarred) "TRUE" else "FALSE")).append(",")
                stringBuilder.append(escapeCsv(tagsStr)).append(",")
                stringBuilder.append(escapeCsv(m.addendums.size.toString())).append(",")
                stringBuilder.append(escapeCsv(m.text)).append(",")
                stringBuilder.append(escapeCsv(addendumsStr)).append("\n")
            }

            val exportFileName = "crossapp_memories_export_${System.currentTimeMillis()}.csv"
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()
            val file = File(exportDir, exportFileName)

            FileOutputStream(file).use { out ->
                out.write(stringBuilder.toString().toByteArray(Charsets.UTF_8))
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            return@withContext Pair(file, uri)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    suspend fun generateCsvString(): String = withContext(Dispatchers.IO) {
        val memories = dao.getAllMemoriesList()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val sb = StringBuilder()
        sb.append("ID,Timestamp_Formatted,App_Name,Category,Title,Source_Type,Sentiment,Tags,Text_Content\n")
        for (m in memories) {
            sb.append(escapeCsv(m.id.toString())).append(",")
            sb.append(escapeCsv(dateFormat.format(Date(m.timestamp)))).append(",")
            sb.append(escapeCsv(m.appName)).append(",")
            sb.append(escapeCsv(m.appCategory)).append(",")
            sb.append(escapeCsv(m.title)).append(",")
            sb.append(escapeCsv(m.sourceType)).append(",")
            sb.append(escapeCsv(m.sentiment)).append(",")
            sb.append(escapeCsv(m.tags.joinToString("; "))).append(",")
            sb.append(escapeCsv(m.text)).append("\n")
        }
        sb.toString()
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuotes) "\"$escaped\"" else escaped
    }

    // -------------------------------------------------------------
    // Q&A / ANSWERS ENGINE
    // -------------------------------------------------------------
    suspend fun answerQuestion(query: String): AnswerResult = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        val all = dao.getAllMemoriesList()
        if (all.isEmpty()) {
            return@withContext AnswerResult(
                query = trimmedQuery,
                answer = "You haven't recorded any memories yet. Start browsing or chatting in your apps, or tap 'Quick Capture' to record your first memory.",
                keyPoints = emptyList(),
                citedMemories = emptyList()
            )
        }

        val queryTokens = trimmedQuery.lowercase().split(Regex("[^a-zA-Z0-9]+")).filter { it.length > 2 }
        
        // Score memories based on keyword matching, phrase matching, recency, category
        val scoredMemories = all.map { memory ->
            val contentLower = (memory.text + " " + memory.title + " " + memory.appName + " " + memory.tags.joinToString(" ")).lowercase()
            var score = 0

            for (token in queryTokens) {
                if (contentLower.contains(token)) {
                    score += 10
                    // Bonus for exact word boundary
                    if (contentLower.contains("\\b$token\\b".toRegex())) {
                        score += 5
                    }
                }
            }

            if (trimmedQuery.length > 4 && contentLower.contains(trimmedQuery.lowercase())) {
                score += 30
            }

            // Intent specific bonus
            if (trimmedQuery.contains("action", ignoreCase = true) || trimmedQuery.contains("todo", ignoreCase = true) || trimmedQuery.contains("task", ignoreCase = true)) {
                if (memory.sentiment == "ACTION_ITEM" || memory.text.contains("need to", ignoreCase = true) || memory.text.contains("remember to", ignoreCase = true)) {
                    score += 25
                }
            }
            if (trimmedQuery.contains("link", ignoreCase = true) || trimmedQuery.contains("url", ignoreCase = true) || trimmedQuery.contains("website", ignoreCase = true)) {
                if (memory.text.contains("http://") || memory.text.contains("https://") || memory.appCategory == "Browsing & Web") {
                    score += 20
                }
            }

            Pair(memory, score)
        }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(5)
            .map { it.first }

        if (scoredMemories.isEmpty()) {
            // General summary fallback if query didn't match specific keywords
            return@withContext AnswerResult(
                query = trimmedQuery,
                answer = "No exact memory matches were found for \"$trimmedQuery\". You have ${all.size} stored memories across ${all.map { it.appName }.distinct().size} apps. Try searching for specific names, topics, keywords, or app names.",
                keyPoints = listOf("Total memories: ${all.size}", "Top apps: ${all.groupBy { it.appName }.keys.take(3).joinToString(", ")}"),
                citedMemories = all.take(3)
            )
        }

        // Synthesize structured answer
        val keyPoints = mutableListOf<String>()
        val answerBuilder = StringBuilder()

        answerBuilder.append("Based on ${scoredMemories.size} relevant memories captured from your apps:\n\n")

        scoredMemories.forEachIndexed { index, mem ->
            val firstLine = mem.text.lines().firstOrNull { it.isNotBlank() } ?: mem.title
            val snippet = if (firstLine.length > 100) firstLine.take(97) + "..." else firstLine
            keyPoints.add("${mem.appName}: $snippet")
            answerBuilder.append("• [${mem.appName}] $snippet\n")
        }

        AnswerResult(
            query = trimmedQuery,
            answer = answerBuilder.toString().trim(),
            keyPoints = keyPoints,
            citedMemories = scoredMemories
        )
    }

    // -------------------------------------------------------------
    // SUMMARIES ENGINE
    // -------------------------------------------------------------
    suspend fun generateSummary(timeframe: String, appFilter: String? = null, categoryFilter: String? = null): SummaryResult = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val fromTime = when (timeframe) {
            "Today" -> now - 24 * 60 * 60 * 1000L
            "Past 7 Days" -> now - 7 * 24 * 60 * 60 * 1000L
            "Past 30 Days" -> now - 30 * 24 * 60 * 60 * 1000L
            else -> 0L // All time
        }

        val all = dao.getAllMemoriesList().filter { m ->
            m.timestamp >= fromTime &&
            (appFilter == null || m.appName.equals(appFilter, ignoreCase = true)) &&
            (categoryFilter == null || m.appCategory.equals(categoryFilter, ignoreCase = true))
        }

        if (all.isEmpty()) {
            return@withContext SummaryResult(
                title = "Summary for $timeframe",
                timeframe = timeframe,
                summary = "No memories recorded during this period.",
                keyTakeaways = listOf("No activity logged yet."),
                topApps = emptyList(),
                memoryCount = 0,
                wordCount = 0
            )
        }

        val totalWords = all.sumOf { it.wordCount }
        val appCounts = all.groupBy { it.appName }.mapValues { it.value.size }.toList().sortedByDescending { it.second }
        val categoryCounts = all.groupBy { it.appCategory }.mapValues { it.value.size }
        val actionItems = all.filter { it.sentiment == "ACTION_ITEM" || it.text.contains("todo", true) || it.text.contains("need to", true) }

        val takeaways = mutableListOf<String>()
        takeaways.add("Logged ${all.size} distinct interactions across ${appCounts.size} apps ($totalWords words total).")
        if (appCounts.isNotEmpty()) {
            takeaways.add("Primary focus was in ${appCounts.first().first} (${appCounts.first().second} entries).")
        }
        if (actionItems.isNotEmpty()) {
            takeaways.add("Identified ${actionItems.size} potential action items and tasks.")
        }

        val summaryText = buildString {
            append("During $timeframe, your phone memory captured ${all.size} events across ${appCounts.size} applications. ")
            append("The most prominent app was ${appCounts.firstOrNull()?.first ?: "General"} with ${appCounts.firstOrNull()?.second ?: 0} memories. ")
            if (categoryCounts.isNotEmpty()) {
                val topCat = categoryCounts.maxByOrNull { it.value }
                append("Your dominant activity was ${topCat?.key ?: "General"} (${topCat?.value ?: 0} logs). ")
            }
            if (actionItems.isNotEmpty()) {
                append("\n\nDetected Action Items:\n")
                actionItems.take(4).forEach { item ->
                    append("• [${item.appName}] ${item.title.ifBlank { item.text.take(60) }}\n")
                }
            }
        }

        SummaryResult(
            title = "Summary for $timeframe",
            timeframe = timeframe,
            summary = summaryText,
            keyTakeaways = takeaways,
            topApps = appCounts,
            memoryCount = all.size,
            wordCount = totalWords
        )
    }

    // -------------------------------------------------------------
    // STATS & PERSONAL INSIGHTS ENGINE
    // -------------------------------------------------------------
    suspend fun getPersonalStats(): PersonalStats = withContext(Dispatchers.IO) {
        val all = dao.getAllMemoriesList()
        if (all.isEmpty()) return@withContext PersonalStats()

        val totalMemories = all.size
        val totalWords = all.sumOf { it.wordCount }
        val starredCount = all.count { it.isStarred }

        // Unique apps
        val appGroup = all.groupBy { it.appName }
        val appStats = appGroup.map { (name, list) ->
            AppUsageStat(
                appName = name,
                packageName = list.firstOrNull()?.packageName ?: "",
                count = list.size,
                percentage = (list.size.toFloat() / totalMemories.toFloat()) * 100f
            )
        }.sortedByDescending { it.count }

        // Category stats
        val catGroup = all.groupBy { it.appCategory }
        val catStats = catGroup.map { (cat, list) ->
            CategoryStat(
                category = cat,
                count = list.size,
                percentage = (list.size.toFloat() / totalMemories.toFloat()) * 100f
            )
        }.sortedByDescending { it.count }

        // Sentiment
        val sentimentMap = all.groupBy { it.sentiment }.mapValues { it.value.size }

        // Hourly distribution (0-23)
        val hourly = IntArray(24)
        val cal = Calendar.getInstance()
        all.forEach {
            cal.timeInMillis = it.timestamp
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            hourly[hour]++
        }

        // Active days
        val dayFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val daysGroup = all.groupBy { dayFormat.format(Date(it.timestamp)) }
        val activeDaysCount = daysGroup.keys.size
        val dailyActivity = daysGroup.map { Pair(it.key, it.value.size) }.takeLast(7)

        // Key themes extraction (frequent non-stop words)
        val stopWords = setOf("the", "and", "this", "that", "with", "from", "for", "are", "was", "were", "you", "your", "have", "has", "had", "will", "would", "about", "what", "when", "where", "which", "there", "their", "they", "been", "some", "more", "also", "into", "just", "like", "time", "than", "them", "then", "its", "our")
        val wordFreq = mutableMapOf<String, Int>()
        all.forEach { mem ->
            mem.text.lowercase().split(Regex("[^a-zA-Z0-9]+")).forEach { word ->
                if (word.length > 3 && !stopWords.contains(word)) {
                    wordFreq[word] = (wordFreq[word] ?: 0) + 1
                }
            }
        }
        val keyThemes = wordFreq.entries.sortedByDescending { it.value }.take(10).map { "#${it.key} (${it.value})" }

        // Action items
        val actionItems = all.filter { it.sentiment == "ACTION_ITEM" || it.text.contains("todo", true) || it.text.contains("need to", true) }
            .map { "[${it.appName}] ${it.text.take(80)}" }
            .take(6)

        // Personal insights synthesis
        val insights = mutableListOf<String>()
        val peakHour = hourly.indices.maxByOrNull { hourly[it] } ?: 14
        val peakHourFormatted = if (peakHour == 0) "12 AM" else if (peakHour < 12) "$peakHour AM" else if (peakHour == 12) "12 PM" else "${peakHour - 12} PM"
        insights.add("⏰ Peak Information Flow: You capture most text around $peakHourFormatted.")

        val topApp = appStats.firstOrNull()
        if (topApp != null) {
            insights.add("📱 Highest Focus App: ${topApp.appName} represents ${String.format("%.1f", topApp.percentage)}% of all remembered content.")
        }

        val topCat = catStats.firstOrNull()
        if (topCat != null) {
            insights.add("🎯 Domain Concentration: ${topCat.category} is your most active digital context.")
        }

        if (starredCount > 0) {
            insights.add("⭐ High-Value Notes: You have flagged $starredCount key memories for immediate reference.")
        }

        PersonalStats(
            totalMemories = totalMemories,
            totalWords = totalWords,
            uniqueAppsCount = appGroup.size,
            activeDaysCount = activeDaysCount,
            starredCount = starredCount,
            appDistribution = appStats,
            categoryDistribution = catStats,
            sentimentDistribution = sentimentMap,
            hourlyActivity = hourly.toList(),
            dailyActivity = dailyActivity,
            keyThemes = keyThemes,
            actionItemsDetected = actionItems,
            personalInsights = insights
        )
    }

    // -------------------------------------------------------------
    // SAMPLE DEMO DATA FOR IMMEDIATE INTERACTIVE TESTING
    // -------------------------------------------------------------
    suspend fun populateSampleMemories() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val samples = listOf(
            MemoryEntity(
                text = "Reviewed the Q3 Roadmap presentation with design team. Main priorities are offline sync, faster vector indexing, and export to CSV functionality. Action: Follow up with Alex on Wednesday by 2:00 PM.",
                appName = "Slack",
                packageName = "com.Slack",
                appCategory = "Chats & Messages",
                title = "Q3 Roadmap Priorities & Sync Review",
                tags = listOf("work", "roadmap", "sync", "meeting"),
                timestamp = now - 35 * 60 * 1000L,
                sourceType = "ACCESSIBILITY",
                sentiment = "ACTION_ITEM",
                wordCount = 33,
                isStarred = true
            ),
            MemoryEntity(
                text = "Article: 'Why Local-First Software is the Future of Personal Computing'. Highlighting the key thesis: users should own their memory, database should live on-device with zero cloud lock-in, and instant zero-latency retrieval is mandatory.",
                appName = "Google Chrome",
                packageName = "com.android.chrome",
                appCategory = "Browsing & Web",
                title = "Why Local-First Software is the Future",
                tags = listOf("tech", "local-first", "reading", "architecture"),
                timestamp = now - 2 * 3600 * 1000L,
                sourceType = "ACCESSIBILITY",
                sentiment = "IMPORTANT",
                wordCount = 34,
                isStarred = true
            ),
            MemoryEntity(
                text = "Hey! Let's book table at Osteria Morini for Sarah's birthday dinner on Friday at 7:30 PM. Address is 218 Lafayette St. Let me know if that time works for everyone!",
                appName = "WhatsApp",
                packageName = "com.whatsapp",
                appCategory = "Chats & Messages",
                title = "Sarah's Birthday Dinner at Osteria Morini",
                tags = listOf("personal", "dinner", "birthday", "reservations"),
                timestamp = now - 4 * 3600 * 1000L,
                sourceType = "NOTIFICATION",
                sentiment = "POSITIVE",
                wordCount = 28,
                isStarred = false
            ),
            MemoryEntity(
                text = "Key math formula for data clustering:\nCosine Similarity: \$\\$\\text{sim}(A, B) = \\frac{A \\cdot B}{\\|A\\| \\|B\\|}\$\$\nEuclidean Distance: \$\$d(p, q) = \\sqrt{\\sum_{i=1}^n (p_i - q_i)^2}\$\$",
                appName = "Obsidian",
                packageName = "md.obsidian",
                appCategory = "Productivity & Work",
                title = "Vector Math & Similarity Metrics",
                tags = listOf("math", "vectors", "formulas", "study"),
                timestamp = now - 9 * 3600 * 1000L,
                sourceType = "PROCESS_TEXT",
                sentiment = "IDEA",
                wordCount = 24,
                isStarred = true
            ),
            MemoryEntity(
                text = "Substack Newsletter: 'The Cognitive Revolution'. Machine learning models on mobile are becoming capable of running 3B-7B parameter reasoning completely locally with under 500MB RAM footprint using NPU quantization.",
                appName = "Substack",
                packageName = "com.substack.app",
                appCategory = "Reading & Articles",
                title = "On-Device AI & NPU Quantization Trends",
                tags = listOf("ai", "npu", "mobile", "newsletter"),
                timestamp = now - 26 * 3600 * 1000L,
                sourceType = "SHARE_INTENT",
                sentiment = "IMPORTANT",
                wordCount = 29,
                isStarred = false
            ),
            MemoryEntity(
                text = "Order confirmation #94821 for mechanical keyboard switches and USB-C braided cable. Expected delivery Tuesday August 30th via Priority Courier.",
                appName = "Amazon",
                packageName = "com.amazon.mShop.android.shopping",
                appCategory = "Finance & Shopping",
                title = "Order #94821 Keyboard Switches Confirmation",
                tags = listOf("shopping", "receipt", "delivery"),
                timestamp = now - 48 * 3600 * 1000L,
                sourceType = "NOTIFICATION",
                sentiment = "NEUTRAL",
                wordCount = 20,
                isStarred = false
            ),
            MemoryEntity(
                text = "Doctor appointment scheduled for annual checkup on September 15 at 10:00 AM. Location: Medical Center Suite 402. Bring insurance card and vaccination record.",
                appName = "Google Keep",
                packageName = "com.google.android.keep",
                appCategory = "Productivity & Work",
                title = "Annual Checkup Appointment Details",
                tags = listOf("health", "appointment", "calendar"),
                timestamp = now - 72 * 3600 * 1000L,
                sourceType = "MANUAL",
                sentiment = "IMPORTANT",
                wordCount = 23,
                isStarred = true
            )
        )
        dao.insertAll(samples)
    }

    private fun detectSentiment(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("todo") || lower.contains("action:") || lower.contains("need to") || lower.contains("remember to") || lower.contains("follow up") || lower.contains("deadline") -> "ACTION_ITEM"
            lower.contains("important") || lower.contains("urgent") || lower.contains("critical") || lower.contains("key") || lower.contains("highlight") -> "IMPORTANT"
            lower.contains("?") || lower.contains("how to") || lower.contains("why") || lower.contains("where is") -> "QUESTION"
            lower.contains("idea") || lower.contains("concept") || lower.contains("theory") || lower.contains("formula") -> "IDEA"
            lower.contains("great") || lower.contains("love") || lower.contains("congrats") || lower.contains("awesome") || lower.contains("birthday") -> "POSITIVE"
            else -> "NEUTRAL"
        }
    }

    private fun extractTagsFromText(text: String): List<String> {
        val tags = mutableListOf<String>()
        val hashtagMatches = Regex("#([a-zA-Z0-9_]+)").findAll(text)
        for (m in hashtagMatches) {
            tags.add(m.groupValues[1].lowercase())
        }
        return tags.distinct()
    }

    private fun generateTitleSnippet(text: String, appName: String): String {
        val firstLine = text.lines().firstOrNull { it.isNotBlank() } ?: "$appName Note"
        return if (firstLine.length > 50) firstLine.take(47) + "..." else firstLine
    }
}
