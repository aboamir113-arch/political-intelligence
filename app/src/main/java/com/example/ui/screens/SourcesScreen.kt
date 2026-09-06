package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

@Composable
fun SourcesScreen(
    viewModel: DeskViewModel,
    uiState: DeskUiState,
    modifier: Modifier = Modifier
) {
    val isAr = uiState.isArabic
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredSources = remember(uiState.sources, uiState.sourceTierFilter) {
        if (uiState.sourceTierFilter == null) uiState.sources
        else uiState.sources.filter { it.tier == uiState.sourceTierFilter }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = IntelBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_source_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (isAr) "إضافة مصدر جديد" else "Add Source"
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Screen Header & Filter Bar
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isAr) "إدارة مصادر الأخبار والاستخبارات" else "Intelligence & News Sources",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isAr)
                                    "دعم كامل لـ (RSS, API, المواقع الرسمية، والإدخال اليدوي) مع فحص الاتصال التلقائي."
                                else
                                    "Full support for RSS, API, Official Sites & Manual URL ingestion with live probes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = IntelSlate
                            )
                        }

                        Button(
                            onClick = { viewModel.syncAllSourcesNow() },
                            enabled = !uiState.isIngestingGlobal,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IntelNavyLight)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isAr) "مزامنة الكل" else "Sync All", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tier Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.sourceTierFilter == null,
                                onClick = { viewModel.setSourceTierFilter(null) },
                                label = {
                                    Text(
                                        text = if (isAr) "الكل (${uiState.sources.size})" else "All (${uiState.sources.size})",
                                        fontSize = 12.sp
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IntelBlue,
                                    selectedLabelColor = Color.White,
                                    containerColor = IntelCard,
                                    labelColor = IntelSlate
                                )
                            )
                        }
                        items(SourceTier.values()) { tier ->
                            val count = uiState.sources.count { it.tier == tier }
                            FilterChip(
                                selected = uiState.sourceTierFilter == tier,
                                onClick = { viewModel.setSourceTierFilter(tier) },
                                label = {
                                    Text(
                                        text = "${if (isAr) tier.displayNameAr() else tier.name} ($count)",
                                        fontSize = 12.sp
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IntelBlue,
                                    selectedLabelColor = Color.White,
                                    containerColor = IntelCard,
                                    labelColor = IntelSlate
                                )
                            )
                        }
                    }

                    // Global Probe Feedback Banner
                    if (uiState.probeStatusMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x2238BDF8),
                            border = androidx.compose.foundation.BorderStroke(1.dp, IntelBlue.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = IntelBlue, modifier = Modifier.size(16.dp))
                                Text(
                                    text = uiState.probeStatusMessage ?: "",
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Sources List
            items(filteredSources, key = { it.id }) { source ->
                SourceManagementCard(
                    source = source,
                    isArabic = isAr,
                    isProbing = uiState.testingSourceId == source.id,
                    isSyncing = uiState.ingestingSourceId == source.id,
                    onProbeSource = { viewModel.testSource(source) },
                    onSyncSource = { viewModel.syncSingleSource(source) },
                    onToggleStatus = { viewModel.toggleSourceStatus(source) },
                    onDelete = { viewModel.deleteSource(source) }
                )
            }
        }
    }

    // Add Source Dialog Modal
    if (showAddDialog) {
        AddSourceDialog(
            isArabic = isAr,
            onDismiss = { showAddDialog = false },
            onConfirm = { nameAr, nameEn, countryCode, tier, fetchMethod, webUrl, rssUrl, apiUrl, apiConfigJson, sourceType, notes, reliability, isOfficial ->
                viewModel.addSource(
                    nameAr = nameAr,
                    nameEn = nameEn,
                    countryCode = countryCode,
                    tier = tier,
                    fetchMethod = fetchMethod,
                    websiteUrl = webUrl,
                    rssUrl = rssUrl,
                    apiUrl = apiUrl,
                    apiConfigJson = apiConfigJson,
                    sourceType = sourceType,
                    notes = notes,
                    reliability = reliability,
                    isOfficial = isOfficial
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SourceManagementCard(
    source: Source,
    isArabic: Boolean,
    isProbing: Boolean,
    isSyncing: Boolean,
    onProbeSource: () -> Unit,
    onSyncSource: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") } }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("source_card_${source.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = IntelCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, IntelBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isArabic) source.nameAr else source.nameEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    SourceTierBadge(source.tier)
                }

                // Status Toggle & Menu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = source.status == SourceStatus.ACTIVE,
                        onCheckedChange = { onToggleStatus() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = IntelGreen,
                            uncheckedThumbColor = IntelSlate,
                            uncheckedTrackColor = IntelNavyDark
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }
            }

            // Subtitle & Methods Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = IntelNavyDark,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "طريقة الجلب: ${source.fetchMethod.displayNameAr()}",
                        color = IntelBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Surface(
                    color = IntelNavyDark,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "الموثوقية: ${source.reliabilityLevel}%",
                        color = IntelGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Surface(
                    color = IntelNavyDark,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "الدولة: ${source.countryCode}",
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Connection State & Last Ingestion Stats
            Surface(
                color = IntelNavyDark.copy(alpha = 0.6f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val (dotColor, stateText) = when (source.connectionState) {
                                ConnectionState.CONNECTED -> Pair(IntelGreen, "متصل بنجاح")
                                ConnectionState.SLOW -> Pair(IntelGold, "استجابة بطيئة")
                                ConnectionState.TIMEOUT -> Pair(IntelGold, "مهلة اتصال منتهية")
                                ConnectionState.INVALID_FEED -> Pair(Color(0xFFFB923C), "تغذية غير صالحة")
                                ConnectionState.AUTHENTICATION_ERROR -> Pair(IntelRed, "خطأ في المصادقة/المفتاح")
                                ConnectionState.BLOCKED -> Pair(IntelRed, "محجوب أو حماية Cloudflare")
                                ConnectionState.NO_NEW_ITEMS -> Pair(IntelSlate, "تغذية فارغة حالياً")
                                ConnectionState.FAILED -> Pair(IntelRed, "فشل الاتصال")
                                ConnectionState.NOT_TESTED -> Pair(IntelSlate, "لم يُختبر بعد")
                            }
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                            Text(
                                text = stateText,
                                color = dotColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "إجمالي المقالات: ${source.totalArticlesIngested}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (!source.lastTestMessage.isNullOrBlank()) {
                        Text(
                            text = source.lastTestMessage ?: "",
                            color = IntelSlate,
                            fontSize = 10.sp
                        )
                    }

                    if (source.lastSyncAt != null) {
                        Text(
                            text = "آخر مزامنة: ${dateFormat.format(Date(source.lastSyncAt))}",
                            color = IntelSlate.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // URLs Preview
            if (!source.rssUrl.isNullOrBlank()) {
                Text(
                    text = "RSS: ${source.rssUrl}",
                    color = IntelSlate,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            } else if (!source.apiUrl.isNullOrBlank()) {
                Text(
                    text = "API: ${source.apiUrl}",
                    color = IntelSlate,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }

            HorizontalDivider(color = IntelBorder)

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "التحديث: كل ${source.updateFrequencyMinutes} د",
                    fontSize = 11.sp,
                    color = IntelSlate
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Test Probe Button
                    OutlinedButton(
                        onClick = onProbeSource,
                        enabled = !isProbing,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).testTag("probe_source_button_${source.id}")
                    ) {
                        if (isProbing) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = IntelBlue)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "فحص..." else "Probing...", fontSize = 11.sp)
                        } else {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(14.dp), tint = IntelBlue)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "اختبار" else "Probe", fontSize = 11.sp, color = IntelBlue)
                        }
                    }

                    // Sync Ingestion Now Button
                    Button(
                        onClick = onSyncSource,
                        enabled = !isSyncing && source.status == SourceStatus.ACTIVE,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).testTag("sync_source_button_${source.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = IntelBlue)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "جلب..." else "Syncing...", fontSize = 11.sp)
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "جلب التغذية" else "Ingest", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSourceDialog(
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (
        nameAr: String,
        nameEn: String,
        countryCode: String,
        tier: SourceTier,
        fetchMethod: FetchMethod,
        webUrl: String,
        rssUrl: String?,
        apiUrl: String?,
        apiConfigJson: String?,
        sourceType: String,
        notes: String,
        reliability: Int,
        isOfficial: Boolean
    ) -> Unit
) {
    var nameAr by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("SA") }
    var tier by remember { mutableStateOf(SourceTier.AGENCY) }
    var fetchMethod by remember { mutableStateOf(FetchMethod.RSS) }
    var webUrl by remember { mutableStateOf("") }
    var rssUrl by remember { mutableStateOf("") }
    var apiUrl by remember { mutableStateOf("") }
    var apiConfigJson by remember { mutableStateOf("") }
    var sourceType by remember { mutableStateOf("NEWS_AGENCY") }
    var notes by remember { mutableStateOf("") }
    var reliability by remember { mutableStateOf(90) }
    var isOfficial by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isArabic) "تسجيل مصدر استخباري وإخباري جديد" else "Register Intelligence Source",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = nameAr,
                        onValueChange = { nameAr = it },
                        label = { Text(if (isArabic) "اسم المصدر بالعربية *" else "Source Name (Arabic) *") },
                        modifier = Modifier.fillMaxWidth().testTag("add_source_name_ar")
                    )
                }
                item {
                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text(if (isArabic) "اسم المصدر بالإنجليزية" else "Source Name (English)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_source_name_en")
                    )
                }
                item {
                    OutlinedTextField(
                        value = countryCode,
                        onValueChange = { countryCode = it.uppercase() },
                        label = { Text(if (isArabic) "رمز الدولة (مثال: SA, EG, US)" else "Country Code (e.g. SA, EG, US)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text(text = if (isArabic) "طريقة الجلب:" else "Fetch Method:", fontSize = 12.sp, color = IntelSlate)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FetchMethod.values().forEach { m ->
                            FilterChip(
                                selected = fetchMethod == m,
                                onClick = { fetchMethod = m },
                                label = { Text(if (isArabic) m.displayNameAr() else m.name, fontSize = 10.sp) }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = webUrl,
                        onValueChange = { webUrl = it },
                        label = { Text(if (isArabic) "رابط الموقع الرسمي *" else "Website URL *") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth().testTag("add_source_web_url")
                    )
                }
                if (fetchMethod == FetchMethod.RSS) {
                    item {
                        OutlinedTextField(
                            value = rssUrl,
                            onValueChange = { rssUrl = it },
                            label = { Text(if (isArabic) "رابط تغذية RSS *" else "RSS Feed URL *") },
                            placeholder = { Text("https://.../rss.xml") },
                            modifier = Modifier.fillMaxWidth().testTag("add_source_rss_url")
                        )
                    }
                }
                if (fetchMethod == FetchMethod.API) {
                    item {
                        OutlinedTextField(
                            value = apiUrl,
                            onValueChange = { apiUrl = it },
                            label = { Text(if (isArabic) "رابط نقطة نهاية API *" else "API Endpoint *") },
                            placeholder = { Text("https://api.example.com/v1/news") },
                            modifier = Modifier.fillMaxWidth().testTag("add_source_api_url")
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = apiConfigJson,
                            onValueChange = { apiConfigJson = it },
                            label = { Text(if (isArabic) "تهيئة API بصيغة JSON (اختياري)" else "API Config JSON (Optional)") },
                            placeholder = { Text("{\"apiKeyParam\":\"apiKey\",\"articlesJsonPath\":\"articles\"}") },
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                item {
                    Text(text = if (isArabic) "تصنيف الموثوقية:" else "Source Tier:", fontSize = 12.sp, color = IntelSlate)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SourceTier.values().forEach { t ->
                            FilterChip(
                                selected = tier == t,
                                onClick = { tier = t },
                                label = { Text(if (isArabic) t.displayNameAr() else t.name, fontSize = 10.sp) }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(if (isArabic) "ملاحظات إدارية واستخباراتية" else "Analyst Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameAr.isNotBlank() && webUrl.isNotBlank()) {
                        onConfirm(
                            nameAr, nameEn, countryCode, tier, fetchMethod, webUrl, rssUrl, apiUrl,
                            apiConfigJson.takeIf { it.isNotBlank() }, sourceType, notes, reliability, isOfficial
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IntelBlue),
                modifier = Modifier.testTag("submit_add_source_button")
            ) {
                Text(if (isArabic) "حفظ واختبار" else "Save & Probe")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}
