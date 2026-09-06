package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun TierBadge(tier: SourceTier, isArabic: Boolean = true, modifier: Modifier = Modifier) {
    val (bgColor, textColor, borderCol) = when (tier) {
        SourceTier.PRIMARY -> Triple(Color(0xFF064E3B), Color(0xFF6EE7B7), Color(0xFF059669))
        SourceTier.AGENCY -> Triple(Color(0xFF0C4A6E), Color(0xFF7DD3FC), Color(0xFF0284C7))
        SourceTier.TRUSTED_MEDIA -> Triple(Color(0xFF312E81), Color(0xFFC7D2FE), Color(0xFF4F46E5))
        SourceTier.SECONDARY -> Triple(Color(0xFF451A03), Color(0xFFFDE68A), Color(0xFFD97706))
        SourceTier.UNVERIFIED -> Triple(Color(0xFF4C1D95), Color(0xFFDDD6FE), Color(0xFF7C3AED))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderCol, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = if (isArabic) tier.displayNameAr() else tier.displayNameEn(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SourceTierBadge(tier: SourceTier, isArabic: Boolean = true, modifier: Modifier = Modifier) {
    TierBadge(tier = tier, isArabic = isArabic, modifier = modifier)
}

@Composable
fun PriorityBadge(priority: FilePriority, isArabic: Boolean, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (priority) {
        FilePriority.CRITICAL -> Pair(Color(0x33EF4444), Color(0xFFF87171))
        FilePriority.HIGH -> Pair(Color(0x33F59E0B), Color(0xFFFBBF24))
        FilePriority.MEDIUM -> Pair(Color(0x333B82F6), Color(0xFF60A5FA))
        FilePriority.LOW -> Pair(Color(0x3364748B), Color(0xFF94A3B8))
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(textColor)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = if (isArabic) priority.displayNameAr() else priority.name,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ConnectionBadge(state: ConnectionState, isArabic: Boolean, modifier: Modifier = Modifier) {
    val (dotColor, labelColor) = when (state) {
        ConnectionState.CONNECTED -> Pair(IntelEmerald, Color(0xFF34D399))
        ConnectionState.SLOW -> Pair(IntelAmber, Color(0xFFFBBF24))
        ConnectionState.TIMEOUT -> Pair(IntelAmber, Color(0xFFFBBF24))
        ConnectionState.INVALID_FEED -> Pair(Color(0xFFFB923C), Color(0xFFFDBA74))
        ConnectionState.AUTHENTICATION_ERROR -> Pair(IntelCrimson, Color(0xFFF87171))
        ConnectionState.BLOCKED -> Pair(IntelCrimson, Color(0xFFF87171))
        ConnectionState.NO_NEW_ITEMS -> Pair(IntelSlate, Color(0xFF94A3B8))
        ConnectionState.FAILED -> Pair(IntelCrimson, Color(0xFFF87171))
        ConnectionState.NOT_TESTED -> Pair(IntelSlate, Color(0xFF94A3B8))
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (isArabic) state.displayNameAr() else state.name,
            color = labelColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RoleChip(role: UserRole, isArabic: Boolean, modifier: Modifier = Modifier) {
    val (bgColor, textColor, borderCol) = when (role) {
        UserRole.ADMIN -> Triple(Color(0xFF3F1B1B), Color(0xFFFCA5A5), Color(0xFFB91C1C))
        UserRole.SENIOR_ANALYST -> Triple(Color(0xFF1E3A5F), Color(0xFF93C5FD), Color(0xFF2563EB))
        UserRole.ANALYST -> Triple(Color(0xFF1B382B), Color(0xFF86EFAC), Color(0xFF16A34A))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderCol, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (isArabic) role.displayNameAr() else role.displayNameEn(),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MetricKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = DeskDarkElevatedCard
        ),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DeskDarkBorder))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = IntelSlate
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = IntelSlate,
                fontSize = 11.sp
            )
        }
    }
}
