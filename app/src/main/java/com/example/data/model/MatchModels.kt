package com.example.data.model

data class Team(
    val id: String,
    val name: String,
    val shortName: String,
    val logoUrl: String = "",
    val logoText: String = "⚽",
    val primaryColorHex: Long = 0xFF1E3A8A,
    val city: String = ""
)

enum class MatchStatus {
    LIVE,
    UPCOMING,
    FINISHED
}

data class CommentaryEvent(
    val id: String,
    val minute: String,
    val title: String,
    val description: String,
    val eventType: EventType,
    val teamBadge: String = "",
    val isImportant: Boolean = false
)

enum class EventType {
    GOAL,
    PENALTY_GOAL,
    MISSED_PENALTY,
    YELLOW_CARD,
    RED_CARD,
    SUBSTITUTION,
    VAR_DECISION,
    DANGEROUS_ATTACK,
    CORNER,
    HALF_TIME,
    MATCH_START,
    MATCH_END
}

data class MomentumPoint(
    val minuteRange: String, // "0-15'", "15-30'", "30-45'", "45-60'", "60-75'", "75-90'"
    val homeDominance: Int, // 0 to 100 (>50 favors home, <50 favors away)
    val eventHighlight: String? = null
)

data class TacticalAnalysisSummary(
    val matchCourseComment: String,
    val attackingInsight: String,
    val defenseInsight: String,
    val keyDuel: String,
    val gamePlanAssessment: String,
    val winProbabilityHome: Int = 50,
    val drawProbability: Int = 25,
    val winProbabilityAway: Int = 25
)

data class MatchStats(
    val possessionHome: Int = 50,
    val possessionAway: Int = 50,
    val expectedGoalsHome: Double = 1.20,
    val expectedGoalsAway: Double = 1.05,
    val totalShotsHome: Int = 0,
    val totalShotsAway: Int = 0,
    val shotsOnTargetHome: Int = 0,
    val shotsOnTargetAway: Int = 0,
    val shotsOffTargetHome: Int = 0,
    val shotsOffTargetAway: Int = 0,
    val blockedShotsHome: Int = 0,
    val blockedShotsAway: Int = 0,
    val cornersHome: Int = 0,
    val cornersAway: Int = 0,
    val foulsHome: Int = 0,
    val foulsAway: Int = 0,
    val offsidesHome: Int = 0,
    val offsidesAway: Int = 0,
    val yellowCardsHome: Int = 0,
    val yellowCardsAway: Int = 0,
    val redCardsHome: Int = 0,
    val redCardsAway: Int = 0,
    val passAccuracyHome: Int = 82,
    val passAccuracyAway: Int = 80,
    val totalPassesHome: Int = 420,
    val totalPassesAway: Int = 390,
    val accuratePassesHome: Int = 344,
    val accuratePassesAway: Int = 312,
    val opponentHalfPassAccuracyHome: Int = 74,
    val opponentHalfPassAccuracyAway: Int = 70,
    val keyPassesHome: Int = 8,
    val keyPassesAway: Int = 6,
    val crossAccuracyHome: Int = 32,
    val crossAccuracyAway: Int = 28,
    val dangerousAttacksHome: Int = 44,
    val dangerousAttacksAway: Int = 36,
    val boxTouchesHome: Int = 20,
    val boxTouchesAway: Int = 15,
    val duelsWonHome: Int = 51,
    val duelsWonAway: Int = 49,
    val tacklesHome: Int = 14,
    val tacklesAway: Int = 12,
    val interceptionsHome: Int = 9,
    val interceptionsAway: Int = 10,
    val savesHome: Int = 2,
    val savesAway: Int = 3,
    val momentumPoints: List<MomentumPoint> = emptyList(),
    val tacticalAnalysis: TacticalAnalysisSummary? = null
)

data class Player(
    val number: Int,
    val name: String,
    val position: String, // "KL", "DF", "OS", "FV"
    val isCaptain: Boolean = false,
    val rating: Double = 7.0,
    val hasYellowCard: Boolean = false,
    val hasRedCard: Boolean = false,
    val goals: Int = 0
)

data class Lineup(
    val formation: String,
    val coach: String,
    val starting11: List<Player>,
    val substitutes: List<Player>
)

data class ChatMessage(
    val id: String,
    val username: String,
    val message: String,
    val timestamp: String,
    val userBadge: String = "⚽",
    val isSpecial: Boolean = false,
    val teamSide: String? = null
)

data class Match(
    val id: String,
    val leagueId: String = "tur.1",
    val league: String,
    val leagueIcon: String,
    val round: String,
    val homeTeam: Team,
    val awayTeam: Team,
    val homeScore: Int,
    val awayScore: Int,
    val currentMinute: String,
    val status: MatchStatus,
    val stadium: String,
    val referee: String,
    val dateText: String,
    val timeText: String,
    val tvChannel: String = "BSN Canlı Skor",
    val stats: MatchStats = MatchStats(),
    val homeLineup: Lineup = Lineup("4-2-3-1", "", emptyList(), emptyList()),
    val awayLineup: Lineup = Lineup("4-3-3", "", emptyList(), emptyList()),
    val commentary: List<CommentaryEvent> = emptyList(),
    val spectatorCount: String = "-",
    val hasVar: Boolean = true
)

data class StandingTeam(
    val rank: Int,
    val teamName: String,
    val teamShort: String,
    val logoUrl: String = "",
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDifference: Int,
    val points: Int,
    val form: List<String> = emptyList()
)

data class LeagueItem(
    val id: String,
    val name: String,
    val icon: String,
    val country: String
)
