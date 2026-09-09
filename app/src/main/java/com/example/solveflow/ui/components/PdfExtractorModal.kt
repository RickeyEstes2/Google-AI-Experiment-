package com.example.solveflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.solveflow.data.model.ExtractedPdfDoc
import com.example.solveflow.engine.pdf.PdfExtractorEngine
import com.example.solveflow.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfExtractorModal(
    onDismiss: () -> Unit,
    onAskInChat: (String) -> Unit
) {
    val documents = remember { PdfExtractorEngine.sampleDocuments }
    var selectedDocIndex by remember { mutableStateOf(0) }
    var selectedPageNumber by remember { mutableStateOf(1) }
    var searchQuery by remember { mutableStateOf("") }

    val currentDoc = documents[selectedDocIndex]
    val currentPage = currentDoc.pages.firstOrNull { it.pageNumber == selectedPageNumber } ?: currentDoc.pages.first()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Zinc900,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Zinc700) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
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
                        Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = Rose500,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "PDF Knowledge Extractor",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFAFAFA)
                        )
                        Text(
                            text = currentDoc.title,
                            fontSize = 12.sp,
                            color = Emerald400
                        )
                    }
                }

                // Document switcher
                TextButton(
                    onClick = {
                        selectedDocIndex = (selectedDocIndex + 1) % documents.size
                        selectedPageNumber = 1
                    }
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Emerald400, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Switch PDF", fontSize = 11.sp, color = Emerald400)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Page Selector Tabs
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                currentDoc.pages.forEach { page ->
                    val isSelected = selectedPageNumber == page.pageNumber
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPageNumber = page.pageNumber },
                        label = { Text("Page ${page.pageNumber}", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Emerald500.copy(alpha = 0.25f),
                            selectedLabelColor = Emerald400
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Page Content Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Zinc800.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, Zinc700, RoundedCornerShape(12.dp))
            ) {
                LazyColumn(modifier = Modifier.padding(14.dp)) {
                    item {
                        Text(
                            text = currentPage.header,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFAFAFA)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentPage.textContent,
                            fontSize = 13.sp,
                            color = Color(0xFFE4E4E7),
                            lineHeight = 18.sp
                        )
                    }

                    if (currentPage.formulasDetected.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "EXTRACTED MATHEMATICAL FORMULAS:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald400,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        items(currentPage.formulasDetected) { formula ->
                            DisplayMathBlock(
                                rawFormula = formula,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions: Ask in Chat
            Button(
                onClick = {
                    val prompt = "Explain and derive the concepts from Page ${currentPage.pageNumber} of \"${currentDoc.title}\":\n${currentPage.textContent}"
                    onAskInChat(prompt)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = Color(0xFF022C22)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Analyze Page with Self-Improving CoT", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
