package com.example.mychat.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mychat.ui.theme.*

@Composable
fun MessageInput(
    onSendMessage: (String) -> Unit,
    isSending: Boolean = false,
    quickReplies: List<String> = emptyList(),
    sentMessages: Set<String> = emptySet(),
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }
    val hapticFeedback = LocalHapticFeedback.current

    // Filter out quick replies that have already been sent in this conversation
    val availableQuickReplies = remember(quickReplies, sentMessages) {
        quickReplies.filter { it !in sentMessages }
    }

    Surface(
        color = WhatsAppInputBg,
        shadowElevation = 0.dp,
        modifier = modifier
    ) {
        Column {
            // Quick Replies row - shown above the text input
            if (availableQuickReplies.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableQuickReplies.forEach { reply ->
                        Surface(
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSendMessage(reply)
                            },
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFFF0F0F0),
                            border = null
                        ) {
                            Text(
                                text = reply,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1C1F26),
                                maxLines = 2,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji/attachment button (like WhatsApp)
            Surface(
                onClick = { },
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "😊",
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Text input field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = {
                        Text(
                            text = "Type a message",
                            color = WhatsAppTextSecondary,
                            fontSize = 15.sp
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (messageText.isNotBlank() && !isSending) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSendMessage(messageText.trim())
                                messageText = ""
                            }
                        }
                    ),
                    enabled = !isSending,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        color = WhatsAppTextPrimary
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        cursorColor = WhatsAppHeaderDark
                    )
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Send/mic button
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = WhatsAppHeaderDark
                )
            } else if (messageText.isNotBlank()) {
                // Send button (like WhatsApp green send)
                Surface(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSendMessage(messageText.trim())
                            messageText = ""
                        }
                    },
                    shape = CircleShape,
                    color = WhatsAppHeaderDark,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "➤",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                }
            } else {
                // Mic button (like WhatsApp)
                Surface(
                    onClick = { /* Voice input - future feature */ },
                    shape = CircleShape,
                    color = Color.Transparent,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "🎤",
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}
}