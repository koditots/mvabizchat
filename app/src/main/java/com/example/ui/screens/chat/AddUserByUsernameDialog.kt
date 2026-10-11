package com.example.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.User
import com.example.data.remote.InviteDetails
import com.example.data.remote.OnlineUserSearchResult
import com.example.data.repository.AppRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AddUserByUsernameDialog(
    repository: AppRepository,
    onDismiss: () -> Unit,
    onUserAddedToChat: (conversationId: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isCheckingOnline by remember { mutableStateOf(false) }
    var searchResult by remember { mutableStateOf<OnlineUserSearchResult?>(null) }
    var inviteDetails by remember { mutableStateOf<InviteDetails?>(null) }
    var copiedFeedback by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val suggestedUsernames = listOf("jordan.lee", "sophia.eng", "liam.oc", "aaliyah.khan", "matthew.v")

    fun performOnlineCheck(query: String) {
        if (query.isBlank()) return
        isCheckingOnline = true
        searchResult = null
        inviteDetails = null
        copiedFeedback = null

        scope.launch {
            val result = repository.searchOnlineUser(query)
            searchResult = result
            if (result is OnlineUserSearchResult.NotFound) {
                inviteDetails = repository.generateInvite(query)
            }
            isCheckingOnline = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PurpleSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("add_user_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PurplePrimaryDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = PurpleNeonGlow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Add Contact",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = TextPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(PurpleE2EEGreen)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "GitHub Online Registry",
                                    fontSize = 11.sp,
                                    color = PurpleAccent
                                )
                            }
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Add someone to your chat list using their unique username or email address. The app verifies registration online via GitHub database.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        searchResult = null
                        inviteDetails = null
                        copiedFeedback = null
                    },
                    placeholder = { Text("e.g. @jordan.lee or jordan.lee@mvaglobal.com", fontSize = 12.sp, color = TextMuted) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_user_input_field"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = PurpleBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = PurpleDeepBg,
                        unfocusedContainerColor = PurpleDeepBg
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Suggestion Chips from GitHub database
                Text(
                    text = "Registered in GitHub Database:",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(suggestedUsernames) { suggested ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PurpleSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                            modifier = Modifier.clickable {
                                searchQuery = suggested
                                performOnlineCheck(suggested)
                            }
                        ) {
                            Text(
                                text = "@$suggested",
                                color = PurpleNeonGlow,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Check Online Button
                Button(
                    onClick = { performOnlineCheck(searchQuery) },
                    enabled = searchQuery.isNotBlank() && !isCheckingOnline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("check_online_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    if (isCheckingOnline) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Checking GitHub Registry...", fontSize = 13.sp)
                    } else {
                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Check Online & Add", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Result Cards
                when (val result = searchResult) {
                    is OnlineUserSearchResult.Found -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = PurpleSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, PurpleE2EEGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("online_user_found_card")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(46.dp)) {
                                        AsyncImage(
                                            model = result.user.avatarUrl,
                                            contentDescription = result.user.name,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = result.user.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = PurpleE2EEGreen, modifier = Modifier.size(14.dp))
                                        }
                                        Text(text = "@${result.user.username} • ${result.user.email}", fontSize = 11.sp, color = PurpleAccent)
                                        Text(text = result.user.statusMessage, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = PurpleE2EEGreen, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Public Key: ${result.user.publicKey} • E2EE Ready",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val newConv = repository.addOnlineUserToChat(result.user)
                                        onUserAddedToChat(newConv.id)
                                        onDismiss()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("start_chat_with_found_user_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PurpleE2EEGreen)
                                ) {
                                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add to Chat List & Start E2EE Chat", fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    }

                    is OnlineUserSearchResult.NotFound -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = PurpleSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("user_not_found_card")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PersonOff, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = "User Not Registered Online", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                        Text(text = "No matching account in GitHub database", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (inviteDetails != null) {
                                    Text(
                                        text = "Invite ${result.query} to register on GitHub & join MVA Business Chat:",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = PurpleDeepBg,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = "Invite Code: ${inviteDetails?.inviteCode}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PurpleNeonGlow)
                                                Text(text = inviteDetails?.inviteLink ?: "", fontSize = 10.sp, color = TextMuted, maxLines = 1)
                                            }
                                            IconButton(onClick = {
                                                inviteDetails?.let {
                                                    clipboardManager.setText(AnnotatedString(it.messageText))
                                                    copiedFeedback = "Copied GitHub invite link & code! 📋"
                                                }
                                            }) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Invite", tint = PurpleAccent)
                                            }
                                        }
                                    }

                                    if (copiedFeedback != null) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = copiedFeedback ?: "", fontSize = 11.sp, color = PurpleE2EEGreen, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }

                    is OnlineUserSearchResult.Error -> {
                        Text(
                            text = result.message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    null -> {}
                }
            }
        }
    }
}
