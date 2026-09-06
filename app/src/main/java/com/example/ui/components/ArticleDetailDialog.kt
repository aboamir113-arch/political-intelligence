package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ArticleDetailDialog(
    article: Article,
    files: List<PoliticalFile>,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onToggleSave: () -> Unit,
    onLinkToFile: (PoliticalFile) -> Unit,
    onClusterArticle: (() -> Unit)? = null,
    summaryResult: ArticleSummaryResult? = null,
    isAiLoading: Boolean = false,
    onGenerateSummary: ((forceRegenerate: Boolean, language: String) -> Unit)? = null,
    onTraceSource: (() -> Unit)? = null,
    onAddToWorkspace: (() -> Unit)? = null
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm 'UTC'", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") } }
    var showLinkFileDialog by remember { mutableStateOf(false) }
    var clusteredFeedback by remember { mutableStateOf<String?>(null) }
    var summaryLanguage by remember { mutableStateOf("ar") }
    var selectedSummaryMode by remember { mutableStateOf(0) } // 0: Key Points, 1: Short, 2: Detailed, 3: Numbers

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("article_detail_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = IntelCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ArticleClassificationBadge(article.classification)
                        SourceTierBadge(article.sourceTier)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onToggleSave, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = if (article.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Save",
                                tint = if (article.isSaved) IntelGold else IntelSlate
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = IntelSlate)
                        }
                    }
                }

                // Recirculated Warning Banner
                if (article.classification == ArticleClassification.RECIRCULATED) {
                    Surface(
                        color = Color(0x22F59E0B),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = IntelGold)
                            Column {
                                Text(
                                    text = if (isArabic) "تنبيه خبر معاد التدوير (Recirculated News)" else "Recirculated News Warning",
                                    color = IntelGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = if (isArabic)
                                        "رصد النظام أن هذا الخبر يعيد نشر وقائع قديمة دون تطورات جوهرية جديدة."
                                    else
                                        "System flagged this article as recirculating older events without new substantive delta.",
                                    color = IntelSlate,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Duplicate Warning Banner
                if (article.isDuplicate) {
                    Surface(
                        color = Color(0x2264748B),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = IntelSlate)
                            Text(
                                text = if (isArabic) "نسخة مكررة من خبر أساسي محفوظ في قاعدة البيانات" else "Duplicate of primary article",
                                color = IntelSlate,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Title
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 24.sp
                )

                // Source Info Card
                Surface(
                    color = IntelNavyDark,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IntelBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = article.sourceName,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "طريقة الجلب: ${article.ingestionMethod.displayNameAr()}",
                                    color = IntelSlate,
                                    fontSize = 11.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = dateFormat.format(Date(article.publishedAt)),
                                    color = IntelSlate,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "وقت الرصد: ${dateFormat.format(Date(article.fetchedAt))}",
                                    color = IntelSlate.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Source Independence Attribution
                        if (!article.originalAgency.isNullOrBlank()) {
                            HorizontalDivider(color = IntelBorder.copy(alpha = 0.5f))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = IntelBlue, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "المصدر المرجعي الأصلي للخبر: ${article.originalAgency}",
                                    color = IntelBlue,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Republishing Outlets
                        val republishingList = remember(article.republishingSourcesJson) {
                            try {
                                if (article.republishingSourcesJson.isNotBlank()) {
                                    val arr = JSONArray(article.republishingSourcesJson)
                                    (0 until arr.length()).map { arr.getString(it) }
                                } else emptyList()
                            } catch (_: Exception) {
                                emptyList()
                            }
                        }
                        if (republishingList.isNotEmpty() || article.duplicateCount > 0) {
                            HorizontalDivider(color = IntelBorder.copy(alpha = 0.5f))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "تكرار النشر عبر الوكالات (${article.duplicateCount + republishingList.size}):",
                                    color = IntelSlate,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    republishingList.forEach { repSource ->
                                        Surface(
                                            color = Color(0x2238BDF8),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = repSource,
                                                color = IntelBlue,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Lead Snippet / Content
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isArabic) "نص الخبر المستخلص" else "Extracted Content",
                        color = IntelSlate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = IntelNavyDark.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = article.fullText?.takeIf { it.isNotBlank() } ?: article.snippet,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // =========================================================================
                // Phase 4: AI Summary & Source Tracing Section
                // =========================================================================
                Surface(
                    color = IntelNavyDark,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IntelCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("article_ai_summary_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Title & Actions Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = IntelCyan, modifier = Modifier.size(18.dp))
                                Text(
                                    text = if (isArabic) "التلخيص والتحليل الذكي (AI Analysis)" else "AI Intelligence Summary",
                                    color = IntelWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            // Language Selector
                            Row(
                                modifier = Modifier
                                    .background(DeskDarkSurface, RoundedCornerShape(8.dp))
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Surface(
                                    color = if (summaryLanguage == "ar") IntelCyan else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.clickable {
                                        summaryLanguage = "ar"
                                        onGenerateSummary?.invoke(false, "ar")
                                    }
                                ) {
                                    Text(
                                        text = "عربي",
                                        color = if (summaryLanguage == "ar") Color(0xFF031424) else IntelSlate,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    color = if (summaryLanguage == "en") IntelCyan else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.clickable {
                                        summaryLanguage = "en"
                                        onGenerateSummary?.invoke(false, "en")
                                    }
                                ) {
                                    Text(
                                        text = "EN",
                                        color = if (summaryLanguage == "en") Color(0xFF031424) else IntelSlate,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Summary Content or Generate Button
                        if (isAiLoading) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(color = IntelCyan, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isArabic) "جاري المعالجة بواسطة الذكاء الاصطناعي..." else "Processing AI summary...",
                                    color = IntelSlate,
                                    fontSize = 12.sp
                                )
                            }
                        } else if (summaryResult != null) {
                            // Mode Chips (Key Points, Short, Detailed, Numbers)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val modes = listOf(
                                    if (isArabic) "نقاط رئيسية" else "Key Points",
                                    if (isArabic) "موجز" else "Short",
                                    if (isArabic) "مفصل" else "Detailed",
                                    if (isArabic) "أرقام ووقائع" else "Numbers"
                                )
                                modes.forEachIndexed { idx, label ->
                                    Surface(
                                        color = if (selectedSummaryMode == idx) IntelCyan.copy(alpha = 0.2f) else DeskDarkSurface,
                                        shape = RoundedCornerShape(6.dp),
                                        border = if (selectedSummaryMode == idx) androidx.compose.foundation.BorderStroke(1.dp, IntelCyan) else null,
                                        modifier = Modifier.clickable { selectedSummaryMode = idx }
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (selectedSummaryMode == idx) IntelCyan else IntelSlate,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Render text based on selected mode
                            Surface(
                                color = DeskDarkSurface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val contentText = when (selectedSummaryMode) {
                                    1 -> summaryResult.shortSummary
                                    2 -> summaryResult.detailedSummary
                                    3 -> if (summaryResult.numbersAndFacts.isNotEmpty()) summaryResult.numbersAndFacts.joinToString("\n• ", prefix = "• ") else if (isArabic) "لم يتم رصد إحصائيات عددية محددة." else "No specific numerical statistics extracted."
                                    else -> if (summaryResult.keyBulletPoints.isNotEmpty()) summaryResult.keyBulletPoints.joinToString("\n• ", prefix = "• ") else summaryResult.shortSummary
                                }
                                Text(
                                    text = contentText,
                                    color = IntelWhite,
                                    fontSize = 12.sp,
                                    lineHeight = 19.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }

                            // Model used & Timestamp footer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "النموذج: ${summaryResult.modelUsed} | الدقة: ${(summaryResult.confidenceScore * 100).toInt()}%",
                                    color = IntelSlate,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )

                                TextButton(
                                    onClick = { onGenerateSummary?.invoke(true, summaryLanguage) },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = IntelCyan, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isArabic) "إعادة التوليد" else "Regenerate", color = IntelCyan, fontSize = 11.sp)
                                }
                            }
                        } else {
                            // Empty state: Prompt to generate
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isArabic) "استخلاص الملخص التنفيذي، الأرقام وسلسلة الإسناد" else "Extract executive summary, facts & attribution",
                                    color = IntelSlate,
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                Button(
                                    onClick = { onGenerateSummary?.invoke(false, summaryLanguage) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = IntelCyan, contentColor = Color(0xFF031424)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("generate_article_summary_button")
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isArabic) "توليد التلخيص" else "Generate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Trace Original Source Action
                        if (onTraceSource != null) {
                            HorizontalDivider(color = DeskDarkBorder.copy(alpha = 0.5f))
                            OutlinedButton(
                                onClick = onTraceSource,
                                modifier = Modifier.fillMaxWidth().testTag("trace_source_button"),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, IntelBlue.copy(alpha = 0.7f)),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.TravelExplore, contentDescription = null, tint = IntelBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isArabic) "تتبع المصدر الأول والناقل الأصلي (Source Tracing)" else "Trace Original Source & Attribution",
                                    color = IntelBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Linked Political Dossier
                Surface(
                    color = IntelNavyDark,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IntelBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isArabic) "الملف السياسي المرتبط" else "Linked Political Dossier",
                                color = IntelSlate,
                                fontSize = 11.sp
                            )
                            Text(
                                text = article.politicalFileTitle ?: (if (isArabic) "غير مرتبط بملف حالياً" else "Not linked to a file"),
                                color = if (article.politicalFileTitle != null) IntelGold else IntelSlate,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = { showLinkFileDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IntelNavyLight)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (article.politicalFileTitle != null) (if (isArabic) "تغيير" else "Change") else (if (isArabic) "ربط بملف" else "Link"), fontSize = 11.sp)
                        }
                    }
                }

                // Phase 3: Cluster into Event / Extract Claims Action
                if (onClusterArticle != null) {
                    Button(
                        onClick = {
                            onClusterArticle.invoke()
                            clusteredFeedback = if (isArabic) "تمت معالجة الخبر وتحديث إضبارة الحدث والأدلة بنجاح ✓" else "Article clustered & evidence extracted successfully ✓"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IntelCyan,
                            contentColor = Color(0xFF031424)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "تجميع في حدث سياسي واستخلاص الادعاءات" else "Cluster into Event & Extract Claims",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Add to Research Workspace Action
                if (onAddToWorkspace != null) {
                    OutlinedButton(
                        onClick = {
                            onAddToWorkspace.invoke()
                            clusteredFeedback = if (isArabic) "تمت إضافة المادة إلى مساحة البحث والتحليل ✓" else "Added to Research Workspace ✓"
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = IntelCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, IntelCyan.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.TravelExplore, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "إضافة إلى مساحة البحث والتحليل (Workspace)" else "Add to Research Workspace",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                clusteredFeedback?.let { msg ->
                    Surface(
                        color = Color(0x2210B981),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = msg,
                            color = IntelGreen,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Topic & Linked Entities
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isArabic) "الكيانات والموضوعات المستخلصة" else "Entities & Extracted Topics",
                        color = IntelSlate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (!article.topicName.isNullOrBlank()) {
                            Surface(color = Color(0x2210B981), shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    text = article.topicName ?: "",
                                    color = IntelGreen,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        if (!article.primaryCountryCode.isNullOrBlank()) {
                            Surface(color = Color(0x2238BDF8), shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    text = "دولة: ${article.primaryCountryCode}",
                                    color = IntelBlue,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Audit & Hashing Verification
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isArabic) "معلومات التدقيق والتشفير" else "Audit & Integrity Hash",
                        color = IntelSlate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Content SHA-256: ${article.contentHash}",
                        color = IntelSlate.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "URL: ${article.originalUrl}",
                        color = IntelBlue.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }

    // Link File Picker Dialog
    if (showLinkFileDialog) {
        Dialog(onDismissRequest = { showLinkFileDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = IntelCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isArabic) "اختر الملف السياسي للربط" else "Select Political Dossier",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    HorizontalDivider(color = IntelBorder)
                    files.forEach { file ->
                        TextButton(
                            onClick = {
                                onLinkToFile(file)
                                showLinkFileDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                                Text(text = file.titleAr, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "الأولوية: ${file.priority.displayNameAr()}", color = IntelSlate, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleClassificationBadge(classification: ArticleClassification) {
    val (bg, fg) = when (classification) {
        ArticleClassification.NEW -> Pair(Color(0x2210B981), IntelGreen)
        ArticleClassification.UPDATE -> Pair(Color(0x2238BDF8), IntelBlue)
        ArticleClassification.DEVELOPMENT -> Pair(Color(0x228B5CF6), Color(0xFFA78BFA))
        ArticleClassification.CONFIRMATION -> Pair(Color(0x2206B6D4), Color(0xFF22D3EE))
        ArticleClassification.RECIRCULATED -> Pair(Color(0x22F59E0B), IntelGold)
        ArticleClassification.DUPLICATE -> Pair(Color(0x2264748B), IntelSlate)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, fg.copy(alpha = 0.4f))
    ) {
        Text(
            text = classification.displayNameAr(),
            color = fg,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
