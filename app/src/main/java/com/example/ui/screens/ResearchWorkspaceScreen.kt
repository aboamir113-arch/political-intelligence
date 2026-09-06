package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeskTab
import com.example.ui.viewmodel.DeskUiState
import com.example.ui.viewmodel.DeskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResearchWorkspaceScreen(
    viewModel: DeskViewModel,
    uiState: DeskUiState
) {
    val clipboardManager = LocalClipboardManager.current
    val isAr = uiState.isArabic
    val workspace = uiState.workspaceState

    var showPickerSheet by remember { mutableStateOf(false) }
    var pickerTab by remember { mutableStateOf("ARTICLES") }
    var pickerSearch by remember { mutableStateOf("") }

    var showSaveDialog by remember { mutableStateOf(false) }
    var saveTitleInput by remember { mutableStateOf("") }
    var showSavedSessionsSheet by remember { mutableStateOf(false) }
    var copyFeedback by remember { mutableStateOf(false) }

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
                                .background(IntelCyan.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TravelExplore,
                                contentDescription = null,
                                tint = IntelCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isAr) "مساحة البحث والتحليل الاستخباري" else "AI Research Workspace",
                                color = IntelWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isAr) "تحليل متعدد المصادر • استعلامات تأصيلية RAG • فحص الوقائع"
                                else "Multi-source intelligence synthesis with grounded RAG",
                                color = IntelSlate,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Top Action buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = { showSavedSessionsSheet = true },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = DeskDarkSurfaceVariant,
                                contentColor = IntelCyan
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (isAr) "الجلسات" else "Sessions", fontSize = 11.sp)
                        }

                        if (!workspace.isEmpty) {
                            OutlinedButton(
                                onClick = {
                                    saveTitleInput = uiState.currentRagQuestion.take(40)
                                    showSaveDialog = true
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = IntelEmerald),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(IntelEmerald.copy(alpha = 0.5f))),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (isAr) "حفظ" else "Save", fontSize = 11.sp)
                            }

                            IconButton(
                                onClick = { viewModel.clearWorkspace() },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = if (isAr) "مسح مساحة البحث" else "Clear Workspace",
                                    tint = IntelCrimson.copy(alpha = 0.8f)
                                )
                            }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Workspace Target Selection Bar
            item {
                Spacer(Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DeskDarkBorder)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Layers, contentDescription = null, tint = IntelCyan, modifier = Modifier.size(18.dp))
                                Text(
                                    text = if (isAr) "المواد المحددة للتحليل (${workspace.totalItemsCount})" else "Selected Entities (${workspace.totalItemsCount})",
                                    color = IntelWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { showPickerSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = IntelBlue),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (isAr) "إضافة مواد" else "Add Items", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (workspace.isEmpty) {
                            Spacer(Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DeskDarkSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (isAr) "مساحة البحث خالية حالياً" else "Workspace is empty",
                                        color = IntelSlate,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = if (isAr) "اضغط على \"إضافة مواد\" لاختيار مقالات، أحداث، ملفات، أو شخصيات لإجراء تحليل RAG مؤصل."
                                        else "Click 'Add Items' to pick articles, events, dossiers or persons for grounded analysis.",
                                        color = IntelSlate.copy(alpha = 0.8f),
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            Spacer(Modifier.height(10.dp))
                            // Chips Row of Selected Items
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Articles
                                items(workspace.selectedArticles) { art ->
                                    WorkspaceItemChip(
                                        title = art.title,
                                        tag = art.sourceName,
                                        color = IntelCyan,
                                        onRemove = { viewModel.removeArticleFromWorkspace(art.id) }
                                    )
                                }
                                // Events
                                items(workspace.selectedEvents) { ev ->
                                    WorkspaceItemChip(
                                        title = ev.titleAr,
                                        tag = ev.status.name,
                                        color = IntelGold,
                                        onRemove = { viewModel.removeEventFromWorkspace(ev.id) }
                                    )
                                }
                                // Political File
                                workspace.selectedPoliticalFile?.let { f ->
                                    item {
                                        WorkspaceItemChip(
                                            title = f.titleAr,
                                            tag = "ملف: ${f.priority.displayNameAr()}",
                                            color = IntelPurple,
                                            onRemove = { viewModel.setPoliticalFileInWorkspace(null) }
                                        )
                                    }
                                }
                                // Persons
                                items(workspace.selectedPersons) { p ->
                                    WorkspaceItemChip(
                                        title = p.nameAr,
                                        tag = p.currentRoleAr,
                                        color = IntelEmerald,
                                        onRemove = { viewModel.removePersonFromWorkspace(p.id) }
                                    )
                                }
                                // Sources
                                items(workspace.selectedSources) { s ->
                                    WorkspaceItemChip(
                                        title = s.nameAr,
                                        tag = s.tier.name,
                                        color = IntelBlue,
                                        onRemove = { viewModel.removeSourceFromWorkspace(s.id) }
                                    )
                                }
                                // Topics
                                items(workspace.selectedTopics) { t ->
                                    WorkspaceItemChip(
                                        title = t.nameAr,
                                        tag = t.category.name,
                                        color = IntelAmber,
                                        onRemove = { viewModel.removeTopicFromWorkspace(t.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Query Input & Analytical Control Panel
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DeskDarkBorder)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isAr) "استعلام المحلل الذكي (Grounded RAG)" else "Grounded Intelligence Query",
                                color = IntelWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Language Selector & Model
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = uiState.selectedAiModel,
                                    color = IntelCyan,
                                    fontSize = 10.sp,
                                    modifier = Modifier
                                        .background(IntelCyan.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )

                                Text(
                                    text = if (uiState.ragLanguage == "ar") "العربية" else "EN",
                                    color = IntelWhite,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(DeskDarkSurfaceVariant, RoundedCornerShape(4.dp))
                                        .clickable {
                                            viewModel.setRagLanguage(if (uiState.ragLanguage == "ar") "en" else "ar")
                                        }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Query Input Field
                        OutlinedTextField(
                            value = uiState.currentRagQuestion,
                            onValueChange = { viewModel.setRagQuestion(it) },
                            placeholder = {
                                Text(
                                    text = if (isAr) "اطرح سؤالك التحليلي حول المواد المحددة أو قاعدة البيانات..."
                                    else "Ask analytical question about selected entities or full intelligence database...",
                                    color = IntelSlate,
                                    fontSize = 12.sp
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DeskDarkSurfaceVariant,
                                unfocusedContainerColor = DeskDarkSurfaceVariant,
                                focusedBorderColor = IntelCyan,
                                unfocusedBorderColor = DeskDarkBorder,
                                focusedTextColor = IntelWhite,
                                unfocusedTextColor = IntelWhite
                            ),
                            shape = RoundedCornerShape(8.dp),
                            trailingIcon = {
                                if (uiState.currentRagQuestion.isNotBlank()) {
                                    IconButton(onClick = { viewModel.setRagQuestion("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = null, tint = IntelSlate)
                                    }
                                }
                            }
                        )

                        Spacer(Modifier.height(8.dp))

                        // Prompt Templates Chips
                        Text(
                            text = if (isAr) "نماذج أسئلة سريعة:" else "Quick analytical prompts:",
                            color = IntelSlate,
                            fontSize = 10.sp
                        )
                        Spacer(Modifier.height(4.dp))

                        val prompts = if (isAr) listOf(
                            "ماذا حدث في هذه القضية؟",
                            "ما الذي تغير منذ آخر تطور؟",
                            "أين توجد التناقضات بين المصادر؟",
                            "ما نقاط الاتفاق بين هذه الروايات؟",
                            "ما المعلومات المؤكدة وما غير المؤكد؟",
                            "ما أقوى الأدلة المتوفرة؟",
                            "كيف تختلف التغطية الإعلامية؟",
                            "ما فجوات المعلومات والمجهول؟"
                        ) else listOf(
                            "What happened in this case?",
                            "What changed since the last update?",
                            "Where are the contradictions?",
                            "What is confirmed vs uncertain?",
                            "What are the strongest evidence items?",
                            "What are the information gaps?"
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(prompts) { prompt ->
                                Surface(
                                    color = DeskDarkSurfaceVariant,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.clickable {
                                        viewModel.setRagQuestion(prompt)
                                        viewModel.executeRagQuery(prompt)
                                    }
                                ) {
                                    Text(
                                        text = prompt,
                                        color = IntelSilver,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Submit Execution Button
                        Button(
                            onClick = { viewModel.executeRagQuery() },
                            enabled = !uiState.isRagLoading && uiState.currentRagQuestion.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = IntelCyan, contentColor = Color(0xFF070B14)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (uiState.isRagLoading) {
                                CircularProgressIndicator(
                                    color = Color(0xFF070B14),
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(if (isAr) "جاري البحث والتأصيل والاستنتاج..." else "Retrieving & Synthesizing RAG...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (isAr) "تنفيذ التحليل الذكي المؤصل" else "Run Grounded Intelligence Analysis", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 3. Error Feedback
            uiState.ragErrorMessage?.let { err ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = IntelCrimson.copy(alpha = 0.15f)),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(IntelCrimson)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = IntelCrimson)
                            Spacer(Modifier.width(8.dp))
                            Text(err, color = IntelCrimson, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 4. RAG Structured Answer Results
            uiState.currentRagAnswer?.let { answer ->
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Grounding Verification Status Banner
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (answer.isGroundingSufficient) IntelEmerald.copy(alpha = 0.12f)
                                else IntelAmber.copy(alpha = 0.12f)
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (answer.isGroundingSufficient) IntelEmerald else IntelAmber
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(
                                        imageVector = if (answer.isGroundingSufficient) Icons.Default.Verified else Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = if (answer.isGroundingSufficient) IntelEmerald else IntelAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (answer.isGroundingSufficient) {
                                            if (isAr) "تحليل مؤصل استخبارياً — خالٍ من الهلوسة والافتراضات"
                                            else "Grounded Intelligence — Verified Against Database Records"
                                        } else {
                                            if (isAr) "تنبيه: أدلة غير كافية للإثبات القطعي"
                                            else "Notice: Insufficient Evidentiary Records"
                                        },
                                        color = if (answer.isGroundingSufficient) IntelEmerald else IntelAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "${answer.sources.size} مصادر موثقة",
                                    color = IntelSlate,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Direct Answer Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(IntelCyan.copy(alpha = 0.4f))),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isAr) "الإجابة المباشرة الموثقة" else "Direct Grounded Answer",
                                        color = IntelCyan,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    IconButton(
                                        onClick = {
                                            val fullText = buildString {
                                                appendLine("=== إجابة الذكاء الاصطناعي التأصيلية ===")
                                                appendLine(answer.answer)
                                                appendLine("\n=== أهم النتائج ===")
                                                answer.keyFindings.forEach { appendLine("• $it") }
                                                appendLine("\n=== التحليل والاستنتاج ===")
                                                appendLine(answer.analysis)
                                                appendLine("\n=== المصادر ===")
                                                answer.sources.forEach { appendLine("- ${it.title} (${it.sourceName})") }
                                            }
                                            clipboardManager.setText(AnnotatedString(fullText))
                                            copyFeedback = true
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = IntelSlate, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = answer.answer,
                                    color = IntelWhite,
                                    fontSize = 13.sp,
                                    lineHeight = 21.sp
                                )
                            }
                        }

                        // Key Findings Card
                        if (answer.keyFindings.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = if (isAr) "أهم النتائج المرصودة" else "Key Intelligence Findings",
                                        color = IntelGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    answer.keyFindings.forEach { finding ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 3.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text("•", color = IntelGold, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 6.dp))
                                            Text(finding, color = IntelSilver, fontSize = 12.sp, lineHeight = 18.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Fact-Checking Classification Matrix
                        if (answer.factVerdicts.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = if (isAr) "مصفوفة فحص الوقائع ومستويات التأكيد" else "Fact-Check & Verification Matrix",
                                        color = IntelCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(8.dp))

                                    answer.factVerdicts.forEach { verdict ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .background(DeskDarkSurfaceVariant, RoundedCornerShape(6.dp))
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(verdict.statement, color = IntelWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                                Text(verdict.rationale, color = IntelSlate, fontSize = 10.sp)
                                            }

                                            Spacer(Modifier.width(8.dp))

                                            val (badgeColor, badgeText) = when (verdict.classification) {
                                                FactClassification.FACT -> IntelEmerald to verdict.classification.labelAr()
                                                FactClassification.INFERENCE -> IntelBlue to verdict.classification.labelAr()
                                                FactClassification.UNCERTAIN -> IntelAmber to verdict.classification.labelAr()
                                                FactClassification.CONFLICTED -> IntelCrimson to verdict.classification.labelAr()
                                                FactClassification.UNKNOWN -> IntelSlate to verdict.classification.labelAr()
                                            }

                                            Text(
                                                text = badgeText,
                                                color = badgeColor,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Conflicts & Contradictions (if any)
                        if (answer.conflicts.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = IntelCrimson.copy(alpha = 0.08f)),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(IntelCrimson.copy(alpha = 0.4f))),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.CompareArrows, contentDescription = null, tint = IntelCrimson, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = if (isAr) "التناقضات وتباين الروايات" else "Diverging Narratives & Conflicts",
                                            color = IntelCrimson,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    answer.conflicts.forEach { c ->
                                        Text("⚠️ $c", color = IntelSilver, fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                                    }
                                }
                            }
                        }

                        // Analytical Synthesis Card
                        if (answer.analysis.isNotBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = if (isAr) "الاستنتاج التحليلي التقديري" else "Strategic Analytical Synthesis",
                                        color = IntelPurple,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = answer.analysis,
                                        color = IntelSilver,
                                        fontSize = 12.sp,
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }

                        // Citations & Verifiable Sources
                        if (answer.sources.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = if (isAr) "المصادر والمراجع الموثقة (${answer.sources.size})" else "Citations & Documented Sources (${answer.sources.size})",
                                        color = IntelCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(8.dp))

                                    answer.sources.forEach { citation ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .background(DeskDarkSurfaceVariant, RoundedCornerShape(6.dp))
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .background(IntelCyan.copy(alpha = 0.2f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = citation.entityType.take(1),
                                                    color = IntelCyan,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(citation.title, color = IntelWhite, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(
                                                    text = "${citation.sourceName ?: "جهة رسمية"} • ${citation.dateString ?: ""}",
                                                    color = IntelSlate,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Actions Row: Save as report / send to chat
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.selectTab(DeskTab.REPORTS)
                                    viewModel.generateDailyBriefReport()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IntelBlue),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Article, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (isAr) "توليد تقرير رسمي" else "Generate Report", fontSize = 11.sp)
                            }

                            FilledTonalButton(
                                onClick = {
                                    viewModel.createAndSelectConversation(uiState.currentRagQuestion.take(30))
                                    viewModel.selectTab(DeskTab.AI_ASSISTANT)
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = DeskDarkSurfaceVariant, contentColor = IntelCyan),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (isAr) "مواصلة الحوار" else "Open in Chat", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Item Picker BottomSheet / Dialog
    // ---------------------------------------------------------------------------------------------
    if (showPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPickerSheet = false },
            containerColor = DeskDarkSurface,
            contentColor = IntelWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxHeight(0.8f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAr) "إضافة مواد إلى مساحة البحث" else "Add Entities to Workspace",
                        color = IntelWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showPickerSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = IntelSlate)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Picker Tabs
                val tabs = listOf(
                    "ARTICLES" to if (isAr) "المقالات" else "Articles",
                    "EVENTS" to if (isAr) "الأحداث" else "Events",
                    "FILES" to if (isAr) "الملفات" else "Files",
                    "PERSONS" to if (isAr) "الشخصيات" else "Persons",
                    "SOURCES" to if (isAr) "المصادر" else "Sources"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(tabs) { (key, label) ->
                        val selected = pickerTab == key
                        Surface(
                            color = if (selected) IntelCyan else DeskDarkSurfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { pickerTab = key }
                        ) {
                            Text(
                                text = label,
                                color = if (selected) Color(0xFF070B14) else IntelSilver,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Search field inside picker
                OutlinedTextField(
                    value = pickerSearch,
                    onValueChange = { pickerSearch = it },
                    placeholder = { Text(if (isAr) "بحث سريع في العناصر..." else "Search items...", fontSize = 11.sp, color = IntelSlate) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DeskDarkSurfaceVariant,
                        unfocusedContainerColor = DeskDarkSurfaceVariant,
                        focusedBorderColor = IntelCyan,
                        unfocusedBorderColor = DeskDarkBorder,
                        focusedTextColor = IntelWhite,
                        unfocusedTextColor = IntelWhite
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(Modifier.height(10.dp))

                // List of Items
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    when (pickerTab) {
                        "ARTICLES" -> {
                            val list = uiState.articles.filter {
                                pickerSearch.isBlank() || it.title.contains(pickerSearch, ignoreCase = true)
                            }
                            items(list) { art ->
                                val isSelected = workspace.selectedArticles.any { it.id == art.id }
                                SelectableItemRow(
                                    title = art.title,
                                    subtitle = "${art.sourceName} • ${art.sourceTier.name}",
                                    isSelected = isSelected,
                                    onToggle = {
                                        if (isSelected) viewModel.removeArticleFromWorkspace(art.id)
                                        else viewModel.addArticleToWorkspace(art)
                                    }
                                )
                            }
                        }
                        "EVENTS" -> {
                            val list = uiState.events.filter {
                                pickerSearch.isBlank() || it.titleAr.contains(pickerSearch, ignoreCase = true)
                            }
                            items(list) { ev ->
                                val isSelected = workspace.selectedEvents.any { it.id == ev.id }
                                SelectableItemRow(
                                    title = ev.titleAr,
                                    subtitle = "الحالة: ${ev.status.name} • الأهمية: ${ev.importance.name}",
                                    isSelected = isSelected,
                                    onToggle = {
                                        if (isSelected) viewModel.removeEventFromWorkspace(ev.id)
                                        else viewModel.addEventToWorkspace(ev)
                                    }
                                )
                            }
                        }
                        "FILES" -> {
                            val list = uiState.files.filter {
                                pickerSearch.isBlank() || it.titleAr.contains(pickerSearch, ignoreCase = true)
                            }
                            items(list) { file ->
                                val isSelected = workspace.selectedPoliticalFile?.id == file.id
                                SelectableItemRow(
                                    title = file.titleAr,
                                    subtitle = "الأولوية: ${file.priority.displayNameAr()}",
                                    isSelected = isSelected,
                                    onToggle = {
                                        if (isSelected) viewModel.setPoliticalFileInWorkspace(null)
                                        else viewModel.setPoliticalFileInWorkspace(file)
                                    }
                                )
                            }
                        }
                        "PERSONS" -> {
                            val list = uiState.persons.filter {
                                pickerSearch.isBlank() || it.nameAr.contains(pickerSearch, ignoreCase = true)
                            }
                            items(list) { person ->
                                val isSelected = workspace.selectedPersons.any { it.id == person.id }
                                SelectableItemRow(
                                    title = person.nameAr,
                                    subtitle = person.currentRoleAr,
                                    isSelected = isSelected,
                                    onToggle = {
                                        if (isSelected) viewModel.removePersonFromWorkspace(person.id)
                                        else viewModel.addPersonToWorkspace(person)
                                    }
                                )
                            }
                        }
                        "SOURCES" -> {
                            val list = uiState.sources.filter {
                                pickerSearch.isBlank() || it.nameAr.contains(pickerSearch, ignoreCase = true)
                            }
                            items(list) { src ->
                                val isSelected = workspace.selectedSources.any { it.id == src.id }
                                SelectableItemRow(
                                    title = src.nameAr,
                                    subtitle = src.tier.name,
                                    isSelected = isSelected,
                                    onToggle = {
                                        if (isSelected) viewModel.removeSourceFromWorkspace(src.id)
                                        else viewModel.addSourceToWorkspace(src)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Save Research Dialog
    // ---------------------------------------------------------------------------------------------
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = DeskDarkSurface,
            title = { Text(if (isAr) "حفظ جلسة مساحة البحث" else "Save Research Session", color = IntelWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (isAr) "أدخل عنواناً للجلسة لتتمكن من استرجاعها لاحقاً:" else "Enter a title for this research session:", color = IntelSlate, fontSize = 12.sp)
                    OutlinedTextField(
                        value = saveTitleInput,
                        onValueChange = { saveTitleInput = it },
                        placeholder = { Text("عنوان الجلسة...", fontSize = 11.sp, color = IntelSlate) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DeskDarkSurfaceVariant,
                            unfocusedContainerColor = DeskDarkSurfaceVariant,
                            focusedBorderColor = IntelCyan,
                            unfocusedBorderColor = DeskDarkBorder,
                            focusedTextColor = IntelWhite,
                            unfocusedTextColor = IntelWhite
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCurrentResearch(saveTitleInput, uiState.currentRagQuestion)
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IntelEmerald)
                ) {
                    Text(if (isAr) "تأكيد الحفظ" else "Save", fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text(if (isAr) "إلغاء" else "Cancel", color = IntelSlate, fontSize = 11.sp)
                }
            }
        )
    }

    // ---------------------------------------------------------------------------------------------
    // Saved Sessions BottomSheet
    // ---------------------------------------------------------------------------------------------
    if (showSavedSessionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSavedSessionsSheet = false },
            containerColor = DeskDarkSurface,
            contentColor = IntelWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxHeight(0.7f)
            ) {
                Text(
                    text = if (isAr) "جلسات البحث والتحليل المحفوظة (${uiState.savedResearchList.size})" else "Saved Research Sessions",
                    color = IntelWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))

                if (uiState.savedResearchList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(if (isAr) "لا توجد جلسات بحث محفوظة بعد" else "No saved research sessions yet", color = IntelSlate, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.savedResearchList) { session ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.loadSavedResearch(session)
                                        showSavedSessionsSheet = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = DeskDarkSurfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(session.title, color = IntelWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(session.mainQuestion, color = IntelSlate, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteSavedResearch(session) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = IntelCrimson.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                    }
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
private fun WorkspaceItemChip(
    title: String,
    tag: String,
    color: Color,
    onRemove: () -> Unit
) {
    Surface(
        color = DeskDarkSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Column {
                Text(
                    text = title.take(28),
                    color = IntelWhite,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = tag,
                    color = color,
                    fontSize = 8.sp
                )
            }
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = IntelSlate, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
private fun SelectableItemRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        color = if (isSelected) IntelCyan.copy(alpha = 0.1f) else DeskDarkSurfaceVariant,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) IntelCyan else Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = IntelWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, color = IntelSlate, fontSize = 9.sp)
            }
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = IntelCyan,
                    uncheckedColor = IntelSlate,
                    checkmarkColor = Color(0xFF070B14)
                )
            )
        }
    }
}
