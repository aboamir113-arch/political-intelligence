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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeskUiState
import com.example.ui.viewmodel.DeskViewModel

enum class EntitySubTab {
    PERSONS,
    STANCE_SHIFTS,
    ORGANIZATIONS,
    COUNTRIES;

    fun titleAr(): String = when (this) {
        PERSONS -> "الشخصيات والمسؤولون"
        STANCE_SHIFTS -> "تحول المواقف السياسية"
        ORGANIZATIONS -> "المؤسسات والوزارات"
        COUNTRIES -> "الدول ذات الأولوية"
    }

    fun titleEn(): String = when (this) {
        PERSONS -> "Persons & Officials"
        STANCE_SHIFTS -> "Stance Shifts"
        ORGANIZATIONS -> "Organizations"
        COUNTRIES -> "Sovereign States"
    }
}

@Composable
fun EntitiesScreen(
    viewModel: DeskViewModel,
    uiState: DeskUiState,
    modifier: Modifier = Modifier
) {
    val isAr = uiState.isArabic
    var activeSubTab by remember { mutableStateOf(EntitySubTab.PERSONS) }
    var showAddPersonDialog by remember { mutableStateOf(false) }
    var showAddStanceDialog by remember { mutableStateOf(false) }
    var selectedPersonForDetail by remember { mutableStateOf<Person?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (activeSubTab == EntitySubTab.PERSONS) {
                FloatingActionButton(
                    onClick = { showAddPersonDialog = true },
                    containerColor = IntelBlue,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = if (isAr) "إضافة شخصية" else "Add Person"
                    )
                }
            } else if (activeSubTab == EntitySubTab.STANCE_SHIFTS) {
                FloatingActionButton(
                    onClick = { showAddStanceDialog = true },
                    containerColor = IntelGold,
                    contentColor = Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PostAdd,
                        contentDescription = if (isAr) "تسجيل تحول في الموقف" else "Record Stance Shift"
                    )
                }
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
            // Header
            item {
                Column {
                    Text(
                        text = if (isAr) "سجل الكيانات والفاعلين السياسيين" else "Entities & Political Actors",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isAr)
                            "تتبع مواقف الدبلوماسيين والمسؤولين والوزارات، ورصد التحولات السلوكية في السياسة الخارجية."
                        else
                            "Monitor diplomatic postures, institutions, and documented semantic policy shifts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = IntelSlate
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sub Tabs Scrollable Row
                    ScrollableTabRow(
                        selectedTabIndex = activeSubTab.ordinal,
                        containerColor = DeskDarkSurface,
                        contentColor = IntelCyan,
                        edgePadding = 0.dp,
                        divider = {}
                    ) {
                        EntitySubTab.values().forEach { tab ->
                            Tab(
                                selected = activeSubTab == tab,
                                onClick = { activeSubTab = tab },
                                text = {
                                    Text(
                                        text = if (isAr) tab.titleAr() else tab.titleEn(),
                                        fontSize = 12.sp,
                                        fontWeight = if (activeSubTab == tab) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Sub Tab Content
            when (activeSubTab) {
                EntitySubTab.PERSONS -> {
                    items(uiState.persons, key = { it.id }) { person ->
                        PersonCardItem(
                            person = person,
                            isArabic = isAr,
                            onClick = { selectedPersonForDetail = person }
                        )
                    }
                }
                EntitySubTab.STANCE_SHIFTS -> {
                    if (uiState.recentStanceShifts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isAr) "لم يتم تسجيل تحولات في المواقف بعد. اضغط + لإضافة تحليل جديد." else "No recorded stance shifts yet. Click + to add.",
                                    color = IntelSlate,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        items(uiState.recentStanceShifts, key = { it.id }) { shift ->
                            StanceShiftCardItem(shift = shift, isArabic = isAr)
                        }
                    }
                }
                EntitySubTab.ORGANIZATIONS -> {
                    items(uiState.organizations, key = { it.id }) { org ->
                        OrgCardItem(org = org, isArabic = isAr)
                    }
                }
                EntitySubTab.COUNTRIES -> {
                    items(uiState.countries, key = { it.code }) { country ->
                        CountryCardItem(country = country, isArabic = isAr)
                    }
                }
            }
        }
    }

    if (showAddPersonDialog) {
        AddPersonDialog(
            isArabic = isAr,
            onDismiss = { showAddPersonDialog = false },
            onConfirm = { nameAr, nameEn, roleAr, roleEn, countryCode, bio, tags ->
                viewModel.addPerson(nameAr, nameEn, roleAr, roleEn, countryCode, bio, tags)
                showAddPersonDialog = false
            }
        )
    }

    if (showAddStanceDialog) {
        AddStanceShiftDialog(
            isArabic = isAr,
            persons = uiState.persons,
            topics = uiState.topics,
            onDismiss = { showAddStanceDialog = false },
            onConfirm = { personId, topicId, issue, prevStance, curStance, shiftType, statement, sourceName, sourceUrl, context, confidence ->
                viewModel.recordStanceShift(
                    personId, topicId, issue, prevStance, curStance, shiftType, statement, sourceName, sourceUrl, context, confidence
                )
                showAddStanceDialog = false
            }
        )

        selectedPersonForDetail?.let { person ->
            PersonDetailIntelligenceDialog(
                person = person,
                isArabic = isAr,
                uiState = uiState,
                onExplainRelationship = { viewModel.requestExplainabilityForRelationship(it) },
                onExplainChange = { viewModel.requestExplainabilityForChange(it) },
                onDismiss = { selectedPersonForDetail = null }
            )
        }
    }
}

@Composable
fun PersonCardItem(
    person: Person,
    isArabic: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = person.photoEmoji, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) person.nameAr else person.nameEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isArabic) person.currentRoleAr else person.currentRoleEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = IntelCyan
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = person.countryCode,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = IntelSlate
                    )
                }
            }

            if (person.bio.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = person.bio,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 16.sp
                )
            }

            if (person.lastStatementSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF162032))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "💬 \"${person.lastStatementSummary}\"",
                        fontSize = 11.sp,
                        color = IntelGold,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (person.previousRoles.isNotBlank()) "السابق: ${person.previousRoles}" else "",
                    fontSize = 11.sp,
                    color = IntelSlate
                )

                if (person.stanceShiftCount > 0) {
                    Text(
                        text = if (isArabic) "🔄 ${person.stanceShiftCount} تحول في المواقف" else "🔄 ${person.stanceShiftCount} Stance shifts",
                        fontSize = 11.sp,
                        color = IntelGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StanceShiftCardItem(shift: PersonPosition, isArabic: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(IntelGold.copy(alpha = 0.4f)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF451A03))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isArabic) "تحول في الموقف السياسي" else "Policy Stance Shift",
                        color = IntelGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (isArabic) "قوة الدليل: ${shift.evidenceConfidence}%" else "Evidence: ${shift.evidenceConfidence}%",
                    fontSize = 11.sp,
                    color = IntelEmerald,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = shift.issueTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Comparative Box: Previous vs Current Stance
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF162032))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (isArabic) "⏪ الموقف السابق: ${shift.previousStance}" else "⏪ Previous Stance: ${shift.previousStance}",
                    fontSize = 12.sp,
                    color = IntelSlate,
                    lineHeight = 16.sp
                )
                Divider(color = DeskDarkBorder.copy(alpha = 0.4f))
                Text(
                    text = if (isArabic) "⏩ الموقف الحالي: ${shift.currentStance}" else "⏩ Current Stance: ${shift.currentStance}",
                    fontSize = 12.sp,
                    color = IntelCyan,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Verbatim Quote & Source
            Text(
                text = "نص التصريح الحرفي: \"${shift.verbatimStatement}\"",
                fontSize = 11.sp,
                color = Color(0xFFE2E8F0),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "المصدر: ${shift.sourceName}",
                    fontSize = 10.sp,
                    color = IntelSlate
                )
                Text(
                    text = "السياق: ${shift.context}",
                    fontSize = 10.sp,
                    color = IntelSlate
                )
            }
        }
    }
}

@Composable
fun OrgCardItem(org: Organization, isArabic: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                Text(
                    text = if (isArabic) org.nameAr else org.nameEn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isArabic) org.type.displayNameAr() else org.type.name,
                        fontSize = 11.sp,
                        color = IntelCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = org.description,
                style = MaterialTheme.typography.bodySmall,
                color = IntelSlate
            )
            if (org.websiteUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = org.websiteUrl,
                    fontSize = 11.sp,
                    color = IntelBlue
                )
            }
        }
    }
}

@Composable
fun CountryCardItem(country: Country, isArabic: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DeskDarkBorder))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = country.flagEmoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isArabic) country.nameAr else country.nameEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${country.region} • ${country.code}",
                        style = MaterialTheme.typography.bodySmall,
                        color = IntelSlate
                    )
                }
            }

            if (country.isStrategicFocus) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0C365C))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isArabic) "أولوية استراتيجية" else "Strategic Focus",
                        fontSize = 11.sp,
                        color = IntelCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AddPersonDialog(
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (
        nameAr: String,
        nameEn: String,
        roleAr: String,
        roleEn: String,
        countryCode: String,
        bio: String,
        tags: String
    ) -> Unit
) {
    var nameAr by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var roleAr by remember { mutableStateOf("") }
    var roleEn by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("SA") }
    var bio by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("Diplomat, Official") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isArabic) "إضافة شخصية سياسية للمتابعة" else "Add Person for Monitoring",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = nameAr,
                        onValueChange = { nameAr = it },
                        label = { Text(if (isArabic) "اسم الشخصية بالعربية *" else "Name (Arabic) *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text(if (isArabic) "الاسم بالإنجليزية" else "Name (English)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = roleAr,
                        onValueChange = { roleAr = it },
                        label = { Text(if (isArabic) "المنصب الحالي بالعربية *" else "Current Role (Arabic) *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = countryCode,
                        onValueChange = { countryCode = it.uppercase() },
                        label = { Text(if (isArabic) "رمز الدولة (مثل: SA, EG, US)" else "Country Code") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text(if (isArabic) "نبذة موجزة وتوجهات سياسية" else "Bio & Strategic Profile") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameAr.isNotBlank() && roleAr.isNotBlank()) {
                        onConfirm(nameAr, nameEn, roleAr, roleEn, countryCode, bio, tags)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IntelBlue)
            ) {
                Text(if (isArabic) "حفظ وإضافة" else "Save & Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}

@Composable
fun AddStanceShiftDialog(
    isArabic: Boolean,
    persons: List<Person>,
    topics: List<Topic>,
    onDismiss: () -> Unit,
    onConfirm: (
        personId: Long,
        topicId: Long,
        issue: String,
        prevStance: String,
        curStance: String,
        shiftType: StanceShiftType,
        statement: String,
        sourceName: String,
        sourceUrl: String,
        context: String,
        confidence: Int
    ) -> Unit
) {
    var selectedPersonId by remember { mutableStateOf(persons.firstOrNull()?.id ?: 1L) }
    var selectedTopicId by remember { mutableStateOf(topics.firstOrNull()?.id ?: 1L) }
    var issue by remember { mutableStateOf("") }
    var prevStance by remember { mutableStateOf("") }
    var curStance by remember { mutableStateOf("") }
    var shiftType by remember { mutableStateOf(StanceShiftType.SIGNIFICANT_REVERSAL) }
    var statement by remember { mutableStateOf("") }
    var sourceName by remember { mutableStateOf("واس / رويترز") }
    var sourceUrl by remember { mutableStateOf("https://...") }
    var context by remember { mutableStateOf("") }
    var confidence by remember { mutableStateOf(90) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isArabic) "توثيق تحول في موقف سياسي" else "Document Stance Shift",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = issue,
                        onValueChange = { issue = it },
                        label = { Text(if (isArabic) "عنوان القضية أو الملف *" else "Issue Title *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = prevStance,
                        onValueChange = { prevStance = it },
                        label = { Text(if (isArabic) "الموقف السابق *" else "Previous Stance *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = curStance,
                        onValueChange = { curStance = it },
                        label = { Text(if (isArabic) "الموقف الحالي الجديد *" else "Current Stance *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = statement,
                        onValueChange = { statement = it },
                        label = { Text(if (isArabic) "التصريح الحرفي الدال على التغيير *" else "Verbatim Statement *") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
                item {
                    OutlinedTextField(
                        value = sourceName,
                        onValueChange = { sourceName = it },
                        label = { Text(if (isArabic) "المصدر الدقيق للتصريح *" else "Verifiable Source *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = context,
                        onValueChange = { context = it },
                        label = { Text(if (isArabic) "سياق التصريح والمناسبة" else "Context & Occasion") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (issue.isNotBlank() && prevStance.isNotBlank() && curStance.isNotBlank()) {
                        onConfirm(
                            selectedPersonId, selectedTopicId, issue, prevStance, curStance,
                            shiftType, statement, sourceName, sourceUrl, context, confidence
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IntelGold)
            ) {
                Text(
                    text = if (isArabic) "حفظ التحول وتوثيقه" else "Save & Document",
                    color = Color(0xFF1E1E1E),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}

@Composable
fun PersonDetailIntelligenceDialog(
    person: Person,
    isArabic: Boolean,
    uiState: DeskUiState,
    onExplainRelationship: (PoliticalRelationship) -> Unit,
    onExplainChange: (DetectedChangeItem) -> Unit,
    onDismiss: () -> Unit
) {
    val personRels = remember(uiState.relationships, person.id) {
        uiState.relationships.filter {
            (it.sourceEntityType == EntityType.PERSON && it.sourceEntityId == person.id) ||
                    (it.targetEntityType == EntityType.PERSON && it.targetEntityId == person.id)
        }
    }

    val personChanges = remember(uiState.detectedChanges, person.id) {
        uiState.detectedChanges.filter {
            it.entityType == EntityType.PERSON && it.entityId == person.id
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(person.photoEmoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isArabic) person.nameAr else person.nameEn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (isArabic) person.currentRoleAr else person.currentRoleEn,
                        fontSize = 12.sp,
                        color = IntelCyan
                    )
                }
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (person.bio.isNotBlank()) {
                    item {
                        Text(
                            text = if (isArabic) "الملف الشخصي والنبذة:" else "Bio & Profile:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = IntelCyan
                        )
                        Text(text = person.bio, fontSize = 12.sp, color = Color(0xFFE2E8F0))
                    }
                }

                if (person.lastStatementSummary.isNotBlank()) {
                    item {
                        Surface(
                            color = Color(0xFF162032),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = if (isArabic) "آخر تصريح مرصود:" else "Latest Statement:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = IntelGold
                                )
                                Text(text = "\"${person.lastStatementSummary}\"", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }

                // Phase 6: Connected Relationships
                item {
                    Text(
                        text = if (isArabic) "شبكة العلاقات والتحالفات (${personRels.size}):" else "Relationships Network (${personRels.size}):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = IntelCyan
                    )
                    if (personRels.isEmpty()) {
                        Text(
                            text = if (isArabic) "لم يتم رصد علاقات موثقة بعد." else "No documented relationships yet.",
                            fontSize = 11.sp,
                            color = IntelSlate
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            personRels.forEach { rel ->
                                val otherName = if (rel.sourceEntityType == EntityType.PERSON && rel.sourceEntityId == person.id) {
                                    rel.targetEntityName
                                } else {
                                    rel.sourceEntityName
                                }
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
                                        Text("$otherName [${rel.relationshipType.displayNameAr()}]", fontSize = 11.sp, color = Color.White)
                                        Text("تفسير ↗", fontSize = 10.sp, color = IntelCyan)
                                    }
                                }
                            }
                        }
                    }
                }

                // Phase 6: Stance Shifts / Changes
                if (personChanges.isNotEmpty()) {
                    item {
                        Text(
                            text = if (isArabic) "تحولات في المواقف مرصودة (${personChanges.size}):" else "Detected Stance Shifts (${personChanges.size}):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AmberAlert
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            personChanges.forEach { ch ->
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
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = IntelBlue)) {
                Text(if (isArabic) "إغلاق" else "Close")
            }
        }
    )
}
