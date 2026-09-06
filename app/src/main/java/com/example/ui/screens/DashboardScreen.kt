package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeskTab
import com.example.ui.viewmodel.DeskUiState
import com.example.ui.viewmodel.DeskViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: DeskViewModel,
    uiState: DeskUiState,
    modifier: Modifier = Modifier
) {
    val isAr = uiState.isArabic
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") } }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Analyst Briefing Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DeskDarkBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isAr) "جلسة المتابعة الاستخباراتية" else "Active Intelligence Session",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = uiState.currentUser?.let {
                                        if (isAr) "${it.fullNameAr} — ${it.role.displayNameAr()}"
                                        else "${it.fullNameEn} — ${it.role.displayNameEn()}"
                                    } ?: (if (isAr) "د. بلال اللقيس — كبير المحللين الاستراتيجيين" else "Dr. Bilal Al-Laqqis — Senior Strategic Analyst"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = IntelSlate
                                )
                            }
                            uiState.currentUser?.let { user ->
                                RoleChip(role = user.role, isArabic = isAr)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = DeskDarkBorder.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // "What Changed" Quick Highlight
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = IntelGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isAr) "ماذا تغير منذ أمس؟ (What Changed)" else "What Changed Since Yesterday?",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IntelGold
                                )
                                Text(
                                    text = if (isAr)
                                        "رصد ${uiState.articlesTodayCount} برقية إخبارية اليوم، مع تجميعها في ${uiState.events.size} ملفات أحداث سياسية، ورصد ${uiState.allClaims.size} ادعاءات للتحقق، و${uiState.openContradictionsCount} تناقضات قيد المراجعة التحليلية."
                                    else
                                        "Ingested ${uiState.articlesTodayCount} dispatches, clustered into ${uiState.events.size} event dossiers, with ${uiState.allClaims.size} verifiable claims and ${uiState.openContradictionsCount} open discrepancies.",
                                    fontSize = 12.sp,
                                    color = Color(0xFFE2E8F0),
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }

            // Open Contradictions Alert Banner (Phase 3)
            if (uiState.openContradictionsCount > 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectTab(DeskTab.EVENTS) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF27131B)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RedHighRisk.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = RedHighRisk, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isAr) "تنبيه تعارض في الروايات الإخبارية" else "Intelligence Discrepancy Alert",
                                    color = RedHighRisk,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = if (isAr)
                                        "تم رصد ${uiState.openContradictionsCount} تناقضات بين المصادر الرسمية والصحفية حول التوقيتات والأرقام تتطلب مراجعة المحلل."
                                    else
                                        "${uiState.openContradictionsCount} cross-source contradictions detected requiring review.",
                                    color = IntelWhite,
                                    fontSize = 11.sp
                                )
                            }
                            Text(text = if (isAr) "مراجعة ←" else "Review →", color = IntelCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Phase 4: AI Intelligence Brief Card
            item {
                DashboardAiBriefCard(
                    brief = uiState.dashboardAiBrief,
                    isAr = isAr,
                    onRefresh = { viewModel.refreshDashboardAiBrief() },
                    onAnalyzeWhatChanged = { viewModel.analyzeWhatChanged(24) },
                    onViewFullBrief = {
                        viewModel.setShowAiIntelligenceDialog(true, AiAnalysisType.DASHBOARD_BRIEF)
                    }
                )
            }

        // 2. Intelligence Metrics (KPIs)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricKpiCard(
                    title = if (isAr) "الأحداث السياسية" else "Event Dossiers",
                    value = "${uiState.events.size}",
                    subtitle = if (isAr) "${uiState.activeDevelopingEventsCount} حدث متطور/نشط" else "${uiState.activeDevelopingEventsCount} active",
                    icon = Icons.Default.EventNote,
                    accentColor = IntelCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = if (isAr) "التنبيهات الذكية" else "Smart Alerts",
                    value = "${uiState.alerts.size}",
                    subtitle = if (isAr) "${uiState.unreadAlertsCount} غير مقروء" else "${uiState.unreadAlertsCount} unread",
                    icon = Icons.Default.NotificationsActive,
                    accentColor = AmberAlert,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricKpiCard(
                    title = if (isAr) "الادعاءات والتحقق" else "Claims Verified",
                    value = "${uiState.allClaims.size}",
                    subtitle = if (isAr) "${uiState.allStatements.size} اقتباس موثق" else "${uiState.allStatements.size} quotes",
                    icon = Icons.Default.Verified,
                    accentColor = IntelEmerald,
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = if (isAr) "أخبار اليوم المرصودة" else "Today's News",
                    value = "${uiState.articlesTodayCount}",
                    subtitle = if (isAr) "${uiState.duplicatesCount} مكرر محجوب" else "${uiState.duplicatesCount} duplicates",
                    icon = Icons.Default.Newspaper,
                    accentColor = IntelBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section: Active Story Dossiers (Phase 3 Spotlight)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.EventNote, contentDescription = null, tint = IntelCyan, modifier = Modifier.size(20.dp))
                    Text(
                        text = if (isAr) "إضبارات الأحداث السياسية النشطة" else "Active Event Dossiers",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                TextButton(
                    onClick = { viewModel.selectTab(DeskTab.EVENTS) }
                ) {
                    Text(
                        text = if (isAr) "عرض كافة الأحداث (${uiState.events.size})" else "All Events (${uiState.events.size})",
                        color = IntelCyan,
                        fontSize = 12.sp
                    )
                }
            }
        }

        items(uiState.events.take(2), key = { "dash_event_${it.id}" }) { ev ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.selectEvent(ev)
                        viewModel.selectTab(DeskTab.EVENTS)
                    },
                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DeskDarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = when (ev.status) {
                                EventStatus.DEVELOPING -> AmberAlert
                                EventStatus.ACTIVE -> IntelCyan
                                EventStatus.RESOLVED -> Color(0xFF10B981)
                                EventStatus.ARCHIVED -> IntelSlate
                            }.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (isAr) ev.status.displayNameAr() else ev.status.displayNameEn(),
                                color = IntelCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "${ev.articleCount} ${if (isAr) "أخبار مرتبطة" else "articles"}",
                            color = IntelSlate,
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = ev.titleAr,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (!ev.whatChangedAr.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF072338), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "ما الجديد: ${ev.whatChangedAr}",
                                color = IntelCyan,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. Section: Real-time News Feed Spotlight (Phase 2 Ingestion)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.RssFeed, contentDescription = null, tint = IntelCyan, modifier = Modifier.size(20.dp))
                    Text(
                        text = if (isAr) "آخر الأخبار والاستخبارات المرصودة" else "Latest Ingested Intelligence",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                TextButton(
                    onClick = { viewModel.selectTab(DeskTab.NEWS_FEED) }
                ) {
                    Text(
                        text = if (isAr) "عرض شريط الأخبار (${uiState.articles.size})" else "Full Feed (${uiState.articles.size})",
                        color = IntelCyan,
                        fontSize = 12.sp
                    )
                }
            }
        }

        items(uiState.articles.take(3), key = { "dash_art_${it.id}" }) { article ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.openArticleDetails(article)
                        viewModel.selectTab(DeskTab.NEWS_FEED)
                    },
                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DeskDarkBorder))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = article.sourceName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            SourceTierBadge(article.sourceTier)
                            ArticleClassificationBadge(article.classification)
                        }
                        Text(
                            text = dateFormat.format(Date(article.publishedAt)),
                            color = IntelSlate,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 18.sp,
                        maxLines = 2
                    )

                    if (!article.originalAgency.isNullOrBlank()) {
                        Text(
                            text = "المصدر المرجعي: ${article.originalAgency}",
                            color = IntelBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 4. Section: Active Political Dossiers
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAr) "الملفات السياسية ذات الأولوية" else "Priority Political Dossiers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                TextButton(
                    onClick = { viewModel.selectTab(DeskTab.POLITICAL_FILES) }
                ) {
                    Text(
                        text = if (isAr) "عرض الكل (${uiState.files.size})" else "View All (${uiState.files.size})",
                        color = IntelCyan,
                        fontSize = 12.sp
                    )
                }
            }
        }

        items(uiState.files.take(2)) { file ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectTab(DeskTab.POLITICAL_FILES) },
                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DeskDarkBorder))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PriorityBadge(priority = file.priority, isArabic = isAr)
                        Text(
                            text = if (isAr) file.status.displayNameAr() else file.status.name,
                            fontSize = 11.sp,
                            color = if (file.status == FileStatus.ACTIVE) IntelEmerald else IntelSlate
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isAr) file.titleAr else file.titleEn,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = file.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = IntelSlate,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 5. Section: Monitored Persons & Position Tracking
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAr) "الشخصيات والتحولات في المواقف" else "Key Persons & Position Tracking",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                TextButton(onClick = { viewModel.selectTab(DeskTab.ENTITIES) }) {
                    Text(
                        text = if (isAr) "إدارة الكيانات" else "Manage Entities",
                        color = IntelCyan,
                        fontSize = 12.sp
                    )
                }
            }
        }

        items(uiState.persons.take(2)) { person ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DeskDarkBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = person.photoEmoji,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAr) person.nameAr else person.nameEn,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isAr) person.currentRoleAr else person.currentRoleEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = IntelSlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (person.stanceShiftCount > 0) {
                            Text(
                                text = if (isAr) "🔄 رصد ${person.stanceShiftCount} تحول في المواقف موثق" else "🔄 ${person.stanceShiftCount} documented stance shift(s)",
                                fontSize = 11.sp,
                                color = IntelGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

    // AI Intelligence Universal Dialog
    if (uiState.showAiIntelligenceDialog) {
        com.example.ui.components.AiIntelligenceDialog(
            viewModel = viewModel,
            uiState = uiState,
            onDismiss = { viewModel.setShowAiIntelligenceDialog(false) }
        )
    }
}
}

@Composable
fun DashboardAiBriefCard(
    brief: DashboardAiBriefResult?,
    isAr: Boolean,
    onRefresh: () -> Unit,
    onAnalyzeWhatChanged: () -> Unit,
    onViewFullBrief: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("dashboard_ai_brief_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF04182B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, IntelCyan.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = IntelCyan, modifier = Modifier.size(18.dp))
                    Text(
                        text = if (isAr) "الإيجاز الاستخباري الذكي الشامل (AI Brief)" else "AI Situation Briefing",
                        color = IntelWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onRefresh, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = IntelCyan, modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (brief != null) {
                // Executive Brief
                Text(
                    text = brief.executiveAssessment,
                    color = IntelWhite,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                // Fast Developments Chips
                if (brief.todayKeyDevelopments.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isAr) "أبرز التطورات المتسارعة اليوم:" else "Fast Developments Today:",
                            color = IntelSlate,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        for (dev in brief.todayKeyDevelopments.take(3)) {
                            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("⚡", fontSize = 11.sp)
                                Text(dev, color = Color(0xFFE2E8F0), fontSize = 11.sp, lineHeight = 16.sp)
                            }
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onAnalyzeWhatChanged,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, IntelGold.copy(alpha = 0.7f)),
                        modifier = Modifier.weight(1f).testTag("dashboard_what_changed_button"),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = IntelGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isAr) "ماذا تغير (24h)؟" else "What Changed?", color = IntelGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onViewFullBrief,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IntelCyan, contentColor = Color(0xFF031424)),
                        modifier = Modifier.weight(1f).testTag("dashboard_view_full_brief_button"),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isAr) "الإيجاز الكامل" else "Full Briefing", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAr) "انقر لتوليد إيجاز الموقف الاستراتيجي اليومي بالذكاء الاصطناعي." else "Click to generate AI situation briefing.",
                        color = IntelSlate,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = onRefresh,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IntelCyan, contentColor = Color(0xFF031424))
                    ) {
                        Text(if (isAr) "توليد" else "Generate", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
