package com.example.mychat

import com.example.mychat.data.model.Message
import com.example.mychat.data.model.MessageStatus
import com.example.mychat.data.model.User
import com.example.mychat.data.repository.ChatRepository
import com.example.mychat.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class MessageReceptionTest {

    private lateinit var chatRepository: ChatRepository
    private lateinit var mockAuthRepository: AuthRepository
    private lateinit var mockFirebaseAuth: FirebaseAuth
    private lateinit var mockFirestore: FirebaseFirestore

    @Before
    fun setup() {
        mockAuthRepository = mockk(relaxed = true)
        mockFirebaseAuth = mockk(relaxed = true)
        mockFirestore = mockk(relaxed = true)
        
        chatRepository = ChatRepository(
            authRepository = mockAuthRepository,
            firebaseAuth = mockFirebaseAuth,
            firestore = mockFirestore
        )
    }

    @Test
    fun `test message filtering for current chat user`() = runBlocking {
        // Arrange
        val currentUser = User("user1", "User 1", "user1@example.com")
        val otherUser = User("user2", "User 2", "user2@example.com")
        
        val sentMessage = Message(
            id = "msg1",
            from = "user1",
            to = "user2", 
            content = "Hello from user1",
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT
        )
        
        val receivedMessage = Message(
            id = "msg2",
            from = "user2",
            to = "user1",
            content = "Hello from user2", 
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT
        )

        // Act
        chatRepository.setCurrentChatUser(otherUser)
        
        // Simulate receiving messages
        val currentMessages = mutableListOf(sentMessage, receivedMessage)
        chatRepository.messages.value = currentMessages

        // Assert
        val chatMessages = chatRepository.chatMessages.first()
        
        // Should contain both messages since they're between user1 and user2
        assert(chatMessages.size == 2)
        assert(chatMessages.any { it.id == "msg1" && it.from == "user1" && it.to == "user2" })
        assert(chatMessages.any { it.id == "msg2" && it.from == "user2" && it.to == "user1" })
    }

    @Test
    fun `test message filtering excludes unrelated messages`() = runBlocking {
        // Arrange
        val currentUser = User("user1", "User 1", "user1@example.com")
        val otherUser = User("user2", "User 2", "user2@example.com")
        val unrelatedUser = User("user3", "User 3", "user3@example.com")
        
        val sentMessage = Message(
            id = "msg1",
            from = "user1",
            to = "user2",
            content = "Hello from user1",
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT
        )
        
        val unrelatedMessage = Message(
            id = "msg3",
            from = "user3",
            to = "user4",
            content = "Unrelated message",
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT
        )

        // Act
        chatRepository.setCurrentChatUser(otherUser)
        
        // Simulate receiving messages
        val currentMessages = mutableListOf(sentMessage, unrelatedMessage)
        chatRepository.messages.value = currentMessages

        // Assert
        val chatMessages = chatRepository.chatMessages.first()
        
        // Should only contain the message between user1 and user2
        assert(chatMessages.size == 1)
        assert(chatMessages.any { it.id == "msg1" })
        assert(!chatMessages.any { it.id == "msg3" })
    }

    @Test
    fun `test message filtering with no current chat user`() = runBlocking {
        // Arrange
        val sentMessage = Message(
            id = "msg1",
            from = "user1",
            to = "user2",
            content = "Hello from user1",
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT
        )

        // Act
        chatRepository.clearCurrentChatUser()
        
        // Simulate receiving messages
        val currentMessages = mutableListOf(sentMessage)
        chatRepository.messages.value = currentMessages

        // Assert
        val chatMessages = chatRepository.chatMessages.first()
        
        // Should be empty when no current chat user is set
        assert(chatMessages.isEmpty())
    }
}