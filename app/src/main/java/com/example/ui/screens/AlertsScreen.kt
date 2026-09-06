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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeskTab
import com.example.ui.viewmodel.DeskUiState
import com.example.ui.viewmodel.DeskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: DeskViewModel,
    uiState: DeskUiState
) {
    val isAr = uiState.isArabic
    var selectedTab by remember { mutableStateOf(0) }
    var selectedSeverity by remember { mutableStateOf<AlertSeverity?>(null) }

    val filteredAlerts = remember(uiState.alerts, selectedSeverity) {
        if (selectedSeverity == null) uiState.alerts
        else uiState.alerts.filter { it.severity == selectedSeverity }
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
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = AmberAlert,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAr) "مركز التنبيهات الذكية (Smart Alerts)" else "Smart Alerts Desk",
                            style = MaterialTheme.typography.titleLarge,
                            color = IntelWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (isAr)
                            "${uiState.unreadAlertsCount} تنبيهات غير مقروءة • تنبيهات التناقضات والتغيرات الميدانية"
                        else
                            "${uiState.unreadAlertsCount} unread • Discrepancies & stance shift alerts",
                        style = MaterialTheme.typography.bodySmall,
                        color = IntelSlate
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (uiState.unreadAlertsCount > 0) {
                        TextButton(
                            onClick = { viewModel.markAllAlertsAsRead() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isAr) "تحديد الكل كمقروء" else "Mark All Read",
                                color = IntelCyan,
                                fontSize = 12.sp
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { viewModel.setShowAlertRuleDialog(true) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = IntelCyan,
                            contentColor = Color(0xFF031424)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isAr) "قاعدة تنبيه" else "New Rule", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-tabs (Incoming Alerts vs Alert Rules)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DeskDarkCard,
                contentColor = IntelCyan
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = if (isAr) "التنبيهات الواردة (${uiState.alerts.size})" else "Alerts (${uiState.alerts.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = if (isAr) "قواعد الرصد الذكية (${uiState.alertRules.size})" else "Rules (${uiState.alertRules.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                // Incoming Alerts View
                // Severity Filter Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedSeverity == null,
                            onClick = { selectedSeverity = null },
                            label = { Text(if (isAr) "الكل (${uiState.alerts.size})" else "All", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IntelCyan,
                                selectedLabelColor = Color(0xFF031424),
                                containerColor = DeskDarkCard,
                                labelColor = IntelSlate
                            )
                        )
                    }

                    items(AlertSeverity.values()) { sev ->
                        val count = uiState.alerts.count { it.severity == sev }
                        FilterChip(
                            selected = selectedSeverity == sev,
                            onClick = { selectedSeverity = if (selectedSeverity == sev) null else sev },
                            label = { Text("${if (isAr) sev.displayNameAr() else sev.name} ($count)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (sev) {
                                    AlertSeverity.CRITICAL -> RedHighRisk
                                    AlertSeverity.HIGH -> AmberAlert
                                    AlertSeverity.MEDIUM -> IntelCyan
                                    AlertSeverity.LOW -> IntelSlate
                                },
                                selectedLabelColor = if (sev == AlertSeverity.CRITICAL) Color.White else Color(0xFF031424),
                                containerColor = DeskDarkCard,
                                labelColor = IntelSlate
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (filteredAlerts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(DeskDarkCard, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = IntelSlate, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isAr) "لا توجد تنبيهات نشطة مطابقة للفلتر المحدد" else "No active alerts matching filter",
                                color = IntelSlate,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredAlerts, key = { it.id }) { alert ->
                            AlertCard(
                                alert = alert,
                                isAr = isAr,
                                onMarkAsRead = { viewModel.markAlertAsRead(alert.id) },
                                onDelete = { viewModel.deleteAlert(alert) },
                                onNavigateToEvent = { eventId ->
                                    val event = uiState.events.find { it.id == eventId }
                                    if (event != null) {
                                        viewModel.selectEvent(event)
                                        viewModel.selectTab(DeskTab.EVENTS)
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                // Alert Rules View
                if (uiState.alertRules.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(DeskDarkCard, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Rule, contentDescription = null, tint = IntelSlate, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isAr) "لم يتم تهيئة قواعد رصد ذكية بعد" else "No alert rules configured yet",
                                color = IntelSlate,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.alertRules, key = { it.id }) { rule ->
                            AlertRuleCard(
                                rule = rule,
                                isAr = isAr,
                                onToggle = { enabled -> viewModel.toggleAlertRule(rule.id, enabled) },
                                onDelete = { viewModel.deleteAlertRule(rule) }
                            )
                        }
                    }
                }
            }
        }

        // Add Rule Dialog
        if (uiState.showAlertRuleDialog) {
            CreateAlertRuleDialog(
                viewModel = viewModel,
                uiState = uiState,
                onDismiss = { viewModel.setShowAlertRuleDialog(false) }
            )
        }
    }
}

@Composable
fun AlertCard(
    alert: AlertItem,
    isAr: Boolean,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit,
    onNavigateToEvent: (Long) -> Unit
) {
    val severityColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> RedHighRisk
        AlertSeverity.HIGH -> AmberAlert
        AlertSeverity.MEDIUM -> IntelCyan
        AlertSeverity.LOW -> IntelSlate
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMarkAsRead() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alert.isRead) DeskDarkCard else Color(0xFF091F34)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (alert.isRead) DeskDarkBorder else severityColor.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Unread Dot or Icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 10.dp, top = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (alert.isRead) Color.Transparent else severityColor)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = when (alert.type) {
                        AlertType.CONTRADICTION -> Icons.Default.Warning
                        AlertType.BREAKING_NEWS -> Icons.Default.Bolt
                        AlertType.NEW_STATEMENT -> Icons.Default.RecordVoiceOver
                        AlertType.IMPORTANT_DEVELOPMENT -> Icons.Default.PriorityHigh
                        AlertType.VERIFICATION_CHANGE -> Icons.Default.Verified
                        AlertType.FILE_UPDATE -> Icons.Default.Folder
                        AlertType.PERSON_UPDATE -> Icons.Default.Person
                        AlertType.RECIRCULATED_NEWS -> Icons.Default.ContentCopy
                        AlertType.SOURCE_UPDATE -> Icons.Default.RssFeed
                    },
                    contentDescription = null,
                    tint = severityColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Body
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = severityColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isAr) alert.type.displayNameAr() else alert.type.displayNameEn(),
                            color = severityColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault()).format(Date(alert.createdAt)),
                        color = IntelSlate,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = alert.titleAr,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = IntelWhite
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = alert.messageAr,
                    style = MaterialTheme.typography.bodySmall,
                    color = IntelSilver,
                    lineHeight = 16.sp
                )

                if (alert.eventId != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isAr) "عرض إضبارة الحدث المرتبط ←" else "View Linked Event Dossier →",
                        color = IntelCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToEvent(alert.eventId) }
                    )
                }
            }

            // Dismiss
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Delete", tint = IntelSlate, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun AlertRuleCard(
    rule: AlertRule,
    isAr: Boolean,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DeskDarkCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeskDarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = IntelWhite
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (rule.alertType != null) {
                        Surface(color = Color(0xFF15263F), shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = if (isAr) rule.alertType.displayNameAr() else rule.alertType.displayNameEn(),
                                color = IntelCyan,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Surface(color = Color(0xFF15263F), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            text = "حد أدنى: ${if (isAr) rule.minSeverity.displayNameAr() else rule.minSeverity.name}",
                            color = AmberAlert,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (!rule.personName.isNullOrBlank()) {
                        Surface(color = Color(0xFF15263F), shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = "شخصية: ${rule.personName}",
                                color = IntelWhite,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = rule.enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = IntelCyan,
                        checkedTrackColor = Color(0xFF0C2744),
                        uncheckedThumbColor = IntelSlate,
                        uncheckedTrackColor = DeskDarkSurface
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedHighRisk)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlertRuleDialog(
    viewModel: DeskViewModel,
    uiState: DeskUiState,
    onDismiss: () -> Unit
) {
    val isAr = uiState.isArabic
    var ruleName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<AlertType?>(AlertType.IMPORTANT_DEVELOPMENT) }
    var selectedSeverity by remember { mutableStateOf(AlertSeverity.HIGH) }
    var personName by remember { mutableStateOf("") }
    var selectedFileId by remember { mutableStateOf<Long?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DeskDarkBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isAr) "إضافة قاعدة تنبيه ذكية جديدة" else "New Smart Alert Rule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = IntelWhite
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = ruleName,
                    onValueChange = { ruleName = it },
                    label = { Text(if (isAr) "اسم القاعدة" else "Rule Name") },
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
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text(if (isAr) "اسم الشخصية المستهدفة (اختياري)" else "Person Name (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IntelCyan,
                        unfocusedBorderColor = DeskDarkBorder,
                        focusedTextColor = IntelWhite,
                        unfocusedTextColor = IntelWhite
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = if (isAr) "نوع التنبيه:" else "Alert Type:", color = IntelCyan, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AlertType.values()) { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = { selectedType = t },
                            label = { Text(if (isAr) t.displayNameAr() else t.displayNameEn(), fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IntelCyan,
                                selectedLabelColor = Color(0xFF031424),
                                containerColor = DeskDarkCard,
                                labelColor = IntelSlate
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(text = if (isAr) "إلغاء" else "Cancel", color = IntelSlate)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (ruleName.isNotBlank()) {
                                val matchedFile = uiState.files.find { it.id == selectedFileId }
                                viewModel.insertAlertRule(
                                    name = ruleName,
                                    alertType = selectedType,
                                    topicId = null,
                                    topicName = null,
                                    personName = personName.ifBlank { null },
                                    organizationName = null,
                                    politicalFileId = selectedFileId,
                                    politicalFileTitle = matchedFile?.titleAr,
                                    countryCode = null,
                                    minSeverity = selectedSeverity
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IntelCyan, contentColor = Color(0xFF031424))
                    ) {
                        Text(text = if (isAr) "حفظ القاعدة" else "Save Rule")
                    }
                }
            }
        }
    }
}
