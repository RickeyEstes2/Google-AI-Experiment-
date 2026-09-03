package com.example.solveflow.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLanguageDialog(
    onDismiss: () -> Unit,
    onAddLanguage: (name: String, extension: String, paradigm: String, boilerplate: String, keywords: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var extension by remember { mutableStateOf("") }
    var paradigm by remember { mutableStateOf("") }
    var boilerplate by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add New Language",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; errorMessage = null },
                        label = { Text("Language Name *") },
                        placeholder = { Text("e.g., Zig, Solidity, Elixir, Scala") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = extension,
                        onValueChange = { extension = it },
                        label = { Text("File Extension *") },
                        placeholder = { Text("e.g., .zig, .sol, .ex, .scala") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = paradigm,
                        onValueChange = { paradigm = it },
                        label = { Text("Programming Paradigm / Ecosystem") },
                        placeholder = { Text("e.g., Systems, Manual memory, Comptime, Smart Contracts") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = keywords,
                        onValueChange = { keywords = it },
                        label = { Text("Syntax Keywords (Comma-separated)") },
                        placeholder = { Text("e.g., fn, const, var, pub, defer, try, catch") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = boilerplate,
                        onValueChange = { boilerplate = it },
                        label = { Text("Standard Template / Boilerplate") },
                        placeholder = { Text("// Entry point or imports template...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || extension.isBlank()) {
                                errorMessage = "Language name and file extension are required."
                            } else {
                                onAddLanguage(
                                    name.trim(),
                                    extension.trim(),
                                    if (paradigm.isBlank()) "General Purpose" else paradigm.trim(),
                                    if (boilerplate.isBlank()) "// $name template\n" else boilerplate.trim(),
                                    keywords.trim()
                                )
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Language")
                    }
                }
            }
        }
    }
}
