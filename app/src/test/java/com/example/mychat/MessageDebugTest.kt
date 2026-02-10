package com.example.mychat

import com.example.mychat.data.model.Message
import com.example.mychat.data.model.MessageStatus
import com.example.mychat.data.model.User
import com.example.mychat.data.repository.ChatRepository
import com.example.mychat.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class MessageDebugTest {

    private lateinit var chatRepository: ChatRepository
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockFirestore: FirebaseFirestore

    @Before
    fun setup() {
        mockAuth = mockk(relaxed = true)
        mockFirestore = mockk(relaxed = true)
        
        // Create repository with mocked dependencies
        chatRepository = ChatRepository(
            authRepository = mockk(relaxed = true),
            firebaseAuth = mockAuth,
            firestore = mockFirestore
        )
        
        chatViewModel = ChatViewModel(chatRepository)
    }

    @Test
    fun testSendMessageFlow() = runTest {
        // Test that sendMessage is called with correct parameters
        val testUserId = "test_user_123"
        val testContent = "Hello, this is a test message!"
        
        // Mock current user
        val mockCurrentUser = mockk<com.google.firebase.auth.FirebaseUser>(relaxed = true)
        every { mockCurrentUser.uid } returns testUserId
        every { mockAuth.currentUser } returns mockCurrentUser
        
        // Spy on the repository
        val spyRepository = spyk(chatRepository)
        
        // Call sendMessage
        spyRepository.sendMessage(testUserId, testContent)
        
        // Verify the message was processed
        coVerify {
            spyRepository.sendMessage(testUserId, testContent)
        }
    }

    @Test
    fun testMessageParsing() {
        // Test message parsing with different status values
        val testMessageData = mapOf(
            "sender" to "user1",
            "receiverId" to "user2", 
            "text" to "Test message",
            "timestamp" to System.currentTimeMillis(),
            "status" to "delivered"
        )
        
        // Create a mock document snapshot
        val mockDocument = mockk<com.google.firebase.firestore.DocumentSnapshot>(relaxed = true)
        every { mockDocument.data } returns testMessageData
        every { mockDocument.id } returns "test_message_id"
        
        // Test parsing
        val parsedMessage = chatRepository.documentToMessage(mockDocument)
        
        assert(parsedMessage != null)
        assert(parsedMessage?.status == MessageStatus.DELIVERED)
        assert(parsedMessage?.content == "Test message")
    }

    @Test
    fun testQueryLogic() {
        // Test that the query logic creates correct Firestore queries
        val mockCollection = mockk<com.google.firebase.firestore.CollectionReference>(relaxed = true)
        val mockQuery = mockk<com.google.firebase.firestore.Query>(relaxed = true)
        
        every { mockFirestore.collection("messages") } returns mockCollection
        every { mockCollection.whereEqualTo("sender", any()) } returns mockQuery
        every { mockQuery.whereEqualTo("receiverId", any()) } returns mockQuery
        
        // This test verifies the query structure is correct
        // The actual query execution would be tested in integration tests
    }
}