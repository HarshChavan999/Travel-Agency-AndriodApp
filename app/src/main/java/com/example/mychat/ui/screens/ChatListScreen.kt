package com.example.mychat.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.mychat.data.model.User
import com.example.mychat.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

data class ChatConversation(
    val userId: String,
    val displayName: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val avatarUrl: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    currentUser: User?,
    messages: List<Message>,
    chatViewModel: ChatViewModel,
    onBack: () -> Unit,
    onOpenChat: (User) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val userNames by chatViewModel.userNames.collectAsState()
    val userAvatars by chatViewModel.userAvatars.collectAsState()

    // Derive unique conversations from messages, currentUser, and userNames cache
    val conversations = remember(messages, currentUser, userNames, userAvatars) {
        val userId = currentUser?.id ?: ""
        val userMap = mutableMapOf<String, ChatConversation>()

        // Group messages by the other participant, ensuring messages involve the current user
        val grouped = messages.filter { it.from == userId || it.to == userId }
            .groupBy { message ->
                if (message.from == userId) message.to else message.from
            }

        grouped.forEach { (otherUserId, msgs) ->
            val lastMsg = msgs.maxByOrNull { it.timestamp }
            if (lastMsg != null) {
                val resolvedName = userNames[otherUserId] ?: otherUserId.take(8).uppercase()
                val resolvedAvatar = userAvatars[otherUserId] ?: ""
                userMap[otherUserId] = ChatConversation(
                    userId = otherUserId,
                    displayName = resolvedName,
                    lastMessage = lastMsg.content,
                    lastMessageTime = lastMsg.timestamp,
                    isOnline = false,
                    avatarUrl = resolvedAvatar
                )
            }
        }

        // Sort by most recent message
        userMap.values.sortedByDescending { it.lastMessageTime }
    }

    // Trigger name fetch for all conversation partners
    LaunchedEffect(conversations) {
        conversations.forEach { conversation ->
            chatViewModel.fetchUserName(conversation.userId)
            chatViewModel.fetchUserAvatar(conversation.userId)
        }
    }

    // Filter by search
    val filteredConversations = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) conversations
        else conversations.filter {
            it.displayName.lowercase().contains(searchQuery.lowercase()) ||
            it.lastMessage.lowercase().contains(searchQuery.lowercase())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Chats",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${conversations.size} conversations",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF075E54) // WhatsApp-style green header
                )
            )
        },
        containerColor = Color(0xFFECE5DD) // WhatsApp-style light background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            Surface(
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    placeholder = {
                        Text(
                            text = "Search chats or contacts",
                            color = Color(0xFF9CA3AF),
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF0F0F0),
                        unfocusedContainerColor = Color(0xFFF0F0F0),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color(0xFF075E54)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                )
            }

            if (filteredConversations.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFFBDBDBD)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No chats found"
                                   else "No chats yet",
                            fontSize = 16.sp,
                            color = Color(0xFF757575),
                            fontWeight = FontWeight.Medium
                        )
                        if (searchQuery.isBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Start a conversation from a listing",
                                fontSize = 13.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    }
                }
            } else {
                // Chat List
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredConversations) { conversation ->
                        ChatListItem(
                            conversation = conversation,
                            onClick = {
                                // Create a User object for navigation with avatarUrl
                                val chatUser = User(
                                    id = conversation.userId,
                                    email = "${conversation.userId}@agency.com",
                                    displayName = conversation.displayName,
                                    isOnline = conversation.isOnline,
                                    avatarUrl = conversation.avatarUrl
                                )
                                onOpenChat(chatUser)
                            }
                        )
                        // Divider between items
                        HorizontalDivider(
                            color = Color(0xFFF0F0F0),
                            thickness = 1.dp,
                            modifier = Modifier.padding(start = 72.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListItem(
    conversation: ChatConversation,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with logo support
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0xFF075E54)),
            contentAlignment = Alignment.Center
        ) {
            if (conversation.avatarUrl.isNotEmpty()) {
                AsyncImage(
                    model = conversation.avatarUrl,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = conversation.displayName.take(1).uppercase(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            // Online indicator
            if (conversation.isOnline) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(Color(0xFF34B7F1), CircleShape)
                        .align(Alignment.BottomEnd)
                        .border(2.dp, Color.White, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Message content
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatChatTime(conversation.lastMessageTime),
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.lastMessage,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (conversation.unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFF25D366),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${conversation.unreadCount}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatChatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 24 * 60 * 60 * 1000 -> {
            // Today - show time
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
        }
        diff < 7 * 24 * 60 * 60 * 1000 -> {
            // This week - show day name
            SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestamp))
        }
        else -> {
            // Older - show date
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(timestamp))
        }
    }
}