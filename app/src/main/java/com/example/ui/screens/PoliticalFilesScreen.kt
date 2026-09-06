package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.PriorityBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeskUiState
import com.example.ui.viewmodel.DeskViewModel

@Composable
fun PoliticalFilesScreen(
    viewModel: DeskViewModel,
    uiState: DeskUiState,
    modifier: Modifier = Modifier
) {
    val isAr = uiState.isArabic
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedFileForDetail by remember { mutableStateOf<PoliticalFile?>(null) }

    val filteredFiles = remember(uiState.files, uiState.filePriorityFilter) {
        if (uiState.filePriorityFilter == null) uiState.files
        else uiState.files.filter { it.priority == uiState.filePriorityFilter }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = IntelBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (isAr) "إنشاء ملف جديد" else "Create File"
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
            // Header & Priority Filter
            item {
                Column {
                    Text(
                        text = if (isAr) "الملفات السياسية والاستراتيجية" else "Strategic Political Dossiers",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isAr)
                            "متابعة بؤر التوتر والملفات الإقليمية الحساسة وربطها بالدول والفاعلين والتطورات."
                        else
                            "Track active geopolitical dossiers, linked sovereigns, actors and developments.",
                        style = MaterialTheme.typography.bodySmall,
                        color = IntelSlate
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = uiState.filePriorityFilter == null,
                                onClick = { viewModel.setFilePriorityFilter(null) },
                                label = {
                                    Text(
                                        text = if (isAr) "جميع الملفات (${uiState.files.size})" else "All Dossiers (${uiState.files.size})",
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IntelCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = IntelCyan
                                )
                            )
                        }
                        items(FilePriority.values()) { priority ->
                            val count = uiState.files.count { it.priority == priority }
                            FilterChip(
                                selected = uiState.filePriorityFilter == priority,
                                onClick = {
                                    viewModel.setFilePriorityFilter(if (uiState.filePriorityFilter == priority) null else priority)
                                },
                                label = {
                                    Text(
                                        text = "${if (isAr) priority.displayNameAr() else priority.name} ($count)",
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IntelBlue.copy(alpha = 0.3f),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Dossiers List
            items(filteredFiles, key = { it.id }) { file ->
                DossierCardItem(
                    file = file,
                    isArabic = isAr,
                    onClick = { selectedFileForDetail = file },
                    onToggleStatus = {
                        val nextStatus = when (file.status) {
                            FileStatus.ACTIVE -> FileStatus.MONITORING
                            FileStatus.MONITORING -> FileStatus.ARCHIVED
                            FileStatus.ARCHIVED -> FileStatus.ACTIVE
                        }
                        viewModel.updateFileStatus(file, nextStatus)
                    }
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateFileDialog(
            isArabic = isAr,
            topics = uiState.topics,
            countries = uiState.countries,
            onDismiss = { showCreateDialog = false },
            onConfirm = { titleAr, titleEn, desc, priority, topicId, primaryCountry, linkedCountries, notes ->
                viewModel.createPoliticalFile(
                    titleAr, titleEn, desc, priority, topicId, primaryCountry, linkedCountries, notes
                )
                showCreateDialog = false
            }
        )
    }

    selectedFileForDetail?.let { file ->
        FileDetailDialog(
            file = file,
            isArabic = isAr,
            uiState = uiState,
            onExplainRelationship = { viewModel.requestExplainabilityForRelationship(it) },
            onExplainChange = { viewModel.requestExplainabilityForChange(it) },
            onDismiss = { selectedFileForDetail = null }
        )
    }
}

@Composable
fun DossierCardItem(
    file: PoliticalFile,
    isArabic: Boolean,
    onClick: () -> Unit,
    onToggleStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                PriorityBadge(priority = file.priority, isArabic = isArabic)
                AssistChip(
                    onClick = onToggleStatus,
                    label = {
                        Text(
                            text = if (isArabic) file.status.displayNameAr() else file.status.name,
                            fontSize = 11.sp
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (file.status == FileStatus.ACTIVE) Color(0xFF0F3627) else Color(0xFF1E293B),
                        labelColor = if (file.status == FileStatus.ACTIVE) IntelEmerald else IntelSlate
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isArabic) file.titleAr else file.titleEn,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = file.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCBD5E1),
                maxLines = 3,
                lineHeight = 18.sp
            )

            if (file.recentDevelopments.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF162032))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "⚡ ",
                        fontSize = 12.sp
                    )
                    Text(
                        text = file.recentDevelopments,
                        fontSize = 11.sp,
                        color = IntelGold,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = DeskDarkBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "الدول المرتبطة: ${file.linkedCountryCodes}" else "Linked: ${file.linkedCountryCodes}",
                    fontSize = 11.sp,
                    color = IntelCyan
                )

                Text(
                    text = if (isArabic) "تفاصيل الملف ↗" else "Details ↗",
                    fontSize = 11.sp,
                    color = IntelSlate,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun FileDetailDialog(
    file: PoliticalFile,
    isArabic: Boolean,
    uiState: DeskUiState,
    onExplainRelationship: (PoliticalRelationship) -> Unit,
    onExplainChange: (DetectedChangeItem) -> Unit,
    onDismiss: () -> Unit
) {
    val fileRels = remember(uiState.relationships, file.id) {
        uiState.relationships.filter {
            it.sourceFileId == file.id ||
                    (it.targetEntityType == EntityType.POLITICAL_FILE && it.targetEntityId == file.id)
        }
    }
    val fileChanges = remember(uiState.detectedChanges, file.id) {
        uiState.detectedChanges.filter {
            it.entityType == EntityType.POLITICAL_FILE && it.entityId == file.id
        }
    }
    val fileGaps = remember(uiState.informationGaps, file.id) {
        uiState.informationGaps.filter {
            it.linkedEntityType == EntityType.POLITICAL_FILE && it.linkedEntityId == file.id
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                PriorityBadge(priority = file.priority, isArabic = isArabic)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isArabic) file.titleAr else file.titleEn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text(
                        text = if (isArabic) "البيان والوصف الاستراتيجي:" else "Strategic Description:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = IntelCyan
                    )
                    Text(text = file.description, fontSize = 12.sp, color = Color(0xFFE2E8F0))
                }
                item {
                    Text(
                        text = if (isArabic) "ماذا تغير؟ (What Changed Delta):" else "What Changed Delta:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = IntelGold
                    )
                    Text(text = file.whatChangedDelta, fontSize = 12.sp, color = IntelGold)
                }

                // Phase 6: Connected Relationships
                if (fileRels.isNotEmpty()) {
                    item {
                        Text(
                            text = if (isArabic) "شبكة العلاقات المرتبطة بالملف (${fileRels.size}):" else "Connected Relationships (${fileRels.size}):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = IntelCyan
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            fileRels.forEach { rel ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth().clickable { onExplainRelationship(rel) },
                                    color = Color(0xFF132338),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${rel.sourceEntityName} ← [${rel.relationshipType.displayNameAr()}]", fontSize = 11.sp, color = Color.White)
                                        Text("لماذا؟", fontSize = 10.sp, color = IntelCyan)
                                    }
                                }
                            }
                        }
                    }
                }

                // Phase 6: Detected Changes
                if (fileChanges.isNotEmpty()) {
                    item {
                        Text(
                            text = if (isArabic) "التحولات والتغيرات المرصودة:" else "Detected Changes:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AmberAlert
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            fileChanges.forEach { ch ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth().clickable { onExplainChange(ch) },
                                    color = Color(0xFF2E1A1A),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Column(modifier = Modifier.padding(6.dp)) {
                                        Text(ch.whatChangedAr, fontSize = 11.sp, color = Color.White)
                                        Text(ch.comparedWithWhatAr, fontSize = 10.sp, color = IntelSlate)
                                    }
                                }
                            }
                        }
                    }
                }

                // Phase 6: Information Gaps
                if (fileGaps.isNotEmpty()) {
                    item {
                        Text(
                            text = if (isArabic) "فجوات معلوماتية قيد المتابعة:" else "Information Gaps:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = RedHighRisk
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            fileGaps.forEach { gap ->
                                Surface(
                                    color = Color(0xFF1C1917),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("• [${gap.status.displayNameAr()}] ${gap.titleAr}", fontSize = 11.sp, color = Color.White, modifier = Modifier.padding(6.dp))
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if (isArabic) "ملاحظات وتوجيهات المحلل:" else "Analyst Directive Notes:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = IntelSlate
                    )
                    Text(text = file.analystNotes.ifBlank { if (isArabic) "لا توجد ملاحظات سرية مدونة." else "No private notes." }, fontSize = 12.sp)
                }
                item {
                    Text(
                        text = if (isArabic) "الدول والأطراف الفاعلة:" else "Actors & Sovereign States:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = IntelSlate
                    )
                    Text(text = "الدول: ${file.linkedCountryCodes}\nالشخصيات: ${file.linkedPersonNames}\nالمؤسسات: ${file.linkedOrgNames}", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = IntelBlue)) {
                Text(if (isArabic) "إغلاق" else "Close")
            }
        }
    )
}

@Composable
fun CreateFileDialog(
    isArabic: Boolean,
    topics: List<Topic>,
    countries: List<Country>,
    onDismiss: () -> Unit,
    onConfirm: (
        titleAr: String,
        titleEn: String,
        description: String,
        priority: FilePriority,
        topicId: Long,
        primaryCountry: String,
        linkedCountries: String,
        analystNotes: String
    ) -> Unit
) {
    var titleAr by remember { mutableStateOf("") }
    var titleEn by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(FilePriority.HIGH) }
    var selectedTopicId by remember { mutableStateOf(topics.firstOrNull()?.id ?: 1L) }
    var primaryCountry by remember { mutableStateOf("SA") }
    var linkedCountries by remember { mutableStateOf("SA,EG,YE") }
    var analystNotes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isArabic) "إنشاء ملف سياسي جديد" else "Create Political Dossier",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = titleAr,
                        onValueChange = { titleAr = it },
                        label = { Text(if (isArabic) "عنوان الملف بالعربية *" else "Dossier Title (Arabic) *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = titleEn,
                        onValueChange = { titleEn = it },
                        label = { Text(if (isArabic) "عنوان الملف بالإنجليزية" else "Dossier Title (English)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(if (isArabic) "الوصف والسياق الاستراتيجي *" else "Description *") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
                item {
                    Text(
                        text = if (isArabic) "مستوى الأهمية:" else "Priority:",
                        fontSize = 12.sp,
                        color = IntelSlate
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilePriority.values().forEach { p ->
                            FilterChip(
                                selected = priority == p,
                                onClick = { priority = p },
                                label = { Text(if (isArabic) p.displayNameAr() else p.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = primaryCountry,
                        onValueChange = { primaryCountry = it.uppercase() },
                        label = { Text(if (isArabic) "الدولة المحورية (رمز: SA, EG)" else "Primary Country") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = linkedCountries,
                        onValueChange = { linkedCountries = it.uppercase() },
                        label = { Text(if (isArabic) "الدول المرتبطة (مفصولة بفواصل)" else "Linked Countries (comma separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = analystNotes,
                        onValueChange = { analystNotes = it },
                        label = { Text(if (isArabic) "ملاحظات المحلل والفرضيات" else "Analyst Notes & Hypotheses") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titleAr.isNotBlank()) {
                        onConfirm(
                            titleAr, titleEn, description, priority, selectedTopicId, primaryCountry, linkedCountries, analystNotes
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IntelBlue)
            ) {
                Text(if (isArabic) "حفظ وإنشاء الملف" else "Save & Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}
