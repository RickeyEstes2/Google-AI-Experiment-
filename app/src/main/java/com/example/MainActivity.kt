package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.MainMemoryScreen
import com.example.ui.theme.DatabaseMastermindTheme
import com.example.ui.viewmodel.MemoryViewModel
import com.example.util.ShareIntentParser

class MainActivity : ComponentActivity() {

    private val viewModel: MemoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleShareIntent(intent)

        setContent {
            DatabaseMastermindTheme {
                MainMemoryScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        val sharedData = ShareIntentParser.parse(intent)
        if (sharedData != null) {
            viewModel.handleIncomingShare(
                url = sharedData.url,
                title = sharedData.title,
                text = sharedData.notes
            )
        }
    }
}
