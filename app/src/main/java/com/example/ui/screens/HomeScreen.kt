package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Match
import com.example.data.model.MatchStatus
import com.example.ui.components.MatchCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToMatch: (Match) -> Unit,
    modifier: Modifier = Modifier
) {
    val matches by viewModel.matches.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val activeLeagueId by viewModel.activeLeagueId.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    val leagues = viewModel.supportedLeagues
    val filterOptions = listOf("Tümü", "Canlı", "Biten", "Gelecek")

    // Filter matches
    val filteredMatches = matches.filter { match ->
        val matchesStatus = when (statusFilter) {
            "Canlı" -> match.status == MatchStatus.LIVE
            "Biten" -> match.status == MatchStatus.FINISHED
            "Gelecek" -> match.status == MatchStatus.UPCOMING
            else -> true
        }
        val matchesSearch = if (searchQuery.isBlank()) true else {
            match.homeTeam.name.contains(searchQuery, ignoreCase = true) ||
            match.awayTeam.name.contains(searchQuery, ignoreCase = true) ||
            match.league.contains(searchQuery, ignoreCase = true)
        }
        matchesStatus && matchesSearch
    }

    val liveCount = matches.count { it.status == MatchStatus.LIVE }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Top Bar: Broadcasting Sport Network
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.bsn_logo),
                            contentDescription = "BSN Logo",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(1.dp, PitchGreenPrimary, CircleShape)
                        )
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "BSN",
                                    color = PitchGreenPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Sport Network",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Gerçek Canlı Maç Skorları & İstatistik",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (liveCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(LiveRed.copy(alpha = 0.2f))
                                    .border(1.dp, LiveRed.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "🔴 $liveCount Canlı",
                                    color = LiveRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.refreshMatches() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                                .testTag("refresh_matches_btn")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = PitchGreenPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Yenile",
                                    tint = PitchGreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Takım veya lig ara...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Ara",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("match_search_bar"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PitchGreenPrimary,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }
        }

        // Real League Selector Tabs
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Ligler",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(leagues) { league ->
                        val isSelected = activeLeagueId == league.id
                        Surface(
                            onClick = { viewModel.selectLeague(league.id) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PitchGreenPrimary else DarkSurfaceVariant,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                            modifier = Modifier.testTag("league_tab_${league.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = league.icon, fontSize = 14.sp)
                                Text(
                                    text = league.name,
                                    color = if (isSelected) DarkBackground else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Match Status Filter Chips (Tümü, Canlı, Biten, Gelecek)
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { filter ->
                    val isSelected = statusFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setStatusFilter(filter) },
                        label = {
                            Text(
                                text = if (filter == "Canlı") "🔴 Canlı" else filter,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue,
                            selectedLabelColor = DarkBackground,
                            containerColor = DarkSurfaceVariant,
                            labelColor = TextSecondary
                        ),
                        border = if (isSelected) null else FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = false,
                            borderColor = DarkCardBorder
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("filter_status_$filter")
                    )
                }
            }
        }

        // Real Match List Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Gerçek Maç Bilgileri & Skorlar",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredMatches.size} Karşılaşma",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        // Matches List
        if (filteredMatches.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = PitchGreenPrimary)
                            Text(text = "İnternetten canlı maçlar yükleniyor...", color = TextSecondary, fontSize = 13.sp)
                        } else {
                            Text(text = "⚽", fontSize = 36.sp)
                            Text(
                                text = "Bu kategoride güncel maç bulunamadı",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                            Button(
                                onClick = { viewModel.refreshMatches() },
                                colors = ButtonDefaults.buttonColors(containerColor = PitchGreenPrimary, contentColor = DarkBackground),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Yenile", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            items(filteredMatches, key = { it.id }) { match ->
                val isFav = favorites.any { it.matchId == match.id }
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    MatchCard(
                        match = match,
                        isFavorite = isFav,
                        onMatchClick = {
                            viewModel.selectMatch(match)
                            onNavigateToMatch(match)
                        },
                        onFavoriteToggle = {
                            viewModel.toggleFavorite(match)
                        }
                    )
                }
            }
        }
    }
}
