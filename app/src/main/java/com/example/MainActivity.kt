package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.MastermindMainScreen
import com.example.ui.theme.DatabaseMastermindTheme
import com.example.ui.viewmodel.MastermindViewModel
import com.example.util.ShareIntentParser

class MainActivity : ComponentActivity() {

    private val viewModel: MastermindViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleShareIntent(intent)

        setContent {
            DatabaseMastermindTheme {
                MastermindMainScreen(viewModel = viewModel)
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
                notes = sharedData.notes
            )
        }
    }
}
