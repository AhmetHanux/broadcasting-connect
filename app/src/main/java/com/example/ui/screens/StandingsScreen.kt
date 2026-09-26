package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.StandingTeam
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun StandingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val standings by viewModel.standings.collectAsState()
    val activeLeagueId by viewModel.activeLeagueId.collectAsState()
    val leagues = viewModel.supportedLeagues

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FormatListNumbered,
                    contentDescription = null,
                    tint = PitchGreenPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Gerçek Puan Durumu",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Text(
                text = "Resmi lig tablosu anlık olarak internetten güncellenmektedir",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        // League Switcher Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(leagues) { league ->
                val isSelected = activeLeagueId == league.id
                Surface(
                    onClick = { viewModel.selectLeague(league.id) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) PitchGreenPrimary else DarkSurfaceVariant,
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = league.icon, fontSize = 12.sp)
                        Text(
                            text = league.name,
                            color = if (isSelected) DarkBackground else TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurfaceVariant)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(22.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Kulüp",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "O",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(26.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = "G",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = "B",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = "M",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Av",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(28.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = "P",
                color = PitchGreenPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.Center
            )
        }

        // Table Rows
        if (standings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PitchGreenPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(standings, key = { it.rank.toString() + it.teamName }) { team ->
                    val isUcl = team.rank in 1..2
                    val isUel = team.rank in 3..4
                    val rankColor = when {
                        isUcl -> ElectricBlue
                        isUel -> TrophyGold
                        else -> TextMuted
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkCardBg)
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${team.rank}",
                            color = rankColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(22.dp),
                            textAlign = TextAlign.Center
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (team.logoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(team.logoUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = team.teamName,
                                    modifier = Modifier.size(18.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            Text(
                                text = team.teamName,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = "${team.played}",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.width(26.dp),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${team.won}",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.width(24.dp),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${team.drawn}",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.width(24.dp),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${team.lost}",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.width(24.dp),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${team.goalDifference}",
                            color = if (team.goalDifference > 0) PitchGreenPrimary else if (team.goalDifference < 0) LiveRed else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.width(28.dp),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${team.points}",
                            color = PitchGreenPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
