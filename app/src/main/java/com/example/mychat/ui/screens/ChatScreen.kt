package com.example.mychat.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.content.Context
import android.net.ConnectivityManager
import com.example.mychat.data.config.AppConfig
import com.example.mychat.data.config.ConfigManager
import com.example.mychat.data.model.Message
import com.example.mychat.data.model.User
import com.example.mychat.ui.components.DateSeparator
import com.example.mychat.ui.components.MessageBubble
import com.example.mychat.ui.components.MessageInput
import com.example.mychat.ui.theme.*
import kotlinx.coroutines.launch

// Ready-made quick replies for users/buyers
private val BUYER_QUICK_REPLIES = listOf(
    "Is this package still available?",
    "Can you provide more details?",
    "Are dates flexible?",
    "Do you offer group discounts?"
)

// Ready-made quick replies for agencies/sellers
private val SELLER_QUICK_REPLIES = listOf(
    "Yes, it's available. When are you planning to travel?",
    "Would you like me to send the complete itinerary?",
    "How many people are travelling?",
    "We have a special offer going on, would you like to hear about it?"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    currentUser: User?,
    chatUser: User?,
    messages: List<Message>,
    isLoadingHistory: Boolean = false,
    hasMoreHistory: Boolean = false,
    historyError: String? = null,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit,
    onLoadMoreHistory: (() -> Unit)? = null,
    onClearHistoryError: (() -> Unit)? = null
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Connection state monitoring
    var isOnline by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        connectivityManager?.let { cm ->
            cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    isOnline = true
                    android.util.Log.d("ChatScreen", "Network available")
                }

                override fun onLost(network: android.net.Network) {
                    isOnline = false
                    android.util.Log.d("ChatScreen", "Network lost")
                }
            })
        }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Group messages by date for separators
    val messagesWithDates = remember(messages) {
        val result = mutableListOf<Pair<Any?, Message>>() // null = date separator, Message = bubble
        var lastDateHeader: String? = null
        for (msg in messages) {
            val dateHeader = com.example.mychat.ui.components.formatDateSeparator(msg.timestamp)
            if (dateHeader != lastDateHeader) {
                result.add(Pair(null, msg)) // date separator marker
                lastDateHeader = dateHeader
            }
            result.add(Pair(msg, msg))
        }
        result
    }

    Scaffold(
        topBar = {
            // WhatsApp-style header with dark green background
            Surface(
                color = WhatsAppHeaderDark,
                shadowElevation = 4.dp
            ) {
                Column {
                    // Status bar spacer area
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back button
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        // Logo + Agency name (dynamically from chat user)
                        val avatarUrl = chatUser?.avatarUrl ?: ""
                        if (avatarUrl.isNotEmpty()) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(WhatsAppPrimary.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✈️",
                                    fontSize = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = chatUser?.displayName ?: "Travel Agency",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Search and menu icons
                        IconButton(onClick = { /* search */ }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        IconButton(onClick = { /* menu */ }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Chat background (like WhatsApp's subtle pattern)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                WhatsAppBackground,
                                WhatsAppBackground.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(
                        start = 8.dp,
                        end = 8.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // History loading indicator at the top
                    if (isLoadingHistory) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = WhatsAppHeaderDark
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Loading history...",
                                    fontSize = 12.sp,
                                    color = WhatsAppTextSecondary
                                )
                            }
                        }
                    }

                    // Load more history button
                    if (hasMoreHistory && !isLoadingHistory) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(
                                    onClick = { onLoadMoreHistory?.invoke() }
                                ) {
                                    Text(
                                        "Load Older Messages",
                                        color = WhatsAppHeaderDark,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // Messages with date separators
                    items(messages) { message ->
                        val isFromCurrentUser = message.from == currentUser?.id
                        MessageBubble(
                            message = message,
                            isFromCurrentUser = isFromCurrentUser,
                            showAvatar = !isFromCurrentUser,
                            chatUserName = chatUser?.displayName ?: "",
                            chatUserAvatarUrl = chatUser?.avatarUrl ?: "",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Empty state
                    if (messages.isEmpty() && !isLoadingHistory) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // WhatsApp lock icon representation
                                    Surface(
                                        color = WhatsAppHeaderDark.copy(alpha = 0.1f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(64.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "🔒",
                                                fontSize = 28.sp
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Messages are end-to-end encrypted.",
                                        fontSize = 13.sp,
                                        color = WhatsAppTextSecondary
                                    )
                                    Text(
                                        text = "No messages here yet. Say hello!",
                                        fontSize = 14.sp,
                                        color = WhatsAppTextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Date separator overlay at top if needed
                // (future enhancement for sticky date header)
            }

            // Offline indicator banner
            if (!isOnline) {
                Surface(
                    color = Color(0xFFDC3545),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📡 No internet connection",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // History error banner
            historyError?.let { error ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️ $error",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { onClearHistoryError?.invoke() }
                        ) {
                            Text(
                                text = "DISMISS",
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // WhatsApp-style input bar
            // Determine quick replies based on user role, fetched from Firestore app_config/global
            val configManager = ConfigManager.getInstance()
            val currentAppConfig by configManager.appConfig.collectAsState()
            val quickRepliesList = remember(currentUser, currentAppConfig) {
                if (currentUser?.role == "agency") currentAppConfig.sellerQuickReplies else currentAppConfig.buyerQuickReplies
            }
            // Get the set of messages already sent by the current user to filter out used quick replies
            val sentMessagesTexts = remember(messages, currentUser) {
                messages.filter { it.from == currentUser?.id }.map { it.content }.toSet()
            }
            MessageInput(
                onSendMessage = { content ->
                    onSendMessage(content)
                    coroutineScope.launch {
                        listState.animateScrollToItem(messages.size)
                    }
                },
                quickReplies = quickRepliesList,
                sentMessages = sentMessagesTexts,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}