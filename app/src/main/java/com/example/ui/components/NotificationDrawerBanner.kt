package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import coil.compose.AsyncImage
import com.example.data.repository.AppRepository
import com.example.ui.theme.*

@Composable
fun NotificationDrawerBanner(
    event: AppRepository.NotificationEvent?,
    onDismiss: () -> Unit,
    onOpenChat: (String) -> Unit,
    onQuickReply: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isReplyingInline by remember(event?.messageId) { mutableStateOf(false) }
    var inlineReplyText by remember(event?.messageId) { mutableStateOf("") }

    AnimatedVisibility(
        visible = event != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        if (event != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("notification_drawer_banner"),
                shape = RoundedCornerShape(16.dp),
                color = PurpleSurfaceVariant,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder)
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
                        // Sender Avatar
                        Box(modifier = Modifier.size(42.dp)) {
                            if (event.senderAvatar.isNotEmpty()) {
                                AsyncImage(
                                    model = event.senderAvatar,
                                    contentDescription = event.senderName,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(PurplePrimaryDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = event.senderName.take(1),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    onOpenChat(event.conversationId)
                                    onDismiss()
                                }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = event.senderName,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Encrypted",
                                    tint = PurpleAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = event.textSnippet,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Inline Reply Toggle
                        IconButton(
                            onClick = { isReplyingInline = !isReplyingInline },
                            modifier = Modifier.size(36.dp).testTag("notif_reply_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Reply,
                                contentDescription = "Quick Reply",
                                tint = PurpleAccent
                            )
                        }

                        // Dismiss
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = TextMuted
                            )
                        }
                    }

                    // Expandable Inline Reply Input Box
                    AnimatedVisibility(visible = isReplyingInline) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = inlineReplyText,
                                    onValueChange = { inlineReplyText = it },
                                    placeholder = { Text("Quick reply...", fontSize = 12.sp, color = TextMuted) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("inline_reply_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(24.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PurplePrimary,
                                        unfocusedBorderColor = PurpleBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedContainerColor = PurpleDeepBg,
                                        unfocusedContainerColor = PurpleDeepBg
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        if (inlineReplyText.isNotBlank()) {
                                            onQuickReply(event.conversationId, inlineReplyText)
                                            inlineReplyText = ""
                                            isReplyingInline = false
                                        }
                                    },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(PurplePrimary)
                                        .testTag("inline_reply_send")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
