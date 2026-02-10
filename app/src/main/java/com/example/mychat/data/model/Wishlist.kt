package com.example.mychat.data.model

data class WishlistItem(
    val id: String,
    val userId: String,
    val listingId: String,
    val addedAt: Long = System.currentTimeMillis()
)

data class Wishlist(
    val userId: String,
    val items: List<String> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
)