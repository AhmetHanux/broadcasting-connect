package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Lineup
import com.example.data.model.Match
import com.example.data.model.Player
import com.example.ui.theme.*

@Composable
fun LineupPitchView(
    match: Match,
    modifier: Modifier = Modifier
) {
    var selectedTeamTab by remember { mutableIntStateOf(0) } // 0: Home, 1: Away

    val activeLineup = if (selectedTeamTab == 0) match.homeLineup else match.awayLineup
    val activeTeam = if (selectedTeamTab == 0) match.homeTeam else match.awayTeam
    val teamColor = Color(activeTeam.primaryColorHex)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkCardBg)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Team Selector Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurfaceVariant)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTeamTab == 0) PitchGreenPrimary else Color.Transparent)
                    .clickable { selectedTeamTab = 0 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${match.homeTeam.logoText} ${match.homeTeam.name} (${match.homeLineup.formation})",
                    color = if (selectedTeamTab == 0) DarkBackground else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTeamTab == 1) PitchGreenPrimary else Color.Transparent)
                    .clickable { selectedTeamTab = 1 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${match.awayTeam.logoText} ${match.awayTeam.name} (${match.awayLineup.formation})",
                    color = if (selectedTeamTab == 1) DarkBackground else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Coach info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Teknik Direktör: ${activeLineup.coach}",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Diziliş: ${activeLineup.formation}",
                color = PitchGreenPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Football Pitch Canvas View
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F381E))
        ) {
            // Pitch markings
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = Stroke(width = 2f)
                val whitePitch = Color.White.copy(alpha = 0.35f)

                // Outer border
                drawRect(
                    color = whitePitch,
                    topLeft = Offset(16f, 16f),
                    size = Size(size.width - 32f, size.height - 32f),
                    style = stroke
                )
                // Halfway line
                drawLine(
                    color = whitePitch,
                    start = Offset(16f, size.height / 2f),
                    end = Offset(size.width - 16f, size.height / 2f),
                    strokeWidth = 2f
                )
                // Center circle
                drawCircle(
                    color = whitePitch,
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = 45f,
                    style = stroke
                )
                // Penalty box top
                drawRect(
                    color = whitePitch,
                    topLeft = Offset(size.width * 0.25f, 16f),
                    size = Size(size.width * 0.5f, 50f),
                    style = stroke
                )
                // Penalty box bottom
                drawRect(
                    color = whitePitch,
                    topLeft = Offset(size.width * 0.25f, size.height - 66f),
                    size = Size(size.width * 0.5f, 50f),
                    style = stroke
                )
            }

            // Pitch Tactical Players Layout
            if (activeLineup.starting11.isNotEmpty()) {
                val gk = activeLineup.starting11.firstOrNull()
                val defenders = activeLineup.starting11.filter { it.position == "DF" }
                val midfielders = activeLineup.starting11.filter { it.position == "OS" }
                val forwards = activeLineup.starting11.filter { it.position == "FV" }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Forwards Row (Top)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        forwards.forEach { p -> PlayerPitchBadge(p, teamColor) }
                    }

                    // Midfielders Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        midfielders.forEach { p -> PlayerPitchBadge(p, teamColor) }
                    }

                    // Defenders Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        defenders.forEach { p -> PlayerPitchBadge(p, teamColor) }
                    }

                    // Goalkeeper (Bottom)
                    if (gk != null) {
                        PlayerPitchBadge(gk, Color(0xFFEAB308))
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Kadrolar maç öncesi açıklanacak", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }

        // Starting 11 List
        Text(
            text = "İlk 11",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            activeLineup.starting11.forEach { player ->
                PlayerListItem(player = player)
            }
        }

        // Substitutes
        if (activeLineup.substitutes.isNotEmpty()) {
            Text(
                text = "Yedekler",
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                activeLineup.substitutes.forEach { player ->
                    PlayerListItem(player = player)
                }
            }
        }
    }
}

@Composable
private fun PlayerPitchBadge(player: Player, badgeColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(badgeColor)
                .border(1.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${player.number}",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }
        Text(
            text = player.name.split(" ").lastOrNull() ?: player.name,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PlayerListItem(player: Player) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(DarkBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${player.number}",
                    color = PitchGreenPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = player.name,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            if (player.isCaptain) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(TrophyGold)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(text = "C", color = DarkBackground, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }

            if (player.goals > 0) {
                Text(text = "⚽".repeat(player.goals), fontSize = 11.sp)
            }

            if (player.hasYellowCard) {
                Box(
                    modifier = Modifier
                        .size(9.dp, 13.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(TrophyGold)
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(PitchGreenDark.copy(alpha = 0.25f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "${player.rating}",
                color = PitchGreenPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
