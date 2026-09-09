package com.gptbot.gptbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.gptbot.gptbot.data.db.AppDatabase
import com.gptbot.gptbot.data.repository.ChatRepository
import com.gptbot.gptbot.data.repository.CodeGenRepository
import com.gptbot.gptbot.ui.screens.ChatBotMainScreen
import com.gptbot.gptbot.ui.screens.CodeGenMainScreen
import com.gptbot.gptbot.ui.theme.SolveFlowTheme
import com.gptbot.gptbot.ui.viewmodel.ChatViewModel
import com.gptbot.gptbot.ui.viewmodel.ChatViewModelFactory
import com.gptbot.gptbot.ui.viewmodel.CodeGenViewModel
import com.gptbot.gptbot.ui.viewmodel.CodeGenViewModelFactory

enum class AppDestination {
    CHATBOT,
    CODE_STUDIO
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)

        // Repositories
        val codeGenRepository = CodeGenRepository(
            languageDao = database.languageDao(),
            snippetDao = database.snippetDao(),
            knowledgeDao = database.knowledgeDao(),
            recordDao = database.generationRecordDao(),
            rlPolicyDao = database.rlPolicyDao()
        )
        val chatRepository = ChatRepository(
            chatDao = database.chatDao(),
            logDao = database.logDao(),
            pdfDao = database.pdfDao(),
            mathDao = database.mathDao()
        )

        // ViewModels
        val codeGenViewModel: CodeGenViewModel by viewModels { CodeGenViewModelFactory(codeGenRepository) }
        val chatViewModel: ChatViewModel by viewModels { ChatViewModelFactory(chatRepository) }

        setContent {
            SolveFlowTheme {
                var currentDestination by remember { mutableStateOf(AppDestination.CHATBOT) }

                when (currentDestination) {
                    AppDestination.CHATBOT -> {
                        ChatBotMainScreen(
                            viewModel = chatViewModel,
                            onNavigateToLegacyCodeGen = {
                                currentDestination = AppDestination.CODE_STUDIO
                            }
                        )
                    }
                    AppDestination.CODE_STUDIO -> {
                        CodeGenMainScreen(
                            viewModel = codeGenViewModel,
                            onNavigateToChat = {
                                currentDestination = AppDestination.CHATBOT
                            }
                        )
                    }
                }
            }
        }
    }
}
