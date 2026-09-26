package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Match
import com.example.data.model.MomentumPoint
import com.example.data.model.TacticalAnalysisSummary
import com.example.ui.theme.*

@Composable
fun MatchStatsView(
    match: Match,
    modifier: Modifier = Modifier
) {
    val stats = match.stats
    val homeColor = Color(match.homeTeam.primaryColorHex)
    val awayColor = Color(match.awayTeam.primaryColorHex)

    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Genel Bakış, 1: Şut & Pas, 2: Taktik & Yorum

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sub-tabs: Genel, Detaylı İstatistik, Taktik Analiz
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurfaceVariant)
                .padding(3.dp)
        ) {
            listOf("📊 Genel & xG", "🎯 Şut & Pas", "🧠 Taktik Analiz").forEachIndexed { index, title ->
                val isSelected = selectedSubTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) PitchGreenPrimary else Color.Transparent)
                        .clickable { selectedSubTab = index }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) DarkBackground else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        fontSize = 11.sp
                    )
                }
            }
        }

        when (selectedSubTab) {
            0 -> {
                // Tab 0: Genel Bakış & xG & Barometre
                GeneralStatsCard(match, homeColor, awayColor)
                MomentumChartCard(stats.momentumPoints, match.homeTeam.shortName, match.awayTeam.shortName, homeColor, awayColor)
            }
            1 -> {
                // Tab 1: Şut, Pas & İkili Mücadele Detayları
                ShotsAndPassesCard(match, homeColor, awayColor)
                DuelsAndDefenseCard(match, homeColor, awayColor)
            }
            2 -> {
                // Tab 2: Taktiksel Maç Yorumu & Gidişat Analizi
                TacticalInsightCard(match, stats.tacticalAnalysis, homeColor, awayColor)
            }
        }
    }
}

@Composable
private fun GeneralStatsCard(
    match: Match,
    homeColor: Color,
    awayColor: Color
) {
    val stats = match.stats

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Teams
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${match.homeTeam.logoText} ${match.homeTeam.name}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "MAÇ BASKISI",
                    color = PitchGreenPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${match.awayTeam.name} ${match.awayTeam.logoText}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            // Topla Oynama (Possession)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "%${stats.possessionHome}",
                        color = homeColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Topla Oynama Oranı",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "%${stats.possessionAway}",
                        color = awayColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DarkSurfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(stats.possessionHome.coerceAtLeast(1).toFloat())
                            .background(homeColor)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(stats.possessionAway.coerceAtLeast(1).toFloat())
                            .background(awayColor)
                    )
                }
            }

            // Gol Beklentisi (xG)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${stats.expectedGoalsHome}",
                        color = homeColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Gol Beklentisi (xG)",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(TrophyGold)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(text = "YENİ", color = DarkBackground, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Text(
                        text = "${stats.expectedGoalsAway}",
                        color = awayColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                val totalXg = (stats.expectedGoalsHome + stats.expectedGoalsAway).coerceAtLeast(0.1)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(DarkSurfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight((stats.expectedGoalsHome / totalXg).toFloat().coerceIn(0.05f, 0.95f))
                            .background(homeColor)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight((stats.expectedGoalsAway / totalXg).toFloat().coerceIn(0.05f, 0.95f))
                            .background(awayColor)
                    )
                }
            }

            Divider(color = DarkCardBorder, thickness = 1.dp)

            StatComparisonRow("Toplam Şut", stats.totalShotsHome, stats.totalShotsAway, homeColor, awayColor)
            StatComparisonRow("İsabetli Şut", stats.shotsOnTargetHome, stats.shotsOnTargetAway, homeColor, awayColor)
            StatComparisonRow("Tehlikeli Atak", stats.dangerousAttacksHome, stats.dangerousAttacksAway, homeColor, awayColor)
            StatComparisonRow("Ceza Sahasında Topla Buluşma", stats.boxTouchesHome, stats.boxTouchesAway, homeColor, awayColor)
            StatComparisonRow("Korner", stats.cornersHome, stats.cornersAway, homeColor, awayColor)
            StatComparisonRow("Faul", stats.foulsHome, stats.foulsAway, homeColor, awayColor)
        }
    }
}

@Composable
private fun MomentumChartCard(
    momentumPoints: List<MomentumPoint>,
    homeShort: String,
    awayShort: String,
    homeColor: Color,
    awayColor: Color
) {
    if (momentumPoints.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.AutoGraph,
                        contentDescription = null,
                        tint = PitchGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Maç Momentum & Baskı Zaman Çizelgesi",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = "$homeShort vs $awayShort",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Text(
                text = "Hangi dakikalarda hangi takımın oyunu domine ettiğini gösterir:",
                color = TextSecondary,
                fontSize = 11.sp
            )

            // Timeline periods bars
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                momentumPoints.forEach { point ->
                    val homeDominance = point.homeDominance
                    val awayDominance = 100 - homeDominance

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = point.minuteRange,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(42.dp)
                            )

                            if (point.eventHighlight != null) {
                                Text(
                                    text = point.eventHighlight,
                                    color = TrophyGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = if (homeDominance >= 50) "%$homeDominance $homeShort" else "%$awayDominance $awayShort",
                                color = if (homeDominance >= 50) homeColor else awayColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(DarkSurfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(homeDominance.toFloat().coerceAtLeast(1f))
                                    .background(homeColor)
                            )
                            Spacer(modifier = Modifier.width(1.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(awayDominance.toFloat().coerceAtLeast(1f))
                                    .background(awayColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShotsAndPassesCard(
    match: Match,
    homeColor: Color,
    awayColor: Color
) {
    val stats = match.stats

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = Icons.Default.SportsSoccer, contentDescription = null, tint = PitchGreenPrimary, modifier = Modifier.size(16.dp))
                Text(text = "Şut & Pas Dağılımı", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            StatComparisonRow("Toplam Şut", stats.totalShotsHome, stats.totalShotsAway, homeColor, awayColor)
            StatComparisonRow("Kaleyi Bulan Şut", stats.shotsOnTargetHome, stats.shotsOnTargetAway, homeColor, awayColor)
            StatComparisonRow("Kaleyi Bulmayan Şut", stats.shotsOffTargetHome, stats.shotsOffTargetAway, homeColor, awayColor)
            StatComparisonRow("Engellenen Şut", stats.blockedShotsHome, stats.blockedShotsAway, homeColor, awayColor)

            Divider(color = DarkCardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

            StatComparisonRow("Toplam Pas", stats.totalPassesHome, stats.totalPassesAway, homeColor, awayColor)
            StatComparisonRow("İsabetli Pas", stats.accuratePassesHome, stats.accuratePassesAway, homeColor, awayColor)
            StatComparisonRow("Genel Pas İsabeti", stats.passAccuracyHome, stats.passAccuracyAway, homeColor, awayColor, isPercentage = true)
            StatComparisonRow("Rakip Yarı Sahada Pas İsabeti", stats.opponentHalfPassAccuracyHome, stats.opponentHalfPassAccuracyAway, homeColor, awayColor, isPercentage = true)
            StatComparisonRow("Kilit Pas", stats.keyPassesHome, stats.keyPassesAway, homeColor, awayColor)
            StatComparisonRow("Orta İsabeti", stats.crossAccuracyHome, stats.crossAccuracyAway, homeColor, awayColor, isPercentage = true)
        }
    }
}

@Composable
private fun DuelsAndDefenseCard(
    match: Match,
    homeColor: Color,
    awayColor: Color
) {
    val stats = match.stats

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                Text(text = "İkili Mücadele & Savunma", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            StatComparisonRow("Kazanılan İkili Mücadele", stats.duelsWonHome, stats.duelsWonAway, homeColor, awayColor, isPercentage = true)
            StatComparisonRow("Başarılı Top Çalma", stats.tacklesHome, stats.tacklesAway, homeColor, awayColor)
            StatComparisonRow("Pas Arası / Engelleme", stats.interceptionsHome, stats.interceptionsAway, homeColor, awayColor)
            StatComparisonRow("Kaleci Kurtarışı", stats.savesHome, stats.savesAway, homeColor, awayColor)
            StatComparisonRow("Ofsayt", stats.offsidesHome, stats.offsidesAway, homeColor, awayColor)
            StatComparisonRow("Sarı Kart", stats.yellowCardsHome, stats.yellowCardsAway, homeColor, awayColor)
        }
    }
}

@Composable
private fun TacticalInsightCard(
    match: Match,
    tactical: TacticalAnalysisSummary?,
    homeColor: Color,
    awayColor: Color
) {
    if (tactical == null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg)
        ) {
            Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                Text(text = "Taktik analiz maç esnasında canlı oluşturulmaktadır.", color = TextMuted, fontSize = 13.sp)
            }
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Win Probability Barometer
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Canlı Kazanma İhtimali", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "AI Simülasyonu", color = PitchGreenPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "%${tactical.winProbabilityHome} ${match.homeTeam.shortName}", color = homeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "%${tactical.drawProbability} Beraberlik", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(text = "%${tactical.winProbabilityAway} ${match.awayTeam.shortName}", color = awayColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Triple Segment Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DarkSurfaceVariant)
                ) {
                    Box(modifier = Modifier.fillMaxHeight().weight(tactical.winProbabilityHome.toFloat()).background(homeColor))
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(modifier = Modifier.fillMaxHeight().weight(tactical.drawProbability.toFloat()).background(TextMuted))
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(modifier = Modifier.fillMaxHeight().weight(tactical.winProbabilityAway.toFloat()).background(awayColor))
                }
            }
        }

        // Match Course Commentary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.Insights, contentDescription = null, tint = PitchGreenPrimary, modifier = Modifier.size(18.dp))
                    Text(text = "Maçın Gidişat Yorumu", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Text(
                    text = tactical.matchCourseComment,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // Attacking & Defending Breakdown
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Attacking Insight
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "⚡", fontSize = 14.sp)
                        Text(text = "Hücum Planı & Tehlike Noktası", color = TrophyGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text(text = tactical.attackingInsight, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                }

                Divider(color = DarkCardBorder, thickness = 1.dp)

                // Defense Insight
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "🛡️", fontSize = 14.sp)
                        Text(text = "Savunma & Boşluk Analizi", color = ElectricBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text(text = tactical.defenseInsight, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                }

                Divider(color = DarkCardBorder, thickness = 1.dp)

                // Key Duel
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "⚔️", fontSize = 14.sp)
                        Text(text = "Kritik Saha İçi Eşleşme", color = PitchGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text(text = tactical.keyDuel, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Divider(color = DarkCardBorder, thickness = 1.dp)

                // Coach Assessment
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = TrophyGold, modifier = Modifier.size(16.dp))
                        Text(text = "Teknik Direktör Hamleleri", color = TrophyGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text(text = tactical.gamePlanAssessment, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun StatComparisonRow(
    label: String,
    homeValue: Int,
    awayValue: Int,
    homeColor: Color,
    awayColor: Color,
    isPercentage: Boolean = false
) {
    val total = (homeValue + awayValue).coerceAtLeast(1)
    val homeWeight = homeValue.toFloat() / total
    val awayWeight = awayValue.toFloat() / total

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isPercentage) "%$homeValue" else "$homeValue",
                color = if (homeValue >= awayValue) homeColor else TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.width(42.dp),
                textAlign = TextAlign.Start
            )

            Text(
                text = label,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )

            Text(
                text = if (isPercentage) "%$awayValue" else "$awayValue",
                color = if (awayValue >= homeValue) awayColor else TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.width(42.dp),
                textAlign = TextAlign.End
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(DarkSurfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(homeWeight.coerceAtLeast(0.01f))
                    .background(homeColor.copy(alpha = 0.85f))
            )
            Spacer(modifier = Modifier.width(2.dp))
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(awayWeight.coerceAtLeast(0.01f))
                    .background(awayColor.copy(alpha = 0.85f))
            )
        }
    }
}
