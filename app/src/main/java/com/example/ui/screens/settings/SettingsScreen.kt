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
import com.example.data.model.User
import com.example.data.repository.AppRepository
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
    val isOfflineSimulated by repository.isOfflineSimulated.collectAsState()
    val popupRepliesEnabled by repository.popupRepliesEnabled.collectAsState()

    var showProfileEditDialog by remember { mutableStateOf(false) }
    var showPinSetupDialog by remember { mutableStateOf(false) }

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

                        // Offline Mode Simulator Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isOfflineSimulated) Icons.Default.CloudOff else Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = if (isOfflineSimulated) Color(0xFFEF4444) else PurpleE2EEGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Offline Mode Simulation", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                                    Text(text = "Queues outgoing messages; auto-syncs when online", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                            Switch(
                                checked = isOfflineSimulated,
                                onCheckedChange = { repository.setOfflineSimulated(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFDC2626)),
                                modifier = Modifier.testTag("offline_mode_switch")
                            )
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

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    if (showProfileEditDialog) {
        ProfileEditDialog(
            user = currentUser,
            onDismiss = { showProfileEditDialog = false },
            onSaveProfile = { name, email, status, dept, avatar, phone ->
                repository.updateCurrentUser(name, email, status, dept)
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
}
