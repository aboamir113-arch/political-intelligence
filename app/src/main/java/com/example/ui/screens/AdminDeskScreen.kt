package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.ui.components.RoleChip
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeskUiState
import com.example.ui.viewmodel.DeskViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminDeskScreen(
    viewModel: DeskViewModel,
    uiState: DeskUiState,
    modifier: Modifier = Modifier
) {
    val isAr = uiState.isArabic
    val activeUser = uiState.currentUser
    val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    var showAddAnalystDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<User?>(null) }

    // Dialog: Add New Analyst
    if (showAddAnalystDialog) {
        var nameAr by remember { mutableStateOf("") }
        var nameEn by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var selectedRole by remember { mutableStateOf(UserRole.ANALYST) }

        AlertDialog(
            onDismissRequest = { showAddAnalystDialog = false },
            containerColor = DeskDarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = IntelCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAr) "إضافة محلل استخباري جديد" else "Add New Desk Analyst",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = nameAr,
                        onValueChange = { nameAr = it },
                        label = { Text(if (isAr) "الاسم الكامل (بالعربية) *" else "Full Name (Arabic) *") },
                        placeholder = { Text(if (isAr) "مثال: د. مازن نجار" else "e.g. د. مازن نجار") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_analyst_name_ar_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IntelCyan,
                            unfocusedBorderColor = DeskDarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text(if (isAr) "الاسم بالإنجليزية (اختياري)" else "Full Name (English)") },
                        placeholder = { Text("e.g. Dr. Mazen Najjar") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_analyst_name_en_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IntelCyan,
                            unfocusedBorderColor = DeskDarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(if (isAr) "البريد الإلكتروني المهني *" else "Email Address *") },
                        placeholder = { Text("analyst@desk.intel") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_analyst_email_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IntelCyan,
                            unfocusedBorderColor = DeskDarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Text(
                        text = if (isAr) "الصلاحية الاستخباراتية:" else "Analyst Role:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IntelCyan
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        UserRole.values().forEach { role ->
                            val isSelected = selectedRole == role
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedRole = role },
                                label = {
                                    Text(
                                        text = if (isAr) role.displayNameAr() else role.displayNameEn(),
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IntelBlue,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF1E293B),
                                    labelColor = IntelSlate
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) IntelCyan else DeskDarkBorder,
                                    enabled = true,
                                    selected = isSelected
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameAr.isNotBlank() || nameEn.isNotBlank()) {
                            viewModel.addUser(
                                fullNameAr = nameAr.trim(),
                                fullNameEn = nameEn.trim(),
                                email = email.trim(),
                                role = selectedRole
                            )
                            showAddAnalystDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IntelBlue),
                    enabled = nameAr.isNotBlank() || nameEn.isNotBlank(),
                    modifier = Modifier.testTag("submit_add_analyst_button")
                ) {
                    Text(if (isAr) "إضافة وتثبيت" else "Add Analyst", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAnalystDialog = false }) {
                    Text(if (isAr) "إلغاء" else "Cancel", color = IntelSlate)
                }
            }
        )
    }

    // Dialog: Confirm Delete Analyst
    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            containerColor = DeskDarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = IntelCrimson,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAr) "تأكيد إزالة المحلل" else "Confirm Remove Analyst",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = if (isAr)
                        "هل أنت متأكد من حذف المحلل \"${user.fullNameAr}\" (${user.username}) من قاعدة بيانات المكتب الاستخباري؟"
                    else
                        "Are you sure you want to remove analyst \"${user.fullNameEn}\" (${user.username}) from the intelligence desk database?",
                    color = Color(0xFFE2E8F0),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteUser(user)
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IntelCrimson),
                    modifier = Modifier.testTag("confirm_delete_analyst_button")
                ) {
                    Text(if (isAr) "حذف نهائي" else "Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text(if (isAr) "إلغاء" else "Cancel", color = IntelSlate)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = if (isAr) "إدارة النظام والمحللين وصلاحيات الوصول" else "Admin Desk & Access Control",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (isAr)
                        "التحكم بالمستخدمين وصلاحيات الأدوار وسجلات التدقيق ونشاطات الرصد."
                    else
                        "Manage analyst profiles, role-based governance, and immutable audit logs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = IntelSlate
                )
            }
        }

        // Active Analyst Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DeskDarkBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(IntelBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activeUser?.avatarInitials ?: "BL",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = activeUser?.let { if (isAr) it.fullNameAr else it.fullNameEn } ?: (if (isAr) "د. بلال اللقيس" else "Dr. Bilal Al-Laqqis"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = activeUser?.email ?: "b.laqqis@desk.intel",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = IntelSlate
                                )
                            }
                        }

                        activeUser?.let {
                            RoleChip(role = it.role, isArabic = isAr)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (isAr) "تبديل الصلاحية للمحلل الحالي:" else "Switch Active Role:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IntelCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserRole.values().forEach { role ->
                            val isSelected = activeUser?.role == role
                            Button(
                                onClick = { viewModel.switchRole(role) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f).height(36.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) IntelBlue else Color(0xFF1E293B),
                                    contentColor = if (isSelected) Color.White else IntelSlate
                                )
                            ) {
                                Text(
                                    text = if (isAr) role.displayNameAr() else role.displayNameEn(),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Role Governance Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DeskDarkBorder))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isAr) "هيكل الصلاحيات والحوكمة الاستخباراتية" else "Role Permissions Breakdown",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isAr)
                            "• محلل (ANALYST): الإطلاع على الأخبار والأحداث، تدوين الملاحظات الشخصية، وصياغة المسودات.\n" +
                            "• محلل أول (SENIOR_ANALYST): جميع صلاحيات المحلل + اعتماد وتعديل الملفات السياسية، نشر التقييمات، ومقارنة الروايات.\n" +
                            "• مدير النظام (ADMIN): إضافة وإدارة المصادر، فحص سلامة التغذيات، وتكوين فترات الرصد ومراجعة سجلات التدقيق."
                        else
                            "• ANALYST: View feeds, personal notes, draft briefs.\n" +
                            "• SENIOR_ANALYST: Full analyst permissions + approve dossiers, publish assessments, narrative checks.\n" +
                            "• ADMIN: Manage sources, probe connectivity, intervals, and audit inspection.",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Available Users Header + Add Analyst Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isAr) "قائمة محللي المكتب المسجلين" else "Registered Desk Analysts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isAr) "${uiState.users.size} محلل مسجل بالنظام" else "${uiState.users.size} registered analysts",
                        fontSize = 11.sp,
                        color = IntelSlate
                    )
                }

                Button(
                    onClick = { showAddAnalystDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IntelBlue),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_analyst_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAr) "إضافة محلل" else "Add Analyst",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        items(uiState.users, key = { it.id }) { user ->
            val isCurrent = user.id == activeUser?.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.switchUser(user) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) Color(0xFF0C2744) else DeskDarkSurface
                ),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (isCurrent) IntelBlue else DeskDarkBorder)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = user.avatarInitials, color = IntelCyan, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isAr) user.fullNameAr else user.fullNameEn,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${user.username} • ${user.email}",
                                color = IntelSlate,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RoleChip(role = user.role, isArabic = isAr)
                        if (isCurrent) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = if (isAr) "المحلل النشط" else "Active Analyst",
                                tint = IntelEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { userToDelete = user },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("delete_analyst_button_${user.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = if (isAr) "إزالة المحلل" else "Remove Analyst",
                                tint = if (isCurrent) IntelSlate else IntelCrimson,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Audit Trail Logs
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAr) "سجل العمليات والتدقيق (Audit Trail)" else "System & Audit Activity Logs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${uiState.activityLogs.size} ${if (isAr) "حدث" else "events"}",
                    fontSize = 11.sp,
                    color = IntelSlate
                )
            }
        }

        items(uiState.activityLogs.take(15), key = { it.id }) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DeskDarkBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(IntelCyan)
                            .padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = log.actionType,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = IntelCyan
                            )
                            Text(
                                text = timeFormat.format(Date(log.timestamp)),
                                fontSize = 10.sp,
                                color = IntelSlate
                            )
                        }
                        Text(
                            text = log.entityTitle,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                        Text(
                            text = log.details,
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }
    }
}
