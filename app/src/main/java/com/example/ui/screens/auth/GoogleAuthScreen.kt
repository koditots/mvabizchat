package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ui.theme.*

@Composable
fun GoogleAuthScreen(
    currentEmail: String,
    onDismiss: () -> Unit,
    onSelectAccount: (name: String, email: String) -> Unit
) {
    val accounts = listOf(
        Triple("Alex Rivera", "alex.rivera@mvaglobal.com", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150"),
        Triple("Alex (Enterprise Admin)", "admin.alex@mva-enterprise.org", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150"),
        Triple("Alex (Work Dev)", "alex.r@cloud-arch.io", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150")
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PurpleSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("google_auth_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Google",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Registration",
                            fontSize = 18.sp,
                            color = PurpleAccent
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Authenticate with your Google Workspace profile to enable seamless multi-device synchronization and encrypted cloud backup.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                accounts.forEach { (name, email, avatar) ->
                    val isCurrent = email == currentEmail
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCurrent) PurplePrimaryDark else PurpleSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isCurrent) PurpleNeonGlow else PurpleBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable {
                                onSelectAccount(name, email)
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = avatar,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text(text = email, fontSize = 12.sp, color = if (isCurrent) PurpleNeonGlow else TextSecondary)
                            }
                            if (isCurrent) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = PurplePrimary
                                ) {
                                    Text(
                                        text = "Active",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = PurpleE2EEGreen, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Protected by Google 2FA & MVA Zero-Trust Ratchet", fontSize = 10.sp, color = TextMuted)
                }
            }
        }
    }
}
