package com.example.mychat.data.repository

import com.example.mychat.data.model.SupportTicket
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SupportRepository(private val authRepository: AuthRepository) {

    private val db = FirebaseFirestore.getInstance()

    fun getSupportTickets(userId: String): Flow<List<SupportTicket>> = callbackFlow {
        val listener = db.collection("support_tickets")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val tickets = snapshot?.documents?.mapNotNull { document ->
                    try {
                        val data = document.data ?: return@mapNotNull null
                        val createdAtMillis = when (val ca = data["createdAt"]) {
                            is Timestamp -> ca.toDate().time
                            is Long -> ca
                            is Number -> ca.toLong()
                            else -> System.currentTimeMillis()
                        }
                        SupportTicket(
                            id = document.id,
                            userId = data["userId"] as? String ?: "",
                            userName = data["userName"] as? String ?: "",
                            userEmail = data["userEmail"] as? String ?: "",
                            bookingId = data["bookingId"] as? String,
                            bookingRef = data["bookingRef"] as? String,
                            agencyName = data["agencyName"] as? String,
                            reason = data["reason"] as? String ?: "Agency is not responding after payment",
                            subject = data["subject"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            status = data["status"] as? String ?: "pending",
                            createdAt = createdAtMillis
                        )
                    } catch (e: Exception) {
                        null
                    }
                }?.sortedByDescending { it.createdAt } ?: emptyList()

                trySend(tickets)
            }

        awaitClose { listener.remove() }
    }

    suspend fun createSupportTicket(ticket: SupportTicket): Result<String> {
        return try {
            val ticketData = hashMapOf(
                "userId" to ticket.userId,
                "userName" to ticket.userName,
                "userEmail" to ticket.userEmail,
                "reason" to ticket.reason,
                "subject" to ticket.subject,
                "description" to ticket.description,
                "status" to SupportTicket.STATUS_PENDING,
                "createdAt" to System.currentTimeMillis()
            )
            // Add optional fields if present
            ticket.bookingId?.let { ticketData["bookingId"] = it }
            ticket.bookingRef?.let { ticketData["bookingRef"] = it }
            ticket.agencyName?.let { ticketData["agencyName"] = it }

            db.collection("support_tickets").add(ticketData).await()
            Result.success("Support ticket submitted successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}