package com.example.mychat.data.repository

import com.example.mychat.data.model.Message
import com.example.mychat.data.model.MessageStatus
import com.example.mychat.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

class ChatRepository(
    private val authRepository: AuthRepository,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: Flow<List<Message>> = _messages.asStateFlow()

    private val _onlineUsers = MutableStateFlow<List<User>>(emptyList())
    val onlineUsers: Flow<List<User>> = _onlineUsers.asStateFlow()

    private val _currentChatUser = MutableStateFlow<User?>(null)
    val currentChatUser: Flow<User?> = _currentChatUser.asStateFlow()

    // Combined flow for messages with specific user
    val chatMessages: Flow<List<Message>> = combine(messages, currentChatUser) { messages, currentUser ->
        android.util.Log.d("ChatRepository", "Combining messages flow: ${messages.size} total messages, current user: ${currentUser?.id}")
        if (currentUser != null) {
            val filteredMessages = messages.filter { it.from == currentUser.id || it.to == currentUser.id }
            android.util.Log.d("ChatRepository", "Filtered messages for user ${currentUser.id}: ${filteredMessages.size}")
            filteredMessages
        } else {
            android.util.Log.d("ChatRepository", "No current user set, returning empty list")
            emptyList()
        }
    }

    private var chatMessagesListener: ListenerRegistration? = null
    private var reverseMessagesListener: ListenerRegistration? = null

    init {
        // Listen to authentication state changes
        firebaseAuth.addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                setupMessageListener()
            } else {
                cleanupListeners()
            }
        }
    }

    private fun setupMessageListener() {
        val currentUser = firebaseAuth.currentUser ?: return
        val userId = currentUser.uid

        android.util.Log.d("ChatRepository", "setupMessageListener called for user: $userId")

        // Listen to all messages where current user is either sender or receiver
        // This ensures we get all messages for the current user
        // NOTE: Using chat_messages collection to match web app DataConnect implementation
        val allMessagesListener = firestore.collection("chat_messages")
            .whereEqualTo("from_user_id", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("ChatRepository", "Error listening to sent messages: ${error.message}")
                    return@addSnapshotListener
                }

                snapshot?.let { querySnapshot ->
                    val messages = mutableListOf<Message>()
                    for (document in querySnapshot.documents) {
                        val message = documentToMessageFromDataConnect(document)
                        if (message != null) {
                            messages.add(message)
                        }
                    }

                    // Update messages list, merging updates and new messages
                    val currentMessagesMap = _messages.value.associateBy { it.id }.toMutableMap()
                    for (msg in messages) {
                        currentMessagesMap[msg.id] = msg
                    }
                    _messages.value = currentMessagesMap.values.sortedBy { it.timestamp }
                    
                    android.util.Log.d("ChatRepository", "Updated sent messages list: ${_messages.value.size} total messages")
                }
            }

        // Also listen for messages where current user is receiver
        val receivedMessagesListener = firestore.collection("chat_messages")
            .whereEqualTo("to_user_id", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("ChatRepository", "Error listening to received messages: ${error.message}")
                    return@addSnapshotListener
                }

                snapshot?.let { querySnapshot ->
                    val messages = mutableListOf<Message>()
                    val currentChatUserVal = _currentChatUser.value
                    
                    for (document in querySnapshot.documents) {
                        var message = documentToMessageFromDataConnect(document)
                        if (message != null) {
                            // If received message is for the current user
                            if (message.to == userId) {
                                if (currentChatUserVal != null && message.from == currentChatUserVal.id) {
                                    // Chat screen is open, mark as read
                                    if (message.status != MessageStatus.READ) {
                                        updateMessageStatus(message.id, MessageStatus.READ)
                                        message = message.copy(status = MessageStatus.READ)
                                    }
                                } else {
                                    // Chat screen is closed, mark as delivered
                                    if (message.status == MessageStatus.SENT) {
                                        updateMessageStatus(message.id, MessageStatus.DELIVERED)
                                        message = message.copy(status = MessageStatus.DELIVERED)
                                    }
                                }
                            }
                            messages.add(message)
                        }
                    }

                    // Update messages list, merging updates and new messages
                    val currentMessagesMap = _messages.value.associateBy { it.id }.toMutableMap()
                    for (msg in messages) {
                        currentMessagesMap[msg.id] = msg
                    }
                    _messages.value = currentMessagesMap.values.sortedBy { it.timestamp }
                    
                    android.util.Log.d("ChatRepository", "Updated received messages list: ${_messages.value.size} total messages")
                }
            }

        // Store both listeners
        this.chatMessagesListener = allMessagesListener
        this.reverseMessagesListener = receivedMessagesListener
    }

    private fun cleanupListeners() {
        chatMessagesListener?.remove()
        reverseMessagesListener?.remove()
        chatMessagesListener = null
        reverseMessagesListener = null
        // NOTE: Do NOT clear _messages.value here — we want history to persist
    }

    private fun parseTimestamp(timestampObj: Any?, createdAtObj: Any?): Long {
        return when (timestampObj) {
            is com.google.firebase.Timestamp -> timestampObj.toDate().time
            is Number -> timestampObj.toLong()
            else -> {
                when (createdAtObj) {
                    is com.google.firebase.Timestamp -> createdAtObj.toDate().time
                    is Number -> createdAtObj.toLong()
                    else -> System.currentTimeMillis()
                }
            }
        }
    }

    private fun documentToMessage(document: com.google.firebase.firestore.DocumentSnapshot): Message? {
        return try {
            val data = document.data ?: return null
            val message = Message(
                id = document.id,
                from = data["sender"] as? String ?: "",
                to = data["receiverId"] as? String ?: "",
                content = data["text"] as? String ?: "",
                timestamp = parseTimestamp(data["timestamp"], data["created_at"]),
                status = when (data["status"] as? String) {
                    "delivered" -> MessageStatus.DELIVERED
                    "read" -> MessageStatus.READ
                    else -> MessageStatus.SENT
                }
            )
            
            // Debug logging
            android.util.Log.d("ChatRepository", "Loaded message: ${message.id} from ${message.from} to ${message.to} at ${message.timestamp}, status: ${message.status}")
            
            message
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Error parsing message document: ${e.message}")
            null
        }
    }

    private fun documentToMessageFromDataConnect(document: com.google.firebase.firestore.DocumentSnapshot): Message? {
        return try {
            val data = document.data ?: return null
            val message = Message(
                id = document.id,
                from = data["from_user_id"] as? String ?: "",
                to = data["to_user_id"] as? String ?: "",
                content = data["content"] as? String ?: "",
                timestamp = parseTimestamp(data["timestamp"], data["created_at"]),
                status = when (data["status"] as? String) {
                    "delivered" -> MessageStatus.DELIVERED
                    "read" -> MessageStatus.READ
                    else -> MessageStatus.SENT
                }
            )
            
            // Debug logging
            android.util.Log.d("ChatRepository", "Loaded DataConnect message: ${message.id} from ${message.from} to ${message.to} at ${message.timestamp}, status: ${message.status}")
            
            message
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "Error parsing DataConnect message document: ${e.message}")
            null
        }
    }

    fun connect() {
        // Firestore is always connected, no explicit connect needed
    }

    fun disconnect() {
        cleanupListeners()
    }

    /** Start listeners if they aren't already running (safe to call multiple times) */
    fun ensureListening() {
        if (chatMessagesListener == null) {
            setupMessageListener()
        }
    }


    fun sendMessage(toUserId: String, content: String) {
        val currentUser = firebaseAuth.currentUser ?: return
        val fromUserId = currentUser.uid

        android.util.Log.d("ChatRepository", "sendMessage called: toUserId=$toUserId, content=$content")
        android.util.Log.d("ChatRepository", "Current user ID: $fromUserId")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val messageId = generateMessageId()
                val timestamp = System.currentTimeMillis()

                android.util.Log.d("ChatRepository", "Creating message with ID: $messageId")

                // Create message data for Firestore (chat_messages collection format)
                val messageData = hashMapOf(
                    "from_user_id" to fromUserId,
                    "to_user_id" to toUserId,
                    "content" to content,
                    "timestamp" to timestamp,
                    "status" to "sent",
                    "created_at" to com.google.firebase.Timestamp(java.util.Date(timestamp))
                )

                android.util.Log.d("ChatRepository", "Message data: $messageData")

                // Save to Firestore
                firestore.collection("chat_messages")
                    .document(messageId)
                    .set(messageData)
                    .await()

                android.util.Log.d("ChatRepository", "Message saved to Firestore successfully")

                // Update local state
                val message = Message(
                    id = messageId,
                    from = fromUserId,
                    to = toUserId,
                    content = content,
                    timestamp = timestamp,
                    status = MessageStatus.SENT
                )

                val currentMessages = _messages.value.toMutableList()
                currentMessages.add(message)
                _messages.value = currentMessages.sortedBy { it.timestamp }

                android.util.Log.d("ChatRepository", "Local state updated, total messages: ${_messages.value.size}")

            } catch (e: Exception) {
                android.util.Log.e("ChatRepository", "Error sending message: ${e.message}", e)
                // Handle error - could queue for retry
                // For now, just log
                e.printStackTrace()
            }
        }
    }

    fun setCurrentChatUser(user: User) {
        android.util.Log.d("ChatRepository", "Setting current chat user: ${user.id} (${user.displayName})")
        _currentChatUser.value = user
        
        // Mark all messages from this user as read
        markAllMessagesFromUserAsRead(user.id)
        
        // Only re-setup listeners if not already running (don't wipe existing messages)
        if (chatMessagesListener == null) {
            setupMessageListener()
        }
    }

    fun clearCurrentChatUser() {
        _currentChatUser.value = null
    }

    fun updateOnlineUsers(users: List<User>) {
        _onlineUsers.value = users
    }

    fun updateMessageStatus(messageId: String, status: MessageStatus) {
        val statusString = when (status) {
            MessageStatus.SENT -> "sent"
            MessageStatus.DELIVERED -> "delivered"
            MessageStatus.READ -> "read"
        }
        
        // Update locally
        val currentMessages = _messages.value.toMutableList()
        val index = currentMessages.indexOfFirst { it.id == messageId }
        if (index >= 0) {
            val updatedMessage = currentMessages[index].copy(status = status)
            currentMessages[index] = updatedMessage
            _messages.value = currentMessages
        }
        
        // Update in Firestore
        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestore.collection("chat_messages")
                    .document(messageId)
                    .update("status", statusString)
                    .await()
                android.util.Log.d("ChatRepository", "Message $messageId status updated to $statusString in Firestore")
            } catch (e: Exception) {
                // If it fails, it might be a legacy message
                try {
                    firestore.collection("messages")
                        .document(messageId)
                        .update("status", statusString)
                        .await()
                    android.util.Log.d("ChatRepository", "Legacy message $messageId status updated to $statusString in Firestore")
                } catch (e2: Exception) {
                    android.util.Log.e("ChatRepository", "Failed to update status to $statusString for message $messageId: ${e2.message}")
                }
            }
        }
    }

    fun markAllMessagesFromUserAsRead(senderId: String) {
        val currentUser = firebaseAuth.currentUser ?: return
        val currentUserId = currentUser.uid
        
        val currentMessages = _messages.value
        val messagesToUpdate = currentMessages.filter { 
            it.from == senderId && it.to == currentUserId && it.status != MessageStatus.READ 
        }
        
        if (messagesToUpdate.isEmpty()) return
        
        android.util.Log.d("ChatRepository", "Marking ${messagesToUpdate.size} messages from $senderId as READ")
        
        // Batch update in Firestore
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val batch = firestore.batch()
                messagesToUpdate.forEach { message ->
                    val docRef = firestore.collection("chat_messages").document(message.id)
                    batch.update(docRef, "status", "read")
                }
                batch.commit().await()
                
                // Update local state
                val updatedMessagesList = _messages.value.map { message ->
                    if (message.from == senderId && message.to == currentUserId && message.status != MessageStatus.READ) {
                        message.copy(status = MessageStatus.READ)
                    } else {
                        message
                    }
                }
                _messages.value = updatedMessagesList
                android.util.Log.d("ChatRepository", "Batch read status update successful")
            } catch (e: Exception) {
                // Try individual fallback for legacy messages or mixed collections
                messagesToUpdate.forEach { message ->
                    updateMessageStatus(message.id, MessageStatus.READ)
                }
            }
        }
    }

    fun getMessagesWithUser(userId: String): List<Message> {
        return _messages.value.filter { it.from == userId || it.to == userId }
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    private fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    private fun generateMessageId(): String {
        return java.util.UUID.randomUUID().toString()
    }

    // Load message history for current chat user
    suspend fun loadMessageHistory(otherUserId: String, limit: Long = 50) {
        try {
            val currentUserId = getCurrentUserId() ?: return

            val historyMessages = mutableListOf<Message>()

            // Query 1: Messages sent by current user to other user
            val query1 = firestore.collection("chat_messages")
                .whereEqualTo("from_user_id", currentUserId)
                .whereEqualTo("to_user_id", otherUserId)
                .get()
                .await()

            for (document in query1.documents) {
                val message = documentToMessageFromDataConnect(document)
                if (message != null) {
                    historyMessages.add(message)
                }
            }

            // Query 2: Messages sent by other user to current user
            val query2 = firestore.collection("chat_messages")
                .whereEqualTo("from_user_id", otherUserId)
                .whereEqualTo("to_user_id", currentUserId)
                .get()
                .await()

            for (document in query2.documents) {
                val message = documentToMessageFromDataConnect(document)
                if (message != null) {
                    historyMessages.add(message)
                }
            }

            // Sort and limit in memory to avoid compound index requirements
            val sortedHistory = historyMessages.sortedBy { it.timestamp }
            val limitedHistory = if (sortedHistory.size > limit) {
                sortedHistory.takeLast(limit.toInt())
            } else {
                sortedHistory
            }

            // Add to existing messages, avoiding duplicates
            val currentMessages = _messages.value.toMutableList()
            val existingIds = currentMessages.map { it.id }.toSet()
            val newMessages = limitedHistory.filter { it.id !in existingIds }

            currentMessages.addAll(newMessages)
            _messages.value = currentMessages.sortedBy { it.timestamp }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
