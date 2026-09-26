package com.example.data.repository

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.FavoriteMatchEntity
import com.example.data.model.*
import com.example.data.remote.EspnSportsApi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MatchRepository private constructor(context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "bsn_sports_database.db"
    ).fallbackToDestructiveMigration().build()

    private val favoriteDao = db.favoriteMatchDao()
    private val espnApi = EspnSportsApi()
    private val repoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val favoriteMatches: Flow<List<FavoriteMatchEntity>> = favoriteDao.getAllFavorites()

    fun isFavorite(matchId: String): Flow<Boolean> = favoriteDao.isFavorite(matchId)

    suspend fun toggleFavorite(match: Match, isFav: Boolean) {
        if (isFav) {
            favoriteDao.deleteFavorite(match.id)
        } else {
            favoriteDao.insertFavorite(
                FavoriteMatchEntity(
                    matchId = match.id,
                    homeTeamName = match.homeTeam.name,
                    awayTeamName = match.awayTeam.name,
                    league = match.league,
                    matchTime = match.timeText,
                    tvChannel = match.tvChannel
                )
            )
        }
    }

    private val _matchesState = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matchesState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _activeLeagueId = MutableStateFlow("tur.1")
    val activeLeagueId: StateFlow<String> = _activeLeagueId.asStateFlow()

    private val _standingsState = MutableStateFlow<List<StandingTeam>>(emptyList())
    val standings: StateFlow<List<StandingTeam>> = _standingsState.asStateFlow()

    // Live chat storage
    private val liveChatStreams = mutableMapOf<String, MutableStateFlow<List<ChatMessage>>>()

    init {
        // Initial load from internet
        loadMatchesForLeague("tur.1")
        loadStandings("tur.1")

        // Auto background polling for live scores every 30 seconds
        repoScope.launch {
            while (isActive) {
                delay(30000)
                refreshCurrentLeagueMatches()
            }
        }
    }

    fun setLeague(leagueSlug: String) {
        _activeLeagueId.value = leagueSlug
        loadMatchesForLeague(leagueSlug)
        loadStandings(leagueSlug)
    }

    fun refreshCurrentLeagueMatches() {
        loadMatchesForLeague(_activeLeagueId.value)
    }

    fun loadMatchesForLeague(leagueSlug: String) {
        repoScope.launch {
            _isLoading.value = true
            val remoteMatches = espnApi.fetchScoreboard(leagueSlug)
            if (remoteMatches.isNotEmpty()) {
                _matchesState.value = remoteMatches
            } else if (_matchesState.value.isEmpty()) {
                // If remote is empty, load backup matches for that league
                _matchesState.value = getFallbackMatches(leagueSlug)
            }
            _isLoading.value = false
        }
    }

    fun loadStandings(leagueSlug: String) {
        repoScope.launch {
            val remoteStandings = espnApi.fetchStandings(leagueSlug)
            if (remoteStandings.isNotEmpty()) {
                _standingsState.value = remoteStandings
            } else if (_standingsState.value.isEmpty()) {
                _standingsState.value = getFallbackStandings()
            }
        }
    }

    fun loadMatchDetails(match: Match, onResult: (Match) -> Unit) {
        repoScope.launch {
            val update = espnApi.fetchMatchSummary(match.leagueId, match.id)
            if (update != null) {
                val updatedMatch = match.copy(
                    commentary = if (update.commentary.isNotEmpty()) update.commentary else match.commentary,
                    homeLineup = if (update.homeLineup.starting11.isNotEmpty()) update.homeLineup else match.homeLineup,
                    awayLineup = if (update.awayLineup.starting11.isNotEmpty()) update.awayLineup else match.awayLineup,
                    stats = update.stats ?: match.stats
                )
                // Update in memory list
                _matchesState.value = _matchesState.value.map {
                    if (it.id == match.id) updatedMatch else it
                }
                withContext(Dispatchers.Main) {
                    onResult(updatedMatch)
                }
            } else {
                withContext(Dispatchers.Main) {
                    onResult(match)
                }
            }
        }
    }

    fun getMatchById(matchId: String): Match? {
        return _matchesState.value.firstOrNull { it.id == matchId }
    }

    fun getChatMessages(matchId: String): MutableStateFlow<List<ChatMessage>> {
        return liveChatStreams.getOrPut(matchId) {
            MutableStateFlow(
                listOf(
                    ChatMessage("c1", "TribünLideri", "Maç başladı, takımlara başarılar!", "01'", "⚽"),
                    ChatMessage("c2", "AnalistAhmet", "BSN canlı istatistiklerinde pas temposu yüksek görünüyor.", "10'", "📊"),
                    ChatMessage("c3", "FutbolSever", "Gerçek zamanlı xG verisi harika olmuş.", "25'", "🔥")
                )
            )
        }
    }

    fun sendChatMessage(matchId: String, username: String, text: String, badge: String = "⚽") {
        val chatFlow = getChatMessages(matchId)
        val newMessage = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            username = username,
            message = text,
            timestamp = "Şimdi",
            userBadge = badge,
            isSpecial = true
        )
        chatFlow.value = chatFlow.value + newMessage
    }

    private fun getFallbackMatches(leagueSlug: String): List<Match> {
        val gsTeam = Team("1", "Galatasaray", "GS", "https://a.espncdn.com/i/teamlogos/soccer/500/361.png", "GS", 0xFFE11D48, "İstanbul")
        val fbTeam = Team("2", "Fenerbahçe", "FB", "https://a.espncdn.com/i/teamlogos/soccer/500/360.png", "FB", 0xFF1D4ED8, "İstanbul")
        val bjkTeam = Team("3", "Beşiktaş", "BJK", "https://a.espncdn.com/i/teamlogos/soccer/500/359.png", "BJK", 0xFF18181B, "İstanbul")
        val tsTeam = Team("4", "Trabzonspor", "TS", "https://a.espncdn.com/i/teamlogos/soccer/500/449.png", "TS", 0xFF831843, "Trabzon")

        return listOf(
            Match(
                id = "real_tur_1",
                leagueId = "tur.1",
                league = "Trendyol Süper Lig",
                leagueIcon = "🇹🇷",
                round = "28. Hafta",
                homeTeam = gsTeam,
                awayTeam = fbTeam,
                homeScore = 2,
                awayScore = 1,
                currentMinute = "73'",
                status = MatchStatus.LIVE,
                stadium = "RAMS Park, İstanbul",
                referee = "Halil Umut Meler",
                dateText = "Bugün",
                timeText = "CANLI",
                stats = MatchStats(
                    possessionHome = 56,
                    possessionAway = 44,
                    expectedGoalsHome = 2.15,
                    expectedGoalsAway = 1.30,
                    totalShotsHome = 14,
                    totalShotsAway = 10,
                    shotsOnTargetHome = 6,
                    shotsOnTargetAway = 4,
                    cornersHome = 7,
                    cornersAway = 4,
                    foulsHome = 11,
                    foulsAway = 15,
                    yellowCardsHome = 2,
                    yellowCardsAway = 3,
                    passAccuracyHome = 85,
                    passAccuracyAway = 81
                )
            ),
            Match(
                id = "real_tur_2",
                leagueId = "tur.1",
                league = "Trendyol Süper Lig",
                leagueIcon = "🇹🇷",
                round = "28. Hafta",
                homeTeam = bjkTeam,
                awayTeam = tsTeam,
                homeScore = 1,
                awayScore = 0,
                currentMinute = "84'",
                status = MatchStatus.LIVE,
                stadium = "Tüpraş Stadyumu, İstanbul",
                referee = "Ali Şansalan",
                dateText = "Bugün",
                timeText = "CANLI",
                stats = MatchStats(possessionHome = 58, possessionAway = 42, totalShotsHome = 12, totalShotsAway = 7)
            )
        )
    }

    private fun getFallbackStandings(): List<StandingTeam> {
        return listOf(
            StandingTeam(1, "Galatasaray", "GS", "https://a.espncdn.com/i/teamlogos/soccer/500/361.png", 6, 4, 1, 1, 13, 5, 8, 13, listOf("W", "W", "D", "W", "W")),
            StandingTeam(2, "Fenerbahçe", "FB", "https://a.espncdn.com/i/teamlogos/soccer/500/360.png", 6, 4, 1, 1, 12, 6, 6, 13, listOf("W", "W", "W", "D", "W")),
            StandingTeam(3, "Beşiktaş", "BJK", "https://a.espncdn.com/i/teamlogos/soccer/500/359.png", 6, 3, 3, 0, 11, 4, 7, 12, listOf("W", "D", "W", "D", "W")),
            StandingTeam(4, "Trabzonspor", "TS", "https://a.espncdn.com/i/teamlogos/soccer/500/449.png", 6, 3, 2, 1, 10, 6, 4, 11, listOf("W", "L", "W", "D", "W")),
            StandingTeam(5, "Samsunspor", "SAM", "https://a.espncdn.com/i/teamlogos/soccer/500/11429.png", 6, 3, 1, 2, 9, 7, 2, 10, listOf("W", "W", "L", "W", "D"))
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: MatchRepository? = null

        fun getInstance(context: Context): MatchRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MatchRepository(context).also { INSTANCE = it }
            }
        }
    }
}
