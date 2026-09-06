package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiConversation
import com.example.data.model.AiMessage
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeskTab
import com.example.ui.viewmodel.DeskUiState
import com.example.ui.viewmodel.DeskViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    viewModel: DeskViewModel,
    uiState: DeskUiState
) {
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val isAr = uiState.isArabic

    var showNewConvDialog by remember { mutableStateOf(false) }
    var newConvTitleInput by remember { mutableStateOf("") }
    var showConversationsSheet by remember { mutableStateOf(false) }

    // Auto-scroll when new messages arrive
    LaunchedEffect(uiState.activeConversationMessages.size) {
        if (uiState.activeConversationMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.activeConversationMessages.size - 1)
        }
    }

    Scaffold(
        containerColor = DeskDarkBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeskDarkSurface)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable { showConversationsSheet = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(IntelPurple.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = IntelPurple, modifier = Modifier.size(20.dp))
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = uiState.activeConversation?.title ?: if (isAr) "محادثة استخبارية" else "Intelligence Chat",
                                    color = IntelWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = IntelSlate, modifier = Modifier.size(18.dp))
                            }
                            Text(
                                text = "${uiState.selectedAiModel} • RAG Grounded Context (${uiState.workspaceState.totalItemsCount} عناصر)",
                                color = IntelCyan,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Top Actions
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = {
                                newConvTitleInput = ""
                                showNewConvDialog = true
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.AddComment, contentDescription = "New Chat", tint = IntelCyan, modifier = Modifier.size(20.dp))
                        }

                        uiState.activeConversation?.let { conv ->
                            IconButton(
                                onClick = { viewModel.deleteConversation(conv) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Chat", tint = IntelSlate, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeskDarkSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Quick suggested follow-up chips
                val suggestions = if (isAr) listOf(
                    "هل هناك مصادر أخرى تدعم هذا؟",
                    "ما التناقض الرئيسي في هذه الرواية؟",
                    "ماذا يعني هذا التطور استراتيجياً؟",
                    "لخص هذا في 3 نقاط تنفيذية"
                ) else listOf(
                    "Any other corroborating sources?",
                    "What is the main contradiction?",
                    "Strategic implications?",
                    "Summarize in 3 bullet points"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    items(suggestions) { suggestion ->
                        Surface(
                            color = DeskDarkSurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.clickable {
                                viewModel.sendAssistantMessage(suggestion)
                            }
                        ) {
                            Text(
                                text = suggestion,
                                color = IntelSilver,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Chat Input Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.currentAssistantMessageInput,
                        onValueChange = { viewModel.setAssistantMessageInput(it) },
                        placeholder = {
                            Text(
                                text = if (isAr) "اسأل المساعد الذكي أو اطلب تحليلاً..." else "Ask the intelligence assistant...",
                                color = IntelSlate,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DeskDarkSurfaceVariant,
                            unfocusedContainerColor = DeskDarkSurfaceVariant,
                            focusedBorderColor = IntelCyan,
                            unfocusedBorderColor = DeskDarkBorder,
                            focusedTextColor = IntelWhite,
                            unfocusedTextColor = IntelWhite
                        ),
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 3
                    )

                    IconButton(
                        onClick = {
                            viewModel.sendAssistantMessage(uiState.currentAssistantMessageInput)
                        },
                        enabled = !uiState.isAssistantResponding && uiState.currentAssistantMessageInput.isNotBlank(),
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                if (uiState.currentAssistantMessageInput.isNotBlank() && !uiState.isAssistantResponding) IntelCyan
                                else DeskDarkSurfaceVariant,
                                CircleShape
                            )
                    ) {
                        if (uiState.isAssistantResponding) {
                            CircularProgressIndicator(
                                color = IntelCyan,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (uiState.currentAssistantMessageInput.isNotBlank()) Color(0xFF070B14) else IntelSlate,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.activeConversationMessages.isEmpty()) {
                // Empty state greeting
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(IntelPurple.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = IntelPurple, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = if (isAr) "المساعد الاستخباري الذكي المتصل بقاعدة البيانات" else "Grounded AI Intelligence Assistant",
                        color = IntelWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (isAr) "أنا متصل بقاعدة بيانات الرصد والأحداث والملفات السياسية. لا أقوم باختلاق أو هلوسة أي معلومات؛ كل إجابة تستند إلى أدلة ومصادر موثقة."
                        else "Connected to the live database of verified events, dossiers, and news. Strictly grounded with zero hallucinations.",
                        color = IntelSlate,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(16.dp))

                    if (uiState.workspaceState.totalItemsCount > 0) {
                        Surface(
                            color = IntelCyan.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, IntelCyan.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IntelCyan, modifier = Modifier.size(16.dp))
                                Text(
                                    text = if (isAr) "مساحة البحث مفعلة: لديك ${uiState.workspaceState.totalItemsCount} عناصر في السياق النشط"
                                    else "Research workspace active: ${uiState.workspaceState.totalItemsCount} entities in context",
                                    color = IntelCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.activeConversationMessages) { msg ->
                        ChatMessageBubble(
                            message = msg,
                            isAr = isAr,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(msg.content))
                            }
                        )
                    }

                    if (uiState.isAssistantResponding) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = IntelCyan,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = if (isAr) "جاري البحث في قاعدة البيانات وصياغة الرد المؤصل..." else "Grounded synthesis in progress...",
                                    color = IntelSlate,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // New Conversation Dialog
    // ---------------------------------------------------------------------------------------------
    if (showNewConvDialog) {
        AlertDialog(
            onDismissRequest = { showNewConvDialog = false },
            containerColor = DeskDarkSurface,
            title = { Text(if (isAr) "بدء محادثة استخبارية جديدة" else "New Intelligence Conversation", color = IntelWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newConvTitleInput,
                    onValueChange = { newConvTitleInput = it },
                    placeholder = { Text(if (isAr) "موضوع المحادثة..." else "Conversation topic...", color = IntelSlate, fontSize = 12.sp) },
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
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createAndSelectConversation(newConvTitleInput)
                        showNewConvDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IntelCyan, contentColor = Color(0xFF070B14))
                ) {
                    Text(if (isAr) "بدء المحادثة" else "Start Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewConvDialog = false }) {
                    Text(if (isAr) "إلغاء" else "Cancel", color = IntelSlate, fontSize = 11.sp)
                }
            }
        )
    }

    // ---------------------------------------------------------------------------------------------
    // Conversations List BottomSheet
    // ---------------------------------------------------------------------------------------------
    if (showConversationsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showConversationsSheet = false },
            containerColor = DeskDarkSurface,
            contentColor = IntelWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .fillMaxHeight(0.7f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAr) "سجل المحادثات الذكية (${uiState.conversations.size})" else "Conversation History",
                        color = IntelWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            showConversationsSheet = false
                            newConvTitleInput = ""
                            showNewConvDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IntelCyan, contentColor = Color(0xFF070B14)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (isAr) "جديدة" else "New", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (uiState.conversations.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(if (isAr) "لا توجد محادثات سابقة" else "No previous conversations", color = IntelSlate, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.conversations) { conv ->
                            val isSelected = uiState.activeConversation?.id == conv.id
                            Surface(
                                color = if (isSelected) IntelCyan.copy(alpha = 0.15f) else DeskDarkSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) IntelCyan else Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectConversation(conv)
                                        showConversationsSheet = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(conv.title, color = IntelWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(conv.updatedAt)),
                                            color = IntelSlate,
                                            fontSize = 9.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteConversation(conv) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = IntelCrimson.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
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
private fun ChatMessageBubble(
    message: AiMessage,
    isAr: Boolean,
    onCopy: () -> Unit
) {
    val isUser = message.sender.equals("USER", ignoreCase = true)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(IntelPurple.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SmartToy, contentDescription = null, tint = IntelPurple, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(8.dp))
        }

        Surface(
            color = if (isUser) IntelBlue.copy(alpha = 0.35f) else DeskDarkSurface,
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isUser) 12.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 12.dp
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isUser) IntelBlue.copy(alpha = 0.5f) else DeskDarkBorder
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isAr) "إجابة المساعد المؤصلة" else "Grounded Intelligence Answer",
                            color = IntelCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onCopy, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = IntelSlate, modifier = Modifier.size(12.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                Text(
                    text = message.content,
                    color = IntelWhite,
                    fontSize = 12.sp,
                    lineHeight = 19.sp
                )

                Spacer(Modifier.height(4.dp))
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                    color = IntelSlate,
                    fontSize = 9.sp,
                    modifier = Modifier.align(if (isUser) Alignment.Start else Alignment.End)
                )
            }
        }
    }
}
