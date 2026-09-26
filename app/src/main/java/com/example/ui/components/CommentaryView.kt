package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommentaryEvent
import com.example.data.model.EventType
import com.example.data.model.Match
import com.example.ui.theme.*

@Composable
fun CommentaryView(
    match: Match,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkCardBg)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Canlı Anlatım & Önemli Anlar",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = "Hakem: ${match.referee}",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        if (match.commentary.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Henüz canlı anlatım başlamadı.",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                match.commentary.forEach { event ->
                    CommentaryItem(event = event)
                }
            }
        }
    }
}

@Composable
private fun CommentaryItem(event: CommentaryEvent) {
    val (icon, badgeColor) = when (event.eventType) {
        EventType.GOAL, EventType.PENALTY_GOAL -> "⚽" to PitchGreenPrimary
        EventType.YELLOW_CARD -> "🟨" to TrophyGold
        EventType.RED_CARD -> "🟥" to LiveRed
        EventType.SUBSTITUTION -> "🔄" to ElectricBlue
        EventType.VAR_DECISION -> "🖥️" to Color(0xFFA855F7)
        EventType.DANGEROUS_ATTACK -> "⚡" to TrophyGold
        EventType.HALF_TIME, EventType.MATCH_START, EventType.MATCH_END -> "⏱️" to TextSecondary
        else -> "📌" to TextMuted
    }

    val isGoal = event.eventType == EventType.GOAL || event.eventType == EventType.PENALTY_GOAL

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isGoal) PitchGreenContainer.copy(alpha = 0.35f) else DarkSurfaceVariant.copy(alpha = 0.45f))
            .border(
                1.dp,
                if (isGoal) PitchGreenPrimary.copy(alpha = 0.5f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Minute pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeColor.copy(alpha = 0.2f))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = event.minute,
                color = badgeColor,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )
        }

        Text(text = icon, fontSize = 14.sp)

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = event.title,
                    color = if (isGoal) PitchGreenPrimary else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                if (event.teamBadge.isNotBlank()) {
                    Text(text = event.teamBadge, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = event.description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
