package com.example.mychat.data.config

/**
 * Data classes representing the remote configuration fetched
 * from Firebase Remote Config and Firestore app_config/global.
 */
data class AppConfig(
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = "We are currently under maintenance. Please check back later.",
    val apiUrl: String = "https://api.example.com",
    val latestVersion: Int = 10,
    val minSupportedVersion: Int = 8,
    val enableChat: Boolean = true,
    val enableNotifications: Boolean = true,
    val enableWishlist: Boolean = true,
    val enableBooking: Boolean = true,
    val enableComparison: Boolean = true,
    val enableGoogleSignIn: Boolean = true,
    val enableAnonymousSignIn: Boolean = true,
    val forceUpdateMessage: String = "A new version is available. Please update your app to continue.",
    val optionalUpdateMessage: String = "A new version is available. Would you like to update?",
    val announcementTitle: String = "",
    val announcementMessage: String = "",
    val announcementDismissible: Boolean = true,
    val announcementLink: String = "",
    val serviceFeePercent: Double = 5.0,
    val maxComparisonItems: Int = 3,
    val searchPlaceholder: String = "Search your Holiday Destination",
    val heroBannerTitle: String = "BOOK YOUR TOUR WITH US",
    val heroBannerSubtitle: String = "Domestic Tour | International tour",
    val popularDestinationsTitle: String = "Popular Destinations",
    val popularDestinationsSubtitle: String = "Explore amazing places",
    // Quick Reply Messages (editable by admin via Firestore app_config/global)
    val buyerQuickReplies: List<String> = listOf(
        "Is this package still available?",
        "Can you provide more details?",
        "Are dates flexible?",
        "Do you offer group discounts?"
    ),
    val sellerQuickReplies: List<String> = listOf(
        "Yes, it's available. When are you planning to travel?",
        "Would you like me to send the complete itinerary?",
        "How many people are travelling?",
        "We have a special offer going on, would you like to hear about it?"
    )
)