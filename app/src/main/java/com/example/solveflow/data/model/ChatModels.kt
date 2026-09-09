package com.example.solveflow.data.model

import kotlinx.serialization.Serializable

/**
 * Modes supported by the Self-Improving CoT Chatbot.
 */
enum class ChatMode(
    val title: String,
    val description: String,
    val iconName: String
) {
    GENERAL_COT(
        title = "Self-Improving CoT",
        description = "Multi-stage chain of thought with self-reflection and iterative improvement loops",
        iconName = "Psychology"
    ),
    BRAINSTORM(
        title = "Brainstorm",
        description = "Divergent multi-perspective ideation (First-principles, analogies, radical moonshots)",
        iconName = "Lightbulb"
    ),
    REVERSE_BRAINSTORM(
        title = "Reverse Brainstorm",
        description = "Stoic inversion & pre-mortem: How to guarantee failure, inverting pitfalls into breakthroughs",
        iconName = "PublishedWithChanges"
    ),
    MATH_SOLVER(
        title = "LaTeX Math & Equations",
        description = "Step-by-step Algebra, Trigonometry, and Calculus solver with authentic LaTeX rendering",
        iconName = "Functions"
    ),
    CONTRADICTION_DETECTOR(
        title = "Contradiction Check",
        description = "Rigorous propositional, quantitative, and temporal contradiction detector",
        iconName = "Balance"
    ),
    LOG_ANALYZER(
        title = "Log Search & Root Cause",
        description = "Search log files with regex, filter stack traces, and isolate root causes with AI",
        iconName = "Article"
    ),
    PDF_EXTRACTOR(
        title = "PDF Knowledge Extraction",
        description = "Extract pages, formulas, and technical specifications from PDF documents",
        iconName = "PictureAsPdf"
    ),
    CODE_GEN(
        title = "Code Synthesis",
        description = "Multi-language code generator with syntax highlighting and time/space complexity",
        iconName = "Code"
    )
}

/**
 * Single iteration of the self-improvement reflection loop.
 */
@Serializable
data class CoTIteration(
    val iterationNumber: Int,
    val critique: String,
    val improvementsMade: List<String>,
    val confidenceScore: Int, // e.g. 72 to 96 percent
    val deltaConfidence: Int  // e.g. +14 percent
)

/**
 * 5-Stage Chain of Thought reasoning data model.
 */
@Serializable
data class ChainOfThought(
    val stage1Decomposition: String = "",
    val stage2Hypothesis: String = "",
    val stage3ContradictionCheck: String = "",
    val stage4SelfImprovement: String = "",
    val stage5Synthesis: String = "",
    val iterations: List<CoTIteration> = emptyList(),
    val totalTimeMs: Long = 0L,
    val isComplete: Boolean = true
)

/**
 * Google Search grounding result item.
 */
@Serializable
data class SearchResultItem(
    val id: String,
    val title: String,
    val snippet: String,
    val url: String,
    val domain: String,
    val citationIndex: Int
)

/**
 * Contradiction analysis report.
 */
@Serializable
data class ContradictionReport(
    val hasContradiction: Boolean = false,
    val severity: String = "NONE", // NONE, LOW, MEDIUM, CRITICAL
    val premiseA: String = "",
    val premiseB: String = "",
    val contradictionType: String = "", // Propositional, Quantitative, Temporal, Semantic
    val detailedAnalysis: String = "",
    val resolutionProposal: String = ""
)

/**
 * Mathematical domain categories for equation solving.
 */
enum class MathDomain(val label: String, val description: String) {
    ALGEBRA("Algebra", "Polynomials, quadratics, systems of equations, logs & exponents"),
    TRIGONOMETRY("Trigonometry", "Identities, unit circle, triangle laws, wave behavior"),
    CALCULUS("Calculus", "Derivatives, integrals, limits, Taylor series, rate of change")
}

/**
 * Step-by-step mathematical solution.
 */
@Serializable
data class MathSolution(
    val problem: String,
    val domain: String,
    val latexProblem: String,
    val steps: List<MathStep>,
    val finalAnswerLatex: String,
    val verificationCheck: String,
    val conceptualExplanation: String
)

@Serializable
data class MathStep(
    val stepNumber: Int,
    val description: String,
    val latexFormula: String,
    val appliedRule: String
)

/**
 * Parsed log entry.
 */
@Serializable
data class LogEntry(
    val id: String,
    val timestamp: String,
    val level: String, // ERROR, WARN, INFO, DEBUG, FATAL
    val tag: String,
    val message: String,
    val stackTrace: String? = null,
    val lineNumber: Int = 0
)

/**
 * Extracted PDF document metadata & content.
 */
@Serializable
data class ExtractedPdfDoc(
    val id: String,
    val title: String,
    val author: String,
    val pageCount: Int,
    val summary: String,
    val pages: List<PdfPageChunk>
)

@Serializable
data class PdfPageChunk(
    val pageNumber: Int,
    val header: String,
    val textContent: String,
    val formulasDetected: List<String> = emptyList()
)
