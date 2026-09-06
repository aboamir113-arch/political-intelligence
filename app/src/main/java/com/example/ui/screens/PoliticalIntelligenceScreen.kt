package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.intelligence.ExplainabilityEngine
import com.example.data.intelligence.TrendAnalysisEngine
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeskTab
import com.example.ui.viewmodel.DeskUiState
import com.example.ui.viewmodel.DeskViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

@Composable
fun PoliticalIntelligenceScreen(
    viewModel: DeskViewModel,
    uiState: DeskUiState,
    modifier: Modifier = Modifier
) {
    val isAr = uiState.isArabic
    val subTabs = remember(isAr) {
        listOf(
            if (isAr) "شبكة العلاقات" else "Relationship Graph",
            if (isAr) "الاتجاهات والتغيرات" else "Trends & Changes",
            if (isAr) "التحليل المقارن" else "Cross-Analysis",
            if (isAr) "المراقبة والإشارات" else "Signals & Monitoring",
            if (isAr) "موجز الاستخبارات ورصد الذكاء" else "Feed & AI Discovery"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeskDarkBackground)
    ) {
        // Sub-navigation tab bar
        ScrollableTabRow(
            selectedTabIndex = uiState.intelligenceActiveSubTab,
            containerColor = DeskDarkSurface,
            contentColor = IntelCyan,
            edgePadding = 12.dp,
            divider = {}
        ) {
            subTabs.forEachIndexed { index, title ->
                val selected = uiState.intelligenceActiveSubTab == index
                Tab(
                    selected = selected,
                    onClick = { viewModel.setIntelligenceSubTab(index) },
                    modifier = Modifier.testTag("intel_subtab_$index"),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            when (index) {
                                0 -> Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(16.dp))
                                1 -> Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                2 -> Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(16.dp))
                                3 -> Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(16.dp))
                                4 -> Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) IntelCyan else IntelSlate
                            )
                            if (index == 4 && uiState.pendingDiscoveriesCount > 0) {
                                Badge(containerColor = IntelGold) {
                                    Text(
                                        text = uiState.pendingDiscoveriesCount.toString(),
                                        color = Color(0xFF031424),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else if (index == 3 && uiState.unacknowledgedSignalsCount > 0) {
                                Badge(containerColor = AmberAlert) {
                                    Text(
                                        text = uiState.unacknowledgedSignalsCount.toString(),
                                        color = Color(0xFF031424),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        HorizontalDivider(color = DeskDarkBorder.copy(alpha = 0.6f))

        // Content switching
        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState.intelligenceActiveSubTab) {
                0 -> RelationshipGraphSubScreen(viewModel = viewModel, uiState = uiState)
                1 -> TrendsAndChangesSubScreen(viewModel = viewModel, uiState = uiState)
                2 -> CrossAnalysisSubScreen(viewModel = viewModel, uiState = uiState)
                3 -> SmartMonitoringAndSignalsSubScreen(viewModel = viewModel, uiState = uiState)
                4 -> IntelligenceFeedAndDiscoverySubScreen(viewModel = viewModel, uiState = uiState)
            }

            // Explainability Dialog Modal
            uiState.explainabilityModalReport?.let { report ->
                ExplainabilityDialog(
                    report = report,
                    isArabic = isAr,
                    onDismiss = { viewModel.dismissExplainabilityModal() }
                )
            }

            // Create Smart Rule Dialog Modal
            if (uiState.showCreateSmartRuleDialog) {
                CreateSmartRuleDialog(
                    isArabic = isAr,
                    onDismiss = { viewModel.setShowCreateSmartRuleDialog(false) },
                    onConfirm = { name, type, target, kw, src, imp ->
                        viewModel.insertSmartRule(name, type, target, kw, src, imp)
                    }
                )
            }

            // Create Information Gap Dialog Modal
            if (uiState.showCreateInfoGapDialog) {
                CreateInformationGapDialog(
                    isArabic = isAr,
                    onDismiss = { viewModel.setShowCreateInfoGapDialog(false) },
                    onConfirm = { title, desc, status, missing ->
                        viewModel.insertInformationGap(
                            titleAr = title,
                            descriptionAr = desc,
                            status = status,
                            missingData = missing
                        )
                    }
                )
            }
        }
    }
}

// ==========================================
// SUB-SCREEN 1: RELATIONSHIP GRAPH
// ==========================================

@Composable
private fun RelationshipGraphSubScreen(
    viewModel: DeskViewModel,
    uiState: DeskUiState
) {
    val isAr = uiState.isArabic

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Controls and Filters Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, DeskDarkBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAr) "شبكة العلاقات السياسية التفاعلية (${uiState.graphNodes.size} عقدة، ${uiState.graphEdges.size} رابط)"
                        else "Interactive Relationship Graph (${uiState.graphNodes.size} nodes, ${uiState.graphEdges.size} edges)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = {
                                scale = 1f
                                offset = Offset.Zero
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(if (isAr) "إعادة ضبط" else "Reset View", fontSize = 10.sp)
                        }

                        Button(
                            onClick = { viewModel.runAiRelationshipDiscovery() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp), tint = IntelCyan)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isAr) "استخراج ذكي" else "AI Mine", fontSize = 10.sp, color = IntelCyan)
                        }
                    }
                }

                // Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = uiState.graphOnlyDirect,
                            onClick = { viewModel.updateGraphFilters(onlyDirect = !uiState.graphOnlyDirect) },
                            label = { Text(if (isAr) "أدلة مباشرة فقط" else "Direct Evidence Only", fontSize = 10.sp) },
                            leadingIcon = {
                                if (uiState.graphOnlyDirect) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                            }
                        )
                    }

                    item {
                        FilterChip(
                            selected = uiState.graphSelectedRelType == null,
                            onClick = { viewModel.updateGraphFilters(relType = null) },
                            label = { Text(if (isAr) "كافة أنواع العلاقات" else "All Types", fontSize = 10.sp) }
                        )
                    }

                    items(listOf(RelationshipType.ALLIED_WITH, RelationshipType.OPPOSES, RelationshipType.NEGOTIATES_WITH, RelationshipType.INVOLVED_IN)) { rType ->
                        FilterChip(
                            selected = uiState.graphSelectedRelType == rType,
                            onClick = {
                                val next = if (uiState.graphSelectedRelType == rType) null else rType
                                viewModel.updateGraphFilters(relType = next)
                            },
                            label = { Text(if (isAr) rType.nameAr else rType.nameEn, fontSize = 10.sp) }
                        )
                    }
                }
            }
        }

        // Main Graph Canvas Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF030D18))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 3.0f)
                        offset += pan
                    }
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(uiState.graphNodes) {
                        detectTapGestures { tapOffset ->
                            // Hit test nodes
                            val canvasCenter = Offset(size.width / 2f, size.height / 2f)
                            val tappedNode = uiState.graphNodes.firstOrNull { node ->
                                val nodeScreenPos = canvasCenter + offset + Offset(node.x, node.y) * scale
                                (tapOffset - nodeScreenPos).getDistance() <= 32f * scale
                            }
                            viewModel.selectGraphNode(tappedNode)
                        }
                    }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)

                // 1. Draw Edges
                uiState.graphEdges.forEach { edge ->
                    val sourceNode = uiState.graphNodes.find { it.id == edge.sourceNodeId }
                    val targetNode = uiState.graphNodes.find { it.id == edge.targetNodeId }

                    if (sourceNode != null && targetNode != null) {
                        val p1 = center + offset + Offset(sourceNode.x, sourceNode.y) * scale
                        val p2 = center + offset + Offset(targetNode.x, targetNode.y) * scale

                        val edgeColor = when (edge.relationshipType) {
                            RelationshipType.ALLIED_WITH, RelationshipType.AGREES_WITH, RelationshipType.SUPPORTS -> Color(0xFF22C55E)
                            RelationshipType.OPPOSES, RelationshipType.CRITICIZES, RelationshipType.DISAGREES_WITH -> Color(0xFFEF4444)
                            RelationshipType.NEGOTIATES_WITH, RelationshipType.MEETS_WITH -> Color(0xFF06B6D4)
                            else -> Color(0xFF94A3B8)
                        }

                        drawLine(
                            color = edgeColor.copy(alpha = if (edge.isDirect) 0.85f else 0.45f),
                            start = p1,
                            end = p2,
                            strokeWidth = if (edge.isDirect) 2.5f * scale else 1.5f * scale,
                            pathEffect = if (!edge.isDirect) PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null
                        )
                    }
                }

                // 2. Draw Nodes
                uiState.graphNodes.forEach { node ->
                    val nodePos = center + offset + Offset(node.x, node.y) * scale
                    val isSelected = uiState.selectedGraphNode?.id == node.id

                    val nodeColor = when (node.entityType) {
                        EntityType.PERSON -> Color(0xFF38BDF8)
                        EntityType.ORGANIZATION -> Color(0xFFA78BFA)
                        EntityType.POLITICAL_FILE -> Color(0xFFFBBF24)
                        EntityType.COUNTRY -> Color(0xFF34D399)
                        EntityType.EVENT -> Color(0xFFF43F5E)
                        else -> Color(0xFF94A3B8)
                    }

                    // Node outer glow / selection ring
                    if (isSelected) {
                        drawCircle(
                            color = IntelCyan.copy(alpha = 0.4f),
                            radius = 28f * scale,
                            center = nodePos
                        )
                    }

                    // Node body
                    drawCircle(
                        color = Color(0xFF0B192C),
                        radius = 20f * scale,
                        center = nodePos
                    )
                    drawCircle(
                        color = nodeColor,
                        radius = 20f * scale,
                        center = nodePos,
                        style = Stroke(width = if (isSelected) 3.5f * scale else 2f * scale)
                    )

                    // Node Label
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = (11f * scale).coerceIn(8f, 22f)
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                        }
                        drawText(node.label.take(14), nodePos.x, nodePos.y + (30f * scale), paint)
                    }
                }
            }

            // Top Legend Overlay
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                color = DeskDarkSurface.copy(alpha = 0.85f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, DeskDarkBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = Color(0xFF22C55E), text = if (isAr) "تحالف / توافق" else "Allied")
                    LegendItem(color = Color(0xFFEF4444), text = if (isAr) "معارضة / تباعد" else "Opposed")
                    LegendItem(color = Color(0xFF06B6D4), text = if (isAr) "تفاوض / لقاء" else "Negotiating")
                }
            }

            // Node Selection Bottom Sheet / Card
            uiState.selectedGraphNode?.let { node ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, IntelCyan.copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = node.label,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${node.entityType.displayNameAr()} • ${node.roleOrCategory}",
                                    fontSize = 11.sp,
                                    color = IntelCyan
                                )
                            }

                            IconButton(
                                onClick = { viewModel.selectGraphNode(null) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = IntelSlate, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Connected Relationships for selected node
                        val connectedRels = uiState.relationships.filter {
                            (it.sourceEntityType == node.entityType && it.sourceEntityId == node.entityId) ||
                                    (it.targetEntityType == node.entityType && it.targetEntityId == node.entityId)
                        }

                        Text(
                            text = if (isAr) "العلاقات المباشرة المسجلة (${connectedRels.size}):" else "Connected Relationships (${connectedRels.size}):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE2E8F0)
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(connectedRels) { rel ->
                                val otherName = if (rel.sourceEntityId == node.entityId) rel.targetEntityName else rel.sourceEntityName
                                Surface(
                                    modifier = Modifier.clickable {
                                        viewModel.requestExplainabilityForRelationship(rel)
                                    },
                                    color = Color(0xFF132338),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, DeskDarkBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "${rel.relationshipType.displayNameAr()} ← $otherName",
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                        Icon(Icons.Default.Info, contentDescription = "Explain", tint = IntelCyan, modifier = Modifier.size(12.dp))
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
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(text = text, fontSize = 10.sp, color = IntelSlate)
    }
}

// ==========================================
// SUB-SCREEN 2: TRENDS & CHANGES
// ==========================================

@Composable
private fun TrendsAndChangesSubScreen(
    viewModel: DeskViewModel,
    uiState: DeskUiState
) {
    val isAr = uiState.isArabic

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Timeframe Selector Chips
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DeskDarkBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isAr) "النطاق الزمني لتحليل الاتجاهات والتحولات:" else "Trend Analysis Timeframe:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(TrendTimeframe.values().filter { it != TrendTimeframe.CUSTOM }) { tf ->
                            FilterChip(
                                selected = uiState.selectedTrendTimeframe == tf,
                                onClick = { viewModel.computeTrends(tf) },
                                label = { Text(if (isAr) tf.nameAr else tf.nameEn, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Coverage Assessment Card
        uiState.activeTrendReport?.let { report ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2236)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, IntelCyan.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (report.isCoverageRising) Color(0xFF064E3B) else Color(0xFF374151)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (report.isCoverageRising) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (report.isCoverageRising) Color(0xFF34D399) else Color(0xFF94A3B8),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isAr) "تقييم وتيرة النشاط والتغطية الإخبارية" else "Activity & Coverage Velocity",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = IntelCyan
                            )
                            Text(
                                text = report.activityAssessmentAr,
                                fontSize = 12.sp,
                                color = Color.White,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            // Rising Entities Grid/List
            item {
                Text(
                    text = if (isAr) "الشخصيات والملفات الأكثر صعوداً وتداولاً:" else "Rising Entities & Dossiers:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Persons
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, DeskDarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(if (isAr) "الشخصيات المتداولة" else "Top Persons", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IntelCyan)
                            report.risingPersons.take(4).forEach { p ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(p.entityName, fontSize = 11.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    Text("${p.mentionCount} ذكر", fontSize = 10.sp, color = IntelGold, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Files
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, DeskDarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(if (isAr) "الملفات الساخنة" else "Active Dossiers", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IntelGold)
                            report.activeFiles.take(4).forEach { f ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(f.entityName, fontSize = 11.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    Text("${f.mentionCount} برقية", fontSize = 10.sp, color = IntelCyan, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Detected Changes ("What Changed?")
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAr) "سجل التحولات والتغيرات المكتشفة (Change Detection):" else "Detected Significant Changes:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Button(
                    onClick = { viewModel.runChangeDetectionScan() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp), tint = IntelCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isAr) "فحص التغيرات" else "Scan Changes", fontSize = 10.sp, color = IntelCyan)
                }
            }
        }

        items(uiState.detectedChanges) { change ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, DeskDarkBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = when (change.changeType) {
                                "STANCE_SHIFT" -> Color(0xFF7C2D12)
                                "ESCALATION" -> Color(0xFF831843)
                                else -> Color(0xFF1E3A5F)
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = change.changeType,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "${(change.confidence * 100).toInt()}% ثقة",
                            fontSize = 10.sp,
                            color = IntelGold,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${change.entityName}: ${change.whatChangedAr}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = change.comparedWithWhatAr,
                        fontSize = 11.sp,
                        color = IntelSlate
                    )

                    HorizontalDivider(color = DeskDarkBorder.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المصدر: ${change.sourceName}",
                            fontSize = 10.sp,
                            color = IntelSlate
                        )

                        TextButton(
                            onClick = { viewModel.requestExplainabilityForChange(change) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(12.dp), tint = IntelCyan)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isAr) "لماذا ظهر هذا الاستنتاج؟" else "Why?", fontSize = 10.sp, color = IntelCyan)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SUB-SCREEN 3: CROSS-ANALYSIS
// ==========================================

@Composable
private fun CrossAnalysisSubScreen(
    viewModel: DeskViewModel,
    uiState: DeskUiState
) {
    val isAr = uiState.isArabic
    var selectedMode by remember { mutableIntStateOf(0) } // 0: Cross-Files, 1: Cross-Events

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mode Selector Tab
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, DeskDarkBorder)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Button(
                        onClick = { selectedMode = 0 },
                        modifier = Modifier.weight(1f).height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMode == 0) IntelCyan else Color.Transparent,
                            contentColor = if (selectedMode == 0) Color(0xFF031424) else IntelSlate
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isAr) "تحليل مقارن للملفات (Cross-File)" else "Cross-File Analysis", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { selectedMode = 1 },
                        modifier = Modifier.weight(1f).height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMode == 1) IntelCyan else Color.Transparent,
                            contentColor = if (selectedMode == 1) Color(0xFF031424) else IntelSlate
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isAr) "مقارنة الأحداث (Cross-Event)" else "Cross-Event Analysis", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (selectedMode == 0) {
            // File Multi-Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DeskDarkBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isAr) "حدد ملفين سياسيين أو أكثر للمقارنة والربط التحليلي:" else "Select 2 or more files to cross-analyze:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(uiState.files) { file ->
                                val selected = uiState.selectedCrossFileIds.contains(file.id)
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.toggleCrossFileSelection(file.id) },
                                    label = { Text(file.titleAr, fontSize = 10.sp, maxLines = 1) },
                                    leadingIcon = {
                                        if (selected) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                                    }
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.runCrossFileAnalysis() },
                            enabled = uiState.selectedCrossFileIds.size >= 2 && !uiState.isCrossFileLoading,
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IntelCyan, contentColor = Color(0xFF031424)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (uiState.isCrossFileLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFF031424))
                            } else {
                                Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isAr) "تنفيذ التحليل البيني للملفات المختارة (${uiState.selectedCrossFileIds.size})" else "Execute Cross-File Analysis", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Cross-File Output
            uiState.activeCrossFileAnalysis?.let { analysis ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1828)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, IntelCyan)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isAr) "مخرجات التحليل السياسي المقارن" else "Comparative Synthesis Output",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IntelCyan
                                )
                                Text(
                                    text = "${(analysis.confidence * 100).toInt()}% ثقة",
                                    fontSize = 11.sp,
                                    color = IntelGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Shared Entities Summary
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(color = Color(0xFF132338), shape = RoundedCornerShape(6.dp), modifier = Modifier.weight(1f)) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(if (isAr) "شخصيات مشتركة" else "Shared Persons", fontSize = 10.sp, color = IntelSlate)
                                        Text("${analysis.sharedPersons.size}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                Surface(color = Color(0xFF132338), shape = RoundedCornerShape(6.dp), modifier = Modifier.weight(1f)) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(if (isAr) "مؤسسات وهيئات" else "Shared Orgs", fontSize = 10.sp, color = IntelSlate)
                                        Text("${analysis.sharedOrganizations.size}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                Surface(color = Color(0xFF132338), shape = RoundedCornerShape(6.dp), modifier = Modifier.weight(1f)) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(if (isAr) "تناقضات بينية" else "Contradictions", fontSize = 10.sp, color = IntelSlate)
                                        Text("${analysis.crossFileContradictions.size}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RedHighRisk)
                                    }
                                }
                            }

                            HorizontalDivider(color = DeskDarkBorder.copy(alpha = 0.5f))

                            Text(
                                text = analysis.synthesisTextAr,
                                fontSize = 12.sp,
                                color = Color.White,
                                lineHeight = 19.sp
                            )

                            if (analysis.evidenceReferences.isNotEmpty()) {
                                HorizontalDivider(color = DeskDarkBorder.copy(alpha = 0.5f))
                                Text(if (isAr) "الأدلة والروابط التوثيقية:" else "Evidence & Sources:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IntelGold)
                                analysis.evidenceReferences.forEach { ref ->
                                    Text("• $ref", fontSize = 11.sp, color = IntelSlate)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Event Multi-Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DeskDarkBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isAr) "حدد حدثين سياسيين أو أكثر للمقارنة التكتيكية:" else "Select 2 or more events to cross-analyze:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(uiState.events) { ev ->
                                val selected = uiState.selectedCrossEventIds.contains(ev.id)
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.toggleCrossEventSelection(ev.id) },
                                    label = { Text(ev.titleAr, fontSize = 10.sp, maxLines = 1) },
                                    leadingIcon = {
                                        if (selected) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                                    }
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.runCrossEventAnalysis() },
                            enabled = uiState.selectedCrossEventIds.size >= 2 && !uiState.isCrossEventLoading,
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IntelCyan, contentColor = Color(0xFF031424)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (uiState.isCrossEventLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFF031424))
                            } else {
                                Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isAr) "تنفيذ مقارنة الأحداث المختارة (${uiState.selectedCrossEventIds.size})" else "Execute Cross-Event Analysis", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Cross-Event Output
            uiState.activeCrossEventAnalysis?.let { evAnalysis ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1828)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, IntelCyan)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = if (isAr) "مخرجات مقارنة الأحداث والمسارات" else "Event Comparison Synthesis",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = IntelCyan
                            )
                            Text(
                                text = evAnalysis.synthesisAr,
                                fontSize = 12.sp,
                                color = Color.White,
                                lineHeight = 18.sp
                            )

                            if (evAnalysis.keyDivergences.isNotEmpty()) {
                                HorizontalDivider(color = DeskDarkBorder.copy(alpha = 0.5f))
                                Text(if (isAr) "أبرز نقاط التباين:" else "Key Divergences:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RedHighRisk)
                                evAnalysis.keyDivergences.forEach { d ->
                                    Text("• $d", fontSize = 11.sp, color = Color.White)
                                }
                            }

                            if (evAnalysis.unverifiedPoints.isNotEmpty()) {
                                HorizontalDivider(color = DeskDarkBorder.copy(alpha = 0.5f))
                                Text(if (isAr) "النقاط غير المؤكدة:" else "Unverified Points:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberAlert)
                                evAnalysis.unverifiedPoints.forEach { u ->
                                    Text("• $u", fontSize = 11.sp, color = IntelSlate)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SUB-SCREEN 4: SMART MONITORING & SIGNALS
// ==========================================

@Composable
private fun SmartMonitoringAndSignalsSubScreen(
    viewModel: DeskViewModel,
    uiState: DeskUiState
) {
    val isAr = uiState.isArabic

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: Early Signals Header & Trigger
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isAr) "المؤشرات والإشارات المبكرة (Early Signals):" else "Early Signals Detection:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isAr) "صياغة مشروطة واحتمالية دون تنبؤ جزمي" else "Probabilistic & evidence-backed indicators",
                        fontSize = 10.sp,
                        color = IntelSlate
                    )
                }

                Button(
                    onClick = { viewModel.runEarlySignalsScan() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(12.dp), tint = IntelCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isAr) "فحص المؤشرات" else "Scan Signals", fontSize = 10.sp, color = IntelCyan)
                }
            }
        }

        items(uiState.earlySignals) { signal ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (!signal.isAcknowledged) AmberAlert.copy(alpha = 0.8f) else DeskDarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFF3B2D05),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = signal.signalCategory,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberAlert,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "${(signal.confidence * 100).toInt()}% ثقة احتمالية",
                            fontSize = 10.sp,
                            color = AmberAlert,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = signal.signalTitleAr,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = signal.signalDescriptionAr,
                        fontSize = 11.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 17.sp
                    )

                    // Historical & Alternative Explanations
                    Surface(
                        color = Color(0xFF0B192C),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "• النمط التاريخي: ${signal.historicalPattern}",
                                fontSize = 10.sp,
                                color = IntelSlate
                            )
                            Text(
                                text = "• ${signal.alternativeExplanations}",
                                fontSize = 10.sp,
                                color = Color(0xFFCBD5E1)
                            )
                            if (signal.unknowns.isNotBlank()) {
                                Text(
                                    text = "• المعلومات الناقصة: ${signal.unknowns}",
                                    fontSize = 10.sp,
                                    color = IntelGold
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = DeskDarkBorder.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.requestExplainabilityForSignal(signal) },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(12.dp), tint = IntelCyan)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isAr) "شرح الخوارزمية (Why?)" else "Explainability", fontSize = 10.sp, color = IntelCyan)
                        }

                        if (!signal.isAcknowledged) {
                            FilledTonalButton(
                                onClick = { viewModel.acknowledgeEarlySignal(signal.id) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text(if (isAr) "تأكيد الاطلاع" else "Acknowledge", fontSize = 10.sp)
                            }
                        } else {
                            Text(if (isAr) "تم الاطلاع ✓" else "Acknowledged ✓", fontSize = 10.sp, color = Color(0xFF22C55E))
                        }
                    }
                }
            }
        }

        // Section 2: Smart Monitoring Rules
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAr) "قواعد المراقبة الذكية المخصصة:" else "Smart Monitoring Rules:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Button(
                    onClick = { viewModel.setShowCreateSmartRuleDialog(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = IntelCyan, contentColor = Color(0xFF031424)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isAr) "+ قاعدة جديدة" else "+ New Rule", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(uiState.smartRules) { rule ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, DeskDarkBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(rule.nameAr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(4.dp)) {
                                Text(rule.targetType.displayNameAr(), fontSize = 9.sp, color = IntelCyan, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                        Text("الهدف: ${rule.targetName} • الكلمات: ${rule.keywordsCommaSeparated}", fontSize = 10.sp, color = IntelSlate, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Switch(
                        checked = rule.isActive,
                        onCheckedChange = { viewModel.toggleSmartRule(rule) },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        }

        // Section 3: Information Gaps
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAr) "الفجوات المعلوماتية (Information Gaps):" else "Information Gaps Intelligence:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Button(
                    onClick = { viewModel.setShowCreateInfoGapDialog(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp), tint = IntelCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isAr) "+ توثيق فجوة" else "+ Add Gap", fontSize = 10.sp, color = IntelCyan)
                }
            }
        }

        items(uiState.informationGaps) { gap ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, DeskDarkBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = when (gap.status) {
                                InformationGapStatus.UNKNOWN -> Color(0xFF7F1D1D)
                                InformationGapStatus.CONTRADICTED -> Color(0xFF78350F)
                                InformationGapStatus.PARTIALLY_KNOWN -> Color(0xFF1E3A8A)
                                else -> Color(0xFF14532D)
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = gap.status.displayNameAr(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "الأولوية: ${gap.criticality.displayNameAr()}",
                            fontSize = 10.sp,
                            color = IntelSlate
                        )
                    }

                    Text(gap.titleAr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(gap.descriptionAr, fontSize = 11.sp, color = IntelSlate)

                    if (gap.missingDataNeeded.isNotBlank()) {
                        Text(
                            text = "البيانات المطلوبة: ${gap.missingDataNeeded}",
                            fontSize = 10.sp,
                            color = IntelGold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// SUB-SCREEN 5: INTELLIGENCE FEED & AI DISCOVERY
// ==========================================

@Composable
private fun IntelligenceFeedAndDiscoverySubScreen(
    viewModel: DeskViewModel,
    uiState: DeskUiState
) {
    val isAr = uiState.isArabic
    val pendingDiscoveries = uiState.relationships.filter { it.discoveryStatus == DiscoveryStatus.DISCOVERED }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section: AI-Assisted Discovery Queue (Human-in-the-Loop)
        if (pendingDiscoveries.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C29)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, IntelGold)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = IntelGold, modifier = Modifier.size(18.dp))
                                Text(
                                    text = if (isAr) "طابور مراجعة المحلل (Human-in-the-Loop):" else "Analyst Review Queue:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IntelGold
                                )
                            }
                            Badge(containerColor = IntelGold) {
                                Text(
                                    text = "${pendingDiscoveries.size} استنتاج جديد",
                                    color = Color(0xFF031424),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = if (isAr)
                                "اكتشف الذكاء الاصطناعي علاقات محتملة قيد التدقيق، ولا تدرج في السجل الرسمي إلا باعتماد المحلل البشري."
                            else
                                "AI inferred candidate relations pending human analyst review before commitment to verified intelligence.",
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            items(pendingDiscoveries) { candidate ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, IntelGold.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${candidate.sourceEntityName} ← [${candidate.relationshipType.displayNameAr()}] → ${candidate.targetEntityName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = "${(candidate.confidence * 100).toInt()}% ثقة",
                                fontSize = 10.sp,
                                color = IntelGold,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(candidate.evidenceSnippet, fontSize = 11.sp, color = IntelSlate)

                        HorizontalDivider(color = DeskDarkBorder.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { viewModel.requestExplainabilityForRelationship(candidate) },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(12.dp), tint = IntelCyan)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isAr) "لماذا؟" else "Why?", fontSize = 10.sp, color = IntelCyan)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { viewModel.rejectRelationshipDiscovery(candidate.id, "مرفوضة من المحلل") },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedHighRisk),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text(if (isAr) "رفض" else "Reject", fontSize = 10.sp)
                                }

                                Button(
                                    onClick = { viewModel.confirmRelationshipDiscovery(candidate.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D), contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isAr) "اعتماد" else "Confirm", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Consolidated Intelligence Feed (Events + Changes + Signals)
        item {
            Text(
                text = if (isAr) "الموجز الاستخباري الموحد (Intelligence Feed):" else "Unified Intelligence Feed:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Merged Feed Items
        val allFeedArticles = uiState.articles.take(8)
        items(allFeedArticles) { art ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, DeskDarkBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (art.importanceScore >= 80) RedHighRisk.copy(alpha = 0.2f) else AmberAlert.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (art.importanceScore >= 80) "أهمية فائقة" else "أهمية عادية",
                                fontSize = 9.sp,
                                color = if (art.importanceScore >= 80) RedHighRisk else AmberAlert,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(art.sourceName, fontSize = 10.sp, color = IntelSlate)
                    }

                    Text(art.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    if (art.snippet.isNotBlank()) {
                        Text(art.snippet, fontSize = 11.sp, color = Color(0xFFE2E8F0), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

// ==========================================
// EXPLAINABILITY DIALOG ("لماذا ظهر هذا الاستنتاج؟")
// ==========================================

@Composable
fun ExplainabilityDialog(
    report: ExplainabilityEngine.ExplainabilityReport,
    isArabic: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF071426)),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, IntelCyan)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = IntelCyan, modifier = Modifier.size(20.dp))
                        Text(
                            text = if (isArabic) "لماذا ظهر هذا الاستنتاج؟" else "Why did this insight appear?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = IntelCyan
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = IntelSlate)
                    }
                }

                HorizontalDivider(color = DeskDarkBorder)

                Text(
                    text = report.insightTitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Surface(color = Color(0xFF0D2138), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "المنهجية التحليلية: ${report.methodologyAr}",
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0)
                        )
                        Text(
                            text = "الخوارزمية / الطريقة: ${report.computationalMethod}",
                            fontSize = 10.sp,
                            color = IntelSlate,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "البيانات المفحوصة: ${report.evaluatedDataPointsCount} معطيات • الثقة: ${(report.confidenceScore * 100).toInt()}%",
                            fontSize = 10.sp,
                            color = IntelGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (report.evidenceSnippets.isNotEmpty()) {
                    Text(if (isArabic) "الأدلة والقرائن المرصودة:" else "Evidence Snippets:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IntelCyan)
                    report.evidenceSnippets.forEach { snip ->
                        Text("• $snip", fontSize = 11.sp, color = IntelSlate, lineHeight = 16.sp)
                    }
                }

                if (report.limitationsAndUncertainty.isNotBlank()) {
                    Surface(color = Color(0xFF2B1D0E), shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = "حدود التحليل وعدم اليقين: ${report.limitationsAndUncertainty}",
                            fontSize = 10.sp,
                            color = AmberAlert,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IntelCyan, contentColor = Color(0xFF031424)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isArabic) "إغلاق" else "Close", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// CREATE SMART RULE DIALOG
// ==========================================

@Composable
fun CreateSmartRuleDialog(
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, targetType: EntityType, targetName: String, keywords: String, sources: String, minImportance: FilePriority) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetType by remember { mutableStateOf(EntityType.PERSON) }
    var targetName by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }
    var sources by remember { mutableStateOf("") }
    var importance by remember { mutableStateOf(FilePriority.HIGH) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, IntelCyan)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isArabic) "إنشاء قاعدة رصد ذكية متقدمة" else "Create Smart Monitoring Rule",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = IntelCyan
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isArabic) "اسم القاعدة" else "Rule Name", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = targetName,
                    onValueChange = { targetName = it },
                    label = { Text(if (isArabic) "اسم الهدف (شخصية / منظمة / ملف)" else "Target Name", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = keywords,
                    onValueChange = { keywords = it },
                    label = { Text(if (isArabic) "كلمات مفتاحية (مفصولة بفواصل)" else "Keywords (comma separated)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isArabic) "إلغاء" else "Cancel", color = IntelSlate)
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank() && targetName.isNotBlank()) {
                                onConfirm(name, targetType, targetName, keywords, sources, importance)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IntelCyan, contentColor = Color(0xFF031424))
                    ) {
                        Text(if (isArabic) "حفظ القاعدة" else "Save Rule")
                    }
                }
            }
        }
    }
}

// ==========================================
// CREATE INFORMATION GAP DIALOG
// ==========================================

@Composable
fun CreateInformationGapDialog(
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, status: InformationGapStatus, missingData: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(InformationGapStatus.UNKNOWN) }
    var missingData by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = DeskDarkSurface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, IntelCyan)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isArabic) "توثيق فجوة استخباراتية ومعلوماتية" else "Document Information Gap",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = IntelCyan
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isArabic) "عنوان الفجوة" else "Gap Title", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(if (isArabic) "وصف المعلومات الناقصة" else "Description", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = missingData,
                    onValueChange = { missingData = it },
                    label = { Text(if (isArabic) "البيانات المطلوبة للتحقق" else "Missing Data Needed", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isArabic) "إلغاء" else "Cancel", color = IntelSlate)
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onConfirm(title, desc, status, missingData)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IntelCyan, contentColor = Color(0xFF031424))
                    ) {
                        Text(if (isArabic) "توثيق الفجوة" else "Save Gap")
                    }
                }
            }
        }
    }
}
