package com.example.mychat.data.config

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * ConfigManager provides a centralized configuration layer that combines:
 * - Firebase Remote Config (fast, low-latency parameter delivery)
 * - Firestore app_config/global (rich, mutable document for dynamic control)
 *
 * This allows the app owner to control behavior, features, announcements,
 * maintenance mode, and version requirements remotely without publishing a new APK.
 */
class ConfigManager {

    companion object {
        private const val TAG = "ConfigManager"
        private const val FIRESTORE_COLLECTION = "app_config"
        private const val FIRESTORE_DOCUMENT = "global"

        @Volatile
        private var instance: ConfigManager? = null

        fun getInstance(): ConfigManager {
            return instance ?: synchronized(this) {
                instance ?: ConfigManager().also { instance = it }
            }
        }
    }

    // ---------- Firebase Instances ----------

    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // ---------- Config State ----------

    private val _appConfig = MutableStateFlow(AppConfig())
    val appConfig: StateFlow<AppConfig> = _appConfig.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _fetchError = MutableStateFlow<String?>(null)
    val fetchError: StateFlow<String?> = _fetchError.asStateFlow()

    // Snapshot of defaults for Remote Config keys
    private val remoteConfigDefaults: Map<String, Any> = mapOf(
        // Feature flags
        "enable_chat" to true,
        "enable_notifications" to true,
        "enable_wishlist" to true,
        "enable_booking" to true,
        "enable_comparison" to true,
        "enable_google_sign_in" to true,
        "enable_anonymous_sign_in" to true,
        "maintenance_mode" to false,

        // Version requirements
        "latest_version" to 1L,
        "min_supported_version" to 1L,

        // UI strings
        "search_placeholder" to "Search your Holiday Destination",
        "hero_banner_title" to "BOOK YOUR TOUR WITH US",
        "hero_banner_subtitle" to "Domestic Tour | International tour",
        "popular_destinations_title" to "Popular Destinations",
        "popular_destinations_subtitle" to "Explore amazing places",

        // Fees and limits
        "service_fee_percent" to 5.0,
        "max_comparison_items" to 3L,

        // Messages
        "maintenance_message" to "We are currently under maintenance. Please check back later.",
        "force_update_message" to "A new version is available. Please update your app to continue.",
        "optional_update_message" to "A new version is available. Would you like to update?",

        // Announcement
        "announcement_title" to "",
        "announcement_message" to "",
        "announcement_dismissible" to true,
        "announcement_link" to "",

        // API
        "api_url" to "https://api.example.com"
    )

    init {
        configureRemoteConfig()
    }

    // ---------- Initialization ----------

    private fun configureRemoteConfig() {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (isDebugMode()) 0 else 3600 // 1 hour in production
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(remoteConfigDefaults)
    }

    private fun isDebugMode(): Boolean {
        return try {
            // Check if debug.keystore exists - heuristic for debug build
            android.util.Log.isLoggable(TAG, android.util.Log.DEBUG)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Fetch Remote Config and Firestore document simultaneously, merging results.
     * Firestore values take precedence over Remote Config for fields that exist in both.
     * Call this once during app startup (e.g. from MainActivity or a splash screen).
     */
    suspend fun fetchConfig(): Result<AppConfig> = try {
        _isLoading.value = true
        _fetchError.value = null

        // Fetch Remote Config
        remoteConfig.fetchAndActivate().await()

        // Fetch Firestore document
        val firestoreDoc = try {
            firestore.collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOCUMENT).get().await()
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Failed to fetch Firestore config: ${e.message}")
            null
        }

        // Merge and produce final config
        val config = mergeConfigs(remoteConfig, firestoreDoc)
        _appConfig.value = config
        _isLoading.value = false

        android.util.Log.i(TAG, "Config fetched successfully. maintenanceMode=${config.maintenanceMode}")
        Result.success(config)
    } catch (e: Exception) {
        android.util.Log.e(TAG, "Failed to fetch config: ${e.message}", e)
        _fetchError.value = e.message
        _isLoading.value = false
        // Return last known config or defaults
        Result.success(_appConfig.value)
    }

    // ---------- Config Merging ----------

    private fun mergeConfigs(rc: FirebaseRemoteConfig, fsDoc: DocumentSnapshot?): AppConfig {
        // Helper to get from Firestore or fallback to Remote Config
        fun <T> get(key: String, rcKey: String = key, default: T): T {
            return if (fsDoc != null && fsDoc.contains(key)) {
                @Suppress("UNCHECKED_CAST")
                (fsDoc.get(key) as? T) ?: default
            } else {
                @Suppress("UNCHECKED_CAST")
                (rc.getValue(rcKey) as? T) ?: default
            }
        }

        return AppConfig(
            // Feature flags
            maintenanceMode = get("maintenance_mode", default = false),
            enableChat = get("enable_chat", default = true),
            enableNotifications = get("enable_notifications", default = true),
            enableWishlist = get("enable_wishlist", default = true),
            enableBooking = get("enable_booking", default = true),
            enableComparison = get("enable_comparison", default = true),
            enableGoogleSignIn = get("enable_google_sign_in", default = true),
            enableAnonymousSignIn = get("enable_anonymous_sign_in", default = true),

            // Version
            latestVersion = (get<Long>("latest_version", default = 1L)).toInt(),
            minSupportedVersion = (get<Long>("min_supported_version", default = 1L)).toInt(),

            // UI strings
            searchPlaceholder = get("search_placeholder", default = "Search your Holiday Destination"),
            heroBannerTitle = get("hero_banner_title", default = "BOOK YOUR TOUR WITH US"),
            heroBannerSubtitle = get("hero_banner_subtitle", default = "Domestic Tour | International tour"),
            popularDestinationsTitle = get("popular_destinations_title", default = "Popular Destinations"),
            popularDestinationsSubtitle = get("popular_destinations_subtitle", default = "Explore amazing places"),

            // Fees and limits
            serviceFeePercent = get("service_fee_percent", default = 5.0),
            maxComparisonItems = (get<Long>("max_comparison_items", default = 3L)).toInt(),

            // Messages
            maintenanceMessage = get("maintenance_message", default = "We are currently under maintenance. Please check back later."),
            forceUpdateMessage = get("force_update_message", default = "A new version is available. Please update your app to continue."),
            optionalUpdateMessage = get("optional_update_message", default = "A new version is available. Would you like to update?"),

            // Announcement
            announcementTitle = get("announcement_title", default = ""),
            announcementMessage = get("announcement_message", default = ""),
            announcementDismissible = get("announcement_dismissible", default = true),
            announcementLink = get("announcement_link", default = ""),

            // API
            apiUrl = get("api_url", default = "https://api.example.com"),

            // Quick Reply Messages (from Firestore app_config/global)
            // Use safe cast with type erasure workaround - Firestore stores arrays as List of Strings
            buyerQuickReplies = (get<Any>("buyerQuickReplies", default = listOf<String>()) as? List<*>)?.mapNotNull { it as? String } ?: listOf(
                "Is this package still available?",
                "Can you provide more details?",
                "Are dates flexible?",
                "Do you offer group discounts?"
            ),
            sellerQuickReplies = (get<Any>("sellerQuickReplies", default = listOf<String>()) as? List<*>)?.mapNotNull { it as? String } ?: listOf(
                "Yes, it's available. When are you planning to travel?",
                "Would you like me to send the complete itinerary?",
                "How many people are travelling?",
                "We have a special offer going on, would you like to hear about it?"
            )
        )
    }

    // ---------- Public API ----------

    /**
     * Check if the app version is below the minimum supported version.
     * If true, the app should force the user to update before continuing.
     */
    fun isForceUpdateRequired(currentVersionCode: Int): Boolean {
        return currentVersionCode < _appConfig.value.minSupportedVersion
    }

    /**
     * Check if an optional update is available (version below latest but above minimum).
     */
    fun isOptionalUpdateAvailable(currentVersionCode: Int): Boolean {
        return currentVersionCode in _appConfig.value.minSupportedVersion until _appConfig.value.latestVersion
    }

    /**
     * Open the app's Play Store page for the user to update.
     */
    fun openPlayStore(activity: Activity, packageName: String = activity.packageName) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            // Fallback to browser if Play Store is not installed
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        }
    }

    /**
     * Open a custom URL for announcements (e.g. promo page).
     */
    fun openAnnouncementLink(activity: Activity, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to open announcement link: ${e.message}")
        }
    }

    /**
     * Trigger a manual refresh of the config from both Remote Config and Firestore.
     */
    suspend fun refreshConfig(): Result<AppConfig> {
        return fetchConfig()
    }
}

/**
 * Composable function to observe the app config state.
 * Returns a State<AppConfig> that updates when the config changes.
 */
@Composable
fun rememberAppConfig(): State<AppConfig> {
    val configManager = rememberConfigManager()
    return produceState(initialValue = configManager.appConfig.value) {
        configManager.appConfig.collect { value ->
            this.value = value
        }
    }
}

/**
 * Composable function to get the ConfigManager singleton.
 */
@Composable
fun rememberConfigManager(): ConfigManager {
    return ConfigManager.getInstance()
}