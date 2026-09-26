package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Match
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    match: Match,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val selectedMatchState by viewModel.selectedMatch.collectAsState()
    val activeMatch = selectedMatchState ?: match

    val chatMessages by viewModel.chatMessages.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val isFav = favorites.any { it.matchId == activeMatch.id }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("📊 İstatistik", "⏱️ Olaylar", "📋 Kadrolar", "💬 Tribün")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${activeMatch.homeTeam.name} vs ${activeMatch.awayTeam.name}",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${activeMatch.league} • ${activeMatch.round}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("match_detail_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri Dön",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.selectMatch(activeMatch) },
                        modifier = Modifier.testTag("match_detail_refresh_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yenile",
                            tint = PitchGreenPrimary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.toggleFavorite(activeMatch) },
                        modifier = Modifier.testTag("match_detail_fav_btn")
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favori",
                            tint = if (isFav) TrophyGold else TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Real Broadcast Match Header (No Video Player)
            MatchHeaderBanner(
                match = activeMatch,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = DarkSurface,
                contentColor = PitchGreenPrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTabIndex == index) PitchGreenPrimary else TextSecondary,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.testTag("match_tab_$index")
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> {
                        // Statistics & Analysis Tab
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                MatchStatsView(match = activeMatch)
                            }
                        }
                    }
                    1 -> {
                        // Commentary & Events Tab
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                CommentaryView(match = activeMatch)
                            }
                        }
                    }
                    2 -> {
                        // Lineup Pitch Tab
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                LineupPitchView(match = activeMatch)
                            }
                        }
                    }
                    3 -> {
                        // Live Chat Tab
                        LiveChatPanel(
                            messages = chatMessages,
                            onSendMessage = { text, badge ->
                                viewModel.sendChatMessage(text, badge)
                            },
                            onTriggerReaction = { emoji ->
                                viewModel.triggerReaction(emoji)
                                viewModel.sendChatMessage(emoji, "🔥")
                            }
                        )
                    }
                }
            }
        }
    }
}
