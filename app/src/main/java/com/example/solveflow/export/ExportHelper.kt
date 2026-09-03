package com.example.solveflow.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.solveflow.data.model.DiagnosticSession
import com.example.solveflow.data.model.FlowchartData
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object ExportHelper {

    fun exportHtml(context: Context, flowchart: FlowchartData, session: DiagnosticSession? = null): File {
        val htmlContent = HtmlExportEngine.generateHtml(flowchart, session)
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeName = flowchart.title.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]"), "_")
        val file = File(exportDir, "flowchart_${safeName}_${System.currentTimeMillis()}.html")
        FileOutputStream(file).use { out ->
            out.write(htmlContent.toByteArray(Charsets.UTF_8))
        }
        return file
    }

    fun exportPdf(context: Context, flowchart: FlowchartData, session: DiagnosticSession? = null): File {
        return PdfExportEngine.generatePdf(context, flowchart, session)
    }

    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share $title"))
    }
}
