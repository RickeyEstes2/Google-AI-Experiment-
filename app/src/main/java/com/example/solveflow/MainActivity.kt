package com.example.solveflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.example.solveflow.data.db.AppDatabase
import com.example.solveflow.data.repository.ChatRepository
import com.example.solveflow.data.repository.CodeGenRepository
import com.example.solveflow.ui.screens.ChatBotMainScreen
import com.example.solveflow.ui.screens.CodeGenMainScreen
import com.example.solveflow.ui.theme.SolveFlowTheme
import com.example.solveflow.ui.viewmodel.ChatViewModel
import com.example.solveflow.ui.viewmodel.ChatViewModelFactory
import com.example.solveflow.ui.viewmodel.CodeGenViewModel
import com.example.solveflow.ui.viewmodel.CodeGenViewModelFactory

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
