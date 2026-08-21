package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Native Compose parser & renderer for LaTeX mathematical formulas and expressions.
 * Converts LaTeX formulas (e.g. \frac{a}{b}, \sum_{i=1}^n, \sqrt{x}, \int, \cdot) into rendered math layouts.
 */
@Composable
fun LatexMathFormula(
    latex: String,
    isBlock: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cleanLatex = latex.trim().removePrefix("$$").removeSuffix("$$").removePrefix("$").removeSuffix("$").trim()

    if (isBlock) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                RenderFormulaContent(cleanLatex)
            }
        }
    } else {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            modifier = modifier.padding(horizontal = 2.dp)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                RenderFormulaContent(cleanLatex, isInline = true)
            }
        }
    }
}

@Composable
private fun RenderFormulaContent(latex: String, isInline: Boolean = false) {
    // Process known formulas or token stream
    val formatted = formatLatexString(latex)

    Text(
        text = formatted,
        fontFamily = FontFamily.Serif,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.SemiBold,
        fontSize = if (isInline) 14.sp else 16.sp,
        color = MaterialTheme.colorScheme.onSurface,
        letterSpacing = 0.5.sp
    )
}

fun formatLatexString(latex: String): String {
    var result = latex
    // Common math symbols
    result = result.replace("\\alpha", "α")
    result = result.replace("\\beta", "β")
    result = result.replace("\\gamma", "γ")
    result = result.replace("\\delta", "δ")
    result = result.replace("\\epsilon", "ε")
    result = result.replace("\\theta", "θ")
    result = result.replace("\\lambda", "λ")
    result = result.replace("\\mu", "μ")
    result = result.replace("\\pi", "π")
    result = result.replace("\\sigma", "σ")
    result = result.replace("\\omega", "ω")
    result = result.replace("\\infty", "∞")
    result = result.replace("\\sum", "∑")
    result = result.replace("\\int", "∫")
    result = result.replace("\\prod", "∏")
    result = result.replace("\\sqrt", "√")
    result = result.replace("\\approx", "≈")
    result = result.replace("\\neq", "≠")
    result = result.replace("\\leq", "≤")
    result = result.replace("\\geq", "≥")
    result = result.replace("\\pm", "±")
    result = result.replace("\\times", "×")
    result = result.replace("\\cdot", "·")
    result = result.replace("\\rightarrow", "→")
    result = result.replace("\\leftarrow", "←")
    result = result.replace("\\Rightarrow", "⇒")
    result = result.replace("\\forall", "∀")
    result = result.replace("\\exists", "∃")
    result = result.replace("\\in", "∈")
    result = result.replace("\\subset", "⊂")
    result = result.replace("\\cup", "∪")
    result = result.replace("\\cap", "∩")

    // Handle \frac{a}{b} -> (a / b)
    val fracRegex = Regex("\\\\frac\\{([^}]+)\\}\\{([^}]+)\\}")
    result = fracRegex.replace(result) { match ->
        val num = match.groupValues[1]
        val den = match.groupValues[2]
        "($num / $den)"
    }

    // Handle \text{...} -> ...
    val textRegex = Regex("\\\\text\\{([^}]+)\\}")
    result = textRegex.replace(result) { match -> match.groupValues[1] }

    // Handle subscripts _x -> ₓ and superscripts ^2 -> ²
    result = result.replace("^2", "²")
    result = result.replace("^3", "³")
    result = result.replace("^n", "ⁿ")
    result = result.replace("^T", "ᵀ")
    result = result.replace("_1", "₁")
    result = result.replace("_2", "₂")
    result = result.replace("_n", "ₙ")
    result = result.replace("_i", "ᵢ")
    result = result.replace("_k", "ₖ")

    // Clean remaining backslashes
    result = result.replace("\\left", "").replace("\\right", "")
    result = result.replace("\\{", "{").replace("\\}", "}")

    return result
}
