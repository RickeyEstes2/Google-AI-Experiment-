package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * LaTeX and Mathematical Expression Parser & Renderer.
 * Supports:
 * - Fractions: \frac{a}{b}
 * - Square root and N-th root: \sqrt{x}, \sqrt[n]{x}
 * - Powers / Subscripts: x^2, x_i, e^{i\pi}
 * - Summations, Integrals, Products: \sum_{i=1}^n, \int_a^b, \prod
 * - Greek symbols: \alpha, \beta, \gamma, \pi, \theta, \Delta, \Sigma, etc.
 * - Operators: \pm, \times, \div, \le, \ge, \neq, \approx, \infty, \to, \sqrt, etc.
 * - Matrices: \begin{matrix} ... \end{matrix} or 2x2 / 3x3 layouts
 */
object LatexParser {

    private val GREEK_MAP = mapOf(
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

    private val SYMBOL_MAP = mapOf(
        "\\times" to "×", "\\div" to "÷", "\\pm" to "±", "\\mp" to "∓",
        "\\cdot" to "·", "\\circ" to "∘", "\\infty" to "∞", "\\nabla" to "∇",
        "\\partial" to "∂", "\\approx" to "≈", "\\neq" to "≠", "\\le" to "≤",
        "\\leq" to "≤", "\\ge" to "≥", "\\geq" to "≥", "\\equiv" to "≡",
        "\\in" to "∈", "\\notin" to "∉", "\\subset" to "⊂", "\\subseteq" to "⊆",
        "\\cup" to "∪", "\\cap" to "∩", "\\to" to "→", "\\leftarrow" to "←",
        "\\leftrightarrow" to "↔", "\\forall" to "∀", "\\exists" to "∃",
        "\\therefore" to "∴", "\\because" to "∵", "\\angle" to "∠"
    )

    fun cleanSymbols(raw: String): String {
        var res = raw
        GREEK_MAP.forEach { (latex, sym) ->
            res = res.replace(latex, sym)
        }
        SYMBOL_MAP.forEach { (latex, sym) ->
            res = res.replace(latex, sym)
        }
        return res
    }

    fun containsLatex(text: String): Boolean {
        return text.contains("$$") || text.contains("$") ||
                text.contains("\\frac") || text.contains("\\sqrt") ||
                text.contains("\\sum") || text.contains("\\int") ||
                text.contains("\\alpha") || text.contains("\\pi") ||
                text.contains("\\begin{matrix}") || text.contains("\\begin{pmatrix}")
    }

    /**
     * Extracts LaTeX segments: either $$block$$ or $inline$ or \begin{matrix}...\end{matrix}
     */
    fun extractLatexBlocks(text: String): List<LatexSegment> {
        val segments = mutableListOf<LatexSegment>()
        var cursor = 0

        // Match display blocks: $$...$$
        // Match inline: $...$
        val regex = Regex("(\\$\\$([^$]+)\\$\\$)|(\\$(?!\\$)([^$]+)\\$)")
        val matches = regex.findAll(text)

        var lastEnd = 0
        for (match in matches) {
            val start = match.range.first
            val end = match.range.last + 1

            if (start > lastEnd) {
                segments.add(LatexSegment.PlainText(text.substring(lastEnd, start)))
            }

            val displayBlock = match.groups[2]?.value
            val inlineBlock = match.groups[4]?.value

            if (displayBlock != null) {
                segments.add(LatexSegment.DisplayMath(displayBlock.trim()))
            } else if (inlineBlock != null) {
                segments.add(LatexSegment.InlineMath(inlineBlock.trim()))
            }

            lastEnd = end
        }

        if (lastEnd < text.length) {
            segments.add(LatexSegment.PlainText(text.substring(lastEnd)))
        }

        return if (segments.isEmpty()) listOf(LatexSegment.PlainText(text)) else segments
    }
}

sealed class LatexSegment {
    data class PlainText(val text: String) : LatexSegment()
    data class InlineMath(val latex: String) : LatexSegment()
    data class DisplayMath(val latex: String) : LatexSegment()
}

/**
 * Visual Renderer for LaTeX Math Blocks and Formulas
 */
@Composable
fun LatexMathBlock(
    latex: String,
    modifier: Modifier = Modifier,
    isDisplayMode: Boolean = true,
    fontSize: Int = if (isDisplayMode) 16 else 14,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDisplayMode) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f) else Color.Transparent,
        border = if (isDisplayMode) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) else null,
        modifier = modifier
            .padding(vertical = if (isDisplayMode) 6.dp else 2.dp)
            .then(if (isDisplayMode) Modifier.fillMaxWidth() else Modifier)
    ) {
        Column(
            modifier = Modifier
                .padding(if (isDisplayMode) 12.dp else 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalAlignment = if (isDisplayMode) Alignment.CenterHorizontally else Alignment.Start
        ) {
            if (latex.contains("\\begin{matrix}") || latex.contains("\\begin{pmatrix}") || latex.contains("\\begin{bmatrix}")) {
                MatrixRenderer(latex = latex, fontSize = fontSize, color = color)
            } else if (latex.contains("\\frac")) {
                FractionParserView(latex = latex, fontSize = fontSize, color = color)
            } else {
                FormattedFormulaRow(latex = latex, fontSize = fontSize, color = color)
            }
        }
    }
}

@Composable
private fun FormattedFormulaRow(
    latex: String,
    fontSize: Int,
    color: Color
) {
    val clean = LatexParser.cleanSymbols(latex)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Check for \sqrt
        if (clean.contains("\\sqrt")) {
            SqrtRenderer(latex = clean, fontSize = fontSize, color = color)
        } else if (clean.contains("\\sum") || clean.contains("\\int") || clean.contains("\\prod")) {
            SumIntRenderer(latex = clean, fontSize = fontSize, color = color)
        } else {
            // General formula with sub/superscripts
            SuperSubscriptText(raw = clean, fontSize = fontSize, color = color)
        }
    }
}

@Composable
fun SuperSubscriptText(
    raw: String,
    fontSize: Int = 15,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    // Basic parser for expressions like x^2, x_{i+1}, e^{i\pi}
    Row(verticalAlignment = Alignment.CenterVertically) {
        var i = 0
        val len = raw.length
        while (i < len) {
            when {
                raw[i] == '^' -> {
                    i++
                    val exp = if (i < len && raw[i] == '{') {
                        val close = raw.indexOf('}', i)
                        if (close != -1) {
                            val content = raw.substring(i + 1, close)
                            i = close + 1
                            content
                        } else {
                            raw.substring(i)
                        }
                    } else if (i < len) {
                        val char = raw[i].toString()
                        i++
                        char
                    } else ""
                    Text(
                        text = exp,
                        fontSize = (fontSize * 0.72).sp,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        color = color,
                        modifier = Modifier.offset(y = (-5).dp)
                    )
                }
                raw[i] == '_' -> {
                    i++
                    val sub = if (i < len && raw[i] == '{') {
                        val close = raw.indexOf('}', i)
                        if (close != -1) {
                            val content = raw.substring(i + 1, close)
                            i = close + 1
                            content
                        } else {
                            raw.substring(i)
                        }
                    } else if (i < len) {
                        val char = raw[i].toString()
                        i++
                        char
                    } else ""
                    Text(
                        text = sub,
                        fontSize = (fontSize * 0.72).sp,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        color = color,
                        modifier = Modifier.offset(y = 4.dp)
                    )
                }
                else -> {
                    // Regular character or sequence
                    val start = i
                    while (i < len && raw[i] != '^' && raw[i] != '_') {
                        i++
                    }
                    val textSeg = raw.substring(start, i)
                    Text(
                        text = textSeg,
                        fontSize = fontSize.sp,
                        fontFamily = FontFamily.Serif,
                        fontStyle = if (textSeg.trim().matches(Regex("^[a-zA-Z]$"))) FontStyle.Italic else FontStyle.Normal,
                        fontWeight = FontWeight.Medium,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
private fun FractionParserView(
    latex: String,
    fontSize: Int,
    color: Color
) {
    // Extracts \frac{numerator}{denominator}
    val fracRegex = Regex("\\\\frac\\{([^\\}]+)\\}\\{([^\\}]+)\\}")
    val match = fracRegex.find(latex)

    if (match != null) {
        val num = match.groupValues[1]
        val den = match.groupValues[2]
        val before = latex.substring(0, match.range.first)
        val after = latex.substring(match.range.last + 1)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (before.isNotBlank()) {
                SuperSubscriptText(raw = LatexParser.cleanSymbols(before), fontSize = fontSize, color = color)
            }

            // Fraction Box
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                SuperSubscriptText(raw = LatexParser.cleanSymbols(num), fontSize = (fontSize * 0.9).toInt(), color = color)
                Box(
                    modifier = Modifier
                        .widthIn(min = 28.dp)
                        .height(1.5.dp)
                        .background(color)
                )
                SuperSubscriptText(raw = LatexParser.cleanSymbols(den), fontSize = (fontSize * 0.9).toInt(), color = color)
            }

            if (after.isNotBlank()) {
                SuperSubscriptText(raw = LatexParser.cleanSymbols(after), fontSize = fontSize, color = color)
            }
        }
    } else {
        SuperSubscriptText(raw = LatexParser.cleanSymbols(latex), fontSize = fontSize, color = color)
    }
}

@Composable
private fun SqrtRenderer(
    latex: String,
    fontSize: Int,
    color: Color
) {
    val sqrtRegex = Regex("\\\\sqrt(\\[([^\\]]+)\\])?\\{([^\\}]+)\\}")
    val match = sqrtRegex.find(latex)

    if (match != null) {
        val nDegree = match.groupValues[2]
        val inner = match.groupValues[3]
        val before = latex.substring(0, match.range.first)
        val after = latex.substring(match.range.last + 1)

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (before.isNotBlank()) {
                SuperSubscriptText(raw = before, fontSize = fontSize, color = color)
            }
            if (nDegree.isNotBlank()) {
                Text(
                    text = nDegree,
                    fontSize = (fontSize * 0.65).sp,
                    fontFamily = FontFamily.Serif,
                    color = color,
                    modifier = Modifier.offset(y = (-6).dp)
                )
            }
            Text(
                text = "√",
                fontSize = (fontSize * 1.3).sp,
                fontFamily = FontFamily.Serif,
                color = color
            )
            Surface(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = color,
                        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 2.dp, bottomEnd = 0.dp, bottomStart = 0.dp)
                    )
                    .padding(top = 1.dp, start = 2.dp, end = 4.dp, bottom = 2.dp),
                color = Color.Transparent
            ) {
                SuperSubscriptText(raw = inner, fontSize = fontSize, color = color)
            }
            if (after.isNotBlank()) {
                SuperSubscriptText(raw = after, fontSize = fontSize, color = color)
            }
        }
    } else {
        SuperSubscriptText(raw = latex, fontSize = fontSize, color = color)
    }
}

@Composable
private fun SumIntRenderer(
    latex: String,
    fontSize: Int,
    color: Color
) {
    val isSum = latex.contains("\\sum")
    val isInt = latex.contains("\\int")
    val isProd = latex.contains("\\prod")
    val symbol = when {
        isSum -> "∑"
        isInt -> "∫"
        isProd -> "∏"
        else -> "∑"
    }

    // Extract sub and super
    val subSuperRegex = Regex("\\\\(sum|int|prod)(_\\{?([^\\^\\}]+)\\}?)?(\\^\\{?([^\\s\\}]+)\\}?)?")
    val match = subSuperRegex.find(latex)

    if (match != null) {
        val lower = match.groupValues[3]
        val upper = match.groupValues[5]
        val rest = latex.substring(match.range.last + 1)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (upper.isNotBlank()) {
                    Text(
                        text = LatexParser.cleanSymbols(upper),
                        fontSize = (fontSize * 0.65).sp,
                        fontFamily = FontFamily.Serif,
                        color = color
                    )
                }
                Text(
                    text = symbol,
                    fontSize = (fontSize * 1.6).sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Light,
                    color = color
                )
                if (lower.isNotBlank()) {
                    Text(
                        text = LatexParser.cleanSymbols(lower),
                        fontSize = (fontSize * 0.65).sp,
                        fontFamily = FontFamily.Serif,
                        color = color
                    )
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            SuperSubscriptText(raw = LatexParser.cleanSymbols(rest), fontSize = fontSize, color = color)
        }
    } else {
        SuperSubscriptText(raw = latex, fontSize = fontSize, color = color)
    }
}

@Composable
private fun MatrixRenderer(
    latex: String,
    fontSize: Int,
    color: Color
) {
    // Parse matrix contents: rows separated by \\, elements by &
    val clean = latex
        .replace("\\begin{matrix}", "")
        .replace("\\end{matrix}", "")
        .replace("\\begin{pmatrix}", "")
        .replace("\\end{pmatrix}", "")
        .replace("\\begin{bmatrix}", "")
        .replace("\\end{bmatrix}", "")
        .trim()

    val rows = clean.split("\\\\").map { row ->
        row.split("&").map { it.trim() }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Opening bracket
        Text(
            text = "[",
            fontSize = (fontSize * (rows.size * 0.9 + 1)).sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Light,
            color = color
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            rows.forEach { rowCols ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowCols.forEach { item ->
                        SuperSubscriptText(
                            raw = LatexParser.cleanSymbols(item),
                            fontSize = fontSize,
                            color = color
                        )
                    }
                }
            }
        }

        // Closing bracket
        Text(
            text = "]",
            fontSize = (fontSize * (rows.size * 0.9 + 1)).sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Light,
            color = color
        )
    }
}

/**
 * LaTeX Formula Preset Library & Quick-Insert Dialog
 */
data class LatexPreset(val name: String, val category: String, val formula: String, val preview: String)

val LATEX_PRESETS = listOf(
    LatexPreset("Quadratic Formula", "Algebra", "$$ x = \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a} $$", "x = (-b ± √(b²-4ac)) / 2a"),
    LatexPreset("Euler's Identity", "Calculus", "$$ e^{i\\pi} + 1 = 0 $$", "e^(iπ) + 1 = 0"),
    LatexPreset("Pythagorean Theorem", "Geometry", "$$ a^2 + b^2 = c^2 $$", "a² + b² = c²"),
    LatexPreset("Summation Series", "Calculus", "$$ \\sum_{i=1}^{n} i = \\frac{n(n+1)}{2} $$", "∑ i = n(n+1)/2"),
    LatexPreset("Gaussian Integral", "Calculus", "$$ \\int_{-\\infty}^{\\infty} e^{-x^2} dx = \\sqrt{\\pi} $$", "∫ e^(-x²) dx = √π"),
    LatexPreset("Bayes' Theorem", "Probability", "$$ P(A|B) = \\frac{P(B|A)P(A)}{P(B)} $$", "P(A|B) = P(B|A)P(A)/P(B)"),
    LatexPreset("Mass-Energy Equivalence", "Physics", "$$ E = mc^2 $$", "E = mc²"),
    LatexPreset("2x2 Matrix", "Linear Algebra", "$$ \\begin{matrix} a & b \\\\ c & d \\end{matrix} $$", "[a, b; c, d]"),
    LatexPreset("Derivative Definition", "Calculus", "$$ f'(x) = \\lim_{h \\to 0} \\frac{f(x+h) - f(x)}{h} $$", "f'(x) = lim (f(x+h)-f(x))/h")
)

@Composable
fun InsertLatexDialog(
    onDismiss: () -> Unit,
    onInsertFormula: (formula: String) -> Unit
) {
    var customFormula by remember { mutableStateOf("$$ E = mc^2 $$") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = remember { listOf("All", "Algebra", "Calculus", "Geometry", "Probability", "Physics", "Linear Algebra") }
    val filteredPresets = remember(selectedCategory) {
        if (selectedCategory == "All") LATEX_PRESETS else LATEX_PRESETS.filter { it.category == selectedCategory }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Functions,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "Insert LaTeX Formula",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Custom formula editor
                OutlinedTextField(
                    value = customFormula,
                    onValueChange = { customFormula = it },
                    label = { Text("LaTeX Expression ($$ ... $$)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick symbol tool strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "\\frac{a}{b}", "\\sqrt{x}", "x^2", "x_i",
                        "\\alpha", "\\beta", "\\pi", "\\theta", "\\sum", "\\int",
                        "\\pm", "\\times", "\\le", "\\ge", "\\infty", "\\to"
                    ).forEach { sym ->
                        SuggestionChip(
                            onClick = {
                                customFormula = if (customFormula.endsWith("$$")) {
                                    customFormula.substring(0, customFormula.length - 2) + " $sym $$"
                                } else {
                                    "$customFormula $sym"
                                }
                            },
                            label = { Text(sym, fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                        )
                    }
                }

                // Live LaTeX Rendered Preview
                Text("Live Render Preview:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                val cleanPreview = customFormula.replace("$$", "").trim()
                LatexMathBlock(
                    latex = cleanPreview,
                    isDisplayMode = true,
                    fontSize = 18
                )

                // Category Chips
                ScrollableTabRow(
                    selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    categories.forEach { cat ->
                        Tab(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            text = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                // Preset List
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredPresets.forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { customFormula = preset.formula }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = preset.preview,
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                TextButton(onClick = {
                                    onInsertFormula(preset.formula)
                                    onDismiss()
                                }) {
                                    Text("Insert")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Insert button
                Button(
                    onClick = {
                        onInsertFormula(customFormula)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Insert into Post")
                }
            }
        }
    }
}
