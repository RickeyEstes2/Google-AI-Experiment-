package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MemoryViewModel
import java.io.File

@Composable
fun ExportSettingsTab(
    viewModel: MemoryViewModel,
    isAccessibilityActive: Boolean,
    exportStatusMessage: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showClearConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // -------------------------------------------------------------
        // CSV EXPORT SECTION
        // -------------------------------------------------------------
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth().testTag("csv_export_card")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(AppIcons.Export, contentDescription = null, tint = Emerald600, modifier = Modifier.size(22.dp))
                    Text(
                        text = "Export Memories to CSV (.csv)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Text(
                    text = "Export your entire cross-app memory database into an RFC-4180 compliant CSV file for backup, spreadsheets, or training custom local models.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    lineHeight = 18.sp
                )

                // CSV Schema Preview
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CardElevatedDark,
                    border = BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("CSV Schema Structure:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SkyBlue500)
                        Text(
                            text = "ID, Timestamp_Formatted, Timestamp_ISO, App_Name, Package_Name, Category, Title, Source_Type, Sentiment, Word_Count, Is_Starred, Tags, Addendums_Count, Text_Content, Addendums_Content",
                            fontSize = 10.5.sp,
                            color = TextLight,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 15.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.exportToCsv { uri ->
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                putExtra(Intent.EXTRA_SUBJECT, "CrossApp Memories Export.csv")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share or Save CSV Export"))
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("export_csv_button")
                ) {
                    Icon(AppIcons.Export, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export & Share CSV File", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                }

                if (exportStatusMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Emerald600.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Emerald600.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✅ $exportStatusMessage",
                            fontSize = 12.sp,
                            color = Emerald600,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // CROSS-APP AUTOMATIC CAPTURE INTEGRATION
        // -------------------------------------------------------------
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(AppIcons.Apps, contentDescription = null, tint = SkyBlue500, modifier = Modifier.size(22.dp))
                    Text(
                        text = "Cross-App Phone Monitor",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                // Status Banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isAccessibilityActive) Emerald600.copy(alpha = 0.15f) else CardElevatedDark,
                    border = BorderStroke(1.dp, if (isAccessibilityActive) Emerald600.copy(alpha = 0.4f) else BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isAccessibilityActive) "Active · Monitoring Screen Text" else "Accessibility Service Standby",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isAccessibilityActive) Emerald600 else TextLight
                            )
                            Text(
                                text = "Captures focused articles, chats, and apps in background",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Actions: Accessibility & Notifications
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Accessibility Settings", fontSize = 11.5.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Notification Access", fontSize = 11.5.sp)
                    }
                }

                // 3 Ways to Capture
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CardElevatedDark,
                    border = BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("3 Built-in Capture Methods:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SkyBlue500)
                        Text("1. Highlight Text in Any App → Choose 'Save to Memory' from context menu.", fontSize = 11.5.sp, color = TextLight)
                        Text("2. Share Sheet → Tap 'Share' on any webpage or note and select CrossApp Memory.", fontSize = 11.5.sp, color = TextLight)
                        Text("3. Live Accessibility Service → Automatically records viewed texts across apps.", fontSize = 11.5.sp, color = TextLight)
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // PRIVACY & ON-DEVICE GUARANTEES
        // -------------------------------------------------------------
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(AppIcons.Security, contentDescription = null, tint = SkyBlue500, modifier = Modifier.size(20.dp))
                    Text(
                        text = "100% Local-First Privacy",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                Text(
                    text = "• Zero Cloud Uploads: All text memories and vector indices are stored 100% offline inside your local SQLite Room database.\n• Password Filter: The engine automatically discards input fields marked with password/PIN flags.\n• Complete Data Portability: You can export your full database to CSV or wipe it anytime.",
                    fontSize = 12.5.sp,
                    color = Slate200,
                    lineHeight = 18.sp
                )
            }
        }

        // -------------------------------------------------------------
        // DATABASE TOOLS (Demo data / Wipe)
        // -------------------------------------------------------------
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Database Management",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            viewModel.populateSampleData()
                            Toast.makeText(context, "Loaded demo memories!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Preload Demo Data", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showClearConfirmation = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose600),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear All Data", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(90.dp))
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Clear All Memories?") },
            text = { Text("This will permanently delete all stored cross-app memories from your device database. Make sure you have exported your CSV file first.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllMemories()
                        showClearConfirmation = false
                        Toast.makeText(context, "Database cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose600)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
