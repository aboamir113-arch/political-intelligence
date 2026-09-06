package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeskUiState
import com.example.ui.viewmodel.DeskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsFeedScreen(
    viewModel: DeskViewModel,
    uiState: DeskUiState,
    modifier: Modifier = Modifier
) {
    val isAr = uiState.isArabic
    var showFilterSheet by remember { mutableStateOf(false) }
    var showLogsSheet by remember { mutableStateOf(false) }

    val dateFormat = remember {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openManualImportDialog() },
                containerColor = IntelBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("manual_import_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.AddLink,
                    contentDescription = if (isAr) "إدخال يدوي لرابط خبر" else "Import News URL"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header & Control Center
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isAr) "شريط الأخبار والرصد الاستخباري" else "Intelligence News Feed",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isAr)
                                    "رصد مباشر للمصادر الموثوقة مع كشف التكرار والتدوير المستمر"
                                else
                                    "Verified multi-source ingestion with duplicate & recirculation detection",
                                style = MaterialTheme.typography.bodySmall,
                                color = IntelSlate
                            )
                        }

                        // Logs Viewer Button
                        IconButton(
                            onClick = { showLogsSheet = true },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(IntelCard)
                                .border(1.dp, IntelBorder, RoundedCornerShape(12.dp))
                                .testTag("open_ingestion_logs_button")
                        ) {
                            Icon(Icons.Default.History, contentDescription = "Logs", tint = IntelBlue)
                        }
                    }

                    // Search Field
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = {
                            Text(
                                if (isAr) "ابحث في العناوين والنصوص والكيانات والمصادر..." else "Search titles, texts, entities...",
                                color = IntelSlate.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IntelBlue) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = IntelSlate)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("news_search_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = IntelCard,
                            unfocusedContainerColor = IntelCard,
                            focusedBorderColor = IntelBlue,
                            unfocusedBorderColor = IntelBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Ingestion KPI & Sync Bar
                    Surface(
                        color = IntelNavyDark,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, IntelBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column {
                                    Text(
                                        text = "${uiState.articlesTodayCount}",
                                        color = IntelGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = if (isAr) "أخبار اليوم" else "Today's",
                                        color = IntelSlate,
                                        fontSize = 10.sp
                                    )
                                }
                                Column {
                                    Text(
                                        text = "${uiState.duplicatesCount}",
                                        color = IntelGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = if (isAr) "المكررات المحجوبة" else "Filtered Dups",
                                        color = IntelSlate,
                                        fontSize = 10.sp
                                    )
                                }
                                Column {
                                    Text(
                                        text = "${uiState.totalArticlesCount}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = if (isAr) "إجمالي المقالات" else "Total Ingested",
                                        color = IntelSlate,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Sync All Sources Button
                            Button(
                                onClick = { viewModel.syncAllSourcesNow() },
                                enabled = !uiState.isIngestingGlobal,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IntelBlue),
                                modifier = Modifier.testTag("sync_all_sources_button")
                            ) {
                                if (uiState.isIngestingGlobal) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isAr) "جارٍ الرصد..." else "Syncing...", fontSize = 11.sp)
                                } else {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isAr) "مزامنة الكل" else "Sync All", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Sync Status Message
                    if (!uiState.syncFeedbackMessage.isNullOrBlank()) {
                        Surface(
                            color = Color(0x2238BDF8),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = uiState.syncFeedbackMessage ?: "",
                                color = IntelBlue,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Date Filters Row (Horizontal Scroll)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(DateFilter.values()) { filter ->
                            FilterChip(
                                selected = uiState.dateFilter == filter,
                                onClick = { viewModel.setDateFilter(filter) },
                                label = {
                                    Text(
                                        text = if (isAr) filter.displayNameAr() else filter.displayNameEn(),
                                        fontSize = 11.sp
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IntelBlue,
                                    selectedLabelColor = Color.White,
                                    containerColor = IntelCard,
                                    labelColor = IntelSlate
                                ),
                                modifier = Modifier.testTag("date_filter_${filter.name}")
                            )
                        }
                    }

                    // Filter Options Bar (Tiers, Classification, Toggles)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Saved Only Filter
                            FilterChip(
                                selected = uiState.onlySavedFilter,
                                onClick = { viewModel.toggleOnlySavedFilter() },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Bookmark,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (uiState.onlySavedFilter) IntelGold else IntelSlate
                                    )
                                },
                                label = { Text(if (isAr) "المحفوظة" else "Saved", fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0x33F59E0B),
                                    selectedLabelColor = IntelGold,
                                    containerColor = IntelCard,
                                    labelColor = IntelSlate
                                ),
                                modifier = Modifier.testTag("filter_saved_only")
                            )

                            // Show Duplicates Toggle
                            FilterChip(
                                selected = uiState.includeDuplicatesFilter,
                                onClick = { viewModel.toggleIncludeDuplicatesFilter() },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (uiState.includeDuplicatesFilter) Color.White else IntelSlate
                                    )
                                },
                                label = { Text(if (isAr) "إظهار المكررات" else "Duplicates", fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IntelNavyLight,
                                    selectedLabelColor = Color.White,
                                    containerColor = IntelCard,
                                    labelColor = IntelSlate
                                ),
                                modifier = Modifier.testTag("filter_include_duplicates")
                            )
                        }

                        // Reset All Filters
                        TextButton(
                            onClick = { viewModel.resetFilters() },
                            modifier = Modifier.testTag("reset_filters_button")
                        ) {
                            Icon(Icons.Default.FilterAltOff, contentDescription = null, modifier = Modifier.size(14.dp), tint = IntelSlate)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isAr) "إعادة ضبط" else "Reset", color = IntelSlate, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Results Counter
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAr) "النتائج المطابقة: ${uiState.articles.size}" else "Matching Articles: ${uiState.articles.size}",
                        color = IntelSlate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Empty State
            if (uiState.articles.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(containerColor = IntelCard),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = IntelSlate,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = if (isAr) "لا توجد أخبار مطابقة لمعايير البحث الحالية" else "No articles found matching filters",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isAr) "جرّب توسيع النطاق الزمني أو الضغط على مزامنة الكل لجلب برقيات جديدة" else "Try broadening the date range or trigger Sync All",
                                color = IntelSlate,
                                fontSize = 12.sp
                            )
                            Button(
                                onClick = { viewModel.syncAllSourcesNow() },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IntelBlue)
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isAr) "جلب الأخبار الآن" else "Sync Ingestion Now")
                            }
                        }
                    }
                }
            }

            // Articles Feed Cards
            items(uiState.articles, key = { it.id }) { article ->
                ArticleCard(
                    article = article,
                    dateFormat = dateFormat,
                    isArabic = isAr,
                    onOpenDetails = { viewModel.openArticleDetails(article) },
                    onToggleSave = { viewModel.toggleSaveArticle(article) }
                )
            }
        }
    }

    // Article Detail Dialog Modal
    if (uiState.activeArticleDetail != null) {
        val activeArticle = uiState.activeArticleDetail
        ArticleDetailDialog(
            article = activeArticle,
            files = uiState.files,
            isArabic = isAr,
            onDismiss = { viewModel.closeArticleDetails() },
            onToggleSave = { viewModel.toggleSaveArticle(activeArticle) },
            onLinkToFile = { file -> viewModel.linkArticleToFile(activeArticle.id, file) },
            onClusterArticle = { viewModel.clusterArticle(activeArticle) },
            summaryResult = uiState.currentArticleSummary,
            isAiLoading = uiState.isAiLoading,
            onGenerateSummary = { force, lang -> viewModel.generateArticleSummary(activeArticle.id, force, lang) },
            onTraceSource = { viewModel.traceOriginalSource(activeArticle.id) },
            onAddToWorkspace = { viewModel.addArticleToWorkspace(activeArticle) }
        )
    }

    // AI Intelligence Universal Dialog
    if (uiState.showAiIntelligenceDialog) {
        com.example.ui.components.AiIntelligenceDialog(
            viewModel = viewModel,
            uiState = uiState,
            onDismiss = { viewModel.setShowAiIntelligenceDialog(false) }
        )
    }

    // Manual URL Import Modal
    if (uiState.showManualImportDialog) {
        ManualArticleImportDialog(
            sources = uiState.sources,
            isImporting = uiState.isManualImporting,
            errorMessage = uiState.manualImportError,
            isArabic = isAr,
            onDismiss = { viewModel.closeManualImportDialog() },
            onImport = { url, sourceId, title, snippet, notes ->
                viewModel.importManualArticle(url, sourceId, title, snippet, null, notes)
            }
        )
    }

    // Ingestion Logs Bottom Sheet
    if (showLogsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLogsSheet = false },
            containerColor = IntelCard,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAr) "سجل عمليات الجلب والرصد الأخير" else "Ingestion Activity Logs",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = { showLogsSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = IntelSlate)
                    }
                }

                HorizontalDivider(color = IntelBorder)

                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.ingestionLogs) { log ->
                        Surface(
                            color = IntelNavyDark,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = log.sourceName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    val (stBg, stFg) = when (log.status) {
                                        IngestionStatus.SUCCESS -> Pair(Color(0x2210B981), IntelGreen)
                                        IngestionStatus.PARTIAL -> Pair(Color(0x22F59E0B), IntelGold)
                                        IngestionStatus.FAILED -> Pair(Color(0x22EF4444), IntelRed)
                                        IngestionStatus.NO_NEW_ITEMS -> Pair(Color(0x2264748B), IntelSlate)
                                    }
                                    Surface(color = stBg, shape = RoundedCornerShape(6.dp)) {
                                        Text(
                                            text = log.status.displayNameAr(),
                                            color = stFg,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "جلب: ${log.fetchedCount} • أُدرج: ${log.insertedCount} • مكرر: ${log.duplicateCount} • أخطاء: ${log.errorCount}",
                                    color = IntelSlate,
                                    fontSize = 11.sp
                                )
                                if (!log.errorDetails.isNullOrBlank()) {
                                    Text(
                                        text = "ملاحظات: ${log.errorDetails}",
                                        color = IntelRed.copy(alpha = 0.8f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleCard(
    article: Article,
    dateFormat: SimpleDateFormat,
    isArabic: Boolean,
    onOpenDetails: () -> Unit,
    onToggleSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetails() }
            .testTag("article_card_${article.id}"),
        colors = CardDefaults.cardColors(containerColor = IntelCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (article.classification == ArticleClassification.RECIRCULATED) IntelGold.copy(alpha = 0.5f) else IntelBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Source & Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = article.sourceName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    SourceTierBadge(article.sourceTier)
                    ArticleClassificationBadge(article.classification)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateFormat.format(Date(article.publishedAt)),
                        color = IntelSlate,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(
                        onClick = onToggleSave,
                        modifier = Modifier.size(32.dp).testTag("save_article_button_${article.id}")
                    ) {
                        Icon(
                            imageVector = if (article.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (article.isSaved) IntelGold else IntelSlate,
                            modifier = Modifier.size(18.dp)
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
                lineHeight = 22.sp
            )

            // Snippet Preview
            Text(
                text = article.snippet,
                style = MaterialTheme.typography.bodySmall,
                color = IntelSlate,
                maxLines = 3,
                lineHeight = 18.sp
            )

            // Source Independence Notice (if agency attribution present)
            if (!article.originalAgency.isNullOrBlank()) {
                Surface(
                    color = Color(0x1A0EA5E9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = IntelBlue, modifier = Modifier.size(14.dp))
                        Text(
                            text = "المصدر المرجعي المنسوب إليه: ${article.originalAgency}",
                            color = IntelBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Bottom Footer: Linked Dossier & Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!article.politicalFileTitle.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = IntelGold, modifier = Modifier.size(14.dp))
                        Text(
                            text = article.politicalFileTitle ?: "",
                            color = IntelGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                TextButton(
                    onClick = onOpenDetails,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(if (isArabic) "التفاصيل والأدلة" else "View Evidence", color = IntelBlue, fontSize = 11.sp)
                    Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = IntelBlue, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
