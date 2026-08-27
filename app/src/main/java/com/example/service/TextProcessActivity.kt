package com.example.service

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.data.model.AppCategory
import com.example.data.repository.MemoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TextProcessActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val selectedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()?.trim() ?: ""
        if (selectedText.isNotBlank()) {
            val repository = MemoryRepository(applicationContext)
            val callingPkg = callingPackage ?: "External App"
            val appLabel = try {
                val pm = packageManager
                val info = pm.getApplicationInfo(callingPkg, 0)
                pm.getApplicationLabel(info).toString()
            } catch (_: Exception) {
                "Selected Text"
            }

            val category = AppCategory.fromPackageOrName(callingPkg, appLabel).displayName

            CoroutineScope(Dispatchers.IO).launch {
                repository.insertMemory(
                    text = selectedText,
                    appName = appLabel,
                    packageName = callingPkg,
                    appCategory = category,
                    title = "Highlight: " + if (selectedText.length > 40) selectedText.take(37) + "..." else selectedText,
                    sourceType = "PROCESS_TEXT",
                    timestamp = System.currentTimeMillis()
                )
            }

            Toast.makeText(this, "✨ Saved to CrossApp Memory!", Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}
