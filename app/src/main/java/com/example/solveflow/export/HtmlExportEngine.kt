package com.example.solveflow.export

import com.example.solveflow.data.model.DiagnosticSession
import com.example.solveflow.data.model.FlowchartData
import com.example.solveflow.data.model.NodeType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HtmlExportEngine {

    fun generateHtml(flowchart: FlowchartData, session: DiagnosticSession? = null): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateString = dateFormat.format(Date())

        val decisionCount = flowchart.nodes.count { it.type == NodeType.DECISION }
        val actionCount = flowchart.nodes.count { it.type == NodeType.ACTION }
        val successCount = flowchart.nodes.count { it.type == NodeType.OUTCOME_SUCCESS }
        val escalateCount = flowchart.nodes.count { it.type == NodeType.OUTCOME_ESCALATE }

        val svgContent = generateSvgDiagram(flowchart, session)

        val sessionHtml = if (session != null) {
            """
            <div class="session-card">
                <h3>🔍 Recorded Diagnostic Execution Session</h3>
                <p><strong>Completed:</strong> ${dateFormat.format(Date(session.completedAt))} | <strong>Status:</strong> <span class="status-tag">${session.finalStatus}</span></p>
                <div class="timeline">
                    ${
                        session.steps.mapIndexed { idx, step ->
                            """
                            <div class="timeline-step">
                                <div class="step-num">${idx + 1}</div>
                                <div class="step-body">
                                    <div class="step-title">${step.nodeTitle}</div>
                                    ${if (step.chosenBranchLabel != null) "<div class='step-choice'>Chosen Path: <strong>${step.chosenBranchLabel}</strong></div>" else ""}
                                </div>
                            </div>
                            """.trimIndent()
                        }.joinToString("\n")
                    }
                </div>
            </div>
            """.trimIndent()
        } else ""

        val nodeCardsHtml = flowchart.nodes.joinToString("\n") { node ->
            val typeClass = when (node.type) {
                NodeType.START -> "type-start"
                NodeType.DECISION -> "type-decision"
                NodeType.ACTION -> "type-action"
                NodeType.OUTCOME_SUCCESS -> "type-success"
                NodeType.OUTCOME_ESCALATE -> "type-escalate"
                NodeType.NOTE -> "type-note"
            }
            val typeLabel = when (node.type) {
                NodeType.START -> "Initial Problem State"
                NodeType.DECISION -> "Diagnostic Check / Decision"
                NodeType.ACTION -> "Investigation / Remedial Action"
                NodeType.OUTCOME_SUCCESS -> "Problem Resolved (Success)"
                NodeType.OUTCOME_ESCALATE -> "Escalation / Failover"
                NodeType.NOTE -> "Guidance Note"
            }

            val branchesHtml = if (node.branches.isNotEmpty()) {
                """
                <div class="branches-container">
                    <div class="branches-header">Decision Branches:</div>
                    <div class="branch-pills">
                        ${
                            node.branches.joinToString("") { b ->
                                val targetNode = flowchart.nodes.find { it.id == b.targetNodeId }
                                val targetLabel = targetNode?.title ?: b.targetNodeId
                                """
                                <div class="branch-pill">
                                    <span class="branch-label">${b.label}</span>
                                    <span class="branch-arrow">➔</span>
                                    <span class="branch-target">${targetLabel}</span>
                                </div>
                                """.trimIndent()
                            }
                        }
                    </div>
                </div>
                """.trimIndent()
            } else ""

            """
            <div class="node-card $typeClass" id="node-${node.id}">
                <div class="node-header">
                    <span class="node-step">Step ${node.stepNumber}</span>
                    <span class="node-badge">$typeLabel</span>
                    <span class="node-id">#${node.id}</span>
                </div>
                <h4 class="node-title">${escapeHtml(node.title)}</h4>
                ${if (node.description.isNotBlank()) "<p class='node-desc'>${escapeHtml(node.description)}</p>" else ""}
                $branchesHtml
            </div>
            """.trimIndent()
        }

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>${escapeHtml(flowchart.title)} - SolveFlow</title>
            <style>
                :root {
                    --bg: #0b1120;
                    --surface: #1e293b;
                    --surface-hover: #334155;
                    --border: #334155;
                    --text: #f8fafc;
                    --text-muted: #94a3b8;
                    --primary: #38bdf8;
                    --success: #10b981;
                    --decision: #6366f1;
                    --action: #f59e0b;
                    --escalate: #f43f5e;
                }
                * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }
                body { background-color: var(--bg); color: var(--text); padding: 24px; line-height: 1.5; }
                .container { max-width: 1080px; margin: 0 auto; }
                .header { background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%); padding: 32px; border-radius: 16px; border: 1px solid var(--border); margin-bottom: 24px; box-shadow: 0 10px 25px -5px rgba(0,0,0,0.3); }
                .category-badge { display: inline-block; background: rgba(56, 189, 248, 0.15); color: var(--primary); padding: 4px 12px; border-radius: 9999px; font-size: 0.85rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 12px; border: 1px solid rgba(56, 189, 248, 0.3); }
                h1 { font-size: 2.2rem; font-weight: 800; margin-bottom: 8px; color: #fff; }
                .description { font-size: 1.1rem; color: var(--text-muted); margin-bottom: 20px; }
                .meta-stats { display: flex; flex-wrap: wrap; gap: 16px; border-top: 1px solid var(--border); padding-top: 16px; font-size: 0.9rem; color: var(--text-muted); }
                .stat-item strong { color: #fff; font-size: 1rem; }
                .btn-print { background: var(--primary); color: #0f172a; border: none; padding: 10px 20px; border-radius: 8px; font-weight: 700; cursor: pointer; float: right; margin-top: -40px; }
                .btn-print:hover { background: #7dd3fc; }
                .diagram-section { background: var(--surface); border: 1px solid var(--border); border-radius: 16px; padding: 24px; margin-bottom: 24px; overflow-x: auto; }
                .section-title { font-size: 1.3rem; font-weight: 700; margin-bottom: 16px; display: flex; align-items: center; gap: 8px; }
                .nodes-grid { display: grid; grid-template-columns: 1fr; gap: 16px; margin-bottom: 24px; }
                .node-card { background: var(--surface); border: 1px solid var(--border); border-radius: 12px; padding: 20px; transition: transform 0.2s, border-color 0.2s; position: relative; }
                .node-card:hover { transform: translateY(-2px); border-color: var(--primary); }
                .node-header { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; font-size: 0.8rem; }
                .node-step { background: #334155; color: #fff; padding: 2px 8px; border-radius: 6px; font-weight: 700; }
                .node-badge { padding: 3px 10px; border-radius: 6px; font-weight: 600; font-size: 0.75rem; text-transform: uppercase; }
                .node-id { margin-left: auto; color: var(--text-muted); font-family: monospace; font-size: 0.75rem; }
                .node-title { font-size: 1.15rem; font-weight: 700; margin-bottom: 6px; }
                .node-desc { font-size: 0.95rem; color: var(--text-muted); margin-bottom: 14px; }
                .type-start { border-left: 6px solid var(--primary); }
                .type-start .node-badge { background: rgba(56, 189, 248, 0.2); color: var(--primary); }
                .type-decision { border-left: 6px solid var(--decision); }
                .type-decision .node-badge { background: rgba(99, 102, 241, 0.2); color: #818cf8; }
                .type-action { border-left: 6px solid var(--action); }
                .type-action .node-badge { background: rgba(245, 158, 11, 0.2); color: var(--action); }
                .type-success { border-left: 6px solid var(--success); }
                .type-success .node-badge { background: rgba(16, 185, 129, 0.2); color: var(--success); }
                .type-escalate { border-left: 6px solid var(--escalate); }
                .type-escalate .node-badge { background: rgba(244, 63, 94, 0.2); color: var(--escalate); }
                .branches-container { border-top: 1px dashed var(--border); padding-top: 12px; margin-top: 8px; }
                .branches-header { font-size: 0.8rem; font-weight: 600; color: var(--text-muted); margin-bottom: 8px; text-transform: uppercase; }
                .branch-pills { display: flex; flex-wrap: wrap; gap: 8px; }
                .branch-pill { background: rgba(255,255,255,0.05); border: 1px solid var(--border); padding: 6px 12px; border-radius: 8px; font-size: 0.85rem; display: flex; align-items: center; gap: 6px; }
                .branch-label { font-weight: 700; color: #38bdf8; }
                .branch-arrow { color: var(--text-muted); }
                .branch-target { color: #e2e8f0; font-size: 0.8rem; }
                .session-card { background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.3); border-radius: 12px; padding: 20px; margin-bottom: 24px; }
                .session-card h3 { color: var(--success); margin-bottom: 8px; }
                .status-tag { background: var(--success); color: #0f172a; padding: 2px 8px; border-radius: 4px; font-weight: 700; font-size: 0.8rem; }
                .timeline { margin-top: 16px; border-left: 2px solid var(--border); margin-left: 12px; padding-left: 16px; }
                .timeline-step { position: relative; margin-bottom: 16px; }
                .timeline-step .step-num { position: absolute; left: -25px; top: 0; background: var(--primary); color: #0f172a; width: 18px; height: 18px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 0.65rem; font-weight: 800; }
                .step-title { font-weight: 600; }
                .step-choice { font-size: 0.85rem; color: var(--primary); margin-top: 2px; }
                .footer { text-align: center; color: var(--text-muted); font-size: 0.85rem; margin-top: 32px; border-top: 1px solid var(--border); padding-top: 16px; }
                @media print {
                    body { background: #fff !important; color: #000 !important; padding: 0 !important; }
                    .header, .diagram-section, .node-card { background: #fff !important; color: #000 !important; border: 1px solid #ccc !important; box-shadow: none !important; }
                    .btn-print { display: none !important; }
                    h1, .node-title { color: #000 !important; }
                    .description, .node-desc, .meta-stats { color: #444 !important; }
                    .node-step { background: #eee !important; color: #000 !important; }
                    .branch-pill { background: #f8f8f8 !important; border-color: #ddd !important; color: #000 !important; }
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <button class="btn-print" onclick="window.print()">Print / Export PDF</button>
                    <span class="category-badge">${escapeHtml(flowchart.category)}</span>
                    <h1>${escapeHtml(flowchart.title)}</h1>
                    <p class="description">${escapeHtml(flowchart.description)}</p>
                    <div class="meta-stats">
                        <div class="stat-item">Total Nodes: <strong>${flowchart.nodes.size}</strong></div>
                        <div class="stat-item">Decisions: <strong>$decisionCount</strong></div>
                        <div class="stat-item">Actions: <strong>$actionCount</strong></div>
                        <div class="stat-item">Resolved Paths: <strong>$successCount</strong></div>
                        <div class="stat-item">Escalations: <strong>$escalateCount</strong></div>
                        <div class="stat-item">Generated: <strong>$dateString</strong></div>
                    </div>
                </div>

                $sessionHtml

                <div class="diagram-section">
                    <h3 class="section-title">📊 Visual Flowchart Flow</h3>
                    $svgContent
                </div>

                <div class="nodes-section">
                    <h3 class="section-title">📋 Diagnostic Step & Logic Matrix</h3>
                    <div class="nodes-grid">
                        $nodeCardsHtml
                    </div>
                </div>

                <div class="footer">
                    Generated by SolveFlow &bull; Problem Solving Flowchart Program &bull; GitHub Actions APK Capable
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    private fun generateSvgDiagram(flowchart: FlowchartData, session: DiagnosticSession?): String {
        val nodeWidth = 240
        val nodeHeight = 70
        val gapY = 50
        val totalNodes = flowchart.nodes.size
        val svgHeight = maxOf(400, totalNodes * (nodeHeight + gapY) + 80)
        val svgWidth = 800

        val sessionVisitedIds = session?.steps?.map { it.nodeId }?.toSet() ?: emptySet()

        val sb = StringBuilder()
        sb.append("""<svg viewBox="0 0 $svgWidth $svgHeight" width="100%" height="$svgHeight" xmlns="http://www.w3.org/2000/svg" style="background:#0f172a; border-radius:12px; display:block;">""")
        sb.append("""
            <defs>
                <marker id="arrowhead" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto">
                    <polygon points="0 0, 8 3, 0 6" fill="#64748b" />
                </marker>
                <marker id="arrowhead-active" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto">
                    <polygon points="0 0, 8 3, 0 6" fill="#38bdf8" />
                </marker>
            </defs>
        """.trimIndent())

        // Compute positions for nodes in a clean vertical column layout with branches
        val nodePositions = mutableMapOf<String, Pair<Int, Int>>()
        var currentY = 40
        flowchart.nodes.forEachIndexed { index, node ->
            val x = (svgWidth - nodeWidth) / 2
            val y = currentY
            nodePositions[node.id] = Pair(x, y)
            currentY += nodeHeight + gapY
        }

        // Draw connection lines first
        flowchart.nodes.forEach { node ->
            val fromPos = nodePositions[node.id] ?: return@forEach
            val fromX = fromPos.first + nodeWidth / 2
            val fromY = fromPos.second + nodeHeight

            node.branches.forEach { branch ->
                val toPos = nodePositions[branch.targetNodeId]
                if (toPos != null) {
                    val toX = toPos.first + nodeWidth / 2
                    val toY = toPos.second

                    val isStepTaken = sessionVisitedIds.contains(node.id) && sessionVisitedIds.contains(branch.targetNodeId)
                    val strokeColor = if (isStepTaken) "#38bdf8" else "#475569"
                    val strokeWidth = if (isStepTaken) "3" else "1.5"
                    val marker = if (isStepTaken) "url(#arrowhead-active)" else "url(#arrowhead)"

                    if (fromX == toX && toY > fromY) {
                        // Direct vertical
                        sb.append("""<line x1="$fromX" y1="$fromY" x2="$toX" y2="${toY - 2}" stroke="$strokeColor" stroke-width="$strokeWidth" marker-end="$marker" />""")
                        // Branch label
                        val labelY = (fromY + toY) / 2
                        sb.append("""<rect x="${fromX - 45}" y="${labelY - 10}" width="90" height="18" rx="4" fill="#1e293b" stroke="$strokeColor" stroke-width="1" />""")
                        sb.append("""<text x="$fromX" y="${labelY + 3}" fill="#cbd5e1" font-size="10" font-weight="bold" text-anchor="middle">${escapeHtml(truncate(branch.label, 14))}</text>""")
                    } else {
                        // Curved branch
                        val midY = (fromY + toY) / 2
                        val pathD = "M $fromX $fromY C $fromX $midY, $toX $midY, $toX ${toY - 2}"
                        sb.append("""<path d="$pathD" fill="none" stroke="$strokeColor" stroke-width="$strokeWidth" marker-end="$marker" />""")
                    }
                }
            }
        }

        // Draw nodes
        flowchart.nodes.forEach { node ->
            val pos = nodePositions[node.id] ?: return@forEach
            val x = pos.first
            val y = pos.second

            val isVisited = sessionVisitedIds.contains(node.id)
            val strokeColor = if (isVisited) "#38bdf8" else "#334155"
            val strokeWidth = if (isVisited) "2.5" else "1"

            val fillColor = when (node.type) {
                NodeType.START -> "#0369a1"
                NodeType.DECISION -> "#4338ca"
                NodeType.ACTION -> "#b45309"
                NodeType.OUTCOME_SUCCESS -> "#047857"
                NodeType.OUTCOME_ESCALATE -> "#be123c"
                NodeType.NOTE -> "#334155"
            }

            val badgeText = when (node.type) {
                NodeType.START -> "START"
                NodeType.DECISION -> "DECISION"
                NodeType.ACTION -> "ACTION"
                NodeType.OUTCOME_SUCCESS -> "RESOLVED"
                NodeType.OUTCOME_ESCALATE -> "ESCALATE"
                NodeType.NOTE -> "NOTE"
            }

            sb.append("""<g id="svg-node-${node.id}" style="cursor:pointer;">""")
            sb.append("""<rect x="$x" y="$y" width="$nodeWidth" height="$nodeHeight" rx="10" fill="$fillColor" stroke="$strokeColor" stroke-width="$strokeWidth" />""")
            sb.append("""<text x="${x + 12}" y="${y + 20}" fill="#94a3b8" font-size="10" font-weight="bold">${node.stepNumber}. $badgeText</text>""")
            sb.append("""<text x="${x + 12}" y="${y + 42}" fill="#ffffff" font-size="12" font-weight="bold">${escapeHtml(truncate(node.title, 28))}</text>""")
            sb.append("""</g>""")
        }

        sb.append("</svg>")
        return sb.toString()
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    private fun truncate(text: String, maxLen: Int): String {
        return if (text.length > maxLen) text.take(maxLen - 3) + "..." else text
    }
}
