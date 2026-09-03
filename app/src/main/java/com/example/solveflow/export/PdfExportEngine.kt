package com.example.solveflow.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.solveflow.data.model.DiagnosticSession
import com.example.solveflow.data.model.FlowchartData
import com.example.solveflow.data.model.NodeType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExportEngine {

    fun generatePdf(context: Context, flowchart: FlowchartData, session: DiagnosticSession? = null): File {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 width in points
        val pageHeight = 842 // A4 height in points
        val margin = 36f

        val paintTitle = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintCategory = Paint().apply {
            color = Color.rgb(14, 116, 144)
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintSub = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 9.5f
            isAntiAlias = true
        }

        val paintCardTitle = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintBadge = Paint().apply {
            color = Color.WHITE
            textSize = 8.5f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val paintBranch = Paint().apply {
            color = Color.rgb(2, 132, 199)
            textSize = 9f
            isAntiAlias = true
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var cursorY = margin + 10f

        fun drawHeader(c: Canvas) {
            c.drawText("SOLVEFLOW PROBLEM-SOLVING SPECIFICATION", margin, cursorY, paintCategory)
            cursorY += 18f
            c.drawText(flowchart.title, margin, cursorY, paintTitle)
            cursorY += 16f
            c.drawText(flowchart.description, margin, cursorY, paintSub)
            cursorY += 18f

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val meta = "Category: ${flowchart.category} | Nodes: ${flowchart.nodes.size} | Generated: ${dateFormat.format(Date())}"
            c.drawText(meta, margin, cursorY, paintSub)
            cursorY += 14f

            // Horizontal divider line
            val dividerPaint = Paint().apply {
                color = Color.rgb(203, 213, 225)
                strokeWidth = 1f
            }
            c.drawLine(margin, cursorY, pageWidth - margin, cursorY, dividerPaint)
            cursorY += 18f
        }

        drawHeader(canvas)

        // If session exists, draw brief session box
        if (session != null) {
            val sessionBox = RectF(margin, cursorY, pageWidth - margin, cursorY + 44f)
            val sessionPaint = Paint().apply {
                color = Color.rgb(240, 253, 244)
                style = Paint.Style.FILL
            }
            val sessionBorder = Paint().apply {
                color = Color.rgb(187, 247, 208)
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            canvas.drawRoundRect(sessionBox, 6f, 6f, sessionPaint)
            canvas.drawRoundRect(sessionBox, 6f, 6f, sessionBorder)

            val sTitlePaint = Paint().apply {
                color = Color.rgb(21, 128, 61)
                textSize = 9.5f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText("Verified Diagnostic Session: ${session.finalStatus} (${session.steps.size} steps evaluated)", margin + 10f, cursorY + 18f, sTitlePaint)
            canvas.drawText("Completed: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(session.completedAt))}", margin + 10f, cursorY + 34f, paintSub)
            cursorY += 56f
        }

        // Draw each node as a structured card
        flowchart.nodes.forEach { node ->
            val estimatedCardHeight = 65f + (node.branches.size * 14f)

            // Check if we need a new page
            if (cursorY + estimatedCardHeight > pageHeight - margin - 20f) {
                // Draw footer on current page
                canvas.drawText("Page $pageNumber", pageWidth / 2f - 15f, pageHeight - 15f, paintSub)
                pdfDocument.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                cursorY = margin + 10f

                // Re-draw minimal header
                canvas.drawText("${flowchart.title} (Continued) - Page $pageNumber", margin, cursorY, paintCategory)
                cursorY += 20f
            }

            // Card background & accent color
            val cardRect = RectF(margin, cursorY, pageWidth - margin, cursorY + estimatedCardHeight - 8f)
            val cardBg = Paint().apply {
                color = Color.rgb(248, 250, 252)
                style = Paint.Style.FILL
            }
            val cardBorder = Paint().apply {
                color = Color.rgb(226, 232, 240)
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            canvas.drawRoundRect(cardRect, 6f, 6f, cardBg)
            canvas.drawRoundRect(cardRect, 6f, 6f, cardBorder)

            // Left accent bar
            val accentColor = when (node.type) {
                NodeType.START -> Color.rgb(2, 132, 199)
                NodeType.DECISION -> Color.rgb(79, 70, 229)
                NodeType.ACTION -> Color.rgb(217, 119, 6)
                NodeType.OUTCOME_SUCCESS -> Color.rgb(16, 185, 129)
                NodeType.OUTCOME_ESCALATE -> Color.rgb(225, 29, 72)
                NodeType.NOTE -> Color.rgb(100, 116, 139)
            }
            val accentPaint = Paint().apply { color = accentColor }
            val accentRect = RectF(margin, cursorY, margin + 5f, cursorY + estimatedCardHeight - 8f)
            canvas.drawRoundRect(accentRect, 3f, 3f, accentPaint)

            // Step & Type badge
            val badgeText = "${node.stepNumber}. ${node.type.name}"
            val badgeBg = RectF(margin + 12f, cursorY + 8f, margin + 12f + (badgeText.length * 6f) + 12f, cursorY + 22f)
            canvas.drawRoundRect(badgeBg, 4f, 4f, accentPaint)
            canvas.drawText(badgeText, margin + 18f, cursorY + 18.5f, paintBadge)

            // Node title
            canvas.drawText(node.title, margin + 12f, cursorY + 36f, paintCardTitle)

            // Node description
            if (node.description.isNotBlank()) {
                val shortDesc = if (node.description.length > 90) node.description.take(87) + "..." else node.description
                canvas.drawText(shortDesc, margin + 12f, cursorY + 49f, paintSub)
            }

            // Branches
            var branchY = cursorY + 63f
            node.branches.forEach { b ->
                val targetNode = flowchart.nodes.find { it.id == b.targetNodeId }
                val targetTitle = targetNode?.title ?: b.targetNodeId
                val branchText = "↳ Choice: [${b.label}] ➔ Next: $targetTitle"
                val truncatedBranch = if (branchText.length > 85) branchText.take(82) + "..." else branchText
                canvas.drawText(truncatedBranch, margin + 20f, branchY, paintBranch)
                branchY += 13f
            }

            cursorY += estimatedCardHeight
        }

        // Draw final page footer
        canvas.drawText("Page $pageNumber | SolveFlow Export", pageWidth / 2f - 40f, pageHeight - 15f, paintSub)
        pdfDocument.finishPage(page)

        // Save PDF to cache/exports directory
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeName = flowchart.title.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]"), "_")
        val file = File(exportDir, "flowchart_${safeName}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return file
    }
}
