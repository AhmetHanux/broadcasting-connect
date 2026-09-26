package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FavoriteMatchEntity
import com.example.data.model.*
import com.example.data.remote.EspnSportsApi
import com.example.data.repository.MatchRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FloatingEmoji(
    val id: Long = System.currentTimeMillis(),
    val emoji: String,
    val startXFraction: Float
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MatchRepository.getInstance(application)

    val matches: StateFlow<List<Match>> = repository.matches
    val isLoading: StateFlow<Boolean> = repository.isLoading
    val activeLeagueId: StateFlow<String> = repository.activeLeagueId
    val standings: StateFlow<List<StandingTeam>> = repository.standings

    val supportedLeagues = EspnSportsApi.SUPPORTED_LEAGUES

    private val _statusFilter = MutableStateFlow("Tümü") // "Tümü", "Canlı", "Biten", "Gelecek"
    val statusFilter = _statusFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedMatch = MutableStateFlow<Match?>(null)
    val selectedMatch = _selectedMatch.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    private val _floatingReactions = MutableStateFlow<List<FloatingEmoji>>(emptyList())
    val floatingReactions = _floatingReactions.asStateFlow()

    val favorites: StateFlow<List<FavoriteMatchEntity>> = repository.favoriteMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var chatObservingJob: Job? = null

    fun selectLeague(leagueSlug: String) {
        repository.setLeague(leagueSlug)
    }

    fun refreshMatches() {
        repository.refreshCurrentLeagueMatches()
    }

    fun setStatusFilter(filter: String) {
        _statusFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectMatch(match: Match) {
        _selectedMatch.value = match

        // Bind chat stream
        chatObservingJob?.cancel()
        chatObservingJob = viewModelScope.launch {
            repository.getChatMessages(match.id).collect { msgs ->
                _chatMessages.value = msgs
            }
        }

        // Fetch deep details (real statistics, rosters, commentary) from ESPN
        repository.loadMatchDetails(match) { updated ->
            if (_selectedMatch.value?.id == updated.id) {
                _selectedMatch.value = updated
            }
        }
    }

    fun sendChatMessage(text: String, userBadge: String = "⚽") {
        val currentMatch = _selectedMatch.value ?: return
        if (text.isBlank()) return
        repository.sendChatMessage(currentMatch.id, "Sen", text.trim(), userBadge)
    }

    fun triggerReaction(emoji: String) {
        val randomX = (20..80).random() / 100f
        val newEmoji = FloatingEmoji(emoji = emoji, startXFraction = randomX)
        _floatingReactions.value = _floatingReactions.value + newEmoji
    }

    fun toggleFavorite(match: Match) {
        viewModelScope.launch {
            val isFav = favorites.value.any { it.matchId == match.id }
            repository.toggleFavorite(match, isFav)
        }
    }

    fun isMatchFavorited(matchId: String): Boolean {
        return favorites.value.any { it.matchId == matchId }
    }
}
