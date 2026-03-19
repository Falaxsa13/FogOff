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
    // MOCK DATA: set to `false` to use the real Firebase leaderboard.
    private val useMockLeaderboard: Boolean = true

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(loading = true, errorMessage = null)
        viewModelScope.launch {
            if (useMockLeaderboard) {
                val top20 = listOf(
                    LeaderboardEntry(uid = "u_01_nova", displayName = "Nova", unlockedHexes = 420),
                    LeaderboardEntry(uid = "u_02_ember", displayName = "Ember", unlockedHexes = 389),
                    LeaderboardEntry(uid = "u_03_kyra", displayName = "Kyra", unlockedHexes = 361),
                    LeaderboardEntry(uid = "u_04_ora", displayName = "Ora", unlockedHexes = 345),
                    LeaderboardEntry(uid = "u_05_milo", displayName = "Milo", unlockedHexes = 332),
                    LeaderboardEntry(uid = "u_06_ivy", displayName = "Ivy", unlockedHexes = 318),
                    LeaderboardEntry(uid = "u_07_sage", displayName = "Sage", unlockedHexes = 305),
                    LeaderboardEntry(uid = "u_08_rhea", displayName = "Rhea", unlockedHexes = 292),
                    LeaderboardEntry(uid = "u_09_cleo", displayName = "Cleo", unlockedHexes = 279),
                    LeaderboardEntry(uid = "u_10_zen", displayName = "Zen", unlockedHexes = 268),
                    LeaderboardEntry(uid = "u_11_luna", displayName = "Luna", unlockedHexes = 252),
                    LeaderboardEntry(uid = "u_12_kai", displayName = "Kai", unlockedHexes = 241),
                    LeaderboardEntry(uid = "u_13_mara", displayName = "Mara", unlockedHexes = 229),
                    LeaderboardEntry(uid = "u_14_otto", displayName = "Otto", unlockedHexes = 214),
                    LeaderboardEntry(uid = "u_15_faye", displayName = "Faye", unlockedHexes = 203),
                    LeaderboardEntry(uid = "u_16_sol", displayName = "Sol", unlockedHexes = 198),
                    LeaderboardEntry(uid = "u_17_aria", displayName = "Aria", unlockedHexes = 187),
                    LeaderboardEntry(uid = "u_18_zaid", displayName = "Zaid", unlockedHexes = 176),
                    LeaderboardEntry(uid = "u_19_noor", displayName = "Noor", unlockedHexes = 162),
                    LeaderboardEntry(uid = "u_20_rowan", displayName = "Rowan", unlockedHexes = 149),
                )

                // Place "You" outside Top 20 to ensure the “Your rank” row shows correctly.
                val myEntry = LeaderboardEntry(
                    uid = "mock_you",
                    displayName = "You",
                    unlockedHexes = 141,
                )
                val myRank = 27

                _uiState.value = FriendsUiState(
                    loading = false,
                    top20 = top20,
                    currentUserRank = myRank,
                    currentUserEntry = myEntry,
                    errorMessage = null,
                )
                return@launch
            }

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
