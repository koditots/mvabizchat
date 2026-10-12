package com.example.ui.screens.chat

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversation: Conversation,
    messages: List<Message>,
    currentUser: User,
    onBack: () -> Unit,
    onSendMessage: (content: String, type: MessageType, mediaUrl: String, fileName: String, fileSize: String, replyToId: String?, replyToSnippet: String?) -> Unit,
    onStartCall: (isVideo: Boolean) -> Unit,
    onVerifySafetyNumbers: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var inputText by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<Message?>(null) }
    var showAttachmentSheet by remember { mutableStateOf(false) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onVerifySafetyNumbers() }
                    ) {
                        Box(modifier = Modifier.size(40.dp)) {
                            if (conversation.avatarUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = conversation.avatarUrl,
                                    contentDescription = conversation.title,
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
                                        text = conversation.title.take(1),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = conversation.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Encrypted",
                                    tint = PurpleE2EEGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Text(
                                text = "End-to-end encrypted • Tap to verify",
                                fontSize = 11.sp,
                                color = PurpleAccent
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onStartCall(false) }, modifier = Modifier.testTag("voice_call_button")) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Voice Call", tint = PurpleAccent)
                    }
                    IconButton(onClick = { onStartCall(true) }, modifier = Modifier.testTag("video_call_button")) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = "Video Call", tint = PurpleAccent)
                    }
                    IconButton(onClick = onVerifySafetyNumbers, modifier = Modifier.testTag("verify_e2ee_button")) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = "Verify Encryption", tint = PurpleE2EEGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PurpleSurface,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = DarkBackground,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // E2EE Disclaimer Header Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PurpleSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                    modifier = Modifier.clickable { onVerifySafetyNumbers() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = PurpleE2EEGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Messages and calls are end-to-end encrypted. Tap to verify.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Messages LazyColumn
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                items(messages) { message ->
                    val isMe = message.senderId == currentUser.id
                    MessageBubble(
                        message = message,
                        isMe = isMe,
                        onReply = { replyingToMessage = message }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Quoting / Reply-To banner
            AnimatedVisibility(visible = replyingToMessage != null) {
                if (replyingToMessage != null) {
                    Surface(
                        color = PurpleSurfaceVariant,
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(32.dp)
                                    .background(PurplePrimary, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = replyingToMessage?.senderName ?: "",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = PurpleAccent
                                )
                                Text(
                                    text = replyingToMessage?.content ?: "",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = { replyingToMessage = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel Reply", tint = TextMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Bottom Input Bar
            Surface(
                color = PurpleSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attachment (+)
                    IconButton(
                        onClick = { showAttachmentSheet = true },
                        modifier = Modifier.testTag("attachment_button")
                    ) {
                        Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Attach", tint = PurpleAccent, modifier = Modifier.size(28.dp))
                    }

                    // Text Field
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Encrypted message...", fontSize = 13.sp, color = TextMuted) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("message_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = PurpleBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = PurpleDeepBg,
                            unfocusedContainerColor = PurpleDeepBg
                        ),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    if (inputText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                onSendMessage(
                                    inputText,
                                    MessageType.TEXT,
                                    "",
                                    "",
                                    "",
                                    replyingToMessage?.id,
                                    replyingToMessage?.content
                                )
                                inputText = ""
                                replyingToMessage = null
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PurplePrimary)
                                .testTag("send_message_button")
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    } else {
                        // Quick Voice Note button
                        IconButton(
                            onClick = {
                                onSendMessage(
                                    "🎤 Voice message (0:14)",
                                    MessageType.AUDIO,
                                    "",
                                    "voice_note_enc.m4a",
                                    "184 KB",
                                    replyingToMessage?.id,
                                    replyingToMessage?.content
                                )
                                replyingToMessage = null
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PurplePrimaryDark)
                                .testTag("voice_note_button")
                        ) {
                            Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Note", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    }

    // Attachment Bottom Sheet
    if (showAttachmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false },
            containerColor = PurpleSurface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = PurpleBorder) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Share Encrypted Media & Files",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttachmentOption(
                        icon = Icons.Default.CameraAlt,
                        label = "Camera",
                        bgColor = Color(0xFFD946EF),
                        onClick = {
                            onSendMessage(
                                "📷 Photo",
                                MessageType.IMAGE,
                                "https://images.unsplash.com/photo-1551836022-d5d88e9218df?w=600",
                                "IMG_Encrypted.jpg",
                                "1.2 MB",
                                null,
                                null
                            )
                            showAttachmentSheet = false
                        }
                    )
                    AttachmentOption(
                        icon = Icons.Default.Image,
                        label = "Gallery",
                        bgColor = Color(0xFF8B5CF6),
                        onClick = {
                            onSendMessage(
                                "📷 Project Diagram",
                                MessageType.IMAGE,
                                "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?w=600",
                                "Architecture_Diagram.png",
                                "2.8 MB",
                                null,
                                null
                            )
                            showAttachmentSheet = false
                        }
                    )
                    AttachmentOption(
                        icon = Icons.Default.Videocam,
                        label = "Video",
                        bgColor = Color(0xFFEC4899),
                        onClick = {
                            onSendMessage(
                                "🎥 Walkthrough Video",
                                MessageType.VIDEO,
                                "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=600",
                                "Sprint_Architecture_Walkthrough.mp4",
                                "8.4 MB",
                                null,
                                null
                            )
                            showAttachmentSheet = false
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttachmentOption(
                        icon = Icons.Default.Description,
                        label = "Document",
                        bgColor = Color(0xFF3B82F6),
                        onClick = {
                            onSendMessage(
                                "📄 Enterprise SLA Agreement",
                                MessageType.DOCUMENT,
                                "",
                                "MVA_SLA_Contract_2026.pdf",
                                "540 KB",
                                null,
                                null
                            )
                            showAttachmentSheet = false
                        }
                    )
                    AttachmentOption(
                        icon = Icons.Default.Audiotrack,
                        label = "Audio Clip",
                        bgColor = Color(0xFF10B981),
                        onClick = {
                            onSendMessage(
                                "🎵 Audio brief (1:20)",
                                MessageType.AUDIO,
                                "",
                                "Audio_Brief.m4a",
                                "890 KB",
                                null,
                                null
                            )
                            showAttachmentSheet = false
                        }
                    )
                    AttachmentOption(
                        icon = Icons.Default.Security,
                        label = "Safety Key",
                        bgColor = PurplePrimary,
                        onClick = {
                            onSendMessage(
                                "🔑 Session Key Ratchet: ${conversation.encryptionKeyFingerprint}",
                                MessageType.TEXT,
                                "",
                                "",
                                "",
                                null,
                                null
                            )
                            showAttachmentSheet = false
                        }
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isMe: Boolean,
    onReply: () -> Unit
) {
    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.US).format(Date(message.timestamp))
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 2.dp,
                bottomEnd = if (isMe) 2.dp else 16.dp
            ),
            color = if (isMe) PurpleOutgoingBubble else PurpleIncomingBubble,
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isMe) PurplePrimary else PurpleBorder),
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clickable { onReply() }
                .testTag("message_bubble_${message.id}")
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // If in group and incoming, display sender name
                if (!isMe) {
                    Text(
                        text = message.senderName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = PurpleAccent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // If replying to previous message
                if (message.replyToSnippet != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PurpleDeepBg.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "Replying: ${message.replyToSnippet}",
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }

                // Content based on type
                when (message.type) {
                    MessageType.TEXT -> {
                        Text(
                            text = message.content,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            lineHeight = 19.sp
                        )
                    }
                    MessageType.IMAGE -> {
                        if (message.mediaUrl.isNotEmpty()) {
                            AsyncImage(
                                model = message.mediaUrl,
                                contentDescription = "Image message",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        Text(text = message.content, fontSize = 13.sp, color = TextPrimary)
                    }
                    MessageType.VIDEO -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            if (message.mediaUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = message.mediaUrl,
                                    contentDescription = "Video thumbnail",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Icon(imageVector = Icons.Default.PlayCircle, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(42.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = message.content, fontSize = 13.sp, color = TextPrimary)
                    }
                    MessageType.AUDIO -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = PurpleNeonGlow, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            LinearProgressIndicator(
                                progress = { 0.45f },
                                modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = PurpleAccent,
                                trackColor = PurpleBorder
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "0:14", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                    MessageType.DOCUMENT -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PurpleDeepBg.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = Color(0xFFEF4444), modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = message.fileName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(text = message.fileSize, fontSize = 10.sp, color = TextMuted)
                            }
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Download", tint = PurpleAccent, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Timestamp & Status Indicator
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeFormatted,
                        fontSize = 10.sp,
                        color = TextMuted
                    )

                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        when (message.status) {
                            MessageStatus.PENDING -> {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Pending / Offline Queue",
                                    tint = TextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            MessageStatus.SENT -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Sent",
                                    tint = TextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            MessageStatus.DELIVERED -> {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Delivered",
                                    tint = TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            MessageStatus.READ -> {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Read",
                                    tint = PurpleNeonGlow,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 11.sp, color = TextPrimary)
    }
}
