package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Source
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualArticleImportDialog(
    sources: List<Source>,
    isImporting: Boolean,
    errorMessage: String?,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onImport: (url: String, sourceId: Long, customTitle: String?, customSnippet: String?, notes: String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    var selectedSourceId by remember { mutableStateOf(sources.firstOrNull()?.id ?: 1L) }
    var customTitle by remember { mutableStateOf("") }
    var customSnippet by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var sourceDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("manual_import_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = IntelCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isArabic) "إدخال خبر يدوي عبر الرابط" else "Manual URL News Import",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isArabic) "استخراج البيانات الوصفية تلقائياً وتوثيق المصدر" else "Auto-extract metadata & bind to intelligence source",
                            style = MaterialTheme.typography.bodySmall,
                            color = IntelSlate
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = IntelSlate)
                    }
                }

                HorizontalDivider(color = IntelBorder)

                // Error Banner
                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        color = Color(0x22EF4444),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = IntelRed)
                            Text(
                                text = errorMessage,
                                color = IntelRed,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Source Selector
                Column {
                    Text(
                        text = if (isArabic) "المصدر التابع له الخبر" else "Origin Source",
                        fontSize = 12.sp,
                        color = IntelSlate,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = sourceDropdownExpanded,
                        onExpandedChange = { sourceDropdownExpanded = !sourceDropdownExpanded }
                    ) {
                        val currentSource = sources.firstOrNull { it.id == selectedSourceId }
                        OutlinedTextField(
                            value = currentSource?.nameAr ?: (if (isArabic) "اختر المصدر" else "Select Source"),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("manual_source_selector"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IntelBlue,
                                unfocusedBorderColor = IntelBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = sourceDropdownExpanded,
                            onDismissRequest = { sourceDropdownExpanded = false },
                            modifier = Modifier.background(IntelNavyDark)
                        ) {
                            sources.forEach { source ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = if (isArabic) source.nameAr else source.nameEn,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "${source.tier.displayNameAr()} • ${source.countryCode}",
                                                color = IntelSlate,
                                                fontSize = 11.sp
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedSourceId = source.id
                                        sourceDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // URL Input
                Column {
                    Text(
                        text = if (isArabic) "رابط الخبر الكامل (URL) *" else "Full Article URL *",
                        fontSize = 12.sp,
                        color = IntelSlate,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        placeholder = { Text("https://example.com/news/12345", color = IntelSlate.copy(alpha = 0.6f)) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = IntelBlue) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_url_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IntelBlue,
                            unfocusedBorderColor = IntelBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                // Optional Custom Title Override
                Column {
                    Text(
                        text = if (isArabic) "عنوان الخبر (اختياري - يترك فارغاً للاستخراج التلقائي)" else "Custom Title (Optional)",
                        fontSize = 12.sp,
                        color = IntelSlate
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = customTitle,
                        onValueChange = { customTitle = it },
                        placeholder = { Text(if (isArabic) "عنوان الخبر المستخرج..." else "Article title...", color = IntelSlate.copy(alpha = 0.5f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_title_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IntelBlue,
                            unfocusedBorderColor = IntelBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                // Optional Custom Snippet
                Column {
                    Text(
                        text = if (isArabic) "الملخص أو الفقرة الاستدلالية (اختياري)" else "Snippet / Lead Paragraph (Optional)",
                        fontSize = 12.sp,
                        color = IntelSlate
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = customSnippet,
                        onValueChange = { customSnippet = it },
                        placeholder = { Text(if (isArabic) "ملخص موجز للخبر..." else "Brief summary...", color = IntelSlate.copy(alpha = 0.5f)) },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_snippet_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IntelBlue,
                            unfocusedBorderColor = IntelBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                // Analyst Notes
                Column {
                    Text(
                        text = if (isArabic) "ملاحظات وتوجيهات المحلل" else "Analyst Directive Notes",
                        fontSize = 12.sp,
                        color = IntelSlate
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text(if (isArabic) "سبب الإدخال اليدوي، درجة الأهمية، أو ربط سياقي..." else "Reason for manual ingestion...", color = IntelSlate.copy(alpha = 0.5f)) },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_notes_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IntelBlue,
                            unfocusedBorderColor = IntelBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = IntelSlate)
                    ) {
                        Text(if (isArabic) "إلغاء" else "Cancel")
                    }

                    Button(
                        onClick = {
                            if (url.isNotBlank()) {
                                onImport(
                                    url.trim(),
                                    selectedSourceId,
                                    customTitle.takeIf { it.isNotBlank() },
                                    customSnippet.takeIf { it.isNotBlank() },
                                    notes.trim()
                                )
                            }
                        },
                        enabled = url.isNotBlank() && !isImporting,
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("submit_manual_import_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IntelBlue)
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isArabic) "جارٍ الاستخراج..." else "Extracting...")
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "استخراج وحفظ الخبر" else "Extract & Ingest")
                        }
                    }
                }
            }
        }
    }
}
