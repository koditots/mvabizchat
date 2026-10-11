package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminDashboardScreen(
    repository: AppRepository,
    onBroadcast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val teamMembers by repository.teamMembers.collectAsState()
    val auditLogs by repository.auditLogs.collectAsState()
    val businessProfile by repository.businessProfile.collectAsState()

    var broadcastText by remember { mutableStateOf("") }
    var broadcastSuccess by remember { mutableStateOf(false) }
    var selectedSection by remember { mutableStateOf("TEAM") } // TEAM, BROADCAST, LOGS, BUSINESS

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .testTag("admin_dashboard_screen")
    ) {
        Text(
            text = "Enterprise Administration",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "MVA Business Workspace Controls & E2EE Governance",
            fontSize = 12.sp,
            color = PurpleAccent
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Metrics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AdminMetricCard(
                title = "Total Team",
                value = "${teamMembers.size + 1}",
                icon = Icons.Default.People,
                modifier = Modifier.weight(1f)
            )
            AdminMetricCard(
                title = "Admins",
                value = "${teamMembers.count { it.role == UserRole.ADMIN || it.role == UserRole.OWNER } + 1}",
                icon = Icons.Default.AdminPanelSettings,
                modifier = Modifier.weight(1f)
            )
            AdminMetricCard(
                title = "E2EE Channels",
                value = "100%",
                icon = Icons.Default.Lock,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Tabs for Admin Sections
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "TEAM" to "Members",
                "BROADCAST" to "Broadcast",
                "LOGS" to "Audit Logs",
                "BUSINESS" to "Business"
            ).forEach { (key, label) ->
                val isSelected = selectedSection == key
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) PurplePrimary else PurpleSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PurpleNeonGlow else PurpleBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedSection = key }
                        .testTag("admin_tab_$key")
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section Content
        when (selectedSection) {
            "TEAM" -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = PurpleSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Enterprise Invite Code", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                    Text(text = "Code: MVA-JOIN-2026-HQ", fontSize = 11.sp, color = PurpleNeonGlow)
                                }
                                FilledTonalButton(
                                    onClick = {
                                        repository.addAuditLog("Invite Code", "Generated new team member onboarding invite link")
                                    },
                                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = PurplePrimary)
                                ) {
                                    Text("Copy Link", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }

                    items(teamMembers) { member ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = PurpleSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = member.avatarUrl,
                                    contentDescription = member.name,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = member.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Text(text = "${member.department} • ${member.email}", fontSize = 11.sp, color = TextSecondary)
                                    Text(
                                        text = "Role: ${member.role.name}",
                                        fontSize = 11.sp,
                                        color = when(member.role) {
                                            UserRole.OWNER -> PurpleNeonGlow
                                            UserRole.ADMIN -> PurpleAccent
                                            UserRole.MEMBER -> TextMuted
                                        },
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Role Promotion Toggle Button
                                IconButton(
                                    onClick = {
                                        val nextRole = when(member.role) {
                                            UserRole.MEMBER -> UserRole.ADMIN
                                            UserRole.ADMIN -> UserRole.MEMBER
                                            UserRole.OWNER -> UserRole.OWNER
                                        }
                                        repository.updateUserRole(member.id, nextRole)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = "Change Role",
                                        tint = PurpleAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }
            "BROADCAST" -> {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PurpleSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Campaign, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Broadcast Announcement", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Instantly sends an encrypted administrative announcement to all company channels and team chats.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = broadcastText,
                                onValueChange = {
                                    broadcastText = it
                                    broadcastSuccess = false
                                },
                                placeholder = { Text("Enter announcement message...", fontSize = 13.sp, color = TextMuted) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .testTag("broadcast_input_field"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PurplePrimary,
                                    unfocusedBorderColor = PurpleBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (broadcastText.isNotBlank()) {
                                        onBroadcast(broadcastText)
                                        broadcastText = ""
                                        broadcastSuccess = true
                                    }
                                },
                                enabled = broadcastText.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("send_broadcast_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Broadcast to All Chats", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            if (broadcastSuccess) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "✓ Announcement successfully delivered to all active team conversations!",
                                    fontSize = 11.sp,
                                    color = PurpleE2EEGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            "LOGS" -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(auditLogs) { log ->
                        val timeStr = SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(Date(log.timestamp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PurpleSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = log.action, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PurpleNeonGlow)
                                    Text(text = timeStr, fontSize = 10.sp, color = TextMuted)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = log.details, fontSize = 11.sp, color = TextSecondary)
                                Text(text = "Triggered by: ${log.adminName}", fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
            "BUSINESS" -> {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PurpleSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = businessProfile.businessName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Text(text = businessProfile.category, fontSize = 12.sp, color = PurpleAccent)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = businessProfile.description, fontSize = 12.sp, color = TextSecondary)

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "Hours: ${businessProfile.openingHours}", fontSize = 12.sp, color = TextPrimary)
                            Text(text = "Website: ${businessProfile.website}", fontSize = 12.sp, color = PurpleAccent)

                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PurpleDeepBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "Automated Away Responder:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PurpleNeonGlow)
                                    Text(text = businessProfile.autoAwayMessage, fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = PurpleSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            Text(text = title, fontSize = 10.sp, color = TextMuted)
        }
    }
}
