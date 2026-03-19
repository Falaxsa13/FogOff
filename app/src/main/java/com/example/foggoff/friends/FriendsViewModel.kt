package com.example.foggoff.friends

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foggoff.data.LeaderboardEntry
import com.example.foggoff.data.LeaderboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FriendsUiState(
    val loading: Boolean = true,
    val top20: List<LeaderboardEntry> = emptyList(),
    val currentUserRank: Int? = null,
    val currentUserEntry: LeaderboardEntry? = null,
    val errorMessage: String? = null,
)

class FriendsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LeaderboardRepository()

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(loading = true, errorMessage = null)
        viewModelScope.launch {
            val result = repository.loadGlobalLeaderboardTop20WithCurrentRank()
            _uiState.value = FriendsUiState(
                loading = false,
                top20 = result.top20,
                currentUserRank = result.currentUserRank,
                currentUserEntry = result.currentUserEntry,
                errorMessage = if (result.top20.isEmpty()) "No leaderboard data yet." else null,
            )
        }
    }
}
