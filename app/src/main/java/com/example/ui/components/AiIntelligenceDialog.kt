package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import com.example.ui.viewmodel.DeskUiState
import com.example.ui.viewmodel.DeskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiIntelligenceDialog(
    viewModel: DeskViewModel,
    uiState: DeskUiState,
    onDismiss: () -> Unit
) {
    val isAr = uiState.isArabic
    val activeType = uiState.activeAiAnalysisType ?: AiAnalysisType.EVENT_SUMMARY
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") } }

    var selectedModel by remember { mutableStateOf(uiState.selectedAiModel) }
    var showModelMenu by remember { mutableStateOf(false) }
    val availableModels = listOf("gemini-3.5-flash", "gemini-3.5-pro", "gemini-3.1-flash-lite")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .fillMaxHeight(0.92f)
                .testTag("ai_intelligence_dialog"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, IntelCyan.copy(alpha = 0.5f))
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = IntelCyan.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = IntelCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isAr) "وحدة التحليل والذكاء الاصطناعي" else "AI Intelligence Engine",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = IntelWhite
                            )
                            Text(
                                text = if (isAr) activeType.displayNameAr() else activeType.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = IntelCyan
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Model Selector Chip
                        Box {
                            Surface(
                                color = IntelNavyLight,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { showModelMenu = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Memory, contentDescription = null, tint = IntelSlate, modifier = Modifier.size(14.dp))
                                    Text(text = selectedModel, color = IntelWhite, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = IntelSlate, modifier = Modifier.size(16.dp))
                                }
                            }

                            DropdownMenu(
                                expanded = showModelMenu,
                                onDismissRequest = { showModelMenu = false }
                            ) {
                                availableModels.forEach { model ->
                                    DropdownMenuItem(
                                        text = { Text(model, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                                        onClick = {
                                            selectedModel = model
                                            viewModel.setAiModel(model)
                                            showModelMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = IntelWhite)
                        }
                    }
                }

                // Loading or Error State
                if (uiState.isAiLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = IntelCyan, strokeWidth = 3.dp)
                            Text(
                                text = if (isAr) "جاري إجراء التحليل الاستخباراتي والتحقق من الأدلة..." else "Synthesizing intelligence & verifying claims...",
                                color = IntelSlate,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else if (uiState.aiErrorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = RedHighRisk, modifier = Modifier.size(48.dp))
                            Text(
                                text = uiState.aiErrorMessage,
                                color = RedHighRisk,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { viewModel.clearAiError() },
                                colors = ButtonDefaults.buttonColors(containerColor = IntelNavyLight)
                            ) {
                                Text(if (isAr) "إغلاق التنبيه" else "Dismiss", color = IntelWhite)
                            }
                        }
                    }
                } else {
                    // Content based on activeType
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        when (activeType) {
                            AiAnalysisType.EVENT_SUMMARY -> {
                                uiState.currentEventSummary?.let { summary ->
                                    item { EventSummaryView(summary = summary, isAr = isAr) }
                                }
                            }
                            AiAnalysisType.NARRATIVE_COMPARISON -> {
                                uiState.currentNarrativeComparison?.let { comparison ->
                                    item { NarrativeComparisonView(comparison = comparison, isAr = isAr) }
                                }
                            }
                            AiAnalysisType.CONTRADICTION_ANALYSIS -> {
                                uiState.currentContradictionAnalysis?.let { analysis ->
                                    item { ContradictionAnalysisView(analysis = analysis, isAr = isAr) }
                                }
                            }
                            AiAnalysisType.SOURCE_TRACING -> {
                                uiState.currentSourceTracing?.let { tracing ->
                                    item { SourceTracingView(tracing = tracing, isAr = isAr) }
                                }
                            }
                            AiAnalysisType.WHAT_CHANGED -> {
                                uiState.currentWhatChanged?.let { whatChanged ->
                                    item { WhatChangedView(whatChanged = whatChanged, isAr = isAr) }
                                }
                            }
                            AiAnalysisType.PERSON_POSITION -> {
                                uiState.currentPersonPosition?.let { position ->
                                    item { PersonPositionView(position = position, isAr = isAr) }
                                }
                            }
                            AiAnalysisType.MEDIA_PERSPECTIVE -> {
                                uiState.currentMediaPerspective?.let { perspective ->
                                    item { MediaPerspectiveView(perspective = perspective, isAr = isAr) }
                                }
                            }
                            AiAnalysisType.EVIDENCE_ANALYSIS -> {
                                uiState.currentEvidenceAnalysis?.let { evidence ->
                                    item { EvidenceAnalysisView(evidence = evidence, isAr = isAr) }
                                }
                            }
                            AiAnalysisType.INFORMATION_GAPS -> {
                                uiState.currentInformationGaps?.let { gaps ->
                                    item { InformationGapsView(gaps = gaps, isAr = isAr) }
                                }
                            }
                            AiAnalysisType.FILE_ANALYSIS -> {
                                uiState.currentFileAnalysis?.let { fileReport ->
                                    item { FileAnalysisView(fileReport = fileReport, isAr = isAr) }
                                }
                            }
                            AiAnalysisType.TIMELINE_SUMMARY -> {
                                uiState.currentTimelineSummary?.let { timeline ->
                                    item { TimelineSummaryView(timeline = timeline, isAr = isAr) }
                                }
                            }
                            AiAnalysisType.DASHBOARD_BRIEF -> {
                                uiState.dashboardAiBrief?.let { brief ->
                                    item { DashboardBriefView(brief = brief, isAr = isAr) }
                                }
                            }
                            else -> {
                                item {
                                    Text(
                                        text = if (isAr) "لا توجد نتائج تحليل متاحة حالياً" else "No analysis results available",
                                        color = IntelSlate,
                                        fontSize = 13.sp
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
fun EventSummaryView(summary: EventSummaryResult, isAr: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Executive Summary
        SectionCard(title = if (isAr) "الموجز التنفيذي" else "Executive Summary", icon = Icons.Default.Summarize) {
            Text(text = summary.executiveSummary, color = IntelWhite, fontSize = 13.sp, lineHeight = 20.sp)
        }

        // 5 Core Pillars
        SectionCard(title = if (isAr) "ماذا حدث؟" else "What Happened?", icon = Icons.Default.Info) {
            Text(text = summary.whatHappened, color = Color(0xFFCBD5E1), fontSize = 12.sp, lineHeight = 19.sp)
        }

        SectionCard(title = if (isAr) "ما الجديد منذ آخر تحديث؟" else "What Changed?", icon = Icons.Default.TrendingUp) {
            Text(text = summary.whatChanged, color = IntelGold, fontSize = 12.sp, lineHeight = 19.sp)
        }

        SectionCard(title = if (isAr) "الأطراف والمواقف المعلنة" else "Key Actors & Stances", icon = Icons.Default.People) {
            Text(text = summary.keyActorsAndStances, color = Color(0xFFCBD5E1), fontSize = 12.sp, lineHeight = 19.sp)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                SectionCard(title = if (isAr) "المعلومات المؤكدة" else "Confirmed Facts", icon = Icons.Default.CheckCircle, accentColor = IntelGreen) {
                    if (summary.confirmedFacts.isEmpty()) {
                        Text(text = if (isAr) "لا توجد حقائق كافية مؤكدة قطيعاً بعد" else "No verified facts yet", color = IntelSlate, fontSize = 11.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (fact in summary.confirmedFacts) {
                                Text(text = "• $fact", color = IntelGreen, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                SectionCard(title = if (isAr) "المعلومات غير المؤكدة" else "Uncertain Info", icon = Icons.Default.HelpOutline, accentColor = AmberAlert) {
                    if (summary.uncertainInfo.isEmpty()) {
                        Text(text = if (isAr) "لا توجد ادعاءات مشكوك فيها مسجلة" else "None", color = IntelSlate, fontSize = 11.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (info in summary.uncertainInfo) {
                                Text(text = "• $info", color = AmberAlert, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        if (summary.conflictingInfo.isNotEmpty()) {
            SectionCard(title = if (isAr) "نقاط التعارض والتضارب" else "Conflicting Information", icon = Icons.Default.Warning, accentColor = RedHighRisk) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (conflict in summary.conflictingInfo) {
                        Text(text = "⚠ $conflict", color = RedHighRisk, fontSize = 11.sp)
                    }
                }
            }
        }

        // Citations List
        CitationsSection(citations = summary.citations, isAr = isAr)
    }
}

@Composable
fun NarrativeComparisonView(comparison: NarrativeComparisonResult, isAr: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionCard(title = if (isAr) "خلاصة مقارنة الروايات الصحفية" else "Synthesis Overview", icon = Icons.Default.CompareArrows) {
            Text(text = comparison.synthesisOverview, color = IntelWhite, fontSize = 13.sp, lineHeight = 20.sp)
        }

        // Source Perspectives
        SectionCard(title = if (isAr) "روايات المصادر المختلفة" else "Source Narratives", icon = Icons.Default.LibraryBooks) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (perspective in comparison.sourcePerspectives) {
                    Surface(
                        color = IntelNavyDark,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = perspective.sourceName, color = IntelCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                SourceTierBadge(perspective.tier)
                            }
                            Text(text = "التأطير: ${perspective.framing}", color = IntelSlate, fontSize = 11.sp)
                            Text(text = perspective.narrativeContent, color = IntelWhite, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Consensus vs Disputed
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                SectionCard(title = if (isAr) "نقاط الإجماع المشتركة" else "Consensus Points", icon = Icons.Default.CheckCircle, accentColor = IntelGreen) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (point in comparison.consensusPoints) {
                            Text(text = "✔ $point", color = IntelGreen, fontSize = 11.sp)
                        }
                    }
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                SectionCard(title = if (isAr) "نقاط الخلاف والتنازع" else "Disputed Points", icon = Icons.Default.FlashOn, accentColor = RedHighRisk) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (point in comparison.disputedPoints) {
                            Text(text = "✖ $point", color = RedHighRisk, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        CitationsSection(citations = comparison.citations, isAr = isAr)
    }
}

@Composable
fun ContradictionAnalysisView(analysis: ContradictionAnalysisResult, isAr: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionCard(title = if (isAr) "تقرير التناقضات بين الروايات" else "Contradictions Report", icon = Icons.Default.Warning, accentColor = RedHighRisk) {
            Text(text = analysis.overallVerdict, color = IntelWhite, fontSize = 13.sp, lineHeight = 20.sp)
        }

        SectionCard(title = if (isAr) "حالات التناقض المرصودة (${analysis.items.size})" else "Detected Discrepancies", icon = Icons.Default.List) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (item in analysis.items) {
                    Surface(
                        color = IntelNavyDark,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RedHighRisk.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "محل التناقض: ${item.disputedTopic}", color = RedHighRisk, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Surface(color = RedHighRisk.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                    Text(text = item.severity.displayNameAr(), color = RedHighRisk, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Text(text = "• ${item.partyA}: \"${item.statementA}\"", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                            Text(text = "• ${item.partyB}: \"${item.statementB}\"", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                            Text(text = "التحليل الاستخباري: ${item.explanation}", color = IntelSlate, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        CitationsSection(citations = analysis.citations, isAr = isAr)
    }
}

@Composable
fun SourceTracingView(tracing: SourceTracingResultData, isAr: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionCard(title = if (isAr) "نتيجة فحص المصدر الأول" else "Originality Verdict", icon = Icons.Default.Verified) {
            Surface(
                color = when (tracing.verdict) {
                    SourceVerdict.ORIGINAL -> IntelGreen.copy(alpha = 0.15f)
                    SourceVerdict.RECIRCULATED -> AmberAlert.copy(alpha = 0.15f)
                    SourceVerdict.DISPUTED -> RedHighRisk.copy(alpha = 0.15f)
                    SourceVerdict.INSUFFICIENT_EVIDENCE -> IntelSlate.copy(alpha = 0.15f)
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "الحكم النهائي: ${tracing.verdict.displayNameAr()}",
                        color = when (tracing.verdict) {
                            SourceVerdict.ORIGINAL -> IntelGreen
                            SourceVerdict.RECIRCULATED -> AmberAlert
                            SourceVerdict.DISPUTED -> RedHighRisk
                            SourceVerdict.INSUFFICIENT_EVIDENCE -> IntelSlate
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(text = tracing.explanation, color = IntelWhite, fontSize = 12.sp)
                }
            }
        }

        SectionCard(title = if (isAr) "مسار النقل وسلسلة الإسناد" else "Attribution Chain", icon = Icons.Default.LinearScale) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for ((idx, hop) in tracing.attributionChain.withIndex()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = IntelNavyLight, shape = CircleShape, modifier = Modifier.size(24.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "${idx + 1}", color = IntelCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = hop, color = IntelWhite, fontSize = 12.sp)
                    }
                }
            }
        }

        CitationsSection(citations = tracing.citations, isAr = isAr)
    }
}

@Composable
fun WhatChangedView(whatChanged: WhatChangedResult, isAr: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionCard(title = if (isAr) "ملخص التغييرات والتطورات الجديدة" else "Delta Summary", icon = Icons.Default.TrendingUp) {
            Text(text = whatChanged.deltaSummary, color = IntelWhite, fontSize = 13.sp, lineHeight = 20.sp)
        }

        SectionCard(title = if (isAr) "الأحداث التي طرأت عليها تطورات جوهرية" else "Evolving Events", icon = Icons.Default.Event) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (ev in whatChanged.evolvingEvents) {
                    Surface(color = IntelNavyDark, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(text = "• $ev", color = IntelCyan, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                    }
                }
            }
        }

        SectionCard(title = if (isAr) "المواقف والتصريحات المستجدة" else "Stance Shifts", icon = Icons.Default.RecordVoiceOver) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (shift in whatChanged.stanceShifts) {
                    Text(text = "• $shift", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                }
            }
        }

        CitationsSection(citations = whatChanged.citations, isAr = isAr)
    }
}

@Composable
fun PersonPositionView(position: PersonPositionResult, isAr: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionCard(title = if (isAr) "الموقف الراهن لـ ${position.personName}" else "Current Position", icon = Icons.Default.Person) {
            Text(text = position.currentStance, color = IntelWhite, fontSize = 13.sp, lineHeight = 20.sp)
        }

        SectionCard(title = if (isAr) "مسار تطور الموقف الزمني" else "Evolution Over Time", icon = Icons.Default.Timeline) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (step in position.evolutionOverTime) {
                    Text(text = "• $step", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                }
            }
        }

        if (position.notedContradictions.isNotEmpty()) {
            SectionCard(title = if (isAr) "تقلبات وتناقضات الموقف المسجلة" else "Noted Contradictions", icon = Icons.Default.Warning, accentColor = RedHighRisk) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (cont in position.notedContradictions) {
                        Text(text = "⚠ $cont", color = RedHighRisk, fontSize = 11.sp)
                    }
                }
            }
        }

        CitationsSection(citations = position.citations, isAr = isAr)
    }
}

@Composable
fun MediaPerspectiveView(perspective: MediaPerspectiveResult, isAr: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionCard(title = if (isAr) "تحليل التأطير وزوايا التغطية الإعلامية" else "Framing Summary", icon = Icons.Default.FilterVintage) {
            Text(text = perspective.overallFramingSummary, color = IntelWhite, fontSize = 13.sp, lineHeight = 20.sp)
        }

        SectionCard(title = if (isAr) "مقارنة زوايا التغطية بين الفئات الإعلامية" else "Coverage Angles", icon = Icons.Default.Compare) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (angle in perspective.coverageAngles) {
                    Surface(color = IntelNavyDark, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = angle.category, color = IntelCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(text = "التركيز: ${angle.focusPoints.joinToString()}", color = IntelWhite, fontSize = 11.sp)
                            Text(text = "المصطلحات المستخدمة: ${angle.loadedLanguageUsed.joinToString()}", color = IntelSlate, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        CitationsSection(citations = perspective.citations, isAr = isAr)
    }
}

@Composable
fun EvidenceAnalysisView(evidence: EvidenceAnalysisResult, isAr: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionCard(title = if (isAr) "تقييم الأدلة لـ [${evidence.claimText}]" else "Evidence Evaluation", icon = Icons.Default.Gavel) {
            Surface(
                color = when (evidence.verdict) {
                    EvidenceVerdict.CONFIRMED, EvidenceVerdict.SUPPORTED, EvidenceVerdict.LIKELY -> IntelGreen.copy(alpha = 0.15f)
                    EvidenceVerdict.REFUTED, EvidenceVerdict.CONTRADICTED -> RedHighRisk.copy(alpha = 0.15f)
                    EvidenceVerdict.DISPUTED -> AmberAlert.copy(alpha = 0.15f)
                    EvidenceVerdict.UNVERIFIED, EvidenceVerdict.INSUFFICIENT_EVIDENCE -> IntelSlate.copy(alpha = 0.15f)
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "الحكم الاستخباري: ${evidence.verdict.displayNameAr()} (درجة الثقة: ${(evidence.confidenceScore * 100).toInt()}%)",
                        color = when (evidence.verdict) {
                            EvidenceVerdict.CONFIRMED, EvidenceVerdict.SUPPORTED, EvidenceVerdict.LIKELY -> IntelGreen
                            EvidenceVerdict.REFUTED, EvidenceVerdict.CONTRADICTED -> RedHighRisk
                            EvidenceVerdict.DISPUTED -> AmberAlert
                            EvidenceVerdict.UNVERIFIED, EvidenceVerdict.INSUFFICIENT_EVIDENCE -> IntelSlate
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(text = evidence.explanation, color = IntelWhite, fontSize = 12.sp)
                }
            }
        }

        CitationsSection(citations = evidence.citations, isAr = isAr)
    }
}

@Composable
fun InformationGapsView(gaps: InformationGapsResult, isAr: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionCard(title = if (isAr) "ملخص فجوات المعلومات والتحقق" else "Gaps Overview", icon = Icons.Default.Search) {
            Text(text = gaps.overview, color = IntelWhite, fontSize = 13.sp, lineHeight = 20.sp)
        }

        SectionCard(title = if (isAr) "الأسئلة الاستخباراتية المفتوحة دون إجابة" else "Unanswered Questions", icon = Icons.Default.HelpOutline, accentColor = AmberAlert) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (q in gaps.unansweredQuestions) {
                    Text(text = "? $q", color = AmberAlert, fontSize = 12.sp)
                }
            }
        }

        SectionCard(title = if (isAr) "إجراءات التحقق الموصى بها" else "Recommended Verifications", icon = Icons.Default.FactCheck) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (rec in gaps.recommendedVerifications) {
                    Text(text = "• $rec", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                }
            }
        }

        CitationsSection(citations = gaps.citations, isAr = isAr)
    }
}

@Composable
fun FileAnalysisView(fileReport: FileAnalysisResult, isAr: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionCard(title = if (isAr) "الموجز الاستراتيجي للملف [${fileReport.fileTitle}]" else "Strategic Overview", icon = Icons.Default.FolderSpecial) {
            Text(text = fileReport.strategicOverview, color = IntelWhite, fontSize = 13.sp, lineHeight = 20.sp)
        }

        SectionCard(title = if (isAr) "مسارات الأحداث الرئيسية" else "Main Trajectories", icon = Icons.Default.Timeline) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (traj in fileReport.mainTrajectories) {
                    Text(text = "• $traj", color = IntelCyan, fontSize = 12.sp)
                }
            }
        }

        SectionCard(title = if (isAr) "السيناريوهات والاتجاهات المستقبلية" else "Outlook & Scenarios", icon = Icons.Default.Visibility, accentColor = IntelGold) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (sc in fileReport.forwardLookingOutlook) {
                    Text(text = "» $sc", color = IntelGold, fontSize = 12.sp)
                }
            }
        }

        CitationsSection(citations = fileReport.citations, isAr = isAr)
    }
}

@Composable
fun TimelineSummaryView(timeline: TimelineSummaryResult, isAr: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionCard(title = if (isAr) "الموجز السردي للتسلسل الزمني" else "Narrative Arc", icon = Icons.Default.AutoStories) {
            Text(text = timeline.narrativeArc, color = IntelWhite, fontSize = 13.sp, lineHeight = 20.sp)
        }

        SectionCard(title = if (isAr) "المحطات المفصلية (Turning Points)" else "Turning Points", icon = Icons.Default.Flag, accentColor = RedHighRisk) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (tp in timeline.turningPoints) {
                    Text(text = "⚑ $tp", color = RedHighRisk, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        SectionCard(title = if (isAr) "التسلسل السببي (Cause & Effect)" else "Causality Sequence", icon = Icons.Default.AltRoute) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (seq in timeline.causalitySequence) {
                    Text(text = "→ $seq", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                }
            }
        }

        CitationsSection(citations = timeline.citations, isAr = isAr)
    }
}

@Composable
fun DashboardBriefView(brief: DashboardAiBriefResult, isAr: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionCard(title = if (isAr) "الإيجاز الاستخباري الصباحي الشامل" else "Daily Intelligence Brief", icon = Icons.Default.Public) {
            Text(text = brief.overallSituationBrief, color = IntelWhite, fontSize = 13.sp, lineHeight = 20.sp)
        }

        SectionCard(title = if (isAr) "أبرز التطورات المتسارعة" else "Key Developments", icon = Icons.Default.FlashOn, accentColor = AmberAlert) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (dev in brief.todayKeyDevelopments) {
                    Text(text = "⚡ $dev", color = AmberAlert, fontSize = 12.sp)
                }
            }
        }

        SectionCard(title = if (isAr) "التصريحات السياسية الحاسمة" else "Critical Statements", icon = Icons.Default.RecordVoiceOver) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (st in brief.criticalStatements) {
                    Text(text = "❝ $st ❞", color = IntelCyan, fontSize = 11.sp)
                }
            }
        }

        CitationsSection(citations = brief.citations, isAr = isAr)
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color = IntelCyan,
    content: @Composable () -> Unit
) {
    Surface(
        color = DeskDarkCard,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeskDarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                Text(text = title, color = accentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            HorizontalDivider(color = DeskDarkBorder.copy(alpha = 0.5f))
            content()
        }
    }
}

@Composable
fun CitationsSection(citations: List<AiCitation>, isAr: Boolean) {
    if (citations.isEmpty()) return

    Surface(
        color = IntelNavyDark.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, IntelBorder.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = IntelSlate, modifier = Modifier.size(14.dp))
                Text(
                    text = if (isAr) "المصادر الموثقة للاستشهاد (${citations.size})" else "Documented Citations (${citations.size})",
                    color = IntelSlate,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (cit in citations) {
                    Surface(
                        color = DeskDarkSurface,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = cit.sourceName, color = IntelCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                SourceTierBadge(cit.sourceTier)
                            }
                            Text(text = cit.articleTitle, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            Text(text = "\"${cit.excerpt}\"", color = IntelSlate, fontSize = 10.sp, maxLines = 2)
                        }
                    }
                }
            }
        }
    }
}
