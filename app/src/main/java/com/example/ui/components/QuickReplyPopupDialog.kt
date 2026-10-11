package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.repository.AppRepository
import com.example.ui.theme.*

@Composable
fun QuickReplyPopupDialog(
    event: AppRepository.NotificationEvent?,
    onDismiss: () -> Unit,
    onSendReply: (conversationId: String, replyText: String) -> Unit
) {
    if (event == null) return

    var replyText by remember(event.messageId) { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PurpleSurface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, PurplePrimary),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("quick_reply_popup_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(46.dp)) {
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

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = event.senderName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "E2EE",
                                tint = PurpleE2EEGreen,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = "Quick Popup Reply • Encrypted",
                            fontSize = 11.sp,
                            color = PurpleAccent
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Received Message Preview Bubble
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PurpleIncomingBubble,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = event.textSnippet,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reply Input Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("Type reply...", fontSize = 13.sp, color = TextMuted) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("popup_reply_textfield"),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = PurpleBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = PurpleDeepBg,
                            unfocusedContainerColor = PurpleDeepBg
                        )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                onSendReply(event.conversationId, replyText)
                                replyText = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(PurplePrimary)
                            .testTag("popup_reply_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
