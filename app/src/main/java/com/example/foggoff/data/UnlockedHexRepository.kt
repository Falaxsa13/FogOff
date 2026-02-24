package com.example.foggoff.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val COLLECTION_USERS = "users"
private const val FIELD_UNLOCKED_H3_IDS = "unlockedH3Ids"

/**
 * Persists and loads unlocked H3 hex IDs in Firestore.
 * Uses anonymous auth so each device has a stable user document.
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
            list.filterIsInstance<String>().toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    suspend fun addUnlockedH3Ids(ids: Set<String>) {
        if (ids.isEmpty()) return
        try {
            val uid = ensureSignedIn() ?: return
            val ref = firestore.collection(COLLECTION_USERS).document(uid)
            val snapshot = ref.get().await()
            val list = ids.toList()
            if (!snapshot.exists()) {
                ref.set(mapOf(FIELD_UNLOCKED_H3_IDS to list)).await()
            } else {
                ref.update(FIELD_UNLOCKED_H3_IDS, FieldValue.arrayUnion(*list.toTypedArray())).await()
            }
        } catch (e: Exception) {
            // Firestore or auth failed; fail silently
        }
    }

    private suspend fun ensureSignedIn(): String? {
        val current = auth.currentUser
        if (current != null) return current.uid
        return try {
            val result = auth.signInAnonymously().await()
            result.user?.uid
        } catch (e: Exception) {
            null
        }
    }
}
