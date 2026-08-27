package com.example.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.data.model.AppCategory
import com.example.data.repository.MemoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MemoryNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repository: MemoryRepository

    override fun onCreate() {
        super.onCreate()
        repository = MemoryRepository(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val pkgName = sbn.packageName ?: return
        if (pkgName == packageName) return

        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim() ?: ""

        val combinedContent = buildString {
            if (title.isNotBlank()) append(title).append(": ")
            if (text.isNotBlank()) append(text)
            if (subText.isNotBlank()) append(" (").append(subText).append(")")
        }.trim()

        if (combinedContent.length > 10) {
            val appLabel = try {
                val pm = packageManager
                val info = pm.getApplicationInfo(pkgName, 0)
                pm.getApplicationLabel(info).toString()
            } catch (_: Exception) {
                pkgName.substringAfterLast(".").replaceFirstChar { it.uppercase() }
            }

            val category = AppCategory.fromPackageOrName(pkgName, appLabel).displayName

            serviceScope.launch {
                repository.insertMemory(
                    text = combinedContent,
                    appName = appLabel,
                    packageName = pkgName,
                    appCategory = category,
                    title = title.ifBlank { "$appLabel Notification" },
                    sourceType = "NOTIFICATION",
                    timestamp = sbn.postTime
                )
            }
        }
    }
}
