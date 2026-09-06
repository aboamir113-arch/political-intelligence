package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * Welcome / Splash Screen for Political Intelligence Desk
 *
 * Configurable parameters allow future rebranding or profile modifications
 * without touching core application architecture.
 *
 * Displays for [durationMillis] (default 3000ms) with staggered subtle entrance animations,
 * then smoothly transitions via [onTimeout].
 */
@Composable
fun WelcomeSplashScreen(
    titleEn: String = "Political Intelligence Desk",
    titleAr: String = "مكتب الاستخبارات السياسية",
    userName: String = "الدكتور بلال اللقيس",
    welcomeNote: String = "نتمنى لك يومًا موفقًا في التحليل والمتابعة وصناعة القرار.",
    durationMillis: Long = 3000L,
    onTimeout: () -> Unit
) {
    // Dynamic Arabic date resolution from device system clock (Levantine Arabic months)
    val currentDateAr = remember {
        val calendar = Calendar.getInstance()
        val dayName = SimpleDateFormat("EEEE", Locale("ar")).format(calendar.time)
        val dayNumber = calendar.get(Calendar.DAY_OF_MONTH)
        val year = calendar.get(Calendar.YEAR)
        val levantMonths = listOf(
            "كانون الثاني", "شباط", "آذار", "نيسان", "أيار", "حزيران",
            "تموز", "آب", "أيلول", "تشرين الأول", "تشرين الثاني", "كانون الأول"
        )
        val monthName = levantMonths.getOrElse(calendar.get(Calendar.MONTH)) {
            SimpleDateFormat("MMMM", Locale("ar")).format(calendar.time)
        }
        "$dayName، $dayNumber $monthName $year"
    }

    // Animation visibility phases
    var showEmblem by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showDate by remember { mutableStateOf(false) }
    var showGreeting by remember { mutableStateOf(false) }
    var fadeOutScreen by remember { mutableStateOf(false) }

    // Coordinated timing orchestrator (strictly bounded to durationMillis)
    LaunchedEffect(Unit) {
        // Stage 1: Emblem appears
        delay(120L)
        showEmblem = true

        // Stage 2: App Name appears
        delay(350L)
        showTitle = true

        // Stage 3: Dynamic Date appears
        delay(350L)
        showDate = true

        // Stage 4: Welcome Greeting appears
        delay(400L)
        showGreeting = true

        // Wait for remaining duration until 2700ms, then initiate soft fade out
        val remainingBeforeFadeOut = (durationMillis - 1220L - 300L).coerceAtLeast(100L)
        delay(remainingBeforeFadeOut)
        fadeOutScreen = true

        // Complete 3000ms lifecycle and invoke onTimeout
        delay(300L)
        onTimeout()
    }

    // Animated alphas and transitions
    val screenAlpha by animateFloatAsState(
        targetValue = if (fadeOutScreen) 0f else 1f,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "screenAlpha"
    )

    val emblemAlpha by animateFloatAsState(
        targetValue = if (showEmblem) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "emblemAlpha"
    )
    val emblemScale by animateFloatAsState(
        targetValue = if (showEmblem) 1f else 0.82f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "emblemScale"
    )

    val titleAlpha by animateFloatAsState(
        targetValue = if (showTitle) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "titleAlpha"
    )

    val dateAlpha by animateFloatAsState(
        targetValue = if (showDate) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "dateAlpha"
    )

    val greetingAlpha by animateFloatAsState(
        targetValue = if (showGreeting) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "greetingAlpha"
    )

    // Force RTL direction for proper Arabic typography and alignment
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeskDarkBackground)
                .alpha(screenAlpha)
                .testTag("welcome_splash_screen"),
            contentAlignment = Alignment.Center
        ) {
            // Background Layer: Abstract Intelligence Network Grid
            IntelligenceNetworkBackground()

            // Foreground Content: Centered, balanced, minimal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 1. Emblem / Logo
                Box(
                    modifier = Modifier
                        .scale(emblemScale)
                        .alpha(emblemAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    IntelligenceEmblem()
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Application Names (English & Arabic)
                Column(
                    modifier = Modifier.alpha(titleAlpha),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = titleEn.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.5.sp,
                        color = IntelCyan.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = titleAr,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Delicate Minimalist Separator
                Box(
                    modifier = Modifier
                        .alpha(titleAlpha)
                        .width(140.dp)
                        .height(1.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    IntelCyan.copy(alpha = 0.5f),
                                    IntelGold.copy(alpha = 0.6f),
                                    IntelCyan.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Dynamic Date
                Row(
                    modifier = Modifier
                        .alpha(dateAlpha)
                        .clip(RoundedCornerShape(20.dp))
                        .background(DeskDarkSurfaceVariant.copy(alpha = 0.7f))
                        .border(
                            width = 1.dp,
                            color = IntelSlate.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(IntelCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentDateAr,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = IntelSilver,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Welcome Message
                Column(
                    modifier = Modifier.alpha(greetingAlpha),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "مرحبًا بك، $userName",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = IntelGold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = welcomeNote,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = IntelSlate,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Clean Abstract Intelligence Crest
 * Minimalist geometric emblem combining strategic radar, shield protection, and data synthesis nodes.
 */
@Composable
private fun IntelligenceEmblem() {
    Box(
        modifier = Modifier.size(92.dp),
        contentAlignment = Alignment.Center
    ) {
        // Subtle outer pulse glow ring
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(IntelCyan.copy(alpha = 0.05f))
                .border(1.dp, IntelCyan.copy(alpha = 0.2f), CircleShape)
        )

        // Inner geometric shield container
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF162032),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .border(1.5.dp, IntelGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "شعار مكتب الاستخبارات السياسية",
                tint = IntelGold,
                modifier = Modifier.size(36.dp)
            )

            // Central strategic core indicator
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(IntelCyan)
            )
        }
    }
}

/**
 * Minimalist Intelligence Network Background
 * Subtle canvas drawing coordinate dots, fine analytical lines, and a gentle central radial gradient.
 */
@Composable
private fun IntelligenceNetworkBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Central soft radial illumination
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    IntelCyan.copy(alpha = 0.07f),
                    IntelBlue.copy(alpha = 0.03f),
                    Color.Transparent
                ),
                center = Offset(width / 2f, height / 2f),
                radius = width.coerceAtLeast(height) * 0.55f
            )
        )

        // Subtle geometric analytical constellation lines
        val strokeColor = IntelSlate.copy(alpha = 0.12f)
        val dotColor = IntelCyan.copy(alpha = 0.22f)

        val nodePoints = listOf(
            Offset(width * 0.18f, height * 0.22f),
            Offset(width * 0.35f, height * 0.16f),
            Offset(width * 0.72f, height * 0.20f),
            Offset(width * 0.85f, height * 0.28f),
            Offset(width * 0.12f, height * 0.75f),
            Offset(width * 0.28f, height * 0.84f),
            Offset(width * 0.68f, height * 0.80f),
            Offset(width * 0.86f, height * 0.72f)
        )

        // Draw connecting telemetry lines
        if (nodePoints.size >= 8) {
            drawLine(strokeColor, nodePoints[0], nodePoints[1], strokeWidth = 1f)
            drawLine(strokeColor, nodePoints[2], nodePoints[3], strokeWidth = 1f)
            drawLine(strokeColor, nodePoints[4], nodePoints[5], strokeWidth = 1f)
            drawLine(strokeColor, nodePoints[6], nodePoints[7], strokeWidth = 1f)
        }

        // Draw constellation points
        nodePoints.forEach { pt ->
            drawCircle(
                color = dotColor,
                radius = 2.5f,
                center = pt
            )
        }
    }
}
