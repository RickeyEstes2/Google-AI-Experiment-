package com.gptbot.gptbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptbot.gptbot.data.model.MathDomain
import com.gptbot.gptbot.data.model.MathSolution
import com.gptbot.gptbot.engine.math.MathSolverEngine
import com.gptbot.gptbot.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathSolverModal(
    onDismiss: () -> Unit,
    onSolveInChat: (String) -> Unit
) {
    var selectedDomain by remember { mutableStateOf(MathDomain.CALCULUS) }
    var inputFormula by remember { mutableStateOf("Integrate by parts: \\int x \\cdot e^{2x} \\, dx") }
    var currentSolution by remember { mutableStateOf<MathSolution?>(null) }

    LaunchedEffect(inputFormula) {
        currentSolution = MathSolverEngine.solve(inputFormula)
    }

    val sampleProblems = remember(selectedDomain) {
        when (selectedDomain) {
            MathDomain.CALCULUS -> listOf(
                "Integrate by parts: \\int x \\cdot e^{2x} \\, dx",
                "Derivative of f(x) = x^3 \\cdot \\sin(x) via product rule",
                "Evaluate limit: \\lim_{x \\to 0} \\frac{\\sin(3x)}{x}",
                "Definite integral: \\int_{0}^{\\pi} \\sin^2(x) \\, dx",
                "Derivative of f(x) = \\ln(x^2 + 1) via chain rule"
            )
            MathDomain.ALGEBRA -> listOf(
                "Solve quadratic: 2x^2 - 7x + 3 = 0",
                "Solve linear system: 2x + 3y = 13 and 5x - y = 7",
                "Factor polynomial: x^3 - 8 (difference of cubes)"
            )
            MathDomain.TRIGONOMETRY -> listOf(
                "Prove identity: \\frac{\\sin(2\\theta)}{1 + \\cos(2\\theta)} = \\tan(\\theta)",
                "Solve triangle: a = 7, b = 10, C = 60^\\circ via Law of Cosines",
                "Verify Pythagorean identity: \\sin^2(\\theta) + \\cos^2(\\theta) = 1"
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Zinc900,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Zinc700) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Functions,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LaTeX Equation Solver Hub",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFAFAFA)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Domain Selector Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MathDomain.values().forEach { domain ->
                    val isSelected = selectedDomain == domain
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedDomain = domain
                            inputFormula = when (domain) {
                                MathDomain.CALCULUS -> "Integrate by parts: \\int x \\cdot e^{2x} \\, dx"
                                MathDomain.ALGEBRA -> "Solve quadratic: 2x^2 - 7x + 3 = 0"
                                MathDomain.TRIGONOMETRY -> "Prove identity: \\frac{\\sin(2\\theta)}{1 + \\cos(2\\theta)} = \\tan(\\theta)"
                            }
                        },
                        label = { Text(domain.label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Emerald500.copy(alpha = 0.25f),
                            selectedLabelColor = Emerald400
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Presets row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(sampleProblems) { problem ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Zinc800,
                        modifier = Modifier
                            .clickable { inputFormula = problem }
                            .border(1.dp, Zinc700, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = problem.take(35) + if (problem.length > 35) "..." else "",
                            fontSize = 11.sp,
                            color = Color(0xFFD4D4D8),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Input field
            OutlinedTextField(
                value = inputFormula,
                onValueChange = { inputFormula = it },
                label = { Text("Equation / Problem to Solve", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald500,
                    unfocusedBorderColor = Zinc700,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = false,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Solution Presentation
            currentSolution?.let { sol ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    item {
                        DisplayMathBlock(rawFormula = sol.latexProblem)
                    }

                    item {
                        Text(
                            text = "STEP-BY-STEP DERIVATION (${sol.steps.size} STEPS):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald400,
                            letterSpacing = 0.5.sp
                        )
                    }

                    items(sol.steps) { step ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Zinc800.copy(alpha = 0.7f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Zinc700, RoundedCornerShape(10.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Step ${step.stepNumber}: ${step.appliedRule}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald400
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = step.description,
                                    fontSize = 12.sp,
                                    color = Color(0xFFE4E4E7)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                DisplayMathBlock(rawFormula = step.latexFormula)
                            }
                        }
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Emerald500.copy(alpha = 0.12f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Emerald400, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "FINAL ANSWER (CLOSED-FORM):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald400
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                DisplayMathBlock(rawFormula = sol.finalAnswerLatex)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "✓ Verification: ${sol.verificationCheck}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFE4E4E7)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action: Feed to Chatbot
            Button(
                onClick = {
                    onSolveInChat("Solve step-by-step with LaTeX and verification: $inputFormula")
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = Color(0xFF022C22)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Analyze & Expand with Self-Improving CoT", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
