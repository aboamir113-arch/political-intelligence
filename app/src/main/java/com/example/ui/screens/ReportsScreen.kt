package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeneratedReport
import com.example.data.model.ReportType
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeskUiState
import com.example.ui.viewmodel.DeskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: DeskViewModel,
    uiState: DeskUiState
) {
    val clipboardManager = LocalClipboardManager.current
    val isAr = uiState.isArabic

    var showPickerReportDialog by remember { mutableStateOf(false) }
    var reportGenerationTarget by remember { mutableStateOf("DAILY") } // DAILY, EVENT, FILE, MONITORING
    var selectedEntityId by remember { mutableStateOf<Long?>(null) }
    var copyFeedback by remember { mutableStateOf(false) }

    val filteredReports = remember(uiState.generatedReports, uiState.selectedReportTypeFilter) {
        val filter = uiState.selectedReportTypeFilter
        if (filter == null) uiState.generatedReports
        else uiState.generatedReports.filter { it.reportType == filter }
    }

    Scaffold(
        containerColor = DeskDarkBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeskDarkSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(IntelGold.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = IntelGold, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = if (isAr) "مركز التقارير السياسية والاستخبارية" else "Executive Reports Center",
                                color = IntelWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isAr) "تقارير يومية • تقارير أحداث متطورة • ملفات استراتيجية"
                                else "Daily briefs, developing event dossiers, and strategic file reports",
                                color = IntelSlate,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Top Action
                    Button(
                        onClick = { showPickerReportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = IntelGold, contentColor = Color(0xFF070B14)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (isAr) "توليد تقرير" else "New Report", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Report Type Filter Chips
                val types: List<Pair<ReportType?, String>> = listOf(
                    null to (if (isAr) "الكل (${uiState.generatedReports.size})" else "All"),
                    ReportType.DAILY_BRIEF to (if (isAr) "التقارير اليومية" else "Daily Briefs"),
                    ReportType.EVENT_REPORT to (if (isAr) "تقارير الأحداث" else "Events"),
                    ReportType.FILE_REPORT to (if (isAr) "الملفات الاستراتيجية" else "Dossiers"),
                    ReportType.MONITORING_REPORT to (if (isAr) "المراقبة والرصد" else "Monitoring"),
                    ReportType.WORKSPACE_REPORT to (if (isAr) "مساحة البحث" else "Workspace")
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(types) { (type, label) ->
                        val selected = uiState.selectedReportTypeFilter == type
                        Surface(
                            color = if (selected) IntelGold else DeskDarkSurfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { viewModel.setReportTypeFilter(type) }
                        ) {
                            Text(
                                text = label,
                                color = if (selected) Color(0xFF070B14) else IntelSilver,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Quick Generation Shortcuts Card
            item {
                Spacer(Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DeskDarkBorder)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isAr) "توليد فوري للتقارير التنفيذية" else "Instant Report Generators",
                            color = IntelWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Instant Daily Brief Button
                            Button(
                                onClick = { viewModel.generateDailyBriefReport() },
                                colors = ButtonDefaults.buttonColors(containerColor = IntelBlue),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !uiState.isReportGenerating
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (isAr) "التقرير اليومي" else "Daily Brief", fontSize = 11.sp)
                                }
                            }

                            // Event Report Button
                            FilledTonalButton(
                                onClick = {
                                    reportGenerationTarget = "EVENT"
                                    showPickerReportDialog = true
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = DeskDarkSurfaceVariant, contentColor = IntelCyan),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isAr) "تقرير حدث" else "Event Report", fontSize = 11.sp)
                            }

                            // Dossier Report Button
                            FilledTonalButton(
                                onClick = {
                                    reportGenerationTarget = "FILE"
                                    showPickerReportDialog = true
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = DeskDarkSurfaceVariant, contentColor = IntelPurple),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isAr) "تقرير ملف" else "Dossier", fontSize = 11.sp)
                            }
                        }

                        if (uiState.isReportGenerating) {
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(IntelGold.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(color = IntelGold, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Text(
                                    text = if (isAr) "جاري استخراج السجلات وتوليد التقرير التنفيذي المؤصل..." else "Generating grounded executive report...",
                                    color = IntelGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        uiState.reportGenerationError?.let { err ->
                            Spacer(Modifier.height(8.dp))
                            Text(err, color = IntelCrimson, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Generated Reports List
            if (filteredReports.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = IntelSlate, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (isAr) "لا توجد تقارير في هذا القسم بعد" else "No generated reports found",
                                color = IntelSlate,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredReports) { report ->
                    ReportCardItem(
                        report = report,
                        viewModel = viewModel,
                        isAr = isAr,
                        onClick = { viewModel.selectReportDetail(report) },
                        onDelete = { viewModel.deleteReport(report) }
                    )
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Full Report Reader Dialog / BottomSheet
    // ---------------------------------------------------------------------------------------------
    uiState.selectedReportDetail?.let { report ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectReportDetail(null) },
            containerColor = DeskDarkSurface,
            contentColor = IntelWhite
        ) {
            val reportData = remember(report.contentJson) {
                viewModel.deserializeReportData(report.contentJson)
            }
            val citations = remember(report.citationsJson) {
                viewModel.deserializeCitations(report.citationsJson)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxHeight(0.9f)
            ) {
                // Reader Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "تقرير تحليلي رسمي • v${report.version}",
                            color = IntelGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = {
                                val fullText = buildString {
                                    appendLine("=== ${report.title} ===")
                                    appendLine("النوع: ${report.reportType.displayNameAr()} | الإصدار: v${report.version}")
                                    appendLine("التاريخ: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(report.createdAt))}")
                                    appendLine("\n--- ملخص تنفيذي ---")
                                    appendLine(report.executiveSummary)
                                    reportData?.sections?.forEach { sec ->
                                        appendLine("\n--- ${sec.title} ---")
                                        appendLine(sec.content)
                                    }
                                }
                                clipboardManager.setText(AnnotatedString(fullText))
                                copyFeedback = true
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = IntelCyan, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = { viewModel.selectReportDetail(null) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = IntelSlate, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title & Classification Stamp
                    item {
                        Column {
                            Text(
                                text = report.title,
                                color = IntelWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (report.subtitle.isNotBlank()) {
                                Text(
                                    text = report.subtitle,
                                    color = IntelSlate,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Metadata Box
                    item {
                        Surface(
                            color = DeskDarkSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("المصادر المعتمدة", color = IntelSlate, fontSize = 9.sp)
                                    Text("${citations.count()}", color = IntelCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("درجة الموثوقية", color = IntelSlate, fontSize = 9.sp)
                                    Text("100%", color = IntelEmerald, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("النموذج التحليلي", color = IntelSlate, fontSize = 9.sp)
                                    Text(report.modelUsed.take(15), color = IntelGold, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    // Executive Summary Highlight
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = IntelBlue.copy(alpha = 0.12f)),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(IntelBlue.copy(alpha = 0.4f))),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = if (isAr) "الملخص التنفيذي المركز" else "Executive Summary",
                                    color = IntelCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = report.executiveSummary,
                                    color = IntelWhite,
                                    fontSize = 12.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    // Detailed Sections
                    reportData?.sections?.let { sections ->
                        items(sections) { sec ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DeskDarkSurfaceVariant),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = sec.title,
                                        color = IntelGold,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = sec.content,
                                        color = IntelSilver,
                                        fontSize = 12.sp,
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }
                    }

                    // Citations & Verifiable Sources
                    item {
                        if (citations.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DeskDarkSurfaceVariant),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = if (isAr) "سجل المصادر والمراجع الموثقة (${citations.count()})" else "Citations (${citations.count()})",
                                        color = IntelCyan,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    citations.forEach { cit ->
                                        Text(
                                            text = "• ${cit.title} (${cit.sourceName ?: "جهة رسمية"})",
                                            color = IntelSlate,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(30.dp)) }
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Report Generator Picker Dialog
    // ---------------------------------------------------------------------------------------------
    if (showPickerReportDialog) {
        AlertDialog(
            onDismissRequest = { showPickerReportDialog = false },
            containerColor = DeskDarkSurface,
            title = { Text(if (isAr) "اختيار نوع التقرير المطلوب" else "Generate Intelligence Report", color = IntelWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Type selector
                    val options = listOf(
                        "DAILY" to (if (isAr) "التقرير السياسي اليومي الشامل" else "Daily Political Brief"),
                        "EVENT" to (if (isAr) "تقرير شامل لحدث سياسي متطور" else "Event Comprehensive Report"),
                        "FILE" to (if (isAr) "تقرير استراتيجي لملف سياسي" else "Political Dossier Report"),
                        "MONITORING" to (if (isAr) "تقرير مراقبة ورصد لشخصية أو كيان" else "Entity Monitoring Report")
                    )

                    options.forEach { (key, title) ->
                        val isSelected = reportGenerationTarget == key
                        Surface(
                            color = if (isSelected) IntelGold.copy(alpha = 0.15f) else DeskDarkSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) IntelGold else Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    reportGenerationTarget = key
                                    selectedEntityId = null
                                }
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) IntelGold else IntelSilver,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Entity Selector for Event or File
                    if (reportGenerationTarget == "EVENT") {
                        Text(if (isAr) "اختر الحدث:" else "Select Event:", color = IntelSlate, fontSize = 11.sp)
                        LazyColumn(modifier = Modifier.height(120.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(uiState.events) { ev ->
                                val sel = selectedEntityId == ev.id
                                Surface(
                                    color = if (sel) IntelCyan.copy(alpha = 0.2f) else DeskDarkSurfaceVariant,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedEntityId = ev.id }
                                ) {
                                    Text(ev.titleAr, color = IntelWhite, fontSize = 11.sp, modifier = Modifier.padding(6.dp), maxLines = 1)
                                }
                            }
                        }
                    } else if (reportGenerationTarget == "FILE") {
                        Text(if (isAr) "اختر الملف السياسي:" else "Select Dossier:", color = IntelSlate, fontSize = 11.sp)
                        LazyColumn(modifier = Modifier.height(120.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(uiState.files) { f ->
                                val sel = selectedEntityId == f.id
                                Surface(
                                    color = if (sel) IntelPurple.copy(alpha = 0.2f) else DeskDarkSurfaceVariant,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedEntityId = f.id }
                                ) {
                                    Text(f.titleAr, color = IntelWhite, fontSize = 11.sp, modifier = Modifier.padding(6.dp), maxLines = 1)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPickerReportDialog = false
                        when (reportGenerationTarget) {
                            "DAILY" -> viewModel.generateDailyBriefReport()
                            "EVENT" -> selectedEntityId?.let { viewModel.generateEventComprehensiveReport(it) }
                            "FILE" -> selectedEntityId?.let { viewModel.generatePoliticalFileStrategicReport(it) }
                            "MONITORING" -> {
                                val firstPerson = uiState.persons.firstOrNull()
                                if (firstPerson != null) {
                                    viewModel.generateMonitoringReport("PERSON", firstPerson.id, firstPerson.nameAr)
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IntelGold, contentColor = Color(0xFF070B14))
                ) {
                    Text(if (isAr) "توليد التقرير الآن" else "Generate Report", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPickerReportDialog = false }) {
                    Text(if (isAr) "إلغاء" else "Cancel", color = IntelSlate, fontSize = 11.sp)
                }
            }
        )
    }
}

@Composable
private fun ReportCardItem(
    report: GeneratedReport,
    viewModel: DeskViewModel,
    isAr: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val citations = remember(report.citationsJson) {
        viewModel.deserializeCitations(report.citationsJson)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DeskDarkBorder)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type & Version badge
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val (typeColor, typeLabel) = when (report.reportType) {
                        ReportType.DAILY_BRIEF -> IntelBlue to report.reportType.displayNameAr()
                        ReportType.EVENT_REPORT -> IntelGold to report.reportType.displayNameAr()
                        ReportType.FILE_REPORT -> IntelPurple to report.reportType.displayNameAr()
                        ReportType.MONITORING_REPORT -> IntelEmerald to report.reportType.displayNameAr()
                        ReportType.WORKSPACE_REPORT -> IntelCyan to report.reportType.displayNameAr()
                    }

                    Text(
                        text = typeLabel,
                        color = typeColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(typeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    Text(
                        text = "v${report.version}",
                        color = IntelSlate,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(DeskDarkSurfaceVariant, RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = IntelSlate, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = report.title,
                color = IntelWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))
            Text(
                text = report.executiveSummary,
                color = IntelSlate,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp
            )

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(report.createdAt)),
                    color = IntelSlate,
                    fontSize = 9.sp
                )

                Text(
                    text = "${citations.count()} مصادر معتمدة",
                    color = IntelCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
