package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.data.repository.AppRepository
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.GitHubCommitsSheet
import com.example.ui.screens.profile.ProfileEditDialog
import com.example.ui.screens.security.PinSetupDialog
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    repository: AppRepository,
    currentUser: User,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onOpenMultiDevice: () -> Unit,
    onOpenBackupRestore: () -> Unit,
    onSwitchGoogleAccount: () -> Unit,
    onLockAppNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBiometricEnabled by repository.biometricEnabled.collectAsState()
    val isAppLocked by repository.isAppLocked.collectAsState()
    val pinCode by repository.pinCode.collectAsState()
    val isOnline by repository.isOnline.collectAsState()
    val popupRepliesEnabled by repository.popupRepliesEnabled.collectAsState()

    val updaterConfig by repository.updaterConfig.collectAsState()
    val updateState by repository.updateState.collectAsState()
    val currentVersionName by repository.currentVersionName.collectAsState()
    val currentCommitSha by repository.currentCommitSha.collectAsState()
    val recentCommits by repository.recentGitHubCommits.collectAsState()

    var showProfileEditDialog by remember { mutableStateOf(false) }
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var selectedUpdateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var showCommitsSheet by remember { mutableStateOf(false) }
    var showRepoConfigDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .testTag("settings_screen")
    ) {
        Text(
            text = "Settings & Security",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Configure Privacy, E2EE, Biometrics & Cloud Sync",
            fontSize = 12.sp,
            color = PurpleAccent
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile Card Header
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = PurpleSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showProfileEditDialog = true }
                        .testTag("settings_profile_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = currentUser.avatarUrl,
                            contentDescription = currentUser.name,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = currentUser.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Text(text = currentUser.email, fontSize = 12.sp, color = TextSecondary)
                            Text(text = currentUser.statusMessage, fontSize = 11.sp, color = PurpleAccent, maxLines = 1)
                        }
                        IconButton(onClick = { showProfileEditDialog = true }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile", tint = PurpleAccent)
                        }
                    }
                }
            }

            // Security & Privacy Group
            item {
                Text(
                    text = "SECURITY & APP PROTECTION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpleAccent,
                    letterSpacing = 1.sp
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PurpleSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Biometric Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = PurpleNeonGlow, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Biometric Authentication", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                                    Text(text = "Unlock using fingerprint or Face ID", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                            Switch(
                                checked = isBiometricEnabled,
                                onCheckedChange = { repository.setBiometricEnabled(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PurplePrimary),
                                modifier = Modifier.testTag("biometric_switch")
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = PurpleBorder)

                        // 4-Digit PIN Security
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPinSetupDialog = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Pin, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Security PIN Lock", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                                    Text(text = "Current PIN: •••• (Tap to modify)", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = PurpleBorder)

                        // Lock Now Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLockAppNow() }
                                .testTag("lock_app_now_row"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Lock App Now", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                                    Text(text = "Requires PIN / Biometric to re-open", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                        }
                    }
                }
            }

            // Sync, Multi-Device & Backup Group
            item {
                Text(
                    text = "SYNCHRONIZATION & STORAGE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpleAccent,
                    letterSpacing = 1.sp
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PurpleSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Multi-device sync
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenMultiDevice() }
                                .testTag("linked_devices_row"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Devices, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Multi-Device Sync", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                                    Text(text = "Linked Web & Desktop sessions", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = PurpleBorder)

                        // Backup & Restore
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenBackupRestore() }
                                .testTag("backup_restore_row"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = PurpleNeonGlow, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Chat Backup & Restore", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                                    Text(text = "Offline export & Google Cloud E2EE", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = PurpleBorder)

                        // Real-time Network Connectivity Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = if (isOnline) PurpleE2EEGreen else Color(0xFFEF4444),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Enterprise Network Connection", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                                    Text(
                                        text = if (isOnline) "Connected • Validated Route" else "Offline • Outgoing queue active",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isOnline) PurplePrimaryDark else Color(0xFF3F1420),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isOnline) PurpleNeonGlow else Color(0xFFEF4444))
                            ) {
                                Text(
                                    text = if (isOnline) "ONLINE" else "OFFLINE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOnline) PurpleNeonGlow else Color(0xFFEF4444),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Notifications & Display Group
            item {
                Text(
                    text = "NOTIFICATIONS & APPEARANCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpleAccent,
                    letterSpacing = 1.sp
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PurpleSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Pop-up Message on Screen for quick replies
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ChatBubble, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Pop-up Quick Replies", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                                    Text(text = "Show heads-up popup on screen for incoming msgs", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                            Switch(
                                checked = popupRepliesEnabled,
                                onCheckedChange = { repository.setPopupRepliesEnabled(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PurplePrimary),
                                modifier = Modifier.testTag("popup_replies_switch")
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = PurpleBorder)

                        // Dark mode toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DarkMode, contentDescription = null, tint = PurpleNeonGlow, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Dark Mode Aesthetic", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                                    Text(text = "Purple & Black Sleek Theme", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { onToggleDarkMode() },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PurplePrimary),
                                modifier = Modifier.testTag("dark_mode_switch")
                            )
                        }
                    }
                }
            }

            // GITHUB APP UPDATER & AUTO-UPDATE
            item {
                Text(
                    text = "GITHUB APP UPDATER & AUTO-UPDATE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpleAccent,
                    letterSpacing = 1.sp
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PurpleSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                    modifier = Modifier.fillMaxWidth().testTag("github_updater_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Current Version & Repository Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PurplePrimaryDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = PurpleNeonGlow,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Installed Version: $currentVersionName",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.Black.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = "#$currentCommitSha",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PurpleAccent,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "GitHub: ${updaterConfig.repoOwner}/${updaterConfig.repoName} (${updaterConfig.branch})",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            // Status Indicator Badge
                            when (updateState) {
                                is UpdateState.Checking -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = PurpleNeonGlow
                                    )
                                }
                                is UpdateState.UpdateAvailable -> {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = PurplePrimary,
                                        modifier = Modifier.clickable {
                                            selectedUpdateInfo = (updateState as UpdateState.UpdateAvailable).update
                                            showUpdateDialog = true
                                        }
                                    ) {
                                        Text(
                                            text = "Update Avail!",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                is UpdateState.AutoUpdatedSilently -> {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0D9488),
                                        modifier = Modifier.clickable {
                                            selectedUpdateInfo = (updateState as UpdateState.AutoUpdatedSilently).update
                                            showUpdateDialog = true
                                        }
                                    ) {
                                        Text(
                                            text = "Auto-Updated!",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                else -> {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = PurpleE2EEGreen.copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleE2EEGreen)
                                    ) {
                                        Text(
                                            text = "Up to Date",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PurpleE2EEGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = PurpleBorder)

                        // Toggle 1: Auto-update from background without interruption
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.AutoMode, contentDescription = null, tint = PurpleNeonGlow, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Background Auto-Update", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                                    Text(text = "Automatically update from background without interruption when pushed to GitHub", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = updaterConfig.autoUpdateInBackground,
                                onCheckedChange = { repository.toggleAutoUpdateInBackground(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PurplePrimary),
                                modifier = Modifier.testTag("auto_update_bg_switch")
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = PurpleBorder)

                        // Toggle 2: Receive notification for updates
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Push Notifications for Updates", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                                    Text(text = "Receive drawer banner alert when changes are pushed to GitHub", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = updaterConfig.notifyOnPush,
                                onCheckedChange = { repository.toggleNotifyOnPush(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PurplePrimary),
                                modifier = Modifier.testTag("notify_on_push_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons Grid
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Check for Updates Now
                                Button(
                                    onClick = { repository.checkForUpdates(silent = false) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                    modifier = Modifier.weight(1f).height(40.dp).testTag("check_updates_btn")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Check Updates", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                // Deploy / Push New Update to GitHub
                                Button(
                                    onClick = { repository.simulateGitHubPushEvent() },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B21A8)),
                                    modifier = Modifier.weight(1f).height(40.dp).testTag("push_update_btn")
                                ) {
                                    Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Deploy Update", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Download APK from GitHub
                                Button(
                                    onClick = { repository.openGitHubApkDownload() },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimaryDark),
                                    modifier = Modifier.weight(1f).height(38.dp).testTag("download_apk_github_btn")
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Download APK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // View Commits / Changelog
                                OutlinedButton(
                                    onClick = {
                                        repository.loadRecentCommits()
                                        showCommitsSheet = true
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PurpleAccent),
                                    modifier = Modifier.weight(1f).height(38.dp).testTag("view_commits_btn")
                                ) {
                                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("View Commits", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            // Google Workspace Registration
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PurpleSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSwitchGoogleAccount() }
                        .testTag("google_account_row")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Google Enterprise Registration", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                            Text(text = "Signed in as ${currentUser.email}", fontSize = 11.sp, color = PurpleAccent)
                        }
                        Icon(Icons.Default.SyncAlt, contentDescription = "Switch Account", tint = PurpleAccent)
                    }
                }
            }

            // Sign Out of Workspace
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF381018),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { repository.logout() }
                        .testTag("logout_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Sign Out", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign Out of Enterprise Account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFFCA5A5)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    if (showProfileEditDialog) {
        ProfileEditDialog(
            user = currentUser,
            onDismiss = { showProfileEditDialog = false },
            onSaveProfile = { name, email, username, status, dept, avatar, phone ->
                repository.updateCurrentUser(name, email, username, status, dept)
            }
        )
    }

    if (showPinSetupDialog) {
        PinSetupDialog(
            currentPin = pinCode,
            onDismiss = { showPinSetupDialog = false },
            onSavePin = { repository.updatePinCode(it) }
        )
    }

    if (showUpdateDialog && selectedUpdateInfo != null) {
        AppUpdateDialog(
            updateInfo = selectedUpdateInfo!!,
            updateState = updateState,
            updaterConfig = updaterConfig,
            onDismiss = { showUpdateDialog = false },
            onDownloadApkFromGitHub = { url -> repository.openGitHubApkDownload(url) },
            onStartInAppDownload = { update -> repository.startDownloadingApk(update) },
            onApplyUpdate = { update ->
                repository.applyInstalledUpdate(update)
                showUpdateDialog = false
            },
            onToggleAutoUpdateBg = { repository.toggleAutoUpdateInBackground(it) }
        )
    }

    if (showCommitsSheet) {
        GitHubCommitsSheet(
            commits = recentCommits,
            currentCommitSha = currentCommitSha,
            onDismiss = { showCommitsSheet = false },
            onDownloadApk = { url -> repository.openGitHubApkDownload(url) },
            onRefresh = { repository.loadRecentCommits() }
        )
    }
}
