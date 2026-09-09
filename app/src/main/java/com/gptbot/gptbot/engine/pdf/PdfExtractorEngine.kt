package com.gptbot.gptbot.engine.pdf

import com.gptbot.gptbot.data.model.ExtractedPdfDoc
import com.gptbot.gptbot.data.model.PdfPageChunk

object PdfExtractorEngine {

    val sampleDocuments = listOf(
        ExtractedPdfDoc(
            id = "pdf_calc_handbook",
            title = "Handbook of Advanced Calculus & Real Analysis",
            author = "Prof. E. Landau & AMS Press",
            pageCount = 4,
            summary = "Rigorous analytical compendium covering differential forms, integration by parts in Banach spaces, trigonometric power series, and L'Hopital indeterminate theorems.",
            pages = listOf(
                PdfPageChunk(
                    pageNumber = 1,
                    header = "Chapter 1: The Fundamental Theorem of Calculus & Differential Forms",
                    textContent = """
Section 1.1: If f is continuous on [a, b], then the function g defined by g(x) = \int_{a}^{x} f(t) dt is continuous on [a, b], differentiable on (a, b), and g'(x) = f(x).
Section 1.2: Let F be any antiderivative of f. Then \int_{a}^{b} f(x) dx = F(b) - F(a).
The Leibniz integral rule states that \frac{d}{dx} \left( \int_{a(x)}^{b(x)} f(x, t) dt \right) = f(x, b(x)) b'(x) - f(x, a(x)) a'(x) + \int_{a(x)}^{b(x)} \frac{\partial f}{\partial x} dt.
                    """.trimIndent(),
                    formulasDetected = listOf(
                        "g'(x) = f(x)",
                        "\\int_{a}^{b} f(x) dx = F(b) - F(a)",
                        "\\frac{d}{dx} \\left( \\int_{a(x)}^{b(x)} f(x, t) dt \\right)"
                    )
                ),
                PdfPageChunk(
                    pageNumber = 2,
                    header = "Chapter 2: Integration by Parts & Higher-Dimensional Manifolds",
                    textContent = """
Section 2.1: The derivation stems from the product rule: d(uv) = u dv + v du. Integrating both sides yields \int u dv = uv - \int v du.
Section 2.2: For repeated integration: \int x^n e^{kx} dx = \frac{x^n e^{kx}}{k} - \frac{n}{k} \int x^{n-1} e^{kx} dx.
Section 2.3: Cauchy-Schwarz Inequality: \left| \int_{a}^{b} f(x)g(x) dx \right|^2 \leq \left( \int_{a}^{b} f^2(x) dx \right) \left( \int_{a}^{b} g^2(x) dx \right).
                    """.trimIndent(),
                    formulasDetected = listOf(
                        "\\int u \\, dv = uv - \\int v \\, du",
                        "\\int x^n e^{kx} dx = \\frac{x^n e^{kx}}{k} - \\frac{n}{k} \\int x^{n-1} e^{kx} dx"
                    )
                ),
                PdfPageChunk(
                    pageNumber = 3,
                    header = "Chapter 3: Trigonometric Identities & Harmonic Analysis",
                    textContent = """
Section 3.1: Euler's Formula e^{i\theta} = \cos(\theta) + i\sin(\theta) yields direct derivations of sum-to-product and double-angle formulas.
\sin(2\theta) = 2\sin(\theta)\cos(\theta), \quad \cos(2\theta) = \cos^2(\theta) - \sin^2(\theta) = 2\cos^2(\theta) - 1.
Section 3.2: Fourier Series Representation for periodic functions f(x) with period 2L:
f(x) = \frac{a_0}{2} + \sum_{n=1}^{\infty} \left( a_n \cos\left(\frac{n\pi x}{L}\right) + b_n \sin\left(\frac{n\pi x}{L}\right) \right).
                    """.trimIndent(),
                    formulasDetected = listOf(
                        "e^{i\\theta} = \\cos(\\theta) + i\\sin(\\theta)",
                        "\\cos(2\\theta) = 2\\cos^2(\\theta) - 1",
                        "f(x) = \\frac{a_0}{2} + \\sum_{n=1}^{\\infty} ..."
                    )
                ),
                PdfPageChunk(
                    pageNumber = 4,
                    header = "Chapter 4: Limits and L'Hôpital's Theorem",
                    textContent = """
Theorem 4.1: Suppose f and g are differentiable on an open interval I containing c (except possibly at c itself).
If \lim_{x \to c} f(x) = 0 and \lim_{x \to c} g(x) = 0, or \lim_{x \to c} |f(x)| = \infty and \lim_{x \to c} |g(x)| = \infty,
and \lim_{x \to c} \frac{f'(x)}{g'(x)} exists, then:
\lim_{x \to c} \frac{f(x)}{g(x)} = \lim_{x \to c} \frac{f'(x)}{g'(x)}.
                    """.trimIndent(),
                    formulasDetected = listOf(
                        "\\lim_{x \\to c} \\frac{f(x)}{g(x)} = \\lim_{x \\to c} \\frac{f'(x)}{g'(x)}"
                    )
                )
            )
        ),
        ExtractedPdfDoc(
            id = "pdf_distributed_consensus",
            title = "Distributed Systems & Byzantine Fault Tolerance Spec",
            author = "Systems Architecture Working Group",
            pageCount = 3,
            summary = "Technical specification on Raft consensus, Paxos invariant proofs, quorum intersections, network partition recovery, and log replication guarantees.",
            pages = listOf(
                PdfPageChunk(
                    pageNumber = 1,
                    header = "1. Consensus Invariants & Quorum Slicing",
                    textContent = """
Leader Election Safety: At most one leader can be elected in a given term.
Log Matching Invariant: If two logs contain an entry with the same index and term, then the logs are identical in all entries up through the given index.
Quorum intersection requirement: Any two quorums Q_1 and Q_2 must satisfy |Q_1 \cap Q_2| \geq 1 to guarantee consistency during network split-brain.
                    """.trimIndent(),
                    formulasDetected = listOf("|Q_1 \\cap Q_2| \\geq 1")
                ),
                PdfPageChunk(
                    pageNumber = 2,
                    header = "2. Log Replication & Commit Protocol",
                    textContent = """
The leader decides when it is safe to apply a log entry to the state machine; such an entry is called committed.
Raft guarantees that committed entries are durable and will eventually be executed by all available state machines.
An entry is committed once it has been stored on a majority of servers (e.g. \lfloor N/2 \rfloor + 1).
                    """.trimIndent(),
                    formulasDetected = listOf("\\lfloor N/2 \\rfloor + 1")
                ),
                PdfPageChunk(
                    pageNumber = 3,
                    header = "3. Split-Brain Prevention & Contradiction Resolution",
                    textContent = """
When network partitions isolate a minority group of nodes, they cannot achieve majority vote quorum.
Heartbeats with lower term numbers are unconditionally rejected.
This eliminates duplicate state mutations and prevents contradictory execution histories.
                    """.trimIndent(),
                    formulasDetected = emptyList()
                )
            )
        )
    )

    fun searchDocument(doc: ExtractedPdfDoc, query: String): List<Pair<Int, String>> {
        if (query.isBlank()) return doc.pages.map { it.pageNumber to it.textContent }
        return doc.pages
            .filter { it.textContent.contains(query, ignoreCase = true) || it.header.contains(query, ignoreCase = true) }
            .map { it.pageNumber to it.textContent }
    }
}
