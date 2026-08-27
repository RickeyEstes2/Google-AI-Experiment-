package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.data.model.AppCategory
import com.example.data.repository.MemoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MemoryAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repository: MemoryRepository
    private var lastRecordedText: String = ""
    private var lastRecordedTime: Long = 0

    override fun onCreate() {
        super.onCreate()
        repository = MemoryRepository(applicationContext)
        _isRunning.value = true
    }

    override fun onDestroy() {
        super.onDestroy()
        _isRunning.value = false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkgName = event.packageName?.toString() ?: return
        // Ignore our own package
        if (pkgName == packageName) return
        // Ignore common system overlays
        if (pkgName.startsWith("com.android.systemui") || pkgName.contains("inputmethod")) return

        // Security check: Ignore password fields
        if (event.isPassword) return

        val eventType = event.eventType
        if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED ||
            eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            eventType == AccessibilityEvent.TYPE_VIEW_CLICKED ||
            eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED ||
            eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED
        ) {
            val rootNode = rootInActiveWindow ?: return
            if (rootNode.isPassword) return

            val extracted = extractTextFromNode(rootNode)
            if (extracted.length > 20) {
                processAndSaveText(extracted, pkgName)
            }
        }
    }

    private fun extractTextFromNode(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""
        if (node.isPassword) return ""

        val sb = StringBuilder()
        val text = node.text?.toString()?.trim()
        val contentDesc = node.contentDescription?.toString()?.trim()

        if (!text.isNullOrBlank() && text.length > 5 && !isSystemNoise(text)) {
            sb.append(text).append("\n")
        } else if (!contentDesc.isNullOrBlank() && contentDesc.length > 8 && !isSystemNoise(contentDesc)) {
            sb.append(contentDesc).append("\n")
        }

        val childCount = node.childCount
        for (i in 0 until childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val childText = extractTextFromNode(child)
                if (childText.isNotBlank()) {
                    sb.append(childText).append("\n")
                }
            }
        }

        return sb.toString().trim()
    }

    private fun isSystemNoise(text: String): Boolean {
        val lower = text.lowercase()
        return lower in setOf("back", "home", "recent apps", "close", "ok", "cancel", "search", "menu", "navigate up", "done", "more options")
    }

    private fun processAndSaveText(text: String, pkgName: String) {
        val now = System.currentTimeMillis()
        val cleaned = text.lines().filter { it.isNotBlank() }.distinct().joinToString("\n")
        if (cleaned.length < 15) return

        // Deduplication within 4 seconds or identical text
        if (cleaned == lastRecordedText && (now - lastRecordedTime) < 15000L) {
            return
        }
        if ((now - lastRecordedTime) < 3000L && cleaned.contains(lastRecordedText)) {
            return
        }

        lastRecordedText = cleaned
        lastRecordedTime = now

        val appName = getAppLabel(applicationContext, pkgName)
        val category = AppCategory.fromPackageOrName(pkgName, appName).displayName

        serviceScope.launch {
            repository.insertMemory(
                text = cleaned,
                appName = appName,
                packageName = pkgName,
                appCategory = category,
                title = generateTitle(cleaned, appName),
                sourceType = "ACCESSIBILITY",
                timestamp = now
            )
        }
    }

    private fun generateTitle(text: String, appName: String): String {
        val firstLine = text.lines().firstOrNull { it.isNotBlank() } ?: "$appName Capture"
        return if (firstLine.length > 60) firstLine.take(57) + "..." else firstLine
    }

    private fun getAppLabel(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (_: Exception) {
            packageName.substringAfterLast(".").replaceFirstChar { it.uppercase() }
        }
    }

    override fun onInterrupt() {}

    companion object {
        private val _isRunning = MutableStateFlow(false)
        val isRunning = _isRunning.asStateFlow()
    }
}
