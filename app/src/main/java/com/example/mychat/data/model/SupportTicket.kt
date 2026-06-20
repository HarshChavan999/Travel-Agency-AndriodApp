package com.example.mychat.data.model

import com.google.firebase.Timestamp

data class SupportTicket(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val bookingId: String? = null,
    val bookingRef: String? = null,
    val agencyName: String? = null,
    val reason: String = "Agency is not responding after payment",
    val subject: String = "",
    val description: String = "",
    val status: String = "pending", // pending, in-review, resolved
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_IN_REVIEW = "in-review"
        const val STATUS_RESOLVED = "resolved"
    }
}