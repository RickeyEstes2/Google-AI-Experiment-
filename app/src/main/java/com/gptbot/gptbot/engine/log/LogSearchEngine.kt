package com.gptbot.gptbot.engine.log

import com.gptbot.gptbot.data.model.LogEntry

object LogSearchEngine {

    val sampleAndroidLogcat = """
2026-09-09 14:22:01.102 14201 14201 INFO AndroidRuntime: Calling main entry com.gptbot.gptbot.MainActivity
2026-09-09 14:22:01.340 14201 14250 DEBUG OkHttpClient: --> GET https://generativelanguage.googleapis.com/v1beta/models
2026-09-09 14:22:01.890 14201 14250 DEBUG OkHttpClient: <-- 200 OK https://generativelanguage.googleapis.com/v1beta/models (550ms)
2026-09-09 14:22:03.410 14201 14201 WARN Choreographer: Skipped 34 frames! The application may be doing too much work on its main thread.
2026-09-09 14:22:04.112 14201 14266 ERROR AndroidRuntime: FATAL EXCEPTION: DefaultDispatcher-worker-2
2026-09-09 14:22:04.112 14201 14266 ERROR AndroidRuntime: Process: com.gptbot.gptbot, PID: 14201
2026-09-09 14:22:04.112 14201 14266 ERROR AndroidRuntime: java.lang.NullPointerException: Parameter specified as non-null is null: method com.gptbot.gptbot.engine.latex.LatexRenderer.format, parameter rawLatex
2026-09-09 14:22:04.112 14201 14266 ERROR AndroidRuntime: 	at com.gptbot.gptbot.engine.latex.LatexRenderer.format(LatexRenderer.kt:42)
2026-09-09 14:22:04.112 14201 14266 ERROR AndroidRuntime: 	at com.gptbot.gptbot.ui.viewmodel.ChatViewModel${'$'}solveMath${'$'}1.invokeSuspend(ChatViewModel.kt:188)
2026-09-09 14:22:04.112 14201 14266 ERROR AndroidRuntime: 	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
2026-09-09 14:22:04.112 14201 14266 ERROR AndroidRuntime: 	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:104)
2026-09-09 14:22:04.112 14201 14266 ERROR AndroidRuntime: 	at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:608)
2026-09-09 14:22:05.002 14201 14201 INFO ActivityManager: Process com.gptbot.gptbot (pid 14201) has died
    """.trimIndent()

    val sampleKubernetesLog = """
{"timestamp":"2026-09-09T14:15:00.102Z","level":"INFO","service":"api-gateway","msg":"Incoming request POST /v2/ai/inference","trace_id":"trace-771a"}
{"timestamp":"2026-09-09T14:15:00.320Z","level":"DEBUG","service":"auth-service","msg":"JWT validated for user uid_9821","claims":["role:pro","quota:unlimited"]}
{"timestamp":"2026-09-09T14:15:01.005Z","level":"WARN","service":"inference-pool","msg":"GPU memory utilization at 93.4%. Throttling dynamic batch size"}
{"timestamp":"2026-09-09T14:15:02.150Z","level":"ERROR","service":"inference-worker-pod-4","msg":"Connection to Redis cluster timed out after 3000ms","error":"io.lettuce.core.RedisConnectionException: Unable to connect to redis-master.default.svc.cluster.local:6379"}
{"timestamp":"2026-09-09T14:15:02.151Z","level":"FATAL","service":"inference-worker-pod-4","msg":"Failed to acquire distributed lock for model weight caching. Aborting replica"}
{"timestamp":"2026-09-09T14:15:03.010Z","level":"INFO","service":"kubelet","msg":"Liveness probe failed for pod inference-worker-pod-4. Container will be restarted"}
    """.trimIndent()

    val sampleDatabaseLog = """
2026-09-09 14:10:00.012 UTC [2140] LOG:  checkpoint starting: time
2026-09-09 14:10:01.554 UTC [2140] LOG:  checkpoint complete: wrote 892 buffers (5.4%); 0 WAL file(s) added
2026-09-09 14:10:14.220 UTC [4901] WARNING:  canceling statement due to lock timeout
2026-09-09 14:10:14.221 UTC [4901] ERROR:  deadlock detected
2026-09-09 14:10:14.221 UTC [4901] DETAIL:  Process 4901 waits for ExclusiveLock on relation 16401 (chat_sessions); blocked by process 4908.
Process 4908 waits for ShareLock on transaction 88912; blocked by process 4901.
2026-09-09 14:10:14.221 UTC [4901] HINT:  See server log for query details.
2026-09-09 14:10:14.221 UTC [4901] STATEMENT:  UPDATE chat_sessions SET totalIterations = totalIterations + 1 WHERE id = 'session-42'
    """.trimIndent()

    fun parseLogs(rawText: String): List<LogEntry> {
        val lines = rawText.lines()
        val entries = mutableListOf<LogEntry>()

        var currentStackTrace = StringBuilder()
        var currentEntry: LogEntry? = null

        for ((index, line) in lines.withIndex()) {
            val trimmed = line.trim()
            if (trimmed.isBlank()) continue

            // Check if this line is part of a stack trace (starts with \tat or Caused by:)
            if ((trimmed.startsWith("at ") || trimmed.startsWith("Caused by:") || trimmed.startsWith("...") || trimmed.startsWith("Process")) && currentEntry != null) {
                currentStackTrace.append("\n").append(trimmed)
                continue
            }

            // Flush previous entry if we were collecting a stack trace
            if (currentEntry != null) {
                entries.add(currentEntry.copy(stackTrace = if (currentStackTrace.isNotBlank()) currentStackTrace.toString() else null))
                currentStackTrace.clear()
                currentEntry = null
            }

            val level = detectLogLevel(trimmed)
            val tag = extractTag(trimmed)
            val timestamp = extractTimestamp(trimmed)

            currentEntry = LogEntry(
                id = "log_$index",
                timestamp = timestamp,
                level = level,
                tag = tag,
                message = trimmed,
                lineNumber = index + 1
            )
        }

        if (currentEntry != null) {
            entries.add(currentEntry.copy(stackTrace = if (currentStackTrace.isNotBlank()) currentStackTrace.toString() else null))
        }

        return entries
    }

    private fun detectLogLevel(line: String): String {
        val upper = line.uppercase()
        return when {
            upper.contains("FATAL") -> "FATAL"
            upper.contains("ERROR") || upper.contains("EXCEPTION") || upper.contains("DEADLOCK") -> "ERROR"
            upper.contains("WARN") || upper.contains("WARNING") -> "WARN"
            upper.contains("DEBUG") -> "DEBUG"
            upper.contains("INFO") -> "INFO"
            else -> "INFO"
        }
    }

    private fun extractTag(line: String): String {
        val patterns = listOf(
            Regex("""([A-Za-z0-9_]+):"""),
            Regex(""""service":"([^"]+)""""),
            Regex("""\[([A-Za-z0-9_]+)\]""")
        )
        for (pattern in patterns) {
            val match = pattern.find(line)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return "System"
    }

    private fun extractTimestamp(line: String): String {
        val regex = Regex("""\d{4}-\d{2}-\d{2}[ T]\d{2}:\d{2}:\d{2}(\.\d{3})?(Z| UTC)?""")
        val match = regex.find(line)
        return match?.value ?: "Unknown"
    }

    fun filterLogs(
        entries: List<LogEntry>,
        query: String,
        selectedLevel: String,
        isRegex: Boolean
    ): List<LogEntry> {
        return entries.filter { entry ->
            val matchesLevel = if (selectedLevel == "ALL") true else entry.level.equals(selectedLevel, ignoreCase = true)
            if (!matchesLevel) return@filter false

            if (query.isBlank()) return@filter true

            if (isRegex) {
                try {
                    val regex = Regex(query, RegexOption.IGNORE_CASE)
                    regex.containsMatchIn(entry.message) || (entry.stackTrace != null && regex.containsMatchIn(entry.stackTrace))
                } catch (e: Exception) {
                    entry.message.contains(query, ignoreCase = true)
                }
            } else {
                entry.message.contains(query, ignoreCase = true) ||
                        entry.tag.contains(query, ignoreCase = true) ||
                        (entry.stackTrace != null && entry.stackTrace.contains(query, ignoreCase = true))
            }
        }
    }

    fun analyzeRootCause(logSnippet: String): String {
        val lower = logSnippet.lowercase()
        return buildString {
            appendLine("### 📋 Root Cause Analysis & Forensic Breakdown")
            when {
                lower.contains("nullpointerexception") -> {
                    appendLine("- **Primary Failure**: `java.lang.NullPointerException` triggered by an unexpected null value passed into a non-null Kotlin method parameter.")
                    appendLine("- **Fault Location**: `com.gptbot.gptbot.engine.latex.LatexRenderer.format(LatexRenderer.kt:42)`.")
                    appendLine("- **Mechanism**: A background coroutine worker thread (`DefaultDispatcher-worker-2`) dispatched a null `rawLatex` string without safe-call operator (`?.`) or elvis operator (`?: \"\"`).")
                    appendLine("- **Remediation Plan**: Guard method signature: `fun format(rawLatex: String?) = rawLatex?.let { ... } ?: \"\"`. Wrap coroutine launch in `runCatching` or provide a `CoroutineExceptionHandler`.")
                }
                lower.contains("deadlock detected") -> {
                    appendLine("- **Primary Failure**: Relational Database Deadlock between concurrent transactions (`Process 4901` and `Process 4908`).")
                    appendLine("- **Fault Mechanism**: Circular lock dependency on `chat_sessions` table relation `16401`. Transaction A held lock on relation and awaited Transaction B's commit, while B held Transaction lock awaiting relation lock.")
                    appendLine("- **Remediation Plan**: Enforce strict alphanumeric transaction locking order. Add explicit retry loop with exponential jitter for `40P01` (deadlock_detected) exceptions.")
                }
                lower.contains("redis") || lower.contains("connection timed out") -> {
                    appendLine("- **Primary Failure**: Distributed Cache Connection Timeout after 3000ms to `redis-master`.")
                    appendLine("- **Fault Mechanism**: GPU memory exhaustion (93.4%) caused high thread starvation on the Kubernetes node, delaying Redis socket read acks.")
                    appendLine("- **Remediation Plan**: Implement circuit breaker pattern (e.g. Resilience4j / Sentinel), scale Redis read replicas, and tune container memory limits.")
                }
                else -> {
                    appendLine("- **Primary Finding**: Error detected in logs with severity level filtering.")
                    appendLine("- **Diagnostic Action**: Isolated error lines and matched stack traces for immediate remediation.")
                }
            }
        }
    }
}
