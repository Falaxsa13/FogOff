package com.example.foggoff.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val LEADERBOARD_COLLECTION_USERS = "users"
private const val LEADERBOARD_FIELD_UNLOCKED_H3_IDS = "unlockedH3Ids"
private const val LEADERBOARD_FIELD_DISPLAY_NAME = "displayName"

data class LeaderboardEntry(
    val uid: String,
    val displayName: String,
    val unlockedHexes: Int,
)

data class LeaderboardResult(
    val top20: List<LeaderboardEntry>,
    val currentUserRank: Int?,
    val currentUserEntry: LeaderboardEntry?,
)

class LeaderboardRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {

    suspend fun loadGlobalLeaderboardTop20WithCurrentRank(): LeaderboardResult {
        val currentUid = auth.currentUser?.uid
        val currentAuthName = auth.currentUser?.displayName?.trim().orEmpty()
        val fallbackCurrentName = currentAuthName.ifBlank { "You" }

        return try {
            val snapshot = firestore.collection(LEADERBOARD_COLLECTION_USERS).get().await()
            val entries = snapshot.documents.map { doc ->
                val unlockedIds = (doc.get(LEADERBOARD_FIELD_UNLOCKED_H3_IDS) as? List<*>)
                    ?.filterIsInstance<String>()
                    .orEmpty()
                val displayName = doc.getString(LEADERBOARD_FIELD_DISPLAY_NAME)
                    ?.trim()
                    .orEmpty()
                    .ifBlank { "Explorer ${doc.id.take(6)}" }
                LeaderboardEntry(
                    uid = doc.id,
                    displayName = displayName,
                    unlockedHexes = unlockedIds.size,
                )
            }.toMutableList()

            if (currentUid != null && entries.none { it.uid == currentUid }) {
                entries += LeaderboardEntry(
                    uid = currentUid,
                    displayName = fallbackCurrentName,
                    unlockedHexes = 0,
                )
            }

            val ranked = entries
                .sortedWith(
                    compareByDescending<LeaderboardEntry> { it.unlockedHexes }
                        .thenBy { it.displayName.lowercase() }
                        .thenBy { it.uid }
                )

            val currentRank = if (currentUid == null) null else ranked.indexOfFirst { it.uid == currentUid }
                .takeIf { it >= 0 }
                ?.plus(1)
            val currentEntry = if (currentUid == null) null else ranked.firstOrNull { it.uid == currentUid }

            LeaderboardResult(
                top20 = ranked.take(20),
                currentUserRank = currentRank,
                currentUserEntry = currentEntry,
            )
        } catch (_: Exception) {
            LeaderboardResult(
                top20 = emptyList(),
                currentUserRank = null,
                currentUserEntry = null,
            )
        }
    }
}
