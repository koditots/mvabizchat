package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUpdateInfo
import com.example.ui.theme.*

@Composable
fun AppUpdateBanner(
    updateInfo: AppUpdateInfo?,
    autoUpdateToast: String?,
    onOpenUpdateDetails: () -> Unit,
    onDownloadApkFromGitHub: () -> Unit,
    onDismissNotification: () -> Unit,
    onDismissToast: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. If background auto-update just occurred silently
    AnimatedVisibility(
        visible = autoUpdateToast != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        if (autoUpdateToast != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("auto_update_silent_toast"),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E1035),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(PurpleNeonGlow, PurpleE2EEGreen))),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PurpleE2EEGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Updated",
                            tint = PurpleE2EEGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AUTOMATIC BACKGROUND UPDATE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PurpleE2EEGreen,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = autoUpdateToast,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onDismissToast,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    // 2. If new GitHub update is waiting for manual install / APK download
    AnimatedVisibility(
        visible = updateInfo != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        if (updateInfo != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("app_update_available_banner"),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1B0B2E),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(PurplePrimary, PurpleAccent))),
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // GitHub / Update Icon
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(PurplePrimaryDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdateAlt,
                                contentDescription = "Update Available",
                                tint = PurpleNeonGlow,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "GITHUB UPDATE PUSHED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PurpleAccent,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = PurpleSurfaceVariant
                                ) {
                                    Text(
                                        text = updateInfo.versionName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PurpleNeonGlow,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = updateInfo.commitMessage,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "By ${updateInfo.authorName} • SHA #${updateInfo.commitSha}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        IconButton(
                            onClick = onDismissNotification,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Direct Download APK Button
                        OutlinedButton(
                            onClick = onDownloadApkFromGitHub,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PurpleAccent
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("banner_download_apk_btn")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download APK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Update Details / In-App Install Button
                        Button(
                            onClick = onOpenUpdateDetails,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PurplePrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("banner_view_update_btn")
                        ) {
                            Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Update Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
