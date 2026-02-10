package com.example.mychat.data.model

data class TravelListing(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val duration: Int, // in days
    val destination: String,
    val type: String, // adventure, luxury, budget, cultural, family, romantic
    val photos: List<String> = emptyList(),
    val rating: Double = 0.0,
    val reviewsCount: Int = 0,
    val agencyId: String,
    val agencyName: String,
    val agencyData: Agency? = null,
    val approved: Boolean = false,
    // Additional attributes for enhanced display
    val packageType: String? = null, // international, domestic
    val countryName: String? = null,
    val stateName: String? = null,
    val placesCovered: List<PlaceCovered> = emptyList(),
    val hotelType: String? = null,
    val mealPlan: String? = null,
    val cost: Double? = null, // Alternative price field
    // Enhanced features for detailed package view
    val itinerary: List<ItineraryDay> = emptyList(),
    val inclusions: String = "", // Comma-separated or newline-separated list
    val exclusions: String = "", // Comma-separated or newline-separated list
    val faqs: List<FAQ> = emptyList(),
    val packageCode: String = "",
    val tourCategories: List<String> = emptyList(),
    val photoUrls: List<String> = emptyList(), // Additional photos beyond main photo
    val videoUrl: String = ""
)

data class PlaceCovered(
    val id: String,
    val name: String,
    val imageUrls: List<String> = emptyList()
)

data class Agency(
    val id: String,
    val companyName: String,
    val name: String, // contact person's name
    val email: String,
    val approved: Boolean = false
)

data class ItineraryDay(
    val day: Int,
    val placeName: String,
    val description: String,
    val activities: List<String> = emptyList(),
    val accommodation: String = ""
)

data class FAQ(
    val question: String,
    val answer: String
)

data class Booking(
    val id: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val userPhone: String? = null,
    val listingId: String,
    val listingTitle: String,
    val agencyId: String,
    val agencyName: String,
    val travelers: Int,
    val travelDate: String? = null,
    val specialRequests: String? = null,
    val preferences: List<String> = emptyList(),
    val totalAmount: Double,
    val status: String = "pending", // pending, confirmed, cancelled
    val createdAt: Long = System.currentTimeMillis(),
    val bookingReference: String
)
