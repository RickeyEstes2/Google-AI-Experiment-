package com.example.solveflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.solveflow.data.db.AppDatabase
import com.example.solveflow.data.repository.FlowchartRepository
import com.example.solveflow.ui.screens.MainScreen
import com.example.solveflow.ui.theme.SolveFlowTheme
import com.example.solveflow.ui.viewmodel.FlowchartViewModel
import com.example.solveflow.ui.viewmodel.FlowchartViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = FlowchartRepository(database.flowchartDao())
        val viewModelFactory = FlowchartViewModelFactory(repository)
        val viewModel: FlowchartViewModel by viewModels { viewModelFactory }

        setContent {
            SolveFlowTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
