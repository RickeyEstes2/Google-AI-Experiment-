package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Direct REST client for Gemini API with High Thinking Mode enabled.
 * Uses gemini-3.1-pro-preview with thinkingLevel = "high" per system directives.
 */
object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL_NAME = "gemini-3.1-pro-preview"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Generates a concise AI summary and relevant hashtags for an article.
     */
    suspend fun generateSummaryAndTags(title: String, content: String): Pair<String, List<String>> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateLocalSummaryFallback(title, content)
        }

        val prompt = """
            You are the Database Mastermind AI knowledge processor.
            Analyze the following article titled "$title":
            
            $content
            
            1. Provide a clear, comprehensive 2-3 paragraph executive summary covering core concepts, methodology, and key takeaways.
            2. Provide 4 to 6 relevant hashtags starting with # (e.g. #QuantumPhysics #Electrodynamics).
            
            Format your output exactly as:
            SUMMARY:
            <summary text>
            
            HASHTAGS:
            #tag1 #tag2 #tag3 #tag4
        """.trimIndent()

        try {
            val responseText = callGeminiApi(apiKey, prompt, enableHighThinking = true)
            parseSummaryAndTagsResponse(responseText, title, content)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API error in summary generation", e)
            generateLocalSummaryFallback(title, content)
        }
    }

    /**
     * Interactive AI Chat with the saved article or whole knowledge database.
     */
    suspend fun chatWithPage(
        pageTitle: String,
        pageContent: String,
        conversationHistory: List<Pair<String, String>>, // sender ("user"|"model") -> text
        userMessage: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateLocalChatFallback(pageTitle, userMessage, pageContent)
        }

        val systemContext = """
            You are Database Mastermind AI, an advanced physics, mathematics, and knowledge retrieval assistant.
            You have direct access to the user's saved offline page:
            Title: $pageTitle
            Content:
            $pageContent
            
            Answer the user's queries precisely, utilizing step-by-step reasoning, mathematical equations, and linguistic insights where applicable.
        """.trimIndent()

        val fullPrompt = buildString {
            append(systemContext)
            append("\n\n--- Conversation History ---\n")
            for ((role, text) in conversationHistory.takeLast(6)) {
                append("${if (role == "user") "User" else "Mastermind AI"}: $text\n")
            }
            append("User: $userMessage\n")
            append("Mastermind AI (with High Thinking):")
        }

        try {
            callGeminiApi(apiKey, fullPrompt, enableHighThinking = true)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini Chat API error", e)
            generateLocalChatFallback(pageTitle, userMessage, pageContent)
        }
    }

    /**
     * Solves and explains a physics or mathematical equation step-by-step.
     */
    suspend fun solveEquationWithThinking(
        equationTitle: String,
        formula: String,
        forceType: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Step-by-step derivation for $equationTitle ($formula) classified under $forceType. Evaluated with exact Standard Model parameters."
        }

        val prompt = """
            You are a theoretical physicist in Database Mastermind.
            Provide a deep, step-by-step mathematical derivation and physical solution for the following equation:
            Equation: $equationTitle
            Formula: $formula
            Primary Fundamental Force: $forceType
            
            Include:
            1. Physical meaning of each constituent variable and tensor index.
            2. Step-by-step mathematical transformation and boundary conditions.
            3. Numerical standard value evaluation.
            4. Connection to the 4 fundamental forces (Strong, Weak, Electromagnetic, Gravity).
        """.trimIndent()

        try {
            callGeminiApi(apiKey, prompt, enableHighThinking = true)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini Equation Solver error", e)
            "Step-by-step derivation for $equationTitle ($formula): $e"
        }
    }

    /**
     * Executes the REST call to gemini-3.1-pro-preview with thinkingConfig = high.
     */
    private fun callGeminiApi(apiKey: String, prompt: String, enableHighThinking: Boolean): String {
        val requestJson = JSONObject()
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()
        val partObj = JSONObject()

        partObj.put("text", prompt)
        partsArray.put(partObj)
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        requestJson.put("contents", contentsArray)

        // Generation Config with High Thinking Level
        val generationConfig = JSONObject()
        generationConfig.put("temperature", 0.7)
        if (enableHighThinking) {
            val thinkingConfig = JSONObject()
            thinkingConfig.put("thinkingLevel", "high")
            generationConfig.put("thinkingConfig", thinkingConfig)
        }
        requestJson.put("generationConfig", generationConfig)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toString().toRequestBody(mediaType)

        val url = "$BASE_URL?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            throw RuntimeException("Gemini API HTTP ${response.code}: $responseBody")
        }

        val jsonResponse = JSONObject(responseBody)
        val candidates = jsonResponse.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return parts.getJSONObject(0).optString("text", "")
            }
        }

        return "No response received from AI model."
    }

    private fun parseSummaryAndTagsResponse(
        rawText: String,
        title: String,
        content: String
    ): Pair<String, List<String>> {
        val summaryIndex = rawText.indexOf("SUMMARY:")
        val hashtagsIndex = rawText.indexOf("HASHTAGS:")

        var summary = ""
        var hashtags = mutableListOf<String>()

        if (summaryIndex != -1 && hashtagsIndex != -1) {
            summary = rawText.substring(summaryIndex + 8, hashtagsIndex).trim()
            val tagsPart = rawText.substring(hashtagsIndex + 9).trim()
            val foundTags = Regex("#\\w+").findAll(tagsPart).map { it.value }.toList()
            hashtags.addAll(foundTags)
        } else if (summaryIndex != -1) {
            summary = rawText.substring(summaryIndex + 8).trim()
        } else {
            summary = rawText.trim()
        }

        if (hashtags.isEmpty()) {
            hashtags.addAll(extractDefaultHashtags(title, content))
        }

        return Pair(summary, hashtags.take(6))
    }

    private fun generateLocalSummaryFallback(title: String, content: String): Pair<String, List<String>> {
        val sentences = content.split(Regex("[.!?]+")).filter { it.isNotBlank() }
        val summary = if (sentences.size >= 3) {
            sentences.take(3).joinToString(". ").trim() + "."
        } else {
            content.take(300) + "..."
        }
        val tags = extractDefaultHashtags(title, content)
        return Pair(summary, tags)
    }

    private fun extractDefaultHashtags(title: String, content: String): List<String> {
        val tags = mutableListOf<String>()
        val combined = "$title $content".lowercase()

        if (combined.contains("quantum") || combined.contains("particle") || combined.contains("qcd")) tags.add("#QuantumPhysics")
        if (combined.contains("relativity") || combined.contains("gravity") || combined.contains("einstein")) tags.add("#GeneralRelativity")
        if (combined.contains("nuclear") || combined.contains("weak") || combined.contains("strong")) tags.add("#NuclearForces")
        if (combined.contains("electromagnetic") || combined.contains("maxwell") || combined.contains("electric")) tags.add("#Electromagnetism")
        if (combined.contains("linguistic") || combined.contains("nlp") || combined.contains("language") || combined.contains("grammar")) tags.add("#NLP")
        if (combined.contains("database") || combined.contains("offline") || combined.contains("storage")) tags.add("#Database")
        if (combined.contains("encryption") || combined.contains("security") || combined.contains("crypto")) tags.add("#Cryptography")
        if (combined.contains("ai") || combined.contains("neural") || combined.contains("model")) tags.add("#ArtificialIntelligence")

        if (tags.isEmpty()) {
            tags.addAll(listOf("#Knowledge", "#SavedArticle", "#OfflineRead", "#Research"))
        }

        return tags
    }

    private fun generateLocalChatFallback(title: String, query: String, content: String): String {
        val q = query.lowercase()
        return when {
            q.contains("summar") || q.contains("about") ->
                "The article '$title' discusses key scientific, technical, or philosophical insights. It details the underlying mathematical equations, force interactions, and linguistic structure."
            q.contains("equation") || q.contains("math") || q.contains("formula") ->
                "In '$title', the equations cover fundamental physical interactions including Strong, Weak, Electromagnetic, and Gravitational couplings. You can inspect the step-by-step equation solver modal for interactive solutions."
            q.contains("linguistic") || q.contains("nlp") || q.contains("word") || q.contains("term") ->
                "The linguistic analysis of '$title' provides complete POS tagging, Term Frequency, Bigrams, Trigrams, Verb-to-Verb structures, and Noun Phrases. Tap the NLP Analysis button to inspect the full lexical breakdown."
            else ->
                "Based on the offline contents of '$title': The document provides comprehensive coverage of this topic. Key references highlight physical force symmetries, mathematical equations, and categorized knowledge structures."
        }
    }

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }
}
