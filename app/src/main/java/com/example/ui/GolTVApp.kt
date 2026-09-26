package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Match
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

enum class ScreenTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    SCORES("Canlı Skor", Icons.Filled.SportsSoccer, Icons.Outlined.SportsSoccer, "nav_scores"),
    STANDINGS("Puan Tablosu", Icons.Filled.FormatListNumbered, Icons.Outlined.FormatListNumbered, "nav_standings"),
    FAVORITES("Favoriler", Icons.Filled.Star, Icons.Outlined.StarBorder, "nav_favorites")
}

@Composable
fun BSNApp(
    viewModel: MainViewModel = viewModel()
) {
    GolTVApp(viewModel = viewModel)
}

@Composable
fun GolTVApp(
    viewModel: MainViewModel = viewModel()
) {
    var currentTab by remember { mutableStateOf(ScreenTab.SCORES) }
    var activeDetailMatch by remember { mutableStateOf<Match?>(null) }

    // If viewing detail match, handle back navigation
    if (activeDetailMatch != null) {
        MatchDetailScreen(
            match = activeDetailMatch!!,
            viewModel = viewModel,
            onBack = { activeDetailMatch = null }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    windowInsets = WindowInsets.navigationBars,
                    tonalElevation = 4.dp
                ) {
                    ScreenTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DarkBackground,
                                selectedTextColor = PitchGreenPrimary,
                                indicatorColor = PitchGreenPrimary,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = DarkBackground
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(DarkBackground)
            ) {
                when (currentTab) {
                    ScreenTab.SCORES -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToMatch = { match ->
                                activeDetailMatch = match
                            }
                        )
                    }
                    ScreenTab.STANDINGS -> {
                        StandingsScreen(viewModel = viewModel)
                    }
                    ScreenTab.FAVORITES -> {
                        FavoritesScreen(
                            viewModel = viewModel,
                            onNavigateToMatch = { match ->
                                activeDetailMatch = match
                            }
                        )
                    }
                }
            }
        }
    }
}
