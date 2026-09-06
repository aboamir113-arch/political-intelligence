package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.TopAppBarDesk
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeskTab
import com.example.ui.viewmodel.DeskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PoliticalIntelligenceDeskApp()
            }
        }
    }
}

@Composable
fun PoliticalIntelligenceDeskApp(
    viewModel: DeskViewModel = viewModel()
) {
    var showSplashScreen by rememberSaveable { mutableStateOf(true) }

    Crossfade(
        targetState = showSplashScreen,
        animationSpec = tween(durationMillis = 400),
        label = "AppSplashCrossfade"
    ) { isSplash ->
        if (isSplash) {
            WelcomeSplashScreen(
                durationMillis = 3000L,
                onTimeout = { showSplashScreen = false }
            )
        } else {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isAr = uiState.isArabic

            // Support dynamic RTL based on Arabic / English toggle
            val layoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBarDesk(
                    uiState = uiState,
                    onToggleLanguage = { viewModel.toggleLanguage() }
                )
            },
            bottomBar = {
                Surface(
                    color = DeskDarkSurface,
                    tonalElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DeskDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.navigationBarsPadding()) {
                        ScrollableTabRow(
                            selectedTabIndex = DeskTab.values().indexOf(uiState.activeTab),
                            containerColor = DeskDarkSurface,
                            contentColor = IntelCyan,
                            edgePadding = 8.dp,
                            divider = {}
                        ) {
                        DeskTab.values().forEach { tab ->
                            val selected = uiState.activeTab == tab
                            Tab(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        BadgedBox(
                                            badge = {
                                                if (tab == DeskTab.ALERTS && uiState.unreadAlertsCount > 0) {
                                                    Badge(containerColor = AmberAlert) {
                                                        Text(
                                                            text = uiState.unreadAlertsCount.toString(),
                                                            color = Color(0xFF031424),
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                } else if (tab == DeskTab.EVENTS && uiState.activeDevelopingEventsCount > 0) {
                                                    Badge(containerColor = IntelCyan) {
                                                        Text(
                                                            text = uiState.activeDevelopingEventsCount.toString(),
                                                            color = Color(0xFF031424),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                } else if (tab == DeskTab.POLITICAL_INTELLIGENCE && uiState.pendingDiscoveriesCount > 0) {
                                                    Badge(containerColor = IntelGold) {
                                                        Text(
                                                            text = uiState.pendingDiscoveriesCount.toString(),
                                                            color = Color(0xFF031424),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                } else if (tab == DeskTab.INTERVIEWS && uiState.upcomingInterviews.isNotEmpty()) {
                                                    Badge(containerColor = IntelCrimson) {
                                                        Text(
                                                            text = uiState.upcomingInterviews.size.toString(),
                                                            color = Color.White,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = when (tab) {
                                                    DeskTab.DASHBOARD -> Icons.Default.Home
                                                    DeskTab.EVENTS -> Icons.Default.EventNote
                                                    DeskTab.NEWS_FEED -> Icons.Default.Newspaper
                                                    DeskTab.POLITICAL_FILES -> Icons.Default.Folder
                                                    DeskTab.ENTITIES -> Icons.Default.People
                                                    DeskTab.RESEARCH_WORKSPACE -> Icons.Default.TravelExplore
                                                    DeskTab.AI_ASSISTANT -> Icons.Default.SmartToy
                                                    DeskTab.REPORTS -> Icons.Default.Assessment
                                                    DeskTab.ALERTS -> Icons.Default.NotificationsActive
                                                    DeskTab.SOURCES -> Icons.Default.RssFeed
                                                    DeskTab.ADMIN_DESK -> Icons.Default.Security
                                                    DeskTab.POLITICAL_INTELLIGENCE -> Icons.Default.Hub
                                                    DeskTab.INTERVIEWS -> Icons.Default.Mic
                                                },
                                                contentDescription = if (isAr) tab.titleAr() else tab.titleEn(),
                                                tint = if (selected) IntelCyan else IntelSlate,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Text(
                                            text = if (isAr) tab.titleAr() else tab.titleEn(),
                                            fontSize = 11.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selected) IntelCyan else IntelSlate
                                        )
                                    }
                                }
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
                    .background(DeskDarkBackground)
            ) {
                Crossfade(targetState = uiState.activeTab, label = "DeskScreenTransition") { tab ->
                    when (tab) {
                        DeskTab.DASHBOARD -> DashboardScreen(
                            viewModel = viewModel,
                            uiState = uiState
                        )
                        DeskTab.EVENTS -> EventsScreen(
                            viewModel = viewModel,
                            uiState = uiState
                        )
                        DeskTab.NEWS_FEED -> NewsFeedScreen(
                            viewModel = viewModel,
                            uiState = uiState
                        )
                        DeskTab.ALERTS -> AlertsScreen(
                            viewModel = viewModel,
                            uiState = uiState
                        )
                        DeskTab.POLITICAL_FILES -> PoliticalFilesScreen(
                            viewModel = viewModel,
                            uiState = uiState
                        )
                        DeskTab.ENTITIES -> EntitiesScreen(
                            viewModel = viewModel,
                            uiState = uiState
                        )
                        DeskTab.RESEARCH_WORKSPACE -> ResearchWorkspaceScreen(
                            viewModel = viewModel,
                            uiState = uiState
                        )
                        DeskTab.AI_ASSISTANT -> AiAssistantScreen(
                            viewModel = viewModel,
                            uiState = uiState
                        )
                        DeskTab.REPORTS -> ReportsScreen(
                            viewModel = viewModel,
                            uiState = uiState
                        )
                        DeskTab.SOURCES -> SourcesScreen(
                            viewModel = viewModel,
                            uiState = uiState
                        )
                        DeskTab.ADMIN_DESK -> AdminDeskScreen(
                            viewModel = viewModel,
                            uiState = uiState
                        )
                        DeskTab.POLITICAL_INTELLIGENCE -> com.example.ui.screens.PoliticalIntelligenceScreen(
                            viewModel = viewModel,
                            uiState = uiState
                        )
                        DeskTab.INTERVIEWS -> com.example.ui.screens.InterviewsScreen(
                            viewModel = viewModel,
                            uiState = uiState
                        )
                    }
                }
            }
        }
    }
}
}
}
