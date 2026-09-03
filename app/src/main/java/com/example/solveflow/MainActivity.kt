package com.example.solveflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.solveflow.data.db.AppDatabase
import com.example.solveflow.data.repository.CodeGenRepository
import com.example.solveflow.ui.screens.CodeGenMainScreen
import com.example.solveflow.ui.theme.SolveFlowTheme
import com.example.solveflow.ui.viewmodel.CodeGenViewModel
import com.example.solveflow.ui.viewmodel.CodeGenViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = CodeGenRepository(
            languageDao = database.languageDao(),
            snippetDao = database.snippetDao(),
            knowledgeDao = database.knowledgeDao(),
            recordDao = database.generationRecordDao(),
            rlPolicyDao = database.rlPolicyDao()
        )
        val viewModelFactory = CodeGenViewModelFactory(repository)
        val viewModel: CodeGenViewModel by viewModels { viewModelFactory }

        setContent {
            SolveFlowTheme {
                CodeGenMainScreen(viewModel = viewModel)
            }
        }
    }
}
