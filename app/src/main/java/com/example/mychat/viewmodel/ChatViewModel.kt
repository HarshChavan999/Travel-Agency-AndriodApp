package com.example.mychat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mychat.data.model.Message
import com.example.mychat.data.model.User
import com.example.mychat.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _chatMessages = MutableStateFlow<List<Message>>(emptyList())
    val chatMessages: StateFlow<List<Message>> = _chatMessages

    private val _onlineUsers = MutableStateFlow<List<User>>(emptyList())
    val onlineUsers: StateFlow<List<User>> = _onlineUsers

    private val _currentChatUser = MutableStateFlow<User?>(null)
    val currentChatUser: StateFlow<User?> = _currentChatUser

    private val _userNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val userNames: StateFlow<Map<String, String>> = _userNames

    private val _userAvatars = MutableStateFlow<Map<String, String>>(emptyMap())
    val userAvatars: StateFlow<Map<String, String>> = _userAvatars

    // History loading states
    private val _isLoadingHistory = MutableStateFlow(false)
    val isLoadingHistory: StateFlow<Boolean> = _isLoadingHistory

    private val _hasMoreHistory = MutableStateFlow(false)
    val hasMoreHistory: StateFlow<Boolean> = _hasMoreHistory

    private val _historyError = MutableStateFlow<String?>(null)
    val historyError: StateFlow<String?> = _historyError

    // Track the oldest message timestamp for pagination
    private var oldestMessageTimestamp: Long? = null

    init {
        viewModelScope.launch {
            chatRepository.messages.collect { messages ->
                _messages.value = messages
            }
        }
        viewModelScope.launch {
            chatRepository.chatMessages.collect { messages ->
                _chatMessages.value = messages
            }
        }
        viewModelScope.launch {
            chatRepository.onlineUsers.collect { users ->
                _onlineUsers.value = users
            }
        }
        viewModelScope.launch {
            chatRepository.currentChatUser.collect { user ->
                _currentChatUser.value = user
            }
        }
    }


    fun sendMessage(toUserId: String, content: String) {
        android.util.Log.d("ChatViewModel", "sendMessage called: toUserId=$toUserId, content=$content")
        if (content.isNotBlank()) {
            chatRepository.sendMessage(toUserId, content)
        } else {
            android.util.Log.w("ChatViewModel", "sendMessage called with empty content")
        }
    }

    fun setCurrentChatUser(user: User) {
        chatRepository.setCurrentChatUser(user)
    }

    fun markAllMessagesFromUserAsRead(senderId: String) {
        chatRepository.markAllMessagesFromUserAsRead(senderId)
    }

    fun clearCurrentChatUser() {
        chatRepository.clearCurrentChatUser()
    }

    fun updateOnlineUsers(users: List<User>) {
        chatRepository.updateOnlineUsers(users)
    }

    fun getMessagesWithUser(userId: String): List<Message> {
        return chatRepository.getMessagesWithUser(userId)
    }

    fun clearMessages() {
        chatRepository.clearMessages()
    }

    /** Ensures the Firestore snapshot listeners are active (e.g. when viewing the chat list) */
    fun ensureListening() {
        chatRepository.ensureListening()
    }

    // History loading functionality
    fun loadChatHistory() {
        val currentUser = _currentChatUser.value ?: return

        if (_isLoadingHistory.value) return // Prevent multiple concurrent requests

        _isLoadingHistory.value = true
        _historyError.value = null

        viewModelScope.launch {
            try {
                chatRepository.loadMessageHistory(currentUser.id)
                _isLoadingHistory.value = false
                // For now, assume we have more history available
                _hasMoreHistory.value = true
            } catch (e: Exception) {
                _isLoadingHistory.value = false
                _historyError.value = e.message ?: "Failed to load chat history"
            }
        }
    }

    fun loadMoreHistory() {
        if (!_hasMoreHistory.value || _isLoadingHistory.value) return
        loadChatHistory()
    }

    fun clearHistoryError() {
        _historyError.value = null
    }

    // Called when starting a new chat conversation
    fun initializeChatHistory() {
        // Reset history state when starting new chat
        oldestMessageTimestamp = null
        _hasMoreHistory.value = false
        _historyError.value = null

        // Load initial history
        loadChatHistory()
    }

    fun fetchUserName(userId: String) {
        if (_userNames.value.containsKey(userId)) return
        viewModelScope.launch {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val doc = db.collection("users").document(userId).get().await()
                if (doc.exists()) {
                    val companyName = doc.getString("companyName")
                    val name = doc.getString("name")
                    val displayName = doc.getString("displayName")
                    
                    val userName = companyName ?: name ?: displayName ?: userId.take(8).uppercase()
                    _userNames.value = _userNames.value + (userId to userName)
                    
                    // Also fetch avatarUrl/logoUrl
                    val logoUrl = doc.getString("logoUrl") ?: doc.getString("avatarUrl") ?: ""
                    if (logoUrl.isNotEmpty()) {
                        _userAvatars.value = _userAvatars.value + (userId to logoUrl)
                    }
                    
                    android.util.Log.d("ChatViewModel", "Fetched user name for $userId: $userName, avatar: ${if (logoUrl.isNotEmpty()) "present" else "none"}")
                } else {
                    _userNames.value = _userNames.value + (userId to userId.take(8).uppercase())
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Error fetching user name for $userId: ${e.message}")
            }
        }
    }

    fun fetchUserAvatar(userId: String) {
        if (_userAvatars.value.containsKey(userId)) return
        viewModelScope.launch {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val doc = db.collection("users").document(userId).get().await()
                if (doc.exists()) {
                    val logoUrl = doc.getString("logoUrl") ?: doc.getString("avatarUrl") ?: ""
                    if (logoUrl.isNotEmpty()) {
                        _userAvatars.value = _userAvatars.value + (userId to logoUrl)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Error fetching user avatar for $userId: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // No cleanup needed for Firestore
    }
}
