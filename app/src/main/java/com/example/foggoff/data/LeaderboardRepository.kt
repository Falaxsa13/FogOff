package com.example.foggoff.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

private const val LEADERBOARD_COLLECTION_USERS = "users"
private const val LEADERBOARD_FIELD_UNLOCKED_H3_IDS = "unlockedH3Ids"
private const val LEADERBOARD_FIELD_UNLOCKED_HEX_COUNT = "unlockedHexCount"
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
            val topSnapshot = firestore.collection(LEADERBOARD_COLLECTION_USERS)
                .orderBy(LEADERBOARD_FIELD_UNLOCKED_HEX_COUNT, Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            val topEntries = topSnapshot.documents.map { doc ->
                val unlockedHexes = doc.getLong(LEADERBOARD_FIELD_UNLOCKED_HEX_COUNT)?.toInt()
                    ?: (doc.get(LEADERBOARD_FIELD_UNLOCKED_H3_IDS) as? List<*>)?.size
                    ?: 0
                val displayName = doc.getString(LEADERBOARD_FIELD_DISPLAY_NAME)
                    ?.trim()
                    .orEmpty()
                    .ifBlank { "Explorer ${doc.id.take(6)}" }
                LeaderboardEntry(
                    uid = doc.id,
                    displayName = displayName,
                    unlockedHexes = unlockedHexes,
                )
            }

            var currentEntry: LeaderboardEntry? = null
            var currentRank: Int? = null
            if (currentUid != null) {
                val userDoc = firestore.collection(LEADERBOARD_COLLECTION_USERS)
                    .document(currentUid)
                    .get()
                    .await()
                val currentHexCount = userDoc.getLong(LEADERBOARD_FIELD_UNLOCKED_HEX_COUNT)?.toInt()
                    ?: (userDoc.get(LEADERBOARD_FIELD_UNLOCKED_H3_IDS) as? List<*>)?.size
                    ?: 0
                val currentDisplayName = userDoc.getString(LEADERBOARD_FIELD_DISPLAY_NAME)
                    ?.trim()
                    .orEmpty()
                    .ifBlank { fallbackCurrentName }

                currentEntry = LeaderboardEntry(
                    uid = currentUid,
                    displayName = currentDisplayName,
                    unlockedHexes = currentHexCount,
                )

                val higherCount = firestore.collection(LEADERBOARD_COLLECTION_USERS)
                    .whereGreaterThan(LEADERBOARD_FIELD_UNLOCKED_HEX_COUNT, currentHexCount)
                    .count()
                    .get(AggregateSource.SERVER)
                    .await()
                    .count
                currentRank = higherCount.toInt() + 1
            }

            LeaderboardResult(
                top20 = topEntries,
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
