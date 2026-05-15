package com.ldreams.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.ldreams.app.ui.components.DreamBackground
import com.ldreams.app.ui.components.PermissionBanner
import com.ldreams.app.service.AppUpdate
import com.ldreams.app.service.UpdateChecker
import com.ldreams.app.ui.theme.NeonCyan
import com.ldreams.app.ui.theme.NeonPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.preferences.collectAsState(initial = com.ldreams.app.data.repository.UserPreferences())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<AppUpdate?>(null) }
    val notificationPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else true

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            DreamBackground()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Notification permission status
                if (!notificationPermissionGranted) {
                    PermissionBanner(
                        title = "Notifications Disabled",
                        description = "Enable notification access to receive reality check reminders, morning alerts, and bedtime prompts",
                        isGranted = false,
                        onRequestPermission = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Notification Settings
                SettingsSection("Notifications") {
                    SettingsToggle(
                        title = "Reality Check Reminders",
                        subtitle = "Random notifications 3-5 times daily",
                        icon = Icons.Default.Psychology,
                        checked = prefs.realityCheckEnabled,
                        onCheckedChange = { viewModel.toggleRealityCheck(it) }
                    )
                    SettingsToggle(
                        title = "Morning Dream Reminder",
                        subtitle = "Remind to write dreams after waking",
                        icon = Icons.Default.NotificationsActive,
                        checked = prefs.morningReminderEnabled,
                        onCheckedChange = { viewModel.toggleMorningReminder(it) }
                    )
                    SettingsToggle(
                        title = "Bedtime Reminder",
                        subtitle = "Lucid dreaming prep before sleep",
                        icon = Icons.Default.Bedtime,
                        checked = prefs.bedtimeReminderEnabled,
                        onCheckedChange = { viewModel.toggleBedtimeReminder(it) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sound & Vibration
                SettingsSection("Sound & Vibration") {
                    SettingsToggle(
                        title = "Sound Alarms",
                        subtitle = "Play gentle sound for alarms",
                        icon = Icons.Default.MusicNote,
                        checked = prefs.soundEnabled,
                        onCheckedChange = { viewModel.toggleSound(it) }
                    )
                    SettingsToggle(
                        title = "Vibration",
                        subtitle = "Vibrate for notifications",
                        icon = Icons.Default.Vibration,
                        checked = prefs.vibrationEnabled,
                        onCheckedChange = { viewModel.toggleVibration(it) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Privacy
                SettingsSection("Privacy") {
                    SettingsToggle(
                        title = "Privacy Lock",
                        subtitle = "Require authentication to open app",
                        icon = Icons.Default.Lock,
                        checked = prefs.privacyLockEnabled,
                        onCheckedChange = { viewModel.togglePrivacyLock(it) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // About
                SettingsSection("About") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "LDREAMS v1.0.0",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Your complete lucid dreaming companion. Build better dream recall, track progress, and unlock the world of lucid dreaming.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Check for Updates button
                            Button(
                                onClick = {
                                    isCheckingUpdate = true
                                    scope.launch {
                                        val result = UpdateChecker.checkForUpdate()
                                        updateInfo = result
                                        isCheckingUpdate = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !isCheckingUpdate
                            ) {
                                if (isCheckingUpdate) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Checking...")
                                } else {
                                    Icon(Icons.Default.SystemUpdate, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Check for Updates", fontWeight = FontWeight.SemiBold)
                                }
                            }

                            // Update result
                            updateInfo?.let { update ->
                                Spacer(modifier = Modifier.height(8.dp))
                                if (update.isAvailable) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = NeonCyan.copy(alpha = 0.15f)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "New version ${update.latestVersion} available!",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = NeonCyan
                                            )
                                            if (update.releaseNotes.isNotBlank()) {
                                                Text(
                                                    text = update.releaseNotes,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 3
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = { UpdateChecker.openDownloadPage(context, update.downloadUrl) },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                                shape = RoundedCornerShape(14.dp)
                                            ) {
                                                Text("Download Update", fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "You're up to date!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = NeonCyan,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    content()
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = NeonPurple,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NeonPurple,
                    checkedTrackColor = NeonPurple.copy(alpha = 0.3f)
                )
            )
        }
    }
}
