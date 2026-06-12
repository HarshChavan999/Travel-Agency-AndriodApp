package com.example.mychat.viewmodel

import android.app.Activity
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mychat.data.config.AppConfig
import com.example.mychat.data.config.ConfigManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel that provides access to the remote configuration and
 * handles version checks, maintenance mode, and announcements.
 */
class ConfigViewModel : ViewModel() {

    private val configManager = ConfigManager.getInstance()

    val appConfig: StateFlow<AppConfig> = configManager.appConfig
    val isLoading: StateFlow<Boolean> = configManager.isLoading
    val fetchError: StateFlow<String?> = configManager.fetchError

    // Dialog visibility states
    private val _showMaintenanceDialog = MutableStateFlow(false)
    val showMaintenanceDialog: StateFlow<Boolean> = _showMaintenanceDialog.asStateFlow()

    private val _showForceUpdateDialog = MutableStateFlow(false)
    val showForceUpdateDialog: StateFlow<Boolean> = _showForceUpdateDialog.asStateFlow()

    private val _showOptionalUpdateDialog = MutableStateFlow(false)
    val showOptionalUpdateDialog: StateFlow<Boolean> = _showOptionalUpdateDialog.asStateFlow()

    private val _showAnnouncement = MutableStateFlow(false)
    val showAnnouncement: StateFlow<Boolean> = _showAnnouncement.asStateFlow()

    private val _announcementDismissed = MutableStateFlow(false)
    val announcementDismissed: StateFlow<Boolean> = _announcementDismissed.asStateFlow()

    private var currentVersionCode: Int = 1

    init {
        fetchRemoteConfig()
    }

    /**
     * Fetch remote config on initialization.
     */
    fun fetchRemoteConfig() {
        viewModelScope.launch {
            configManager.fetchConfig()
        }
    }

    /**
     * Called when the app starts to check for maintenance mode, updates, and announcements.
     * Should be invoked from MainActivity after config is fetched.
     */
    fun performStartupChecks(activity: Activity) {
        val config = appConfig.value

        // 1. Maintenance mode check
        if (config.maintenanceMode) {
            _showMaintenanceDialog.value = true
            return
        }

        // 2. Get current app version
        currentVersionCode = try {
            val pkgInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
            pkgInfo.longVersionCode.toInt()
        } catch (e: PackageManager.NameNotFoundException) {
            1
        }

        // 3. Force update check
        if (configManager.isForceUpdateRequired(currentVersionCode)) {
            _showForceUpdateDialog.value = true
            return
        }

        // 4. Optional update check
        if (configManager.isOptionalUpdateAvailable(currentVersionCode)) {
            _showOptionalUpdateDialog.value = true
        }

        // 5. Announcement check
        if (config.announcementTitle.isNotBlank() && config.announcementMessage.isNotBlank() && !_announcementDismissed.value) {
            _showAnnouncement.value = true
        }
    }

    fun dismissMaintenance() {
        _showMaintenanceDialog.value = false
    }

    fun dismissForceUpdate() {
        _showForceUpdateDialog.value = false
    }

    fun dismissOptionalUpdate() {
        _showOptionalUpdateDialog.value = false
    }

    fun dismissAnnouncement() {
        _showAnnouncement.value = false
        _announcementDismissed.value = true
    }

    fun openPlayStore(activity: Activity) {
        configManager.openPlayStore(activity)
    }

    fun openAnnouncementLink(activity: Activity) {
        val url = appConfig.value.announcementLink
        if (url.isNotBlank()) {
            configManager.openAnnouncementLink(activity, url)
        }
    }

    fun refreshConfig() {
        viewModelScope.launch {
            configManager.refreshConfig()
        }
    }
}