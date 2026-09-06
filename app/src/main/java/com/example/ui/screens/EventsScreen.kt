package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeskUiState
import com.example.ui.viewmodel.DeskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    viewModel: DeskViewModel,
    uiState: DeskUiState
) {
    val isAr = uiState.isArabic
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<EventStatus?>(null) }
    var selectedImportance by remember { mutableStateOf<EventImportance?>(null) }

    val filteredEvents = remember(uiState.events, searchQuery, selectedStatus, selectedImportance) {
        uiState.events.filter { event ->
            val matchesQuery = searchQuery.isBlank() ||
                    event.titleAr.contains(searchQuery, ignoreCase = true) ||
                    event.summaryAr.contains(searchQuery, ignoreCase = true) ||
                    event.linkedPersonNames.contains(searchQuery, ignoreCase = true) ||
                    event.linkedOrgNames.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatus == null || event.status == selectedStatus
            val matchesImportance = selectedImportance == null || event.importance == selectedImportance
            matchesQuery && matchesStatus && matchesImportance
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = IntelCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAr) "ملف الأحداث السياسية (Story Dossiers)" else "Political Events & Dossiers",
                            style = MaterialTheme.typography.titleLarge,
                            color = IntelWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (isAr)
                            "تجميع الأخبار المترابطة، التحقق من الادعاءات، توثيق الاقتباسات وتتبع الخط الزمني"
                        else
                            "Clustered stories, verifiable claims, quotes & timeline intelligence",
                        style = MaterialTheme.typography.bodySmall,
                        color = IntelSlate
                    )
                }

                FilledTonalButton(
                    onClick = { viewModel.setShowCreateEventDialog(true) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = IntelCyan,
                        contentColor = Color(0xFF031424)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isAr) "حدث جديد" else "New Event", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        if (isAr) "بحث في الأحداث، الشخصيات، أو المؤسسات المعنية..." else "Search events, actors, or organizations...",
                        color = IntelSlate,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = IntelCyan)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = IntelSlate)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IntelCyan,
                    unfocusedBorderColor = DeskDarkBorder,
                    focusedTextColor = IntelWhite,
                    unfocusedTextColor = IntelWhite,
                    focusedContainerColor = DeskDarkCard,
                    unfocusedContainerColor = DeskDarkCard
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Status Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedStatus == null,
                        onClick = { selectedStatus = null },
                        label = { Text(if (isAr) "جميع الحالات (${uiState.events.size})" else "All", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IntelCyan,
                            selectedLabelColor = Color(0xFF031424),
                            containerColor = DeskDarkCard,
                            labelColor = IntelSlate
                        )
                    )
                }

                items(EventStatus.values()) { status ->
                    val count = uiState.events.count { it.status == status }
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = if (selectedStatus == status) null else status },
                        label = { Text("${if (isAr) status.displayNameAr() else status.displayNameEn()} ($count)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (status) {
                                EventStatus.DEVELOPING -> AmberAlert
                                EventStatus.ACTIVE -> IntelCyan
                                EventStatus.RESOLVED -> Color(0xFF10B981)
                                EventStatus.ARCHIVED -> IntelSlate
                            },
                            selectedLabelColor = Color(0xFF031424),
                            containerColor = DeskDarkCard,
                            labelColor = IntelSlate
                        )
                    )
                }
            }

            // Importance Filter Chips
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    Text(
                        text = if (isAr) "الأهمية:" else "Importance:",
                        style = MaterialTheme.typography.labelSmall,
                        color = IntelSlate
                    )
                }
                items(EventImportance.values()) { imp ->
                    FilterChip(
                        selected = selectedImportance == imp,
                        onClick = { selectedImportance = if (selectedImportance == imp) null else imp },
                        label = { Text(if (isAr) imp.displayNameAr() else imp.displayNameEn(), fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (imp) {
                                EventImportance.CRITICAL -> RedHighRisk
                                EventImportance.HIGH -> AmberAlert
                                EventImportance.MEDIUM -> IntelCyan
                                EventImportance.LOW -> IntelSlate
                            },
                            selectedLabelColor = if (imp == EventImportance.CRITICAL) Color.White else Color(0xFF031424),
                            containerColor = DeskDarkCard,
                            labelColor = IntelSlate
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Events List
            if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(DeskDarkCard, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = IntelSlate, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isAr) "لا توجد أحداث سياسية مطابقة للفلاتر الحالية" else "No matching events found",
                            color = IntelSlate,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredEvents, key = { it.id }) { event ->
                        EventCard(
                            event = event,
                            isAr = isAr,
                            onOpenDetail = { viewModel.selectEvent(event) },
                            onDelete = { viewModel.deleteEvent(event) }
                        )
                    }
                }
            }
        }

        // Active Event Detail Modal / Sheet
        if (uiState.activeEventDetail != null) {
            EventDetailSheet(
                event = uiState.activeEventDetail,
                viewModel = viewModel,
                uiState = uiState,
                onDismiss = { viewModel.selectEvent(null) }
            )
        }

        // Create Event Dialog
        if (uiState.showCreateEventDialog) {
            CreateEventDialog(
                viewModel = viewModel,
                uiState = uiState,
                onDismiss = { viewModel.setShowCreateEventDialog(false) }
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
    }
}

@Composable
fun EventCard(
    event: PoliticalEvent,
    isAr: Boolean,
    onOpenDetail: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (event.status) {
        EventStatus.DEVELOPING -> AmberAlert
        EventStatus.ACTIVE -> IntelCyan
        EventStatus.RESOLVED -> Color(0xFF10B981)
        EventStatus.ARCHIVED -> IntelSlate
    }

    val importanceColor = when (event.importance) {
        EventImportance.CRITICAL -> RedHighRisk
        EventImportance.HIGH -> AmberAlert
        EventImportance.MEDIUM -> IntelCyan
        EventImportance.LOW -> IntelSlate
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetail() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DeskDarkCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeskDarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Badges & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Status Badge
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
                    ) {
                        Text(
                            text = if (isAr) event.status.displayNameAr() else event.status.displayNameEn(),
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Importance Badge
                    Surface(
                        color = importanceColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isAr) event.importance.displayNameAr() else event.importance.displayNameEn(),
                            color = importanceColor,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (event.primaryCountryCode != null) {
                        Surface(
                            color = Color(0xFF15263F),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = event.primaryCountryCode,
                                color = IntelWhite,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Date
                Text(
                    text = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(event.latestUpdate)),
                    style = MaterialTheme.typography.labelSmall,
                    color = IntelSlate
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = event.titleAr,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = IntelWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Summary
            Text(
                text = event.summaryAr,
                style = MaterialTheme.typography.bodySmall,
                color = IntelSilver,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // "What Changed" / ما الجديد؟ Highlight Card
            if (!event.whatChangedAr.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF072338), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF0F3B5D), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ChangeHistory, contentDescription = null, tint = IntelCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAr) "ما الجديد في هذا التطور؟" else "What Changed:",
                                color = IntelCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.whatChangedAr,
                            color = IntelWhite,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Info & Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Intelligence Metrics
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Article, contentDescription = null, tint = IntelSlate, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${event.articleCount} ${if (isAr) "أخبار" else "articles"}",
                            fontSize = 11.sp,
                            color = IntelSlate
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = IntelSlate, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${event.independentSourceCount} ${if (isAr) "مصادر مستقلة" else "sources"}",
                            fontSize = 11.sp,
                            color = IntelSlate
                        )
                    }
                }

                // Action to Open Dossier
                TextButton(
                    onClick = onOpenDetail,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isAr) "استعراض الإضبارة ←" else "View Dossier →",
                        color = IntelCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailSheet(
    event: PoliticalEvent,
    viewModel: DeskViewModel,
    uiState: DeskUiState,
    onDismiss: () -> Unit
) {
    val isAr = uiState.isArabic
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        if (isAr) "نظرة عامة" else "Overview",
        if (isAr) "الخط الزمني (${uiState.eventTimeline.size})" else "Timeline",
        if (isAr) "التصريحات (${uiState.eventStatements.size})" else "Statements",
        if (isAr) "الادعاءات والأدلة (${uiState.eventClaims.size})" else "Claims & Evidence",
        if (isAr) "التناقضات (${uiState.eventContradictions.size})" else "Contradictions",
        if (isAr) "الأخبار المرتبطة (${uiState.eventArticles.size})" else "Articles",
        if (isAr) "الذكاء الاصطناعي (AI)" else "AI Analysis"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DeskDarkBorder)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeskDarkCard)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = when (event.status) {
                                    EventStatus.DEVELOPING -> AmberAlert
                                    EventStatus.ACTIVE -> IntelCyan
                                    EventStatus.RESOLVED -> Color(0xFF10B981)
                                    EventStatus.ARCHIVED -> IntelSlate
                                }.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (isAr) event.status.displayNameAr() else event.status.displayNameEn(),
                                    color = IntelCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إضبارة حدث #${event.id}",
                                style = MaterialTheme.typography.labelSmall,
                                color = IntelSlate
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.titleAr,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = IntelWhite,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = IntelWhite)
                    }
                }

                // Sub Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DeskDarkCard,
                    contentColor = IntelCyan,
                    edgePadding = 12.dp
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                // Content Panel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> EventOverviewTab(event = event, isAr = isAr)
                        1 -> EventTimelineTab(timeline = uiState.eventTimeline, isAr = isAr)
                        2 -> EventStatementsTab(statements = uiState.eventStatements, isAr = isAr)
                        3 -> EventClaimsEvidenceTab(claims = uiState.eventClaims, evidence = uiState.eventEvidence, viewModel = viewModel, isAr = isAr)
                        4 -> EventContradictionsTab(contradictions = uiState.eventContradictions, viewModel = viewModel, isAr = isAr)
                        5 -> EventArticlesTab(articles = uiState.eventArticles, onRemove = { articleId -> viewModel.removeArticleFromEvent(articleId, event.id) }, isAr = isAr)
                        6 -> EventAiIntelligenceTab(event = event, viewModel = viewModel, uiState = uiState, isAr = isAr)
                    }
                }
            }
        }
    }
}

@Composable
fun EventOverviewTab(event: PoliticalEvent, isAr: Boolean) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            // What changed highlight
            if (!event.whatChangedAr.isNullOrBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF072742)),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IntelCyan.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = IntelCyan)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAr) "ما الجديد وتداعيات التطور الأخير؟" else "What Changed / Latest Development",
                                style = MaterialTheme.typography.titleSmall,
                                color = IntelCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = event.whatChangedAr, color = IntelWhite, fontSize = 13.sp, lineHeight = 18.sp)
                        if (!event.latestDevelopmentAr.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "• ${event.latestDevelopmentAr}", color = IntelSilver, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            // Core Summary
            Card(
                colors = CardDefaults.cardColors(containerColor = DeskDarkCard),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isAr) "ملخص التقدير السياسي للحدث" else "Intelligence Summary",
                        style = MaterialTheme.typography.titleSmall,
                        color = IntelCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = event.summaryAr, color = IntelWhite, fontSize = 13.sp, lineHeight = 20.sp)
                    if (event.description.isNotBlank() && event.description != event.summaryAr) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = event.description, color = IntelSilver, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }
            }
        }

        item {
            // Geopolitical Entities Linked
            Card(
                colors = CardDefaults.cardColors(containerColor = DeskDarkCard),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isAr) "الأطراف والجهات والكيانات المعنية" else "Key Actors & Geographic Scope",
                        style = MaterialTheme.typography.titleSmall,
                        color = IntelCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (event.linkedPersonNames.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = IntelSlate, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "${if (isAr) "الشخصيات:" else "Persons:"} ${event.linkedPersonNames}", color = IntelWhite, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (event.linkedOrgNames.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CorporateFare, contentDescription = null, tint = IntelSlate, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "${if (isAr) "المؤسسات والهيئات:" else "Organizations:"} ${event.linkedOrgNames}", color = IntelWhite, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (event.linkedCountryCodes.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Public, contentDescription = null, tint = IntelSlate, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "${if (isAr) "النطاق الجغرافي والدول:" else "Countries:"} ${event.linkedCountryCodes}", color = IntelWhite, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventTimelineTab(timeline: List<TimelineItem>, isAr: Boolean) {
    if (timeline.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = if (isAr) "لا توجد تطورات زمنية مسجلة بعد" else "No timeline items", color = IntelSlate)
        }
        return
    }

    val uriHandler = LocalUriHandler.current

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(timeline) { item ->
            Row(modifier = Modifier.fillMaxWidth()) {
                // Milestone Bullet & Line
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 12.dp, top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (item.isMilestone) 16.dp else 12.dp)
                            .clip(CircleShape)
                            .background(if (item.isMilestone) IntelCyan else AmberAlert)
                    )
                }

                // Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DeskDarkCard),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (item.isMilestone) IntelCyan.copy(alpha = 0.4f) else DeskDarkBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                color = Color(0xFF15263F),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = item.sourceName,
                                    color = IntelCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(item.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = IntelSlate
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = item.titleAr, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = IntelWhite)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = item.descriptionAr, style = MaterialTheme.typography.bodySmall, color = IntelSilver)

                        if (!item.originalUrl.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isAr) "رابط التغطية الأصلية ↗" else "Original Source ↗",
                                color = IntelCyan,
                                fontSize = 11.sp,
                                modifier = Modifier.clickable { runCatching { uriHandler.openUri(item.originalUrl) } }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventStatementsTab(statements: List<Statement>, isAr: Boolean) {
    if (statements.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = if (isAr) "لا توجد تصريحات أو اقتباسات مباشرة مسجلة في هذا الحدث" else "No direct quotes recorded", color = IntelSlate)
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(statements) { st ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DeskDarkCard),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DeskDarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Speaker Info & Source
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = st.personName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = IntelWhite
                            )
                            if (!st.roleTitle.isNullOrBlank()) {
                                Text(text = st.roleTitle, style = MaterialTheme.typography.labelSmall, color = IntelCyan)
                            }
                        }

                        Surface(
                            color = if (st.isOfficialDeclaration) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF15263F),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (st.isOfficialDeclaration) (if (isAr) "إفادة رسمية" else "Official") else st.sourceName,
                                color = if (st.isOfficialDeclaration) Color(0xFF10B981) else IntelSlate,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Verbatim Quote
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF07192C), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF0E304E), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "«${st.quoteText}»",
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp
                        )
                    }

                    if (!st.context.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${if (isAr) "السياق:" else "Context:"} ${st.context}",
                            color = IntelSlate,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${if (isAr) "المصدر الأصلي:" else "Source:"} ${st.sourceName} • ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(st.statementDate))}",
                        color = IntelSlate,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EventClaimsEvidenceTab(
    claims: List<Claim>,
    evidence: List<EvidenceItem>,
    viewModel: DeskViewModel,
    isAr: Boolean
) {
    var editingClaim by remember { mutableStateOf<Claim?>(null) }

    if (claims.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = if (isAr) "لا توجد ادعاءات مستخلصة للتحقق" else "No claims extracted", color = IntelSlate)
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        items(claims) { claim ->
            val statusColor = when (claim.verificationStatus) {
                VerificationStatus.CONFIRMED -> Color(0xFF10B981)
                VerificationStatus.SUPPORTED -> IntelCyan
                VerificationStatus.LIKELY -> Color(0xFF38BDF8)
                VerificationStatus.UNVERIFIED -> AmberAlert
                VerificationStatus.CONTRADICTED -> RedHighRisk
                VerificationStatus.INSUFFICIENT_EVIDENCE -> Color(0xFF94A3B8)
                VerificationStatus.NOT_APPLICABLE -> IntelSlate
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = DeskDarkCard),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header Status & Confidence
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
                        ) {
                            Text(
                                text = if (isAr) claim.verificationStatus.displayNameAr() else claim.verificationStatus.displayNameEn(),
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = "درجة الثقة: ${claim.confidenceScore}%",
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Claim Statement
                    Text(
                        text = claim.statement,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = IntelWhite,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Evidentiary Rationale (لماذا الأدلة قوية أو ضعيفة)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF041829), RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isAr) "السند التحليلي للتحقق:" else "Evidentiary Rationale:",
                                color = IntelCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = claim.confidenceReasonAr,
                                color = IntelSilver,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Attached Evidence Items
                    val claimEvidence = evidence.filter { it.claimId == claim.id }
                    if (claimEvidence.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isAr) "الأدلة والقرائن المرتبطة (${claimEvidence.size}):" else "Attached Evidence (${claimEvidence.size}):",
                            style = MaterialTheme.typography.labelSmall,
                            color = IntelSlate
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            claimEvidence.forEach { ev ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF092036), RoundedCornerShape(6.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (ev.evidenceType == EvidenceType.OFFICIAL_DOCUMENT) Icons.Default.Description else Icons.Default.Feed,
                                        contentDescription = null,
                                        tint = IntelCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${ev.sourceName} [${if (isAr) ev.evidenceType.displayNameAr() else ev.evidenceType.displayNameEn()}]",
                                            color = IntelWhite,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(text = ev.excerpt, color = IntelSilver, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }

                    // Analyst Review Override Button
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { editingClaim = claim },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = IntelCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, IntelCyan.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isAr) "تعديل تقييم التحقق" else "Update Verification", fontSize = 11.sp)
                    }
                }
            }
        }
    }

    // Modal to change Verification Status
    if (editingClaim != null) {
        VerificationOverrideDialog(
            claim = editingClaim!!,
            isAr = isAr,
            onConfirm = { newStatus, reason ->
                viewModel.updateClaimVerification(editingClaim!!.id, newStatus, reason)
                editingClaim = null
            },
            onDismiss = { editingClaim = null }
        )
    }
}

@Composable
fun VerificationOverrideDialog(
    claim: Claim,
    isAr: Boolean,
    onConfirm: (VerificationStatus, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStatus by remember { mutableStateOf(claim.verificationStatus) }
    var reasonText by remember { mutableStateOf(claim.confidenceReasonAr) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DeskDarkBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isAr) "تعديل حالة التحقق وإسناد الأدلة" else "Update Verification Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = IntelWhite
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = claim.statement, color = IntelSilver, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = if (isAr) "اختر الحالة الجديدة:" else "Select Status:", color = IntelCyan, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        VerificationStatus.CONFIRMED,
                        VerificationStatus.SUPPORTED,
                        VerificationStatus.LIKELY,
                        VerificationStatus.UNVERIFIED,
                        VerificationStatus.CONTRADICTED,
                        VerificationStatus.INSUFFICIENT_EVIDENCE
                    ).forEach { st ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStatus = st }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedStatus == st,
                                onClick = { selectedStatus = st },
                                colors = RadioButtonDefaults.colors(selectedColor = IntelCyan, unselectedColor = IntelSlate)
                            )
                            Text(text = if (isAr) st.displayNameAr() else st.displayNameEn(), color = IntelWhite, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    label = { Text(if (isAr) "سبب التقييم (شفافية الأدلة)" else "Evidentiary reason") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IntelCyan,
                        unfocusedBorderColor = DeskDarkBorder,
                        focusedTextColor = IntelWhite,
                        unfocusedTextColor = IntelWhite
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(text = if (isAr) "إلغاء" else "Cancel", color = IntelSlate)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(selectedStatus, reasonText) },
                        colors = ButtonDefaults.buttonColors(containerColor = IntelCyan, contentColor = Color(0xFF031424))
                    ) {
                        Text(text = if (isAr) "حفظ التقييم" else "Save")
                    }
                }
            }
        }
    }
}

@Composable
fun EventContradictionsTab(
    contradictions: List<ContradictionItem>,
    viewModel: DeskViewModel,
    isAr: Boolean
) {
    if (contradictions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = if (isAr) "لا توجد تناقضات مسجلة بين المصادر في هذا الحدث" else "No contradictions detected", color = IntelSlate)
            }
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        items(contradictions) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DeskDarkCard),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RedHighRisk.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = RedHighRisk.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (isAr) item.contradictionType.displayNameAr() else item.contradictionType.displayNameEn(),
                                color = RedHighRisk,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            color = if (item.status == ContradictionStatus.OPEN) AmberAlert.copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (isAr) item.status.displayNameAr() else item.status.name,
                                color = if (item.status == ContradictionStatus.OPEN) AmberAlert else Color(0xFF10B981),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = item.descriptionAr, color = IntelWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    // Side-by-side Claims Comparison
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Source A
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F253B), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "الرواية الأولى (${item.claimASource}):", color = IntelCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(text = item.claimAStatement, color = IntelWhite, fontSize = 11.sp)
                            }
                        }

                        // Source B
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF24141E), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "الرواية المقابلة (${item.claimBSource}):", color = AmberAlert, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(text = item.claimBStatement, color = IntelWhite, fontSize = 11.sp)
                            }
                        }
                    }

                    if (item.analystNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "ملاحظات المحلل: ${item.analystNotes}", color = IntelSlate, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (item.status == ContradictionStatus.OPEN) {
                            Button(
                                onClick = { viewModel.updateContradictionStatus(item.id, ContradictionStatus.UNDER_REVIEW, "قيد مراجعة المحلل الاستخباري") },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberAlert, contentColor = Color(0xFF031424)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(text = if (isAr) "بدء المراجعة" else "Review", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { viewModel.updateContradictionStatus(item.id, ContradictionStatus.RESOLVED, "تم توضيح التعارض بمصدر أولي معتمد") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color(0xFF031424)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(text = if (isAr) "تسوية التعارض" else "Resolve", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventArticlesTab(
    articles: List<Article>,
    onRemove: (Long) -> Unit,
    isAr: Boolean
) {
    if (articles.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = if (isAr) "لا توجد مقالات مرتبطة بهذا الحدث" else "No linked articles", color = IntelSlate)
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(articles) { article ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DeskDarkCard),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DeskDarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = article.sourceName, color = IntelCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(article.publishedAt)),
                                color = IntelSlate,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = article.title, color = IntelWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    IconButton(onClick = { onRemove(article.id) }) {
                        Icon(Icons.Default.LinkOff, contentDescription = "Unlink", tint = RedHighRisk)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventDialog(
    viewModel: DeskViewModel,
    uiState: DeskUiState,
    onDismiss: () -> Unit
) {
    val isAr = uiState.isArabic
    var titleAr by remember { mutableStateOf("") }
    var summaryAr by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf(EventStatus.DEVELOPING) }
    var selectedImportance by remember { mutableStateOf(EventImportance.HIGH) }
    var selectedFileId by remember { mutableStateOf<Long?>(null) }
    var selectedTopicId by remember { mutableStateOf<Long?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DeskDarkBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isAr) "إنشاء إضبارة حدث سياسي جديد" else "Create New Event Dossier",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = IntelWhite
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = titleAr,
                    onValueChange = { titleAr = it },
                    label = { Text(if (isAr) "عنوان الحدث السياسي" else "Event Title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IntelCyan,
                        unfocusedBorderColor = DeskDarkBorder,
                        focusedTextColor = IntelWhite,
                        unfocusedTextColor = IntelWhite
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = summaryAr,
                    onValueChange = { summaryAr = it },
                    label = { Text(if (isAr) "ملخص وتقدير الموقف" else "Summary") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IntelCyan,
                        unfocusedBorderColor = DeskDarkBorder,
                        focusedTextColor = IntelWhite,
                        unfocusedTextColor = IntelWhite
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(if (isAr) "المكان / النطاق الجغرافي" else "Location") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IntelCyan,
                        unfocusedBorderColor = DeskDarkBorder,
                        focusedTextColor = IntelWhite,
                        unfocusedTextColor = IntelWhite
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(text = if (isAr) "إلغاء" else "Cancel", color = IntelSlate)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (titleAr.isNotBlank()) {
                                val matchedFile = uiState.files.find { it.id == selectedFileId }
                                val matchedTopic = uiState.topics.find { it.id == selectedTopicId }
                                viewModel.createEvent(
                                    titleAr = titleAr,
                                    titleEn = titleAr,
                                    summaryAr = summaryAr,
                                    description = summaryAr,
                                    status = selectedStatus,
                                    importance = selectedImportance,
                                    location = location,
                                    primaryCountryCode = "SA",
                                    linkedCountryCodes = "",
                                    linkedPersonNames = "",
                                    linkedOrgNames = "",
                                    topicId = selectedTopicId,
                                    topicName = matchedTopic?.nameAr,
                                    politicalFileId = selectedFileId,
                                    politicalFileTitle = matchedFile?.titleAr
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IntelCyan, contentColor = Color(0xFF031424))
                    ) {
                        Text(text = if (isAr) "إنشاء الإضبارة" else "Create Dossier")
                    }
                }
            }
        }
    }
}

@Composable
fun EventAiIntelligenceTab(
    event: PoliticalEvent,
    viewModel: DeskViewModel,
    uiState: DeskUiState,
    isAr: Boolean
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize().testTag("event_ai_intelligence_tab")
    ) {
        // Banner
        item {
            Surface(
                color = Color(0xFF072742),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, IntelCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = IntelCyan, modifier = Modifier.size(28.dp))
                    Column {
                        Text(
                            text = if (isAr) "مركز المعالجة والتحليل الذكي للحدث (Phase 4 AI)" else "AI Event Intelligence Hub",
                            color = IntelWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isAr)
                                "توليد ملخصات تنفيذية شاملة، مقارنة الروايات الإعلامية، رصد فجوات المعلومات وكشف التناقضات بدقة عالية."
                            else
                                "Synthesize cross-source narratives, detect contradictions, trace attributions & unmask gaps.",
                            color = IntelSlate,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Action Grid
        item {
            Text(
                text = if (isAr) "التحليلات الاستخباراتية المتخصصة" else "Analytical Workflows",
                color = IntelSlate,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // 1. Event Summary
                AiActionCard(
                    title = if (isAr) "توليد موجز الحدث الاستراتيجي (AI Event Summary)" else "Generate AI Event Summary",
                    description = if (isAr) "إجابة محكمة على: ماذا حدث؟ ما الجديد؟ من الأطراف؟ ما المؤكد وما المشكوك فيه؟" else "Executive 5-pillar briefing with confirmed vs uncertain facts.",
                    icon = Icons.Default.Summarize,
                    accentColor = IntelCyan,
                    isAr = isAr,
                    buttonTag = "event_ai_summary_button",
                    onClick = { viewModel.generateEventSummary(event.id) }
                )

                // 2. Narrative Comparison
                AiActionCard(
                    title = if (isAr) "مقارنة الروايات وتعدد المصادر (Narrative Comparison)" else "Cross-Source Narrative Comparison",
                    description = if (isAr) "مقارنة تغطية المصادر المختلفة، رصد زوايا التغطية، ونقاط الإجماع والخلاف بين الوكالات." else "Compare viewpoints, framing, consensus & disputed points across news outlets.",
                    icon = Icons.Default.CompareArrows,
                    accentColor = IntelBlue,
                    isAr = isAr,
                    buttonTag = "event_narrative_comparison_button",
                    onClick = { viewModel.compareNarratives(event.id) }
                )

                // 3. Contradiction Analysis
                AiActionCard(
                    title = if (isAr) "فحص التناقضات الدلالية والتوقيتية (Contradictions)" else "Detect Factual & Semantic Contradictions",
                    description = if (isAr) "كشف تضارب الأرقام، تباين التوقيتات، ونفي الإدارات والجهات الرسمية." else "Unmask timing, numerical and categorical contradictions between claims.",
                    icon = Icons.Default.Warning,
                    accentColor = RedHighRisk,
                    isAr = isAr,
                    buttonTag = "event_contradiction_analysis_button",
                    onClick = { viewModel.analyzeContradictions(event.id) }
                )

                // 4. Media Perspectives
                AiActionCard(
                    title = if (isAr) "تحليل التأطير وزوايا التغطية (Media Perspectives)" else "Media Perspective & Framing Analysis",
                    description = if (isAr) "تحليل المفردات المشحونة، النبرة العاطفية، واختلاف الأجندات بين الإعلام الدولي والمحلي." else "Identify loaded vocabulary, emotional tone, and editorial agendas.",
                    icon = Icons.Default.FilterVintage,
                    accentColor = Color(0xFFA78BFA),
                    isAr = isAr,
                    buttonTag = "event_media_perspective_button",
                    onClick = { viewModel.analyzeMediaPerspective(event.id) }
                )

                // 5. Information Gaps
                AiActionCard(
                    title = if (isAr) "رصد فجوات المعلومات والتحقق (Information Gaps)" else "Identify Unverified Claims & Information Gaps",
                    description = if (isAr) "حصر الأسئلة الاستخباراتية المفتوحة التي لم تؤكدها المصادر وتقديم توصيات للتحقق الميداني." else "Pinpoint unanswered questions and propose concrete verification actions.",
                    icon = Icons.Default.Search,
                    accentColor = AmberAlert,
                    isAr = isAr,
                    buttonTag = "event_information_gaps_button",
                    onClick = { viewModel.analyzeInformationGaps(event.id) }
                )

                // 6. Timeline Causality
                AiActionCard(
                    title = if (isAr) "موجز التسلسل السببي للخط الزمني (Timeline Arc)" else "Timeline Causality & Turning Points",
                    description = if (isAr) "تتبع القوس السردي، المحطات المفصلية، وتطور الأحداث من السبب إلى الأثر." else "Analyze causality sequence, turning points and narrative trajectory.",
                    icon = Icons.Default.Timeline,
                    accentColor = IntelGreen,
                    isAr = isAr,
                    buttonTag = "event_timeline_summary_button",
                    onClick = { viewModel.generateTimelineSummary(event.id) }
                )
            }
        }
    }
}

@Composable
fun AiActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    isAr: Boolean,
    buttonTag: String,
    onClick: () -> Unit
) {
    Surface(
        color = DeskDarkCard,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeskDarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = accentColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    }
                }
                Column {
                    Text(text = title, color = IntelWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(text = description, color = IntelSlate, fontSize = 10.sp, lineHeight = 14.sp)
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color(0xFF031424)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag(buttonTag)
            ) {
                Text(text = if (isAr) "تشغيل" else "Run", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
