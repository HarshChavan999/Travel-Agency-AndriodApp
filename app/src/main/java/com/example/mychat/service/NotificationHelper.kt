package com.example.mychat.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mychat.MainActivity

object NotificationHelper {
    private const val CHANNEL_ID = "chat_messages"
    private const val CHANNEL_NAME = "Chat Messages"
    private const val CHANNEL_DESC = "Notifications for new messages from travel agencies"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            android.util.Log.d("NotificationHelper", "Notification channel created: $CHANNEL_ID")
        }
    }

    fun showMessageNotification(
        context: Context,
        senderName: String,
        messageContent: String,
        senderUserId: String,
        senderAvatarUrl: String = ""
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_chat", true)
            putExtra("chat_user_id", senderUserId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        var bitmap: android.graphics.Bitmap? = null

        // Load avatar bitmap if URL is provided
        if (senderAvatarUrl.isNotEmpty()) {
            try {
                val url = java.net.URL(senderAvatarUrl)
                val connection = url.openConnection()
                connection.doInput = true
                connection.connect()
                val inputStream = connection.getInputStream()
                bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream.close()
            } catch (e: Exception) {
                android.util.Log.w("NotificationHelper", "Failed to load large icon: ${e.message}")
            }
        }

        // Build notification using MessagingStyle (exactly like WhatsApp)
        val userIcon = if (bitmap != null) {
            androidx.core.graphics.drawable.IconCompat.createWithBitmap(bitmap)
        } else {
            null
        }

        val sender = androidx.core.app.Person.Builder()
            .setName(senderName)
            .setIcon(userIcon)
            .setKey(senderUserId)
            .build()

        val messagingStyle = NotificationCompat.MessagingStyle(sender)
            .addMessage(messageContent, System.currentTimeMillis(), sender)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.example.mychat.R.mipmap.ic_launcher)
            .setStyle(messagingStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notificationBuilder.build())
            android.util.Log.d("NotificationHelper", "Notification shown for message from $senderName")
        } catch (e: SecurityException) {
            android.util.Log.w("NotificationHelper", "Notification permission not granted: ${e.message}")
        }
    }
}