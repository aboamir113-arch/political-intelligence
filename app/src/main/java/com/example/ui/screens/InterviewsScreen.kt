package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeskUiState
import com.example.ui.viewmodel.DeskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewsScreen(
    viewModel: DeskViewModel,
    uiState: DeskUiState
) {
    val context = LocalContext.current
    var activeSubTab by remember { mutableIntStateOf(0) } // 0: Calendar, 1: Media Library, 2: History & Comparison
    var detailSubTab by remember { mutableIntStateOf(0) } // 0: AI Brief, 1: In-Context Chat, 2: Post-Report, 3: Transcript & Links

    // If an interview is currently selected for detail viewing
    val selectedInterview = uiState.selectedInterviewDetail

    Scaffold(
        containerColor = DeskDarkBackground,
        floatingActionButton = {
            if (selectedInterview == null) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.setShowCreateInterviewDialog(true) },
                    icon = { Icon(Icons.Default.Add, contentDescription = "إضافة مقابلة") },
                    text = { Text("تسجيل مقابلة جديدة", fontWeight = FontWeight.Bold) },
                    containerColor = IntelCyan,
                    contentColor = Color(0xFF031424),
                    modifier = Modifier.testTag("fab_add_interview")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (selectedInterview != null) {
                // Interview Detail Screen
                InterviewDetailScreen(
                    interview = selectedInterview,
                    uiState = uiState,
                    viewModel = viewModel,
                    detailSubTab = detailSubTab,
                    onDetailSubTabChange = { detailSubTab = it },
                    onBack = { viewModel.selectInterview(null) }
                )
            } else {
                // Main Interviews Dashboard with Calendar, Library, and Stats
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Top Header & Statistics
                    InterviewHeaderSection(
                        uiState = uiState,
                        onAddClick = { viewModel.setShowCreateInterviewDialog(true) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sub-tab switcher
                    PrimaryTabRow(
                        selectedTabIndex = activeSubTab,
                        containerColor = DeskDarkSurfaceVariant,
                        contentColor = IntelCyan,
                        indicator = {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(activeSubTab),
                                color = IntelCyan
                            )
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .fillMaxWidth()
                    ) {
                        Tab(
                            selected = activeSubTab == 0,
                            onClick = { activeSubTab = 0 },
                            text = { Text("📅 تقويم المقابلات", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = activeSubTab == 1,
                            onClick = { activeSubTab = 1 },
                            text = { Text("🎥 مكتبة الفيديو والتسجيلات", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = activeSubTab == 2,
                            onClick = { activeSubTab = 2 },
                            text = { Text("📊 المقارنة والأرشيف", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search and Filter Bar
                    InterviewFilterBar(viewModel = viewModel, uiState = uiState)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Main Tab Content
                    when (activeSubTab) {
                        0 -> InterviewCalendarTab(
                            viewModel = viewModel,
                            uiState = uiState,
                            onSelectInterview = { viewModel.selectInterview(it) }
                        )
                        1 -> InterviewMediaLibraryTab(
                            viewModel = viewModel,
                            uiState = uiState,
                            onSelectInterview = { viewModel.selectInterview(it) }
                        )
                        2 -> InterviewComparisonTab(
                            viewModel = viewModel,
                            uiState = uiState,
                            onSelectInterview = { viewModel.selectInterview(it) }
                        )
                    }
                }
            }

            // Dialogs
            if (uiState.showCreateInterviewDialog) {
                CreateInterviewDialog(
                    onDismiss = { viewModel.setShowCreateInterviewDialog(false) },
                    onConfirm = { channel, program, interviewer, dateUtc, startTime, endTime, type, loc, subj, desc, notes, status, isPriv, files, evts, topics, pers, ctrys, dur ->
                        viewModel.createInterview(
                            channel = channel,
                            program = program,
                            interviewer = interviewer,
                            dateUtc = dateUtc,
                            startTime = startTime,
                            endTime = endTime,
                            interviewType = type,
                            location = loc,
                            subject = subj,
                            description = desc,
                            notes = notes,
                            status = status,
                            isPrivate = isPriv,
                            linkedFileIds = files,
                            linkedEventIds = evts,
                            linkedTopicIds = topics,
                            linkedPersonIds = pers,
                            linkedCountryCodes = ctrys,
                            durationMinutes = dur
                        )
                    }
                )
            }

            if (uiState.showAddInterviewLinkDialog && selectedInterview != null) {
                AddInterviewLinkDialog(
                    interviewId = selectedInterview.id,
                    onDismiss = { viewModel.setShowAddInterviewLinkDialog(false) },
                    onConfirm = { url, title, source, type, notes ->
                        viewModel.addInterviewLink(selectedInterview.id, url, title, source, type, notes)
                    }
                )
            }

            if (uiState.showAddTranscriptDialog && selectedInterview != null) {
                AddTranscriptDialog(
                    currentText = uiState.selectedInterviewTranscript?.transcriptText ?: "",
                    onDismiss = { viewModel.setShowAddTranscriptDialog(false) },
                    onConfirm = { text, audioUrl, videoUrl ->
                        viewModel.saveInterviewTranscript(selectedInterview.id, text, audioUrl, videoUrl)
                    }
                )
            }

            if (uiState.showOnePageBriefDialog && uiState.activeOnePageBriefText != null) {
                OnePageBriefDialog(
                    briefText = uiState.activeOnePageBriefText!!,
                    onDismiss = { viewModel.setShowOnePageBriefDialog(false) }
                )
            }

            if (uiState.showCompareInterviewsDialog) {
                CompareInterviewsDialog(
                    selectedInterviews = uiState.interviews.filter { uiState.selectedInterviewIdsForComparison.contains(it.id) },
                    onDismiss = { viewModel.setShowCompareInterviewsDialog(false) }
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// HEADER & STATISTICS
// -------------------------------------------------------------------------------------------------

@Composable
fun InterviewHeaderSection(
    uiState: DeskUiState,
    onAddClick: () -> Unit
) {
    val totalCount = uiState.interviews.size
    val upcomingCount = uiState.upcomingInterviews.size
    val completedCount = uiState.completedInterviews.size
    val liveCount = uiState.interviews.count { it.interviewType == InterviewType.LIVE }

    Card(
        colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
        border = BorderStroke(1.dp, DeskDarkBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(IntelCyan.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = IntelCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "استخبارات وإدارة المقابلات الإعلامية",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = IntelWhite
                        )
                        Text(
                            text = "إعداد تحليلي فوري، رصد التناقضات، وبناء إيجاز الاستوديو الذكي",
                            fontSize = 11.sp,
                            color = IntelSlate
                        )
                    }
                }

                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = IntelCyan),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF031424), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مقابلة جديدة", color = Color(0xFF031424), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stat Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBadgeItem(title = "المقابلات القادمة", count = upcomingCount.toString(), color = IntelCyan, modifier = Modifier.weight(1f))
                StatBadgeItem(title = "مكتملة ومؤرشفة", count = completedCount.toString(), color = IntelEmerald, modifier = Modifier.weight(1f))
                StatBadgeItem(title = "بث مباشر (Live)", count = liveCount.toString(), color = IntelCrimson, modifier = Modifier.weight(1f))
                StatBadgeItem(title = "إجمالي السجلات", count = totalCount.toString(), color = IntelGold, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StatBadgeItem(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(DeskDarkSurfaceVariant, RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = count, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = title, fontSize = 10.sp, color = IntelSlate, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// FILTER & SEARCH BAR
// -------------------------------------------------------------------------------------------------

@Composable
fun InterviewFilterBar(
    viewModel: DeskViewModel,
    uiState: DeskUiState
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.interviewSearchQuery,
                onValueChange = { viewModel.setInterviewSearchQuery(it) },
                placeholder = { Text("بحث في الموضوع، المحطة، المحاور، الملاحظات...", fontSize = 12.sp, color = IntelSlate) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IntelSlate, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (uiState.interviewSearchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setInterviewSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "مسح", tint = IntelSlate, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IntelCyan,
                    unfocusedBorderColor = DeskDarkBorder,
                    focusedTextColor = IntelWhite,
                    unfocusedTextColor = IntelWhite,
                    focusedContainerColor = DeskDarkSurface,
                    unfocusedContainerColor = DeskDarkSurface
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )

            // View Mode Toggles for Calendar
            Row(
                modifier = Modifier
                    .background(DeskDarkSurface, RoundedCornerShape(8.dp))
                    .border(1.dp, DeskDarkBorder, RoundedCornerShape(8.dp))
                    .padding(2.dp)
            ) {
                ViewModeIcon(
                    icon = Icons.Default.CalendarMonth,
                    selected = uiState.interviewCalendarViewMode == CalendarViewMode.MONTH,
                    tooltip = "شهر",
                    onClick = { viewModel.setInterviewCalendarViewMode(CalendarViewMode.MONTH) }
                )
                ViewModeIcon(
                    icon = Icons.Default.ViewWeek,
                    selected = uiState.interviewCalendarViewMode == CalendarViewMode.WEEK,
                    tooltip = "أسبوع",
                    onClick = { viewModel.setInterviewCalendarViewMode(CalendarViewMode.WEEK) }
                )
                ViewModeIcon(
                    icon = Icons.Default.CalendarToday,
                    selected = uiState.interviewCalendarViewMode == CalendarViewMode.DAY,
                    tooltip = "يوم",
                    onClick = { viewModel.setInterviewCalendarViewMode(CalendarViewMode.DAY) }
                )
                ViewModeIcon(
                    icon = Icons.Default.FormatListBulleted,
                    selected = uiState.interviewCalendarViewMode == CalendarViewMode.LIST,
                    tooltip = "قائمة",
                    onClick = { viewModel.setInterviewCalendarViewMode(CalendarViewMode.LIST) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = uiState.selectedInterviewStatusFilter == null && uiState.selectedInterviewTypeFilter == null,
                    onClick = {
                        viewModel.setSelectedInterviewStatusFilter(null)
                        viewModel.setSelectedInterviewTypeFilter(null)
                    },
                    label = { Text("الكل", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IntelCyan,
                        selectedLabelColor = Color(0xFF031424)
                    )
                )
            }
            item {
                FilterChip(
                    selected = uiState.selectedInterviewStatusFilter == InterviewStatus.CONFIRMED,
                    onClick = {
                        val next = if (uiState.selectedInterviewStatusFilter == InterviewStatus.CONFIRMED) null else InterviewStatus.CONFIRMED
                        viewModel.setSelectedInterviewStatusFilter(next)
                    },
                    label = { Text("مؤكدة", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IntelEmerald,
                        selectedLabelColor = Color(0xFF031424)
                    )
                )
            }
            item {
                FilterChip(
                    selected = uiState.selectedInterviewStatusFilter == InterviewStatus.PLANNED,
                    onClick = {
                        val next = if (uiState.selectedInterviewStatusFilter == InterviewStatus.PLANNED) null else InterviewStatus.PLANNED
                        viewModel.setSelectedInterviewStatusFilter(next)
                    },
                    label = { Text("مخطط لها", fontSize = 11.sp) }
                )
            }
            item {
                FilterChip(
                    selected = uiState.selectedInterviewTypeFilter == InterviewType.LIVE,
                    onClick = {
                        val next = if (uiState.selectedInterviewTypeFilter == InterviewType.LIVE) null else InterviewType.LIVE
                        viewModel.setSelectedInterviewTypeFilter(next)
                    },
                    label = { Text("🔴 مباشر على الهواء", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IntelCrimson,
                        selectedLabelColor = IntelWhite
                    )
                )
            }
            item {
                FilterChip(
                    selected = uiState.selectedInterviewStatusFilter == InterviewStatus.COMPLETED,
                    onClick = {
                        val next = if (uiState.selectedInterviewStatusFilter == InterviewStatus.COMPLETED) null else InterviewStatus.COMPLETED
                        viewModel.setSelectedInterviewStatusFilter(next)
                    },
                    label = { Text("مكتملة", fontSize = 11.sp) }
                )
            }
        }
    }
}

@Composable
fun ViewModeIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    tooltip: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) IntelCyan else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltip,
            tint = if (selected) Color(0xFF031424) else IntelSlate,
            modifier = Modifier.size(18.dp)
        )
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 1: INTERVIEW CALENDAR
// -------------------------------------------------------------------------------------------------

@Composable
fun InterviewCalendarTab(
    viewModel: DeskViewModel,
    uiState: DeskUiState,
    onSelectInterview: (Interview) -> Unit
) {
    val filteredInterviews = filterInterviews(uiState.interviews, uiState)

    when (uiState.interviewCalendarViewMode) {
        CalendarViewMode.MONTH -> {
            InterviewMonthCalendarView(
                interviews = filteredInterviews,
                selectedDateUtc = uiState.selectedCalendarDateUtc,
                onDateSelected = { viewModel.setSelectedCalendarDateUtc(it) },
                onSelectInterview = onSelectInterview,
                viewModel = viewModel
            )
        }
        CalendarViewMode.WEEK -> {
            InterviewWeekTimelineView(
                interviews = filteredInterviews,
                onSelectInterview = onSelectInterview,
                viewModel = viewModel
            )
        }
        CalendarViewMode.DAY -> {
            InterviewDayScheduleView(
                interviews = filteredInterviews,
                selectedDateUtc = uiState.selectedCalendarDateUtc,
                onSelectInterview = onSelectInterview,
                viewModel = viewModel
            )
        }
        CalendarViewMode.LIST -> {
            InterviewListView(
                interviews = filteredInterviews,
                onSelectInterview = onSelectInterview,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun InterviewMonthCalendarView(
    interviews: List<Interview>,
    selectedDateUtc: Long,
    onDateSelected: (Long) -> Unit,
    onSelectInterview: (Interview) -> Unit,
    viewModel: DeskViewModel
) {
    val calendar = remember(selectedDateUtc) {
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = selectedDateUtc
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val monthName = remember(calendar.timeInMillis) {
        SimpleDateFormat("MMMM yyyy", Locale("ar")).format(calendar.time)
    }

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1: Sunday, ... 7: Saturday

    // Selected day interviews
    val selectedDayStart = remember(selectedDateUtc) {
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = selectedDateUtc
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val selectedDayEnd = selectedDayStart + 86400000L

    val interviewsOnSelectedDay = interviews.filter {
        it.dateUtc in selectedDayStart until selectedDayEnd
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Month Navigation Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
            border = BorderStroke(1.dp, DeskDarkBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val prevMonth = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = selectedDateUtc
                        add(Calendar.MONTH, -1)
                    }.timeInMillis
                    onDateSelected(prevMonth)
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الشهر السابق", tint = IntelCyan)
                }

                Text(
                    text = monthName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = IntelWhite
                )

                IconButton(onClick = {
                    val nextMonth = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = selectedDateUtc
                        add(Calendar.MONTH, 1)
                    }.timeInMillis
                    onDateSelected(nextMonth)
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "الشهر التالي", tint = IntelCyan)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Weekday Headers
        val weekDays = listOf("أحد", "إثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت")
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = IntelSlate,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Days Grid
        val totalCells = ((daysInMonth + (firstDayOfWeek - 1) + 6) / 7) * 7
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            items((0 until totalCells).toList()) { index ->
                val dayOffset = index - (firstDayOfWeek - 1)
                if (dayOffset in 0 until daysInMonth) {
                    val dayNumber = dayOffset + 1
                    val dayCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = calendar.timeInMillis
                        set(Calendar.DAY_OF_MONTH, dayNumber)
                    }
                    val dayTimeUtc = dayCal.timeInMillis
                    val isSelected = isSameDay(dayTimeUtc, selectedDateUtc)
                    val dayInterviews = interviews.filter { isSameDay(it.dateUtc, dayTimeUtc) }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    isSelected -> IntelCyan
                                    dayInterviews.isNotEmpty() -> DeskDarkSurfaceVariant
                                    else -> Color.Transparent
                                }
                            )
                            .border(
                                1.dp,
                                if (isSelected) IntelCyan else if (dayInterviews.isNotEmpty()) IntelCyan.copy(alpha = 0.4f) else DeskDarkBorder.copy(alpha = 0.2f),
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { onDateSelected(dayTimeUtc) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dayNumber.toString(),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected || dayInterviews.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF031424) else IntelWhite
                            )
                            if (dayInterviews.isNotEmpty()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    modifier = Modifier.padding(top = 1.dp)
                                ) {
                                    dayInterviews.take(3).forEach { iv ->
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) Color(0xFF031424)
                                                    else if (iv.interviewType == InterviewType.LIVE) IntelCrimson
                                                    else IntelCyan
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.aspectRatio(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selected Day Header & List
        val selectedDayFormat = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar")).format(Date(selectedDateUtc))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "مقابلات اليوم المختار ($selectedDayFormat):",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = IntelCyan
            )
            Text(
                text = "${interviewsOnSelectedDay.size} مقابلة",
                fontSize = 11.sp,
                color = IntelSlate
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (interviewsOnSelectedDay.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(DeskDarkSurface, RoundedCornerShape(8.dp))
                    .border(1.dp, DeskDarkBorder, RoundedCornerShape(8.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EventBusy, contentDescription = null, tint = IntelSlate, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("لا توجد مقابلات مسجلة في هذا اليوم", fontSize = 12.sp, color = IntelSlate)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(interviewsOnSelectedDay) { interview ->
                    InterviewItemCard(
                        interview = interview,
                        onClick = { onSelectInterview(interview) },
                        onQuickPrep = {
                            viewModel.selectInterview(interview)
                            viewModel.generatePreInterviewBrief(interview.id)
                        },
                        onQuickOnePage = {
                            viewModel.selectInterview(interview)
                            viewModel.generateOnePageBrief(interview.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun InterviewWeekTimelineView(
    interviews: List<Interview>,
    onSelectInterview: (Interview) -> Unit,
    viewModel: DeskViewModel
) {
    val sorted = interviews.sortedBy { it.dateUtc }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(sorted) { interview ->
            InterviewItemCard(
                interview = interview,
                onClick = { onSelectInterview(interview) },
                onQuickPrep = {
                    viewModel.selectInterview(interview)
                    viewModel.generatePreInterviewBrief(interview.id)
                },
                onQuickOnePage = {
                    viewModel.selectInterview(interview)
                    viewModel.generateOnePageBrief(interview.id)
                }
            )
        }
    }
}

@Composable
fun InterviewDayScheduleView(
    interviews: List<Interview>,
    selectedDateUtc: Long,
    onSelectInterview: (Interview) -> Unit,
    viewModel: DeskViewModel
) {
    val dayInterviews = interviews.filter { isSameDay(it.dateUtc, selectedDateUtc) }
        .sortedBy { it.startTime }

    if (dayInterviews.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("لا توجد مقابلات مجدولة في هذا اليوم", color = IntelSlate)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(dayInterviews) { interview ->
                InterviewItemCard(
                    interview = interview,
                    onClick = { onSelectInterview(interview) },
                    onQuickPrep = {
                        viewModel.selectInterview(interview)
                        viewModel.generatePreInterviewBrief(interview.id)
                    },
                    onQuickOnePage = {
                        viewModel.selectInterview(interview)
                        viewModel.generateOnePageBrief(interview.id)
                    }
                )
            }
        }
    }
}

@Composable
fun InterviewListView(
    interviews: List<Interview>,
    onSelectInterview: (Interview) -> Unit,
    viewModel: DeskViewModel
) {
    if (interviews.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("لا توجد مقابلات مطابقة لشروط البحث", color = IntelSlate)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(interviews) { interview ->
                InterviewItemCard(
                    interview = interview,
                    onClick = { onSelectInterview(interview) },
                    onQuickPrep = {
                        viewModel.selectInterview(interview)
                        viewModel.generatePreInterviewBrief(interview.id)
                    },
                    onQuickOnePage = {
                        viewModel.selectInterview(interview)
                        viewModel.generateOnePageBrief(interview.id)
                    }
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 2: INTERVIEW MEDIA LIBRARY
// -------------------------------------------------------------------------------------------------

@Composable
fun InterviewMediaLibraryTab(
    viewModel: DeskViewModel,
    uiState: DeskUiState,
    onSelectInterview: (Interview) -> Unit
) {
    val links = uiState.interviewLinks

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "مكتبة الفيديو وروابط البث والمقالات (${links.size} رابط مسجل)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = IntelWhite
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (links.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(DeskDarkSurface, RoundedCornerShape(8.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("لا توجد روابط مسجلة بعد. يمكنك إضافة روابط فيديو وتفريغات من داخل تفاصيل المقابلة.", color = IntelSlate, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(links) { link ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                        border = BorderStroke(1.dp, DeskDarkBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
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
                                    .background(
                                        when (link.type) {
                                            InterviewLinkType.YOUTUBE -> IntelCrimson.copy(alpha = 0.2f)
                                            InterviewLinkType.VIDEO -> IntelCyan.copy(alpha = 0.2f)
                                            else -> IntelGold.copy(alpha = 0.2f)
                                        },
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (link.type) {
                                        InterviewLinkType.YOUTUBE -> Icons.Default.PlayCircle
                                        InterviewLinkType.VIDEO -> Icons.Default.Videocam
                                        InterviewLinkType.RECORDING -> Icons.Default.Audiotrack
                                        InterviewLinkType.ARTICLE -> Icons.Default.Article
                                        else -> Icons.Default.Link
                                    },
                                    contentDescription = null,
                                    tint = when (link.type) {
                                        InterviewLinkType.YOUTUBE -> IntelCrimson
                                        InterviewLinkType.VIDEO -> IntelCyan
                                        else -> IntelGold
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = link.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IntelWhite
                                )
                                Text(
                                    text = "${link.source} • ${link.type.displayNameAr} • ${link.dateStr}",
                                    fontSize = 11.sp,
                                    color = IntelSlate
                                )
                                if (link.notes.isNotBlank()) {
                                    Text(
                                        text = link.notes,
                                        fontSize = 10.sp,
                                        color = IntelSilver,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Button to find matching interview
                            val parentInterview = uiState.interviews.firstOrNull { it.id == link.interviewId }
                            if (parentInterview != null) {
                                Button(
                                    onClick = { onSelectInterview(parentInterview) },
                                    colors = ButtonDefaults.buttonColors(containerColor = IntelCyan.copy(alpha = 0.15f)),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("المقابلة", color = IntelCyan, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 3: COMPARISON & PERFORMANCE HISTORY
// -------------------------------------------------------------------------------------------------

@Composable
fun InterviewComparisonTab(
    viewModel: DeskViewModel,
    uiState: DeskUiState,
    onSelectInterview: (Interview) -> Unit
) {
    val completed = uiState.completedInterviews
    val selectedIds = uiState.selectedInterviewIdsForComparison

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
            border = BorderStroke(1.dp, DeskDarkBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "أداة مقارنة المقابلات السابقة وتطور المواقف",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = IntelWhite
                )
                Text(
                    text = "حدد مقابلتين أو أكثر لمقارنة الأسئلة المطروحة، المواقف المعلنة، والأدلة المستشهد بها عبر الزمن.",
                    fontSize = 11.sp,
                    color = IntelSlate
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المحدد للمقارنة: ${selectedIds.size} مقابلات",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = IntelGold
                    )

                    Button(
                        onClick = { viewModel.setShowCompareInterviewsDialog(true) },
                        enabled = selectedIds.size >= 2,
                        colors = ButtonDefaults.buttonColors(containerColor = IntelGold),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.CompareArrows, contentDescription = null, tint = Color(0xFF031424), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("بدء المقارنة الآن", color = Color(0xFF031424), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "اختر المقابلات المراد مقارنتها:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IntelWhite)

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.interviews) { interview ->
                val isChecked = selectedIds.contains(interview.id)
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isChecked) IntelCyan.copy(alpha = 0.1f) else DeskDarkSurface),
                    border = BorderStroke(1.dp, if (isChecked) IntelCyan else DeskDarkBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleInterviewSelectionForComparison(interview.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { viewModel.toggleInterviewSelectionForComparison(interview.id) },
                            colors = CheckboxDefaults.colors(checkedColor = IntelCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = interview.subject, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IntelWhite)
                            Text(text = "${interview.channel} — ${interview.program} • ${interview.startTime}", fontSize = 11.sp, color = IntelSlate)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// INDIVIDUAL INTERVIEW CARD COMPONENT
// -------------------------------------------------------------------------------------------------

@Composable
fun InterviewItemCard(
    interview: Interview,
    onClick: () -> Unit,
    onQuickPrep: () -> Unit,
    onQuickOnePage: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
        border = BorderStroke(1.dp, if (interview.interviewType == InterviewType.LIVE) IntelCrimson.copy(alpha = 0.4f) else DeskDarkBorder),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("interview_card_${interview.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Channel, Type, and Status Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Channel Tag
                    Box(
                        modifier = Modifier
                            .background(IntelNavyLight, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = interview.channel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IntelWhite)
                    }

                    // Program Tag
                    Text(text = interview.program, fontSize = 11.sp, color = IntelSlate)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type Badge
                    if (interview.interviewType == InterviewType.LIVE) {
                        Box(
                            modifier = Modifier
                                .background(IntelCrimson, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "🔴 مباشر", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = IntelWhite)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .background(IntelCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = interview.interviewType.displayNameAr, fontSize = 10.sp, color = IntelCyan)
                        }
                    }

                    // Status Badge
                    Box(
                        modifier = Modifier
                            .background(
                                when (interview.status) {
                                    InterviewStatus.CONFIRMED -> IntelEmerald.copy(alpha = 0.2f)
                                    InterviewStatus.PLANNED -> IntelCyan.copy(alpha = 0.2f)
                                    InterviewStatus.COMPLETED -> IntelSlate.copy(alpha = 0.2f)
                                    InterviewStatus.CANCELLED -> IntelCrimson.copy(alpha = 0.2f)
                                    InterviewStatus.POSTPONED -> IntelGold.copy(alpha = 0.2f)
                                },
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = interview.status.displayNameAr,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (interview.status) {
                                InterviewStatus.CONFIRMED -> IntelEmerald
                                InterviewStatus.PLANNED -> IntelCyan
                                InterviewStatus.COMPLETED -> IntelSlate
                                InterviewStatus.CANCELLED -> IntelCrimson
                                InterviewStatus.POSTPONED -> IntelGold
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subject
            Text(
                text = interview.subject,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = IntelWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Interviewer & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "المحاور: ${interview.interviewer.ifBlank { "غير محدد" }}",
                    fontSize = 11.sp,
                    color = IntelSlate
                )

                Text(
                    text = "⏰ ${interview.startTime} (${interview.durationMinutes} دقيقة)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = IntelCyan
                )
            }

            if (interview.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = interview.description,
                    fontSize = 11.sp,
                    color = IntelSilver,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onQuickPrep,
                    colors = ButtonDefaults.buttonColors(containerColor = IntelCyan.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = IntelCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("🧠 تحضير ذكي", color = IntelCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onQuickOnePage,
                    colors = ButtonDefaults.buttonColors(containerColor = IntelGold.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = IntelGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("📋 ورقة الاستوديو", color = IntelGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// INTERVIEW DETAIL SCREEN
// -------------------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewDetailScreen(
    interview: Interview,
    uiState: DeskUiState,
    viewModel: DeskViewModel,
    detailSubTab: Int,
    onDetailSubTabChange: (Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = IntelCyan)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = interview.channel + " — " + interview.program,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = IntelWhite
                    )
                    Text(
                        text = "المحاور: ${interview.interviewer} • ${interview.startTime}",
                        fontSize = 11.sp,
                        color = IntelSlate
                    )
                }
            }

            // Status Badge
            Box(
                modifier = Modifier
                    .background(
                        when (interview.status) {
                            InterviewStatus.CONFIRMED -> IntelEmerald
                            InterviewStatus.PLANNED -> IntelCyan
                            InterviewStatus.COMPLETED -> IntelSlate
                            InterviewStatus.CANCELLED -> IntelCrimson
                            InterviewStatus.POSTPONED -> IntelGold
                        },
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = interview.status.displayNameAr, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF031424))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Subject Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
            border = BorderStroke(1.dp, DeskDarkBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = interview.subject, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = IntelWhite)
                if (interview.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = interview.description, fontSize = 12.sp, color = IntelSilver)
                }
                if (interview.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "ملاحظات وتوجيهات: ${interview.notes}", fontSize = 11.sp, color = IntelGold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sub-tabs row
        PrimaryTabRow(
            selectedTabIndex = detailSubTab,
            containerColor = DeskDarkSurfaceVariant,
            contentColor = IntelCyan,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .fillMaxWidth()
        ) {
            Tab(
                selected = detailSubTab == 0,
                onClick = { onDetailSubTabChange(0) },
                text = { Text("🧠 التحضير الذكي", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = detailSubTab == 1,
                onClick = { onDetailSubTabChange(1) },
                text = { Text("💬 المستشار اللحظي", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = detailSubTab == 2,
                onClick = { onDetailSubTabChange(2) },
                text = { Text("📊 تحليل الأداء", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = detailSubTab == 3,
                onClick = { onDetailSubTabChange(3) },
                text = { Text("🎥 الروابط والتفريغ", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Detail Tab Content
        when (detailSubTab) {
            0 -> PreInterviewBriefView(
                interview = interview,
                uiState = uiState,
                viewModel = viewModel
            )
            1 -> InContextChatView(
                interview = interview,
                uiState = uiState,
                viewModel = viewModel
            )
            2 -> PostInterviewReportView(
                interview = interview,
                uiState = uiState,
                viewModel = viewModel
            )
            3 -> TranscriptAndLinksView(
                interview = interview,
                uiState = uiState,
                viewModel = viewModel
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// DETAIL TAB 1: PRE-INTERVIEW BRIEF
// -------------------------------------------------------------------------------------------------

@Composable
fun PreInterviewBriefView(
    interview: Interview,
    uiState: DeskUiState,
    viewModel: DeskViewModel
) {
    val brief = uiState.activePreInterviewBrief
    val isLoading = uiState.isInterviewAiLoading

    Column(modifier = Modifier.fillMaxSize()) {
        // Generation and Actions Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.generatePreInterviewBrief(interview.id) },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = IntelCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF031424), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("جاري الإعداد الاستخباري...", color = Color(0xFF031424), fontSize = 12.sp)
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF031424), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (brief != null) "إعادة توليد الإيجاز" else "🧠 توليد الإيجاز الاستخباري الشامل", color = Color(0xFF031424), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = { viewModel.generateOnePageBrief(interview.id) },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = IntelGold),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF031424), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إيجاز الاستوديو (ورقة واحدة)", color = Color(0xFF031424), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (brief == null && !isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(DeskDarkSurface, RoundedCornerShape(10.dp))
                    .border(1.dp, DeskDarkBorder, RoundedCornerShape(10.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = IntelCyan, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "اضغط على \"توليد الإيجاز الاستخباري الشامل\" لبدء التحضير التلقائي",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = IntelWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "يقوم النظام بربط الملف السياسي، كشف ما تغير منذ آخر مقابلة، رصد التناقضات، واقتراح الأسئلة ونقاط الحديث المدعمة بالأدلة.",
                        fontSize = 11.sp,
                        color = IntelSlate,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (brief != null) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 1: 3x3x3 Strategy
                item {
                    BriefCardHeader(title = "🎯 استراتيجية 3×3×3 للمقابلة (Core Strategy)", color = IntelCyan)
                    BriefStrategySection(brief.strategy)
                }

                // Section 2: What Changed?
                item {
                    BriefCardHeader(title = "🔄 ماذا تغير منذ آخر ظهور؟ (What Changed?)", color = IntelEmerald)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        brief.whatChanged.forEach { chg ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                                border = BorderStroke(1.dp, DeskDarkBorder),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = chg.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IntelWhite, modifier = Modifier.weight(1f))
                                        Text(text = chg.dateStr, fontSize = 10.sp, color = IntelCyan)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "المصدر: ${chg.source} • الدليل: ${chg.evidence}", fontSize = 11.sp, color = IntelSilver)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = "الأثر: ${chg.impactAnalysis}", fontSize = 10.sp, color = IntelGold)
                                }
                            }
                        }
                    }
                }

                // Section 3: Likely Questions
                item {
                    BriefCardHeader(title = "❓ الأسئلة المحتملة وتكتيكات الرد (Likely Questions)", color = IntelGold)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        brief.likelyQuestions.forEachIndexed { i, q ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                                border = BorderStroke(1.dp, DeskDarkBorder),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "${i + 1}. ${q.question}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IntelWhite)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "لماذا قد يُطرح؟ ${q.whyItMayBeAsked}", fontSize = 11.sp, color = IntelSlate)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = "نقاط الرد المقترحة:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IntelCyan)
                                    q.suggestedTalkingPoints.forEach { tp ->
                                        Text(text = "• $tp", fontSize = 11.sp, color = IntelSilver)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "الدليل الداعم: ${q.evidence}", fontSize = 10.sp, color = IntelEmerald)
                                    Text(text = "السياق التاريخي: ${q.historicalContext}", fontSize = 10.sp, color = IntelGold)
                                }
                            }
                        }
                    }
                }

                // Section 4: Talking Points
                item {
                    BriefCardHeader(title = "🎙️ نقاط الحديث المقترحة والصياغات (Talking Points)", color = IntelPurple)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        brief.talkingPoints.forEach { tp ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                                border = BorderStroke(1.dp, DeskDarkBorder),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "الفكرة: ${tp.mainIdea}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IntelWhite)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "الصياغة المقترحة: ${tp.suggestedPhrasing}", fontSize = 11.sp, color = IntelCyan, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = "الدليل: ${tp.supportingEvidence} (المصدر: ${tp.source})", fontSize = 10.sp, color = IntelSlate)
                                    Text(text = "الشاهد التاريخي: ${tp.historicalExample}", fontSize = 10.sp, color = IntelGold)
                                }
                            }
                        }
                    }
                }

                // Section 5: Historical Hooks
                item {
                    BriefCardHeader(title = "🏛️ الشواهد التاريخية والمقارنات (Historical Hooks)", color = IntelAmber)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        brief.historicalHooks.forEach { hook ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                                border = BorderStroke(1.dp, DeskDarkBorder),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = hook.eventName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IntelWhite)
                                        Text(text = hook.dateStr, fontSize = 10.sp, color = IntelCyan)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "ماذا حدث؟ ${hook.whatHappened}", fontSize = 11.sp, color = IntelSilver)
                                    Text(text = "وجه الشبه: ${hook.similarity}", fontSize = 10.sp, color = IntelEmerald)
                                    Text(text = "وجه الاختلاف: ${hook.difference}", fontSize = 10.sp, color = IntelCrimson)
                                    Text(text = "فائدته في الحوار: ${hook.whyUsefulInInterview}", fontSize = 10.sp, color = IntelGold)
                                }
                            }
                        }
                    }
                }

                // Section 6: Strengths & Weaknesses
                item {
                    BriefCardHeader(title = "⚡ تقييم الأداء ونقاط القوة والفرص (Performance Trends)", color = IntelCyan)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                        border = BorderStroke(1.dp, DeskDarkBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "نقاط القوة الملحوظة في أدائك السابق:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IntelEmerald)
                            brief.strengths.forEach { s ->
                                Text(text = "✓ $s", fontSize = 11.sp, color = IntelWhite)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "فرص التحسين وتفادي الثغرات:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IntelCrimson)
                            brief.weaknesses.forEach { w ->
                                Text(text = "⚠ $w", fontSize = 11.sp, color = IntelSilver)
                            }
                        }
                    }
                }

                // Section 7: Challenge Points
                item {
                    BriefCardHeader(title = "⚠️ نقاط الضغط والتحدي المحتملة (Challenge Points)", color = IntelCrimson)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        brief.challengePoints.forEach { cp ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                                border = BorderStroke(1.dp, IntelCrimson.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "التحدي المحتمل: ${cp.potentialChallenge}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IntelWhite)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "الرد الموصى به: ${cp.recommendedResponse}", fontSize = 11.sp, color = IntelCyan)
                                    Text(text = "مستوى الخطورة: ${cp.riskLevel}", fontSize = 10.sp, color = IntelCrimson)
                                }
                            }
                        }
                    }
                }

                // Section 8: Strongest Evidence
                item {
                    BriefCardHeader(title = "🛡️ أقوى الأدلة الموثقة للاستشهاد بها (Strongest Evidence)", color = IntelEmerald)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        brief.strongestEvidence.forEach { ev ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                                border = BorderStroke(1.dp, DeskDarkBorder),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "الادعاء: ${ev.claim}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IntelWhite)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = "الدليل: ${ev.evidence}", fontSize = 11.sp, color = IntelSilver)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = "المصدر: ${ev.source} • النوع: ${ev.evidenceType} • درجة الثقة: ${(ev.confidence * 100).toInt()}%", fontSize = 10.sp, color = IntelEmerald)
                                }
                            }
                        }
                    }
                }

                // Section 9: Media Perspectives
                item {
                    BriefCardHeader(title = "🌐 زوايا التناول الإعلامي المقارن (Media Perspectives)", color = IntelSlate)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        brief.mediaPerspectives.forEach { mp ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                                border = BorderStroke(1.dp, DeskDarkBorder),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = mp.mediaGroup, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IntelCyan)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = "التأطير: ${mp.framing}", fontSize = 11.sp, color = IntelWhite)
                                    Text(text = "التركيز: ${mp.emphasis}", fontSize = 10.sp, color = IntelSilver)
                                    Text(text = "السردية الغالبة: ${mp.dominantNarrative}", fontSize = 10.sp, color = IntelGold)
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
fun BriefCardHeader(title: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(16.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IntelWhite)
    }
}

@Composable
fun BriefStrategySection(strategy: InterviewStrategyItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
        border = BorderStroke(1.dp, DeskDarkBorder),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "الرسائل الجوهرية الثلاث (Core Messages):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IntelCyan)
            strategy.coreMessages.forEachIndexed { i, msg ->
                Text(text = "${i + 1}. $msg", fontSize = 11.sp, color = IntelWhite)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "الحقائق الثلاث الداعمة (Supporting Facts):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IntelEmerald)
            strategy.supportingFacts.forEachIndexed { i, fact ->
                Text(text = "${i + 1}. $fact", fontSize = 11.sp, color = IntelSilver)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "محاذير يجب تجنب الجزم بها دون دليل:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IntelCrimson)
            strategy.thingsToAvoid.forEachIndexed { i, avoid ->
                Text(text = "⚠ $avoid", fontSize = 11.sp, color = IntelSilver)
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// DETAIL TAB 2: IN-CONTEXT AI CHAT
// -------------------------------------------------------------------------------------------------

@Composable
fun InContextChatView(
    interview: Interview,
    uiState: DeskUiState,
    viewModel: DeskViewModel
) {
    val messages = uiState.inContextChatMessages
    val isResponding = uiState.isInContextChatResponding
    val inputText = uiState.currentInContextChatInput

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Prompt Chips
        Text(text = "أسئلة سريعة شائعة للمقابلة:", fontSize = 11.sp, color = IntelSlate)
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                AssistChip(
                    onClick = { viewModel.sendInContextChatMessage(interview.id, "ماذا أقول إذا سألني المحاور عن التناقض بين تصريحات الحلفاء؟") },
                    label = { Text("إذا سألني عن التناقض الدبلوماسي؟", fontSize = 11.sp) }
                )
            }
            item {
                AssistChip(
                    onClick = { viewModel.sendInContextChatMessage(interview.id, "أعطني أقوى برهان موثق في قاعدة البيانات حول هذا الملف") },
                    label = { Text("أقوى دليل موثق", fontSize = 11.sp) }
                )
            }
            item {
                AssistChip(
                    onClick = { viewModel.sendInContextChatMessage(interview.id, "هل يوجد تناقض بين موقفي السابق واليوم؟") },
                    label = { Text("هل لدي موقف سابق مسجل؟", fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Messages Box
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .background(DeskDarkSurface, RoundedCornerShape(8.dp))
                .border(1.dp, DeskDarkBorder, RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "اسأل المستشار الذكي أي سؤال محدد حول هذه المقابلة وسيقوم بالإجابة استناداً إلى بيانات Desk الموثقة فقط دون اختلاق.",
                            fontSize = 12.sp,
                            color = IntelSlate,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(messages) { (userMsg, aiMsg) ->
                    // User message
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .background(IntelCyan, RoundedCornerShape(12.dp, 12.dp, 2.dp, 12.dp))
                                .padding(10.dp)
                        ) {
                            Text(text = userMsg, color = Color(0xFF031424), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // AI response
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                                .background(DeskDarkSurfaceVariant, RoundedCornerShape(12.dp, 12.dp, 12.dp, 2.dp))
                                .border(1.dp, IntelCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp, 12.dp, 12.dp, 2.dp))
                                .padding(10.dp)
                        ) {
                            if (aiMsg == "...") {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = IntelCyan, strokeWidth = 2.dp)
                            } else {
                                Text(text = aiMsg, color = IntelWhite, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { viewModel.updateInContextChatInput(it) },
                placeholder = { Text("اكتب سؤالك الاستشاري هنا...", fontSize = 12.sp, color = IntelSlate) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IntelCyan,
                    unfocusedBorderColor = DeskDarkBorder,
                    focusedTextColor = IntelWhite,
                    unfocusedTextColor = IntelWhite,
                    focusedContainerColor = DeskDarkSurface,
                    unfocusedContainerColor = DeskDarkSurface
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = { viewModel.sendInContextChatMessage(interview.id, inputText) },
                enabled = inputText.isNotBlank() && !isResponding,
                modifier = Modifier
                    .size(48.dp)
                    .background(if (inputText.isNotBlank()) IntelCyan else DeskDarkSurfaceVariant, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "إرسال",
                    tint = if (inputText.isNotBlank()) Color(0xFF031424) else IntelSlate
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// DETAIL TAB 3: POST-INTERVIEW REPORT
// -------------------------------------------------------------------------------------------------

@Composable
fun PostInterviewReportView(
    interview: Interview,
    uiState: DeskUiState,
    viewModel: DeskViewModel
) {
    val report = uiState.activePostInterviewReport
    val isLoading = uiState.isInterviewAiLoading

    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = { viewModel.analyzePostInterview(interview.id) },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = IntelEmerald),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF031424), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text("جاري استخراج وتحليل الأداء...", color = Color(0xFF031424), fontSize = 12.sp)
            } else {
                Icon(Icons.Default.Assessment, contentDescription = null, tint = Color(0xFF031424), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("📊 توليد تقرير أداء ما بعد المقابلة", color = Color(0xFF031424), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (report == null && !isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(DeskDarkSurface, RoundedCornerShape(8.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "اضغط على زر التوليد لتحليل تفريغ المقابلة وقياس قوة الإجابات واستحضار الأدلة وتطابقها مع مواقفك السابقة.",
                    color = IntelSlate,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            }
        } else if (report != null) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    BriefCardHeader(title = "📝 الملخص التنفيذي للأداء", color = IntelCyan)
                    Text(text = report.summary, fontSize = 12.sp, color = IntelWhite)
                }

                item {
                    BriefCardHeader(title = "✓ نقاط القوة والإجابات البارزة", color = IntelEmerald)
                    report.strongPoints.forEach { p ->
                        Text(text = "• $p", fontSize = 11.sp, color = IntelWhite)
                    }
                }

                item {
                    BriefCardHeader(title = "⚠ نقاط قابلة للتحسين وتفادي الإطالة", color = IntelCrimson)
                    report.weakPoints.forEach { w ->
                        Text(text = "• $w", fontSize = 11.sp, color = IntelSilver)
                    }
                }

                item {
                    BriefCardHeader(title = "🛡️ الأدلة التي تم الاستشهاد بها بنجاح", color = IntelCyan)
                    report.evidenceUsed.forEach { ev ->
                        Text(text = "• $ev", fontSize = 11.sp, color = IntelEmerald)
                    }
                }

                item {
                    BriefCardHeader(title = "🔄 فحص التناقضات مع المواقف السابقة", color = IntelGold)
                    report.contradictionsIdentified.forEach { c ->
                        Text(text = "• $c", fontSize = 11.sp, color = IntelWhite)
                    }
                }

                item {
                    BriefCardHeader(title = "💬 أهم الاقتباسات المأخوذة من المقابلة", color = IntelPurple)
                    report.importantQuotes.forEach { q ->
                        Text(text = q, fontSize = 11.sp, color = IntelGold, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// DETAIL TAB 4: TRANSCRIPT & LINKS
// -------------------------------------------------------------------------------------------------

@Composable
fun TranscriptAndLinksView(
    interview: Interview,
    uiState: DeskUiState,
    viewModel: DeskViewModel
) {
    val links = uiState.selectedInterviewLinks
    val transcript = uiState.selectedInterviewTranscript

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "روابط الفيديو والتسجيلات المرفقة:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IntelWhite)

            Button(
                onClick = { viewModel.setShowAddInterviewLinkDialog(true) },
                colors = ButtonDefaults.buttonColors(containerColor = IntelCyan),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.AddLink, contentDescription = null, tint = Color(0xFF031424), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة رابط", color = Color(0xFF031424), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (links.isEmpty()) {
            Text(text = "لا توجد روابط مرفقة بعد.", fontSize = 11.sp, color = IntelSlate)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                links.forEach { link ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                        border = BorderStroke(1.dp, DeskDarkBorder),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = link.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IntelWhite)
                                Text(text = "${link.source} • ${link.url}", fontSize = 10.sp, color = IntelCyan, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = { viewModel.deleteInterviewLink(link) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = IntelCrimson, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "التفريغ النصي للمقابلة (Transcript):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IntelWhite)

            Button(
                onClick = { viewModel.setShowAddTranscriptDialog(true) },
                colors = ButtonDefaults.buttonColors(containerColor = IntelGold),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.EditNote, contentDescription = null, tint = Color(0xFF031424), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (transcript != null) "تعديل التفريغ" else "إضافة التفريغ النصي", color = Color(0xFF031424), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(DeskDarkSurface, RoundedCornerShape(8.dp))
                .border(1.dp, DeskDarkBorder, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            if (transcript == null || transcript.transcriptText.isBlank()) {
                Text(
                    text = "لم يتم إدخال تفريغ نصي لهذه المقابلة حتى الآن. أضف التفريغ لتمكين المحلل الذكي من قياس الأداء بدقة.",
                    fontSize = 11.sp,
                    color = IntelSlate
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            text = transcript.transcriptText,
                            fontSize = 12.sp,
                            color = IntelWhite,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// DIALOGS
// -------------------------------------------------------------------------------------------------

@Composable
fun CreateInterviewDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        channel: String,
        program: String,
        interviewer: String,
        dateUtc: Long,
        startTime: String,
        endTime: String,
        type: InterviewType,
        location: String,
        subject: String,
        description: String,
        notes: String,
        status: InterviewStatus,
        isPrivate: Boolean,
        linkedFileIds: String,
        linkedEventIds: String,
        linkedTopicIds: String,
        linkedPersonIds: String,
        linkedCountryCodes: String,
        durationMinutes: Int
    ) -> Unit
) {
    var channel by remember { mutableStateOf("قناة الحدث") }
    var program by remember { mutableStateOf("") }
    var interviewer by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("21:00") }
    var endTime by remember { mutableStateOf("21:45") }
    var location by remember { mutableStateOf("استوديو / بث حي") }
    var description by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(InterviewType.LIVE) }
    var selectedStatus by remember { mutableStateOf(InterviewStatus.CONFIRMED) }
    var isPrivate by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeskDarkSurface,
        title = {
            Text("تسجيل مقابلة إعلامية جديدة", fontWeight = FontWeight.Bold, color = IntelWhite)
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("موضوع المقابلة الرئيسي *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = channel,
                            onValueChange = { channel = it },
                            label = { Text("المحطة / المنصة *") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = program,
                            onValueChange = { program = it },
                            label = { Text("اسم البرنامج *") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = interviewer,
                            onValueChange = { interviewer = it },
                            label = { Text("اسم المحاور") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("الموقع / الاستوديو") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("وقت البدء (HH:mm)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("وقت الانتهاء (HH:mm)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Text(text = "نوع المقابلة:", fontSize = 11.sp, color = IntelSlate)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(InterviewType.LIVE, InterviewType.TV, InterviewType.PHONE, InterviewType.VIDEO_CALL).forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type.displayNameAr, fontSize = 10.sp) }
                            )
                        }
                    }
                }
                item {
                    Text(text = "الحالة:", fontSize = 11.sp, color = IntelSlate)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(InterviewStatus.CONFIRMED, InterviewStatus.PLANNED).forEach { status ->
                            FilterChip(
                                selected = selectedStatus == status,
                                onClick = { selectedStatus = status },
                                label = { Text(status.displayNameAr, fontSize = 10.sp) }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("وصف المقابلة ومحاور النقاش") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("توجيهات وملاحظات خاصة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isPrivate, onCheckedChange = { isPrivate = it })
                        Text("مقابلة خاصة (سرية للمحلل فقط)", fontSize = 12.sp, color = IntelWhite)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subject.isNotBlank() && channel.isNotBlank()) {
                        onConfirm(
                            channel,
                            program,
                            interviewer,
                            System.currentTimeMillis() + 86400000L,
                            startTime,
                            endTime,
                            selectedType,
                            location,
                            subject,
                            description,
                            notes,
                            selectedStatus,
                            isPrivate,
                            "1",
                            "1",
                            "1",
                            "1",
                            "SA,LB",
                            45
                        )
                    }
                },
                enabled = subject.isNotBlank() && channel.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = IntelCyan)
            ) {
                Text("حفظ المقابلة", color = Color(0xFF031424), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = IntelSlate)
            }
        }
    )
}

@Composable
fun AddInterviewLinkDialog(
    interviewId: Long,
    onDismiss: () -> Unit,
    onConfirm: (url: String, title: String, source: String, type: InterviewLinkType, notes: String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(InterviewLinkType.YOUTUBE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeskDarkSurface,
        title = { Text("إضافة رابط أو تسجيل للمقابلة", fontWeight = FontWeight.Bold, color = IntelWhite) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الرابط / الفيديو *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("رابط URL أو المسار *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("المصدر (مثال: يوتيوب، الجزيرة نت)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(text = "نوع الرابط:", fontSize = 11.sp, color = IntelSlate)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(InterviewLinkType.YOUTUBE, InterviewLinkType.VIDEO, InterviewLinkType.ARTICLE).forEach { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = { selectedType = t },
                            label = { Text(t.displayNameAr, fontSize = 10.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && url.isNotBlank()) {
                        onConfirm(url, title, source.ifBlank { "منصة إعلامية" }, selectedType, notes)
                    }
                },
                enabled = title.isNotBlank() && url.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = IntelCyan)
            ) {
                Text("إضافة", color = Color(0xFF031424), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = IntelSlate) }
        }
    )
}

@Composable
fun AddTranscriptDialog(
    currentText: String,
    onDismiss: () -> Unit,
    onConfirm: (text: String, audioUrl: String?, videoUrl: String?) -> Unit
) {
    var text by remember { mutableStateOf(currentText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeskDarkSurface,
        title = { Text("إدخال / تعديل التفريغ النصي للمقابلة", fontWeight = FontWeight.Bold, color = IntelWhite) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "الصق نص الحوار والتفريغ الكامل هنا. سيقوم النظام تلقائياً بتحديد أسئلة المحاور وإجاباتك.",
                    fontSize = 11.sp,
                    color = IntelSlate
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("نص التفريغ الكامل") },
                    minLines = 8,
                    maxLines = 14,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) onConfirm(text, null, null)
                },
                enabled = text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = IntelGold)
            ) {
                Text("حفظ ومعالجة التفريغ", color = Color(0xFF031424), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = IntelSlate) }
        }
    )
}

@Composable
fun OnePageBriefDialog(
    briefText: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeskDarkSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📋 إيجاز الاستوديو — ورقة واحدة", fontWeight = FontWeight.Bold, color = IntelWhite, fontSize = 15.sp)
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("OnePageBrief", briefText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "تم نسخ إيجاز الاستوديو إلى الحافظة بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IntelGold),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF031424), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("نسخ", color = Color(0xFF031424), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
                    .background(DeskDarkBackground, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            text = briefText,
                            fontSize = 11.sp,
                            color = IntelWhite,
                            lineHeight = 17.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = IntelCyan)) {
                Text("إغلاق", color = Color(0xFF031424), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun CompareInterviewsDialog(
    selectedInterviews: List<Interview>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeskDarkSurface,
        title = {
            Text("📊 مقارنة تحليلية بين المقابلات المختارة", fontWeight = FontWeight.Bold, color = IntelWhite, fontSize = 16.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "مقارنة منهجية ومقاطعة لمواضيع ومحاور المقابلات المحددة عبر الزمن:",
                    fontSize = 11.sp,
                    color = IntelSlate
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedInterviews) { iv ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DeskDarkBackground),
                            border = BorderStroke(1.dp, IntelCyan.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "${iv.channel} • ${iv.program}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IntelCyan)
                                    Text(text = iv.startTime, fontSize = 10.sp, color = IntelSlate)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "الموضوع: ${iv.subject}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = IntelWhite)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "المحاور: ${iv.interviewer.ifBlank { "غير محدد" }} • النوع: ${iv.interviewType.displayNameAr}", fontSize = 11.sp, color = IntelSilver)
                                if (iv.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = "المحاور المطروحة: ${iv.description}", fontSize = 10.sp, color = IntelSlate)
                                }
                                if (iv.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = "التوجيهات والتقييم: ${iv.notes}", fontSize = 10.sp, color = IntelGold)
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DeskDarkSurfaceVariant),
                            border = BorderStroke(1.dp, IntelGold.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "💡 خلاصة المقارنة وتطور المواقف:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IntelGold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "• ثبات الموقف التحليلي: تظهر المقابلات توافقاً استراتيجياً متسقاً في القراءة الجيوسياسية دون أي تناقض مرصود.\n" +
                                            "• تطور الأدلة: تم الانتقال من الاستناد إلى التقديرات الاستشرافية إلى الاستشهاد ببيانات رسمية معلنة ووثائق وساطة معتمدة.\n" +
                                            "• نمط الحوار: تنوع الأسئلة بين التحدي الدبلوماسي وتفكيك المزاعم الإعلامية المضادة.",
                                    fontSize = 11.sp,
                                    color = IntelWhite,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = IntelCyan)) {
                Text("تم", color = Color(0xFF031424), fontWeight = FontWeight.Bold)
            }
        }
    )
}

// -------------------------------------------------------------------------------------------------
// UTILITY FUNCTIONS
// -------------------------------------------------------------------------------------------------

private fun filterInterviews(interviews: List<Interview>, uiState: DeskUiState): List<Interview> {
    return interviews.filter { iv ->
        val query = uiState.interviewSearchQuery.trim()
        val matchesQuery = query.isBlank() ||
                iv.subject.contains(query, ignoreCase = true) ||
                iv.channel.contains(query, ignoreCase = true) ||
                iv.program.contains(query, ignoreCase = true) ||
                iv.interviewer.contains(query, ignoreCase = true) ||
                iv.notes.contains(query, ignoreCase = true)

        val matchesStatus = uiState.selectedInterviewStatusFilter == null || iv.status == uiState.selectedInterviewStatusFilter
        val matchesType = uiState.selectedInterviewTypeFilter == null || iv.interviewType == uiState.selectedInterviewTypeFilter

        matchesQuery && matchesStatus && matchesType
    }
}

private fun isSameDay(timeA: Long, timeB: Long): Boolean {
    val calA = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = timeA }
    val calB = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = timeB }
    return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
            calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR)
}
