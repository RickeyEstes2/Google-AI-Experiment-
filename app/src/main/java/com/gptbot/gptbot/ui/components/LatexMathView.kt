package com.gptbot.gptbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptbot.gptbot.ui.theme.Emerald400
import com.gptbot.gptbot.ui.theme.Emerald500
import com.gptbot.gptbot.ui.theme.Zinc700
import com.gptbot.gptbot.ui.theme.Zinc800
import com.gptbot.gptbot.ui.theme.Zinc900

object LatexFormatter {

    fun formatLatex(raw: String): String {
        var text = raw

        // Greek letters
        val greekMap = mapOf(
            "\\alpha" to "α", "\\beta" to "β", "\\gamma" to "γ", "\\delta" to "δ",
            "\\epsilon" to "ε", "\\zeta" to "ζ", "\\eta" to "η", "\\theta" to "θ",
            "\\iota" to "ι", "\\kappa" to "κ", "\\lambda" to "λ", "\\mu" to "μ",
            "\\nu" to "ν", "\\xi" to "ξ", "\\pi" to "π", "\\rho" to "ρ",
            "\\sigma" to "σ", "\\tau" to "τ", "\\upsilon" to "υ", "\\phi" to "φ",
            "\\chi" to "χ", "\\psi" to "ψ", "\\omega" to "ω",
            "\\Gamma" to "Γ", "\\Delta" to "Δ", "\\Theta" to "Θ", "\\Lambda" to "Λ",
            "\\Xi" to "Ξ", "\\Pi" to "Π", "\\Sigma" to "Σ", "\\Phi" to "Φ",
            "\\Psi" to "Ψ", "\\Omega" to "Ω"
        )
        for ((tex, symbol) in greekMap) {
            text = text.replace(tex, symbol)
        }

        // Mathematical operators & relations
        val symbolMap = mapOf(
            "\\cdot" to " · ", "\\times" to " × ", "\\pm" to "±", "\\mp" to "∓",
            "\\leq" to "≤", "\\geq" to "≥", "\\neq" to "≠", "\\approx" to "≈",
            "\\equiv" to "≡", "\\sim" to "∼", "\\propto" to "∝",
            "\\infty" to "∞", "\\partial" to "∂", "\\nabla" to "∇",
            "\\in" to " ∈ ", "\\notin" to " ∉ ", "\\subset" to " ⊂ ", "\\subseteq" to " ⊆ ",
            "\\cap" to " ∩ ", "\\cup" to " ∪ ", "\\forall" to "∀", "\\exists" to "∃",
            "\\implies" to " ⟹ ", "\\iff" to " ⟺ ", "\\to" to " → ", "\\leftarrow" to " ← ",
            "\\int" to "∫", "\\iint" to "∬", "\\iiint" to "∭", "\\oint" to "∮",
            "\\sum" to "∑", "\\prod" to "∏",
            "\\circ" to "°", "\\prime" to "′", "\\quad" to "   ", "\\," to " "
        )
        for ((tex, symbol) in symbolMap) {
            text = text.replace(tex, symbol)
        }

        // Functions
        val funcMap = listOf(
            "\\sin", "\\cos", "\\tan", "\\sec", "\\csc", "\\cot",
            "\\arcsin", "\\arccos", "\\arctan", "\\sinh", "\\cosh", "\\tanh",
            "\\ln", "\\log", "\\exp", "\\lim", "\\max", "\\min", "\\arg\\max"
        )
        for (fn in funcMap) {
            val clean = fn.removePrefix("\\")
            text = text.replace(fn, clean)
        }

        // Fractions: \frac{a}{b} -> (a / b)
        val fracRegex = Regex("""\\frac\{([^}]+)\}\{([^}]+)\}""")
        text = fracRegex.replace(text) { match ->
            val num = match.groupValues[1]
            val den = match.groupValues[2]
            "(${num} / ${den})"
        }

        // Square roots: \sqrt{a} -> √(a)
        val sqrtRegex = Regex("""\\sqrt\{([^}]+)\}""")
        text = sqrtRegex.replace(text) { match ->
            "√(" + match.groupValues[1] + ")"
        }

        // Superscripts
        text = convertSuperscripts(text)
        // Subscripts
        text = convertSubscripts(text)

        // Clean up remaining LaTeX artifacts
        text = text.replace("\\left", "").replace("\\right", "")
        text = text.replace("{", "").replace("}", "")
        text = text.replace("\\", "")

        return text
    }

    private fun convertSuperscripts(input: String): String {
        val superMap = mapOf(
            '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
            '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
            '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾',
            'n' to 'ⁿ', 'i' to 'ⁱ', 'x' to 'ˣ'
        )

        // Replace x^{...} or x^digit
        val bracketSuper = Regex("""\^\{([^}]+)\}""")
        var res = bracketSuper.replace(input) { match ->
            match.groupValues[1].map { superMap[it] ?: it }.joinToString("")
        }

        val singleSuper = Regex("""\^([0-9nix+\-])""")
        res = singleSuper.replace(res) { match ->
            val ch = match.groupValues[1][0]
            "${superMap[ch] ?: ch}"
        }
        return res
    }

    private fun convertSubscripts(input: String): String {
        val subMap = mapOf(
            '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
            '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
            '+' to '₊', '-' to '₋', '=' to '₌', '(' to '₍', ')' to '₎',
            'a' to 'ₐ', 'e' to 'ₑ', 'o' to 'ₒ', 'x' to 'ₓ', 'h' to 'ₕ',
            'k' to 'ₖ', 'l' to 'ₗ', 'm' to 'ₘ', 'n' to 'ₙ', 'p' to 'ₚ',
            's' to 'ₛ', 't' to 'ₜ'
        )

        val bracketSub = Regex("""_\{([^}]+)\}""")
        var res = bracketSub.replace(input) { match ->
            match.groupValues[1].map { subMap[it] ?: it }.joinToString("")
        }

        val singleSub = Regex("""_([0-9aeoxhklmnpst+\-])""")
        res = singleSub.replace(res) { match ->
            val ch = match.groupValues[1][0]
            "${subMap[ch] ?: ch}"
        }
        return res
    }
}

/**
 * Display math block component for standalone LaTeX formulas ($$...$$).
 */
@Composable
fun DisplayMathBlock(
    rawFormula: String,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    val formatted = remember(rawFormula) {
        LatexFormatter.formatLatex(rawFormula)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Zinc900,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Emerald500.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Functions,
                        contentDescription = "LaTeX Formula",
                        tint = Emerald400,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MATHEMATICAL FORMULATION (LaTeX)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald400,
                        letterSpacing = 0.5.sp
                    )
                }

                IconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(rawFormula))
                        copied = true
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy LaTeX",
                        tint = if (copied) Emerald400 else Color(0xFFA1A1AA),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SelectionContainer {
                Text(
                    text = formatted,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFFFAFAFA),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Zinc800.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Raw LaTeX: $$ $rawFormula $$",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFA1A1AA),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
