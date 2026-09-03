package com.example.solveflow.engine.generator

import android.util.Log
import com.example.solveflow.data.model.ProgrammingLanguage
import com.example.solveflow.data.model.RlStrategy
import com.example.solveflow.engine.dbscan.DataPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@Serializable
private data class GeminiApiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

@Serializable
private data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
private data class GeminiPart(
    val text: String
)

@Serializable
private data class GeminiApiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@Serializable
private data class GeminiCandidate(
    val content: GeminiContent? = null
)

object GeminiCodeService {
    private const val TAG = "GeminiCodeService"
    private val jsonParser = Json { ignoreUnknownKeys = true }

    suspend fun generateWithGemini(
        apiKey: String,
        prompt: String,
        language: ProgrammingLanguage,
        strategy: RlStrategy,
        exemplars: List<DataPoint>
    ): Pair<String, String>? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext null

        val exemplarContext = if (exemplars.isNotEmpty()) {
            "Relevant reference patterns retrieved from dense semantic cluster:\n" +
                    exemplars.joinToString("\n\n") { "--- ${it.title} ---\n${it.textContent}" }
        } else {
            ""
        }

        val systemPrompt = """
You are an expert multi-language code generator and software architect.
Target Language: ${language.name} (${language.extension})
Reinforcement Learning Strategy: ${strategy.title} - ${strategy.description}

Rules:
1. Generate complete, syntactically correct, production-grade code adhering to the specified RL Strategy.
2. Provide a clear, structured explanation with:
   - Architecture & Design Overview
   - Step-by-Step Logic Breakdown
   - Time & Space Complexity Analysis
   - Error Mitigation & Robustness Highlights
3. Format your response strictly with two markers:
<<<CODE_START>>>
[Your generated code here]
<<<CODE_END>>>
<<<EXPLANATION_START>>>
[Your explanation here]
<<<EXPLANATION_END>>>
        """.trimIndent()

        val userPrompt = buildString {
            append("Task: ").append(prompt).append("\n\n")
            if (exemplarContext.isNotBlank()) {
                append(exemplarContext).append("\n\n")
            }
            append("Generate the solution in ").append(language.name).append(" using ").append(strategy.title).append(" style.")
        }

        try {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 30000
                readTimeout = 30000
                doOutput = true
                doInput = true
            }

            val requestObj = GeminiApiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = userPrompt)))
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemPrompt))
                )
            )

            val requestBody = jsonParser.encodeToString(GeminiApiRequest.serializer(), requestObj)
            OutputStreamWriter(conn.outputStream).use { it.write(requestBody) }

            val responseCode = conn.responseCode
            if (responseCode == 200) {
                val rawResponse = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val parsed = jsonParser.decodeFromString(GeminiApiResponse.serializer(), rawResponse)
                val fullText = parsed.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""

                // Extract code and explanation
                val code = if (fullText.contains("<<<CODE_START>>>") && fullText.contains("<<<CODE_END>>>")) {
                    fullText.substringAfter("<<<CODE_START>>>").substringBefore("<<<CODE_END>>>").trim()
                } else if (fullText.contains("```")) {
                    fullText.substringAfter("```").substringAfter("\n").substringBefore("```").trim()
                } else {
                    fullText
                }

                val explanation = if (fullText.contains("<<<EXPLANATION_START>>>") && fullText.contains("<<<EXPLANATION_END>>>")) {
                    fullText.substringAfter("<<<EXPLANATION_START>>>").substringBefore("<<<EXPLANATION_END>>>").trim()
                } else {
                    "Generated via Gemini 3.5 Flash with ${strategy.title} strategy."
                }

                Pair(code, explanation)
            } else {
                Log.w(TAG, "Gemini API failed with code $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating with Gemini", e)
            null
        }
    }
}
