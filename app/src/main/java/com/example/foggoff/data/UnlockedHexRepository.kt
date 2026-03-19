package com.example.foggoff.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val COLLECTION_USERS = "users"
private const val FIELD_UNLOCKED_H3_IDS = "unlockedH3Ids"
private const val FIELD_UNLOCKED_HEX_COUNT = "unlockedHexCount"
private const val FIELD_UNLOCKED_COUNTRIES = "unlockedCountries"
private const val FIELD_DISPLAY_NAME = "displayName"

/**
 * Persists and loads unlocked H3 hex IDs in Firestore.
 * Requires the user to be signed in (e.g. via Google Sign-In).
 */
class UnlockedHexRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {

    suspend fun loadUnlockedH3Ids(): Set<String> {
        return try {
            val uid = ensureSignedIn() ?: return emptySet()
            val doc = firestore.collection(COLLECTION_USERS).document(uid).get().await()
            val list = doc.get(FIELD_UNLOCKED_H3_IDS) as? List<*> ?: return emptySet()
            val ids = list.filterIsInstance<String>().toSet()
            // Best-effort migration for leaderboard performance.
            try {
                firestore.collection(COLLECTION_USERS).document(uid)
                    .update(FIELD_UNLOCKED_HEX_COUNT, ids.size)
                    .await()
            } catch (_: Exception) {
                // Ignore migration failures; caller still gets loaded ids.
            }
            ids
        } catch (e: Exception) {
            emptySet()
        }
    }

    suspend fun addUnlockedH3Ids(ids: Set<String>) {
        addUnlockedH3Ids(ids = ids, unlockedCountryCodes = emptySet())
    }

    suspend fun addUnlockedH3Ids(
        ids: Set<String>,
        unlockedCountryCodes: Set<String>,
    ) {
        if (ids.isEmpty() && unlockedCountryCodes.isEmpty()) return
        try {
            val uid = ensureSignedIn() ?: return
            val ref = firestore.collection(COLLECTION_USERS).document(uid)
            val snapshot = ref.get().await()
            val list = ids.toList()
            val countries = unlockedCountryCodes.map { it.trim() }
                .filter { it.isNotBlank() }
                .toSet()
                .toList()
            val displayName = auth.currentUser?.displayName?.trim().orEmpty()
            val existingIds = (snapshot.get(FIELD_UNLOCKED_H3_IDS) as? List<*>)
                ?.filterIsInstance<String>()
                ?.toSet()
                .orEmpty()
            val newTotalCount = (existingIds + ids).size
            if (!snapshot.exists()) {
                val payload = mutableMapOf<String, Any>(
                    FIELD_UNLOCKED_H3_IDS to list,
                    FIELD_UNLOCKED_HEX_COUNT to list.size,
                )
                if (countries.isNotEmpty()) {
                    payload[FIELD_UNLOCKED_COUNTRIES] = countries
                }
                if (displayName.isNotBlank()) {
                    payload[FIELD_DISPLAY_NAME] = displayName
                }
                ref.set(payload).await()
            } else {
                val updatePayload = mutableMapOf<String, Any>(
                    FIELD_UNLOCKED_H3_IDS to FieldValue.arrayUnion(*list.toTypedArray()),
                    FIELD_UNLOCKED_HEX_COUNT to newTotalCount,
                )
                if (countries.isNotEmpty()) {
                    updatePayload[FIELD_UNLOCKED_COUNTRIES] =
                        FieldValue.arrayUnion(*countries.toTypedArray())
                }
                if (displayName.isNotBlank()) {
                    updatePayload[FIELD_DISPLAY_NAME] = displayName
                }
                ref.update(updatePayload).await()
            }
        } catch (e: Exception) {
            // Firestore or auth failed; fail silently
        }
    }

    suspend fun loadUnlockedCountryCodes(): List<String> {
        return try {
            val uid = ensureSignedIn() ?: return emptyList()
            val doc = firestore.collection(COLLECTION_USERS).document(uid).get().await()
            val list = doc.get(FIELD_UNLOCKED_COUNTRIES) as? List<*> ?: return emptyList()
            list.filterIsInstance<String>().map { it.trim() }.filter { it.isNotBlank() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun ensureSignedIn(): String? = auth.currentUser?.uid
}
