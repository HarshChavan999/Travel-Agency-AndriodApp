package com.example.mychat.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FCMTokenRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /**
     * Save the FCM token to the user's document in Firestore
     */
    suspend fun saveToken(token: String) {
        val currentUser = firebaseAuth.currentUser ?: return
        val userId = currentUser.uid

        try {
            // Save token to the user's document in the users collection
            firestore.collection("users")
                .document(userId)
                .set(mapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
                .await()

            // Also save in a separate fcm_tokens collection for easier querying
            // from Cloud Functions
            val tokenData = hashMapOf(
                "userId" to userId,
                "token" to token,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("fcm_tokens")
                .document(userId)
                .set(tokenData)
                .await()

            android.util.Log.d("FCMTokenRepo", "FCM token saved successfully for user: $userId")
        } catch (e: Exception) {
            android.util.Log.e("FCMTokenRepo", "Error saving FCM token: ${e.message}")
        }
    }

    /**
     * Remove the FCM token when user signs out
     */
    suspend fun removeToken() {
        val currentUser = firebaseAuth.currentUser ?: return
        val userId = currentUser.uid

        try {
            // Remove token from user document
            firestore.collection("users")
                .document(userId)
                .update("fcmToken", null)
                .await()
        } catch (e: Exception) {
            android.util.Log.e("FCMTokenRepo", "Error removing FCM token: ${e.message}")
        }

        try {
            // Delete from fcm_tokens collection
            firestore.collection("fcm_tokens")
                .document(userId)
                .delete()
                .await()
        } catch (e: Exception) {
            android.util.Log.e("FCMTokenRepo", "Error deleting FCM token document: ${e.message}")
        }
    }

    /**
     * Get the FCM token for a specific user
     */
    suspend fun getTokenForUser(userId: String): String? {
        return try {
            val doc = firestore.collection("fcm_tokens")
                .document(userId)
                .get()
                .await()

            doc.getString("token")
        } catch (e: Exception) {
            android.util.Log.e("FCMTokenRepo", "Error getting FCM token for user $userId: ${e.message}")
            null
        }
    }
}