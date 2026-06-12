package com.example.mychat.ui.components

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mychat.data.config.AppConfig

/**
 * Dialog shown when the app is in maintenance mode.
 * Prevents any further interaction with the app.
 */
@Composable
fun MaintenanceDialog(
    config: AppConfig,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Not dismissable */ },
        title = {
            Text(
                text = "🔧 Under Maintenance",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = config.maintenanceMessage,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Dialog shown when a force update is required.
 * User must update to continue using the app.
 */
@Composable
fun ForceUpdateDialog(
    config: AppConfig,
    activity: Activity,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Not dismissable */ },
        title = {
            Text(
                text = "📲 Update Required",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = config.forceUpdateMessage,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Current version: ${getAppVersion(activity)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(onClick = onUpdate) {
                Text("Update Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Exit")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Dialog shown when an optional update is available.
 * User can choose to update or ignore.
 */
@Composable
fun OptionalUpdateDialog(
    config: AppConfig,
    activity: Activity,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "📲 New Version Available",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = config.optionalUpdateMessage,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Current version: ${getAppVersion(activity)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(onClick = onUpdate) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Dialog shown for dynamic announcements/promotions.
 */
@Composable
fun AnnouncementDialog(
    config: AppConfig,
    onDismiss: () -> Unit,
    onLinkClick: () -> Unit
) {
    val requestDismiss: () -> Unit = {
        if (config.announcementDismissible) {
            onDismiss()
        }
    }
    AlertDialog(
        onDismissRequest = requestDismiss,
        title = {
            Text(
                text = config.announcementTitle,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = config.announcementMessage,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            AnnouncementConfirmButton(config, onLinkClick)
        },
        dismissButton = {
            AnnouncementDismissButton(config, onDismiss)
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun AnnouncementConfirmButton(config: AppConfig, onLinkClick: () -> Unit) {
    if (config.announcementLink.isNotBlank()) {
        Button(onClick = onLinkClick) {
            Text("Learn More")
        }
    }
}

@Composable
private fun AnnouncementDismissButton(config: AppConfig, onDismiss: () -> Unit) {
    if (config.announcementDismissible) {
        TextButton(onClick = onDismiss) {
            Text("Dismiss")
        }
    }
}

private fun getAppVersion(activity: Activity): String {
    return try {
        val pkgInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
        pkgInfo.versionName ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
}