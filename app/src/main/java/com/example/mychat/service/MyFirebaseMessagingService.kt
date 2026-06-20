package com.example.mychat.service

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mychat.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    /**
     * Called when a new FCM token is generated (app install, token refresh, etc.)
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        android.util.Log.d(TAG, "New FCM token received: $token")

        // Save the token to Firestore
        saveTokenToFirestore(token)
    }

    /**
     * Called when a message is received from FCM
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        android.util.Log.d(TAG, "FCM message received from: ${remoteMessage.from}")

        // Check if message contains a notification payload (shown automatically when app is in background)
        remoteMessage.notification?.let {
            android.util.Log.d(TAG, "Notification body: ${it.body}")
        }

        // Check if message contains data payload
        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            android.util.Log.d(TAG, "Message data payload: $data")
            handleDataMessage(data)
        }
    }

    /**
     * Handle data messages from FCM
     * Data payload fields:
     *   - type: "new_message"
     *   - senderId: userId of the sender
     *   - senderName: display name of the sender
     *   - message: text content of the message
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"] ?: return
        android.util.Log.d(TAG, "Handling data message of type: $type")

        when (type) {
            "new_message" -> {
                val senderName = data["senderName"] ?: "Unknown"
                val messageContent = data["message"] ?: ""
                val senderId = data["senderId"] ?: ""

                if (messageContent.isNotEmpty()) {
                    showNotification(senderName, messageContent, senderId)
                }
            }
            "new_booking" -> {
                val title = "New Booking"
                val message = data["message"] ?: "You have a new booking request"
                showNotification(title, message, "")
            }
            else -> {
                android.util.Log.d(TAG, "Unknown message type: $type")
            }
        }
    }

    /**
     * Show a notification for the received message
     */
    private fun showNotification(title: String, message: String, senderUserId: String) {
        // Create notification channel first
        NotificationHelper.createNotificationChannel(this)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_chat", true)
            putExtra("chat_user_id", senderUserId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "chat_messages")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()

        try {
            NotificationManagerCompat.from(this).notify(1001, notification)
            android.util.Log.d(TAG, "Notification shown: $title - $message")
        } catch (e: SecurityException) {
            android.util.Log.w(TAG, "Notification permission not granted: ${e.message}")
        }
    }

    /**
     * Save the FCM token to Firestore using FCMTokenRepository
     */
    private fun saveTokenToFirestore(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tokenRepo = com.example.mychat.data.repository.FCMTokenRepository()
                tokenRepo.saveToken(token)
                android.util.Log.d(TAG, "FCM token saved to Firestore successfully")
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error saving FCM token to Firestore: ${e.message}")
            }
        }
    }

    /**
     * Called when the Firebase SDK detects a new FCM token
     * This handles the initial token generation and refresh
     */
    fun registerFCMToken() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                android.util.Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            android.util.Log.d(TAG, "FCM token fetched: $token")
            saveTokenToFirestore(token)
        }
    }
}