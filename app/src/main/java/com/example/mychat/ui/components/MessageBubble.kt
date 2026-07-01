package com.example.mychat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mychat.data.model.Message
import com.example.mychat.data.model.MessageStatus
import com.example.mychat.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// WhatsApp-style date separator
@Composable
fun DateSeparator(dateText: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = WhatsAppDateBubble,
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 0.dp
        ) {
            Text(
                text = dateText,
                color = WhatsAppDateText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isFromCurrentUser: Boolean,
    showAvatar: Boolean = true,
    chatUserName: String = "",
    chatUserAvatarUrl: String = "",
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isFromCurrentUser) WhatsAppSentBubble else WhatsAppReceivedBubbleWhite
    val textColor = WhatsAppTextPrimary
    val timestampColor = WhatsAppTextSecondary

    // WhatsApp bubble corner radii: small on one side, large on others
    val shape = if (isFromCurrentUser) {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 4.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = if (isFromCurrentUser) 64.dp else 8.dp,
                end = if (isFromCurrentUser) 8.dp else 64.dp,
                top = 2.dp,
                bottom = 2.dp
            ),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // Avatar for received messages
        if (!isFromCurrentUser && showAvatar) {
            if (chatUserAvatarUrl.isNotEmpty()) {
                AsyncImage(
                    model = chatUserAvatarUrl,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(WhatsAppHeaderDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chatUserName.take(2).uppercase(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
        } else if (!isFromCurrentUser && !showAvatar) {
            Spacer(modifier = Modifier.width(36.dp))
        }

        // The bubble
        Surface(
            color = backgroundColor,
            shape = shape,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .padding(
                        start = 12.dp,
                        end = 8.dp,
                        top = 6.dp,
                        bottom = 4.dp
                    )
            ) {
                Text(
                    text = message.content,
                    color = textColor,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Timestamp + status row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formatTimestamp(message.timestamp),
                        color = timestampColor,
                        fontSize = 11.sp
                    )

                    // Status ticks for sent messages only
                    if (isFromCurrentUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        val tickColor = when (message.status) {
                            MessageStatus.DELIVERED -> WhatsAppTickDelivered
                            MessageStatus.READ -> WhatsAppTickRead
                            MessageStatus.SENT -> WhatsAppTickSent
                        }
                        val tickIcon = when (message.status) {
                            MessageStatus.DELIVERED -> "✓✓"
                            MessageStatus.READ -> "✓✓"
                            MessageStatus.SENT -> "✓"
                        }
                        Text(
                            text = tickIcon,
                            color = tickColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val messageTime = Date(timestamp)
    val today = Date(now)
    val yesterday = Date(now - 24 * 60 * 60 * 1000)

    val messageCalendar = java.util.Calendar.getInstance().apply { time = messageTime }
    val todayCalendar = java.util.Calendar.getInstance().apply { time = today }
    val yesterdayCalendar = java.util.Calendar.getInstance().apply { time = yesterday }

    return when {
        // Today - show time only
        messageCalendar.get(java.util.Calendar.YEAR) == todayCalendar.get(java.util.Calendar.YEAR) &&
        messageCalendar.get(java.util.Calendar.DAY_OF_YEAR) == todayCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(messageTime)
        }
        // Yesterday - show "Yesterday"
        messageCalendar.get(java.util.Calendar.YEAR) == yesterdayCalendar.get(java.util.Calendar.YEAR) &&
        messageCalendar.get(java.util.Calendar.DAY_OF_YEAR) == yesterdayCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> {
            "Yesterday ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(messageTime)}"
        }
        // Within this week - show day name
        now - timestamp < 7 * 24 * 60 * 60 * 1000 -> {
            "${SimpleDateFormat("EEE", Locale.getDefault()).format(messageTime)} ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(messageTime)}"
        }
        // Older - show date and time
        else -> {
            SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(messageTime)
        }
    }
}

fun formatDateSeparator(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val messageTime = Date(timestamp)
    val today = Date(now)
    val yesterday = Date(now - 24 * 60 * 60 * 1000)

    val messageCalendar = java.util.Calendar.getInstance().apply { time = messageTime }
    val todayCalendar = java.util.Calendar.getInstance().apply { time = today }
    val yesterdayCalendar = java.util.Calendar.getInstance().apply { time = yesterday }

    return when {
        messageCalendar.get(java.util.Calendar.YEAR) == todayCalendar.get(java.util.Calendar.YEAR) &&
        messageCalendar.get(java.util.Calendar.DAY_OF_YEAR) == todayCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> "Today"
        messageCalendar.get(java.util.Calendar.YEAR) == yesterdayCalendar.get(java.util.Calendar.YEAR) &&
        messageCalendar.get(java.util.Calendar.DAY_OF_YEAR) == yesterdayCalendar.get(java.util.Calendar.DAY_OF_YEAR) -> "Yesterday"
        else -> SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(messageTime)
    }
}