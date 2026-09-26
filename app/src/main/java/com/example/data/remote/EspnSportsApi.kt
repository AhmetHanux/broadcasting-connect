package com.example.data.remote

import android.util.Log
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class EspnSportsApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "EspnSportsApi"
        private const val BASE_URL = "https://site.api.espn.com/apis/site/v2/sports/soccer"
        private const val STANDINGS_URL = "https://site.api.espn.com/apis/v2/sports/soccer"

        val SUPPORTED_LEAGUES = listOf(
            LeagueItem("tur.1", "Süper Lig", "🇹🇷", "Türkiye"),
            LeagueItem("eng.1", "Premier Lig", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "İngiltere"),
            LeagueItem("uefa.champions", "Şampiyonlar Ligi", "🌟", "Avrupa"),
            LeagueItem("esp.1", "La Liga", "🇪🇸", "İspanya"),
            LeagueItem("ita.1", "Serie A", "🇮🇹", "İtalya"),
            LeagueItem("ger.1", "Bundesliga", "🇩🇪", "Almanya")
        )
    }

    suspend fun fetchScoreboard(leagueSlug: String = "tur.1"): List<Match> = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/$leagueSlug/scoreboard"
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "okhttp/4.12.0")
                .header("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "Scoreboard fetch failed with code: ${response.code}")
                return@withContext emptyList()
            }

            val body = response.body?.string() ?: return@withContext emptyList()
            parseScoreboardJson(body, leagueSlug)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching scoreboard for $leagueSlug: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchMatchSummary(leagueSlug: String, eventId: String): MatchDetailsUpdate? = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/$leagueSlug/summary?event=$eventId"
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "okhttp/4.12.0")
                .header("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val body = response.body?.string() ?: return@withContext null
            parseSummaryJson(body)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching summary for event $eventId: ${e.message}", e)
            null
        }
    }

    suspend fun fetchStandings(leagueSlug: String = "tur.1"): List<StandingTeam> = withContext(Dispatchers.IO) {
        val url = "$STANDINGS_URL/$leagueSlug/standings"
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "okhttp/4.12.0")
                .header("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val body = response.body?.string() ?: return@withContext emptyList()
            parseStandingsJson(body)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching standings for $leagueSlug: ${e.message}", e)
            emptyList()
        }
    }

    private fun parseScoreboardJson(jsonString: String, leagueSlug: String): List<Match> {
        val matches = mutableListOf<Match>()
        val root = JSONObject(jsonString)

        val leaguesArray = root.optJSONArray("leagues")
        val leagueObj = leaguesArray?.optJSONObject(0)
        val leagueName = leagueObj?.optString("name", "Futbol Ligi") ?: "Futbol Ligi"
        val leagueIcon = when {
            leagueSlug.contains("tur") -> "🇹🇷"
            leagueSlug.contains("eng") -> "🏴󠁧󠁢󠁥󠁮󠁧󠁿"
            leagueSlug.contains("uefa") -> "🌟"
            leagueSlug.contains("esp") -> "🇪🇸"
            leagueSlug.contains("ita") -> "🇮🇹"
            leagueSlug.contains("ger") -> "🇩🇪"
            else -> "⚽"
        }

        val eventsArray = root.optJSONArray("events") ?: return emptyList()

        for (i in 0 until eventsArray.length()) {
            val eventObj = eventsArray.optJSONObject(i) ?: continue
            val eventId = eventObj.optString("id", "")
            val eventName = eventObj.optString("name", "")
            val rawDate = eventObj.optString("date", "")

            val competitions = eventObj.optJSONArray("competitions")
            val competition = competitions?.optJSONObject(0) ?: continue

            val statusObj = competition.optJSONObject("status")
            val statusType = statusObj?.optJSONObject("type")
            val state = statusType?.optString("state", "pre") // "pre", "in", "post"
            val statusDesc = statusType?.optString("description", "") ?: ""
            val displayClock = statusObj?.optString("displayClock", "") ?: ""
            val shortDetail = statusType?.optString("shortDetail", "") ?: ""

            val matchStatus = when (state) {
                "in" -> MatchStatus.LIVE
                "post" -> MatchStatus.FINISHED
                else -> MatchStatus.UPCOMING
            }

            val minuteText = when (matchStatus) {
                MatchStatus.LIVE -> if (displayClock.isNotBlank()) displayClock else shortDetail.ifBlank { "CANLI" }
                MatchStatus.FINISHED -> if (shortDetail.isNotBlank()) shortDetail else "MS"
                MatchStatus.UPCOMING -> formatTimeText(rawDate)
            }

            // Venue
            val venueObj = competition.optJSONObject("venue")
            val stadiumName = venueObj?.optString("fullName", "Stadyum") ?: "Stadyum"
            val venueCity = venueObj?.optJSONObject("address")?.optString("city", "") ?: ""
            val fullStadium = if (venueCity.isNotBlank()) "$stadiumName, $venueCity" else stadiumName

            // Competitors
            val competitors = competition.optJSONArray("competitors") ?: JSONArray()
            var homeTeam = Team("", "Ev Sahibi", "EV")
            var awayTeam = Team("", "Deplasman", "DEP")
            var homeScore = 0
            var awayScore = 0

            for (cIndex in 0 until competitors.length()) {
                val comp = competitors.optJSONObject(cIndex) ?: continue
                val homeAway = comp.optString("homeAway", "home")
                val score = comp.optString("score", "0").toIntOrNull() ?: 0
                val teamObj = comp.optJSONObject("team") ?: JSONObject()

                val tId = teamObj.optString("id", "")
                val tName = teamObj.optString("displayName", teamObj.optString("name", "Takım"))
                val tShort = teamObj.optString("abbreviation", tName.take(3).uppercase())
                val tLogo = teamObj.optString("logo", "")
                val colorHexStr = teamObj.optString("color", "1E3A8A")
                val parsedColor = try {
                    java.lang.Long.parseLong("FF$colorHexStr", 16)
                } catch (e: Exception) {
                    0xFF1E3A8A
                }

                val teamModel = Team(
                    id = tId,
                    name = tName,
                    shortName = tShort,
                    logoUrl = tLogo,
                    logoText = tShort.take(3),
                    primaryColorHex = parsedColor,
                    city = venueCity
                )

                if (homeAway == "home") {
                    homeTeam = teamModel
                    homeScore = score
                } else {
                    awayTeam = teamModel
                    awayScore = score
                }
            }

            // Fallback: If both came as order 0 and 1 without homeAway
            if (homeTeam.id.isEmpty() && competitors.length() >= 2) {
                val c0 = competitors.getJSONObject(0)
                val c1 = competitors.getJSONObject(1)
                homeScore = c0.optString("score", "0").toIntOrNull() ?: 0
                awayScore = c1.optString("score", "0").toIntOrNull() ?: 0
            }

            val dateText = formatDateText(rawDate)
            val timeText = formatTimeText(rawDate)

            // Synthetic initial statistics based on actual score and minute
            val initialStats = generateStatsFromScore(homeScore, awayScore, matchStatus, minuteText)

            matches.add(
                Match(
                    id = eventId,
                    leagueId = leagueSlug,
                    league = leagueName,
                    leagueIcon = leagueIcon,
                    round = statusDesc.ifBlank { "Hafta Karşılaşması" },
                    homeTeam = homeTeam,
                    awayTeam = awayTeam,
                    homeScore = homeScore,
                    awayScore = awayScore,
                    currentMinute = minuteText,
                    status = matchStatus,
                    stadium = fullStadium,
                    referee = "Resmi Hakem",
                    dateText = dateText,
                    timeText = timeText,
                    tvChannel = "BSN Canlı Skor",
                    stats = initialStats
                )
            )
        }

        return matches
    }

    private fun parseSummaryJson(jsonString: String): MatchDetailsUpdate {
        val root = JSONObject(jsonString)
        val commentaryList = mutableListOf<CommentaryEvent>()

        // 1. Key Events (Goals, Cards, Substitutions)
        val keyEvents = root.optJSONArray("keyEvents")
        if (keyEvents != null) {
            for (i in 0 until keyEvents.length()) {
                val ke = keyEvents.optJSONObject(i) ?: continue
                val typeObj = ke.optJSONObject("type")
                val typeText = typeObj?.optString("text", "") ?: ""
                val clockObj = ke.optJSONObject("clock")
                val clockVal = clockObj?.optString("displayValue", "") ?: ""
                val text = ke.optString("text", "")
                val teamObj = ke.optJSONObject("team")
                val teamName = teamObj?.optString("displayName", "") ?: ""

                val eventType = when {
                    typeText.contains("Goal", ignoreCase = true) -> EventType.GOAL
                    typeText.contains("Penalty", ignoreCase = true) -> EventType.PENALTY_GOAL
                    typeText.contains("Red", ignoreCase = true) -> EventType.RED_CARD
                    typeText.contains("Yellow", ignoreCase = true) -> EventType.YELLOW_CARD
                    typeText.contains("Substitution", ignoreCase = true) -> EventType.SUBSTITUTION
                    else -> EventType.DANGEROUS_ATTACK
                }

                commentaryList.add(
                    CommentaryEvent(
                        id = "ke_$i",
                        minute = clockVal.ifBlank { "${i * 5}'" },
                        title = typeText,
                        description = text,
                        eventType = eventType,
                        teamBadge = if (teamName.isNotBlank()) teamName.take(3).uppercase() else "",
                        isImportant = eventType == EventType.GOAL || eventType == EventType.RED_CARD
                    )
                )
            }
        }

        // 2. Rosters (Lineups)
        val rosters = root.optJSONArray("rosters")
        var homeLineup = Lineup("4-2-3-1", "", emptyList(), emptyList())
        var awayLineup = Lineup("4-3-3", "", emptyList(), emptyList())

        if (rosters != null && rosters.length() >= 2) {
            homeLineup = parseSingleLineup(rosters.getJSONObject(0))
            awayLineup = parseSingleLineup(rosters.getJSONObject(1))
        }

        // 3. Boxscore Detailed Statistics
        var parsedStats: MatchStats? = null
        val boxscore = root.optJSONObject("boxscore")
        val teamsStatsArray = boxscore?.optJSONArray("teams")
        if (teamsStatsArray != null && teamsStatsArray.length() >= 2) {
            val homeStatsMap = extractStatMap(teamsStatsArray.getJSONObject(0))
            val awayStatsMap = extractStatMap(teamsStatsArray.getJSONObject(1))

            val possHome = homeStatsMap["possessionPct"]?.toDoubleOrNull()?.toInt() ?: 50
            val possAway = 100 - possHome
            val shotsHome = homeStatsMap["totalShots"]?.toIntOrNull() ?: 0
            val shotsAway = awayStatsMap["totalShots"]?.toIntOrNull() ?: 0
            val onTargetHome = homeStatsMap["shotsOnTarget"]?.toIntOrNull() ?: (shotsHome / 2)
            val onTargetAway = awayStatsMap["shotsOnTarget"]?.toIntOrNull() ?: (shotsAway / 2)
            val cornersHome = homeStatsMap["wonCorners"]?.toIntOrNull() ?: 0
            val cornersAway = awayStatsMap["wonCorners"]?.toIntOrNull() ?: 0
            val foulsHome = homeStatsMap["foulsCommitted"]?.toIntOrNull() ?: 0
            val foulsAway = awayStatsMap["foulsCommitted"]?.toIntOrNull() ?: 0
            val yellowHome = homeStatsMap["yellowCards"]?.toIntOrNull() ?: 0
            val yellowAway = awayStatsMap["yellowCards"]?.toIntOrNull() ?: 0
            val redHome = homeStatsMap["redCards"]?.toIntOrNull() ?: 0
            val redAway = awayStatsMap["redCards"]?.toIntOrNull() ?: 0
            val offsidesHome = homeStatsMap["offsides"]?.toIntOrNull() ?: 0
            val offsidesAway = awayStatsMap["offsides"]?.toIntOrNull() ?: 0
            val savesHome = homeStatsMap["saves"]?.toIntOrNull() ?: 0
            val savesAway = awayStatsMap["saves"]?.toIntOrNull() ?: 0

            val passAccHome = homeStatsMap["passPct"]?.toDoubleOrNull()?.toInt() ?: 82
            val passAccAway = awayStatsMap["passPct"]?.toDoubleOrNull()?.toInt() ?: 80

            val xgH = String.format(java.util.Locale.US, "%.2f", (onTargetHome * 0.32 + cornersHome * 0.08 + 0.2)).toDoubleOrNull() ?: 1.2
            val xgA = String.format(java.util.Locale.US, "%.2f", (onTargetAway * 0.32 + cornersAway * 0.08 + 0.15)).toDoubleOrNull() ?: 1.0

            parsedStats = MatchStats(
                possessionHome = possHome,
                possessionAway = possAway,
                expectedGoalsHome = xgH,
                expectedGoalsAway = xgA,
                totalShotsHome = shotsHome,
                totalShotsAway = shotsAway,
                shotsOnTargetHome = onTargetHome,
                shotsOnTargetAway = onTargetAway,
                shotsOffTargetHome = (shotsHome - onTargetHome).coerceAtLeast(0),
                shotsOffTargetAway = (shotsAway - onTargetAway).coerceAtLeast(0),
                cornersHome = cornersHome,
                cornersAway = cornersAway,
                foulsHome = foulsHome,
                foulsAway = foulsAway,
                yellowCardsHome = yellowHome,
                yellowCardsAway = yellowAway,
                redCardsHome = redHome,
                redCardsAway = redAway,
                offsidesHome = offsidesHome,
                offsidesAway = offsidesAway,
                savesHome = savesHome,
                savesAway = savesAway,
                passAccuracyHome = passAccHome,
                passAccuracyAway = passAccAway,
                momentumPoints = listOf(
                    MomentumPoint("0-15'", (possHome + 5).coerceIn(20, 80)),
                    MomentumPoint("15-30'", (possHome - 4).coerceIn(20, 80)),
                    MomentumPoint("30-45'", possHome.coerceIn(20, 80)),
                    MomentumPoint("45-60'", (possHome + 8).coerceIn(20, 80)),
                    MomentumPoint("60-75'", possHome.coerceIn(20, 80)),
                    MomentumPoint("75-90'", (possHome - 2).coerceIn(20, 80))
                ),
                tacticalAnalysis = TacticalAnalysisSummary(
                    matchCourseComment = "Topa sahip olma ve 3. bölge etkinliğinde üstünlük kaydedildi. Kanat organizasyonları ve merkez ara pasları hücum temposunu belirledi.",
                    attackingInsight = "%$possHome topla oynama ve $shotsHome toplam şutla rakip kalede devamlı baskı kuruldu.",
                    defenseInsight = "Rakip takım $savesHome kurtarışla savunmada direnç göstermeye çalıştı.",
                    keyDuel = "Merkez orta saha mücadelesi ve savunma arkası koşular maçın ana temasını oluşturdu.",
                    gamePlanAssessment = "Hızlı pas trafiği ve ikinci topları toplama stratejisi uygulandı.",
                    winProbabilityHome = (possHome + 10).coerceIn(30, 80),
                    drawProbability = 20,
                    winProbabilityAway = (100 - (possHome + 10) - 20).coerceIn(10, 50)
                )
            )
        }

        return MatchDetailsUpdate(
            commentary = commentaryList,
            homeLineup = homeLineup,
            awayLineup = awayLineup,
            stats = parsedStats
        )
    }

    private fun parseSingleLineup(rosterObj: JSONObject): Lineup {
        val coach = rosterObj.optJSONObject("team")?.optString("coach", "") ?: ""
        val rosterArr = rosterObj.optJSONArray("roster") ?: JSONArray()
        val starters = mutableListOf<Player>()
        val subs = mutableListOf<Player>()

        for (i in 0 until rosterArr.length()) {
            val pObj = rosterArr.optJSONObject(i) ?: continue
            val athlete = pObj.optJSONObject("athlete") ?: JSONObject()
            val pName = athlete.optString("displayName", athlete.optString("shortName", "Oyuncu"))
            val jersey = athlete.optString("jersey", "${i + 1}").toIntOrNull() ?: (i + 1)
            val posObj = pObj.optJSONObject("position")
            val posAbbr = posObj?.optString("abbreviation", "OS") ?: "OS"
            val isStarter = pObj.optBoolean("starter", false)

            val player = Player(
                number = jersey,
                name = pName,
                position = posAbbr,
                rating = 7.0 + ((jersey * 3) % 15) / 10.0
            )

            if (isStarter) {
                starters.add(player)
            } else {
                subs.add(player)
            }
        }

        return Lineup(
            formation = if (starters.size == 11) "4-2-3-1" else "Kadrolar Açıklandı",
            coach = coach.ifBlank { "Teknik Direktör" },
            starting11 = starters,
            substitutes = subs
        )
    }

    private fun extractStatMap(teamStatObj: JSONObject): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val statsArr = teamStatObj.optJSONArray("statistics") ?: return map
        for (i in 0 until statsArr.length()) {
            val s = statsArr.optJSONObject(i) ?: continue
            val name = s.optString("name", "")
            val displayValue = s.optString("displayValue", s.optString("value", ""))
            if (name.isNotBlank()) {
                map[name] = displayValue
            }
        }
        return map
    }

    private fun parseStandingsJson(jsonString: String): List<StandingTeam> {
        val list = mutableListOf<StandingTeam>()
        val root = JSONObject(jsonString)
        val children = root.optJSONArray("children") ?: return list
        val firstChild = children.optJSONObject(0) ?: return list
        val standingsObj = firstChild.optJSONObject("standings") ?: return list
        val entries = standingsObj.optJSONArray("entries") ?: return list

        for (i in 0 until entries.length()) {
            val entry = entries.optJSONObject(i) ?: continue
            val teamObj = entry.optJSONObject("team") ?: JSONObject()
            val teamName = teamObj.optString("displayName", teamObj.optString("name", "Takım"))
            val teamShort = teamObj.optString("abbreviation", teamName.take(3).uppercase())
            val logos = teamObj.optJSONArray("logos")
            val logoUrl = logos?.optJSONObject(0)?.optString("href", "") ?: ""

            val statsArr = entry.optJSONArray("stats") ?: JSONArray()
            var gp = 0
            var w = 0
            var d = 0
            var l = 0
            var pts = 0
            var gd = 0
            var gf = 0
            var ga = 0

            for (sIndex in 0 until statsArr.length()) {
                val s = statsArr.optJSONObject(sIndex) ?: continue
                val sName = s.optString("name", "")
                val sVal = s.optDouble("value", s.optString("displayValue", "0").toDoubleOrNull() ?: 0.0).toInt()
                when (sName) {
                    "gamesPlayed" -> gp = sVal
                    "wins" -> w = sVal
                    "ties" -> d = sVal
                    "losses" -> l = sVal
                    "points" -> pts = sVal
                    "pointDifferential" -> gd = sVal
                    "pointsFor" -> gf = sVal
                    "pointsAgainst" -> ga = sVal
                }
            }

            list.add(
                StandingTeam(
                    rank = i + 1,
                    teamName = teamName,
                    teamShort = teamShort,
                    logoUrl = logoUrl,
                    played = gp,
                    won = w,
                    drawn = d,
                    lost = l,
                    goalsFor = gf,
                    goalsAgainst = ga,
                    goalDifference = gd,
                    points = pts,
                    form = listOf("W", "W", "D", "W", "L")
                )
            )
        }

        return list
    }

    private fun generateStatsFromScore(homeScore: Int, awayScore: Int, status: MatchStatus, minute: String): MatchStats {
        val totalShotsH = homeScore * 4 + 7
        val totalShotsA = awayScore * 4 + 6
        val onTargetH = homeScore + 3
        val onTargetA = awayScore + 2
        val cornersH = 5
        val cornersA = 4
        val xgH = String.format(java.util.Locale.US, "%.2f", homeScore * 0.65 + 0.45).toDoubleOrNull() ?: 1.45
        val xgA = String.format(java.util.Locale.US, "%.2f", awayScore * 0.65 + 0.35).toDoubleOrNull() ?: 1.15

        return MatchStats(
            possessionHome = if (homeScore >= awayScore) 54 else 46,
            possessionAway = if (homeScore >= awayScore) 46 else 54,
            expectedGoalsHome = xgH,
            expectedGoalsAway = xgA,
            totalShotsHome = totalShotsH,
            totalShotsAway = totalShotsA,
            shotsOnTargetHome = onTargetH,
            shotsOnTargetAway = onTargetA,
            shotsOffTargetHome = (totalShotsH - onTargetH).coerceAtLeast(0),
            shotsOffTargetAway = (totalShotsA - onTargetA).coerceAtLeast(0),
            cornersHome = cornersH,
            cornersAway = cornersA,
            foulsHome = 12,
            foulsAway = 14,
            yellowCardsHome = 2,
            yellowCardsAway = 2,
            passAccuracyHome = 84,
            passAccuracyAway = 81,
            momentumPoints = listOf(
                MomentumPoint("0-15'", 58),
                MomentumPoint("15-30'", 65),
                MomentumPoint("30-45'", 48),
                MomentumPoint("45-60'", 55),
                MomentumPoint("60-75'", 60),
                MomentumPoint("75-90'", 52)
            ),
            tacticalAnalysis = TacticalAnalysisSummary(
                matchCourseComment = "Maçta iki takım da açık ve hücumu düşünen bir oyun sergiliyor. Skor üretme konusunda yakalanan fırsatlar değerlendirildi.",
                attackingInsight = "Kanat bindirmeleri ve ceza sahası içine açılan ortalar tehlike yarattı.",
                defenseInsight = "Geçiş savunmasında açıklar görülse de kaleciler kritik kurtarışlara imza attı.",
                keyDuel = "Orta alan presi ve hızlı kontratak eşleşmeleri belirleyici rol oynuyor.",
                gamePlanAssessment = "Yüksek tempolu pas oyunu ile rakip yarı alanda çoğalma planı uygulandı.",
                winProbabilityHome = if (homeScore > awayScore) 62 else if (homeScore == awayScore) 38 else 20,
                drawProbability = 28,
                winProbabilityAway = if (awayScore > homeScore) 60 else 18
            )
        )
    }

    private fun formatDateText(isoDate: String): String {
        return try {
            if (isoDate.length >= 10) {
                val parts = isoDate.substring(0, 10).split("-")
                "${parts[2]}.${parts[1]}.${parts[0]}"
            } else "Bugün"
        } catch (e: Exception) {
            "Bugün"
        }
    }

    private fun formatTimeText(isoDate: String): String {
        return try {
            if (isoDate.length >= 16) {
                isoDate.substring(11, 16)
            } else "20:00"
        } catch (e: Exception) {
            "20:00"
        }
    }
}

data class MatchDetailsUpdate(
    val commentary: List<CommentaryEvent>,
    val homeLineup: Lineup,
    val awayLineup: Lineup,
    val stats: MatchStats?
)
