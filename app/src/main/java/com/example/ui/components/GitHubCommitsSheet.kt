package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.AppUpdateInfo
import com.example.ui.theme.*

@Composable
fun GitHubCommitsSheet(
    commits: List<AppUpdateInfo>,
    currentCommitSha: String,
    onDismiss: () -> Unit,
    onDownloadApk: (String) -> Unit,
    onRefresh: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .padding(vertical = 16.dp)
                .testTag("github_commits_sheet"),
            shape = RoundedCornerShape(24.dp),
            color = DarkCardBackground,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, PurpleBorder),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Source,
                        contentDescription = null,
                        tint = PurpleNeonGlow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GitHub Repository Commits",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "merversaus/mva-business-chat • branch: main",
                            fontSize = 11.sp,
                            color = PurpleAccent
                        )
                    }
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PurpleAccent)
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // List of Commits
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(commits) { commit ->
                        val isInstalled = commit.commitSha.equals(currentCommitSha, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isInstalled) PurpleSurfaceVariant else PurpleSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isInstalled) PurpleNeonGlow else PurpleBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (commit.authorAvatar.isNotBlank()) {
                                        AsyncImage(
                                            model = commit.authorAvatar,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = commit.authorName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = commit.dateString,
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    if (isInstalled) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = PurpleE2EEGreen.copy(alpha = 0.2f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleE2EEGreen)
                                        ) {
                                            Text(
                                                text = "CURRENTLY INSTALLED",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PurpleE2EEGreen,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color.Black.copy(alpha = 0.5f)
                                        ) {
                                            Text(
                                                text = "SHA: ${commit.commitSha}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PurpleAccent,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = commit.commitMessage,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${commit.versionName} • ${(commit.downloadSizeBytes / 1_000_000.0).let { String.format("%.1f MB", it) }}",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )

                                    FilledTonalButton(
                                        onClick = { onDownloadApk(commit.downloadUrl) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = PurplePrimaryDark,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Download APK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
