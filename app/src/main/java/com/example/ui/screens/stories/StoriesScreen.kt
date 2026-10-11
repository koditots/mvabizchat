package com.example.ui.screens.stories

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.MessageType
import com.example.data.model.Story
import com.example.data.model.StoryAudience
import com.example.data.model.User
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StoriesScreen(
    currentUser: User,
    stories: List<Story>,
    onViewStory: (Story) -> Unit,
    onPostStory: (textContent: String, mediaUrl: String, mediaType: MessageType, bgColor: String, audience: StoryAudience) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateStoryDialog by remember { mutableStateOf(false) }

    val myStories = stories.filter { it.userId == currentUser.id }
    val teamStories = stories.filter { it.userId != currentUser.id }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // E2EE Stories Security Header
        Surface(
            color = PurpleSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Encrypted",
                    tint = PurpleE2EEGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Status updates are end-to-end encrypted and disappear after 24 hours.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                // My Status Section
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = PurpleSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (myStories.isNotEmpty()) {
                                onViewStory(myStories.first())
                            } else {
                                showCreateStoryDialog = true
                            }
                        }
                        .testTag("my_status_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(56.dp)) {
                            AsyncImage(
                                model = currentUser.avatarUrl,
                                contentDescription = "My Status",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(
                                        width = if (myStories.isNotEmpty()) 2.5.dp else 1.dp,
                                        brush = Brush.linearGradient(listOf(PurpleAccent, PurplePrimary)),
                                        shape = CircleShape
                                    ),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(PurplePrimary)
                                    .clickable { showCreateStoryDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Status",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "My Status",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = if (myStories.isNotEmpty()) "${myStories.size} active update(s) • Tap to view" else "Tap to add status update",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        IconButton(
                            onClick = { showCreateStoryDialog = true },
                            modifier = Modifier.testTag("add_status_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Add Story",
                                tint = PurpleAccent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "RECENT TEAM UPDATES",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = PurpleAccent,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (teamStories.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AmpStories,
                                contentDescription = null,
                                tint = PurpleBorder,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No recent updates from team members",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(teamStories) { story ->
                    StoryItemRow(story = story, onClick = { onViewStory(story) })
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showCreateStoryDialog) {
        CreateStoryDialog(
            currentUser = currentUser,
            onDismiss = { showCreateStoryDialog = false },
            onPost = { text, media, type, bg, audience ->
                onPostStory(text, media, type, bg, audience)
                showCreateStoryDialog = false
            }
        )
    }
}

@Composable
fun StoryItemRow(story: Story, onClick: () -> Unit) {
    val timeStr = remember(story.createdAt) {
        val diff = (System.currentTimeMillis() - story.createdAt) / 1000 / 60
        when {
            diff < 1 -> "Just now"
            diff < 60 -> "${diff}m ago"
            else -> "${diff / 60}h ago"
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PurpleSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("story_item_${story.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.5.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                PurplePrimary,
                                PurpleAccent,
                                PurpleNeonGlow,
                                PurplePrimary
                            )
                        ),
                        shape = CircleShape
                    )
                    .padding(3.dp)
            ) {
                AsyncImage(
                    model = story.userAvatar,
                    contentDescription = story.userName,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = story.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encrypted",
                        tint = PurpleE2EEGreen,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = story.textContent.ifEmpty { "📷 Photo story" },
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "$timeStr • ${when(story.audience) {
                        StoryAudience.ALL_WORKSPACE -> "All Workspace"
                        StoryAudience.ALLOWED_TEAM_ONLY -> "Allowed Team"
                        StoryAudience.DIRECT_CONTACTS -> "Direct Contacts"
                    }}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun CreateStoryDialog(
    currentUser: User,
    onDismiss: () -> Unit,
    onPost: (textContent: String, mediaUrl: String, mediaType: MessageType, bgColor: String, audience: StoryAudience) -> Unit
) {
    var storyType by remember { mutableStateOf(MessageType.TEXT) } // TEXT, IMAGE, VIDEO
    var textContent by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#7E22CE") }
    var selectedMediaUrl by remember { mutableStateOf("") }
    var selectedAudience by remember { mutableStateOf(StoryAudience.ALL_WORKSPACE) }

    val colorPresets = listOf(
        "#7E22CE", "#6B21A8", "#4C1D95", "#1E1038", "#0F071D", "#BE185D", "#047857"
    )

    val imagePresets = listOf(
        "https://images.unsplash.com/photo-1551836022-d5d88e9218df?w=600",
        "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=600",
        "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?w=600",
        "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=600"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .testTag("create_story_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = PurpleDeepBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    Row {
                        FilterChip(
                            selected = storyType == MessageType.TEXT,
                            onClick = { storyType = MessageType.TEXT },
                            label = { Text("Text") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PurplePrimary,
                                selectedLabelColor = Color.White,
                                labelColor = TextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(
                            selected = storyType == MessageType.IMAGE,
                            onClick = {
                                storyType = MessageType.IMAGE
                                if (selectedMediaUrl.isEmpty()) selectedMediaUrl = imagePresets.first()
                            },
                            label = { Text("Photo/Video") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PurplePrimary,
                                selectedLabelColor = Color.White,
                                labelColor = TextSecondary
                            )
                        )
                    }

                    TextButton(
                        onClick = {
                            if (textContent.isNotBlank() || selectedMediaUrl.isNotBlank()) {
                                onPost(textContent, selectedMediaUrl, storyType, selectedColor, selectedAudience)
                            }
                        },
                        enabled = textContent.isNotBlank() || selectedMediaUrl.isNotBlank(),
                        modifier = Modifier.testTag("post_story_button")
                    ) {
                        Text("Post", fontWeight = FontWeight.Bold, color = PurpleAccent, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Canvas Preview Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (storyType == MessageType.TEXT) Color(android.graphics.Color.parseColor(selectedColor))
                            else Color.Black
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (storyType == MessageType.IMAGE && selectedMediaUrl.isNotEmpty()) {
                        AsyncImage(
                            model = selectedMediaUrl,
                            contentDescription = "Story Media",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f))
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        OutlinedTextField(
                            value = textContent,
                            onValueChange = { textContent = it },
                            placeholder = {
                                Text(
                                    "Type what's happening at work...",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center
                                )
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("story_text_input")
                        )
                    }

                    // E2EE badge overlay
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted",
                            tint = PurpleE2EEGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "End-to-End Encrypted Story",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // If text story, show background color palette
                if (storyType == MessageType.TEXT) {
                    Text("Background Color:", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(colorPresets) { hex ->
                            val isSelected = hex == selectedColor
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else PurpleBorder,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = hex }
                            )
                        }
                    }
                } else {
                    // Image presets selector
                    Text("Select Media Asset:", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(imagePresets) { imgUrl ->
                            val isSelected = imgUrl == selectedMediaUrl
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) PurplePrimary else PurpleBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedMediaUrl = imgUrl }
                            ) {
                                AsyncImage(
                                    model = imgUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Privacy Settings Selector
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PurpleSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Privacy",
                                tint = PurpleAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Story Privacy Controls",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StoryAudience.values().forEach { aud ->
                                val isSelected = aud == selectedAudience
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) PurplePrimary else PurpleSurface,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 3.dp)
                                        .clickable { selectedAudience = aud }
                                ) {
                                    Text(
                                        text = when(aud) {
                                            StoryAudience.ALL_WORKSPACE -> "All Work"
                                            StoryAudience.ALLOWED_TEAM_ONLY -> "Allowed"
                                            StoryAudience.DIRECT_CONTACTS -> "Contacts"
                                        },
                                        color = if (isSelected) Color.White else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
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

@Composable
fun StoryViewerDialog(
    story: Story?,
    onDismiss: () -> Unit,
    onReplyToStory: (String) -> Unit
) {
    if (story == null) return

    var progress by remember { mutableStateOf(0f) }
    var replyText by remember { mutableStateOf("") }
    var isPaused by remember { mutableStateOf(false) }

    // Auto progress timer
    LaunchedEffect(story.id, isPaused) {
        if (!isPaused) {
            while (progress < 1f) {
                delay(50)
                progress += 0.01f
            }
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (story.mediaType == MessageType.TEXT) Color(android.graphics.Color.parseColor(story.backgroundColorHex))
                    else Color.Black
                )
                .testTag("story_viewer_dialog")
        ) {
            // Media Image if applicable
            if (story.mediaUrl.isNotEmpty()) {
                AsyncImage(
                    model = story.mediaUrl,
                    contentDescription = "Story Media",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )
            }

            // Text Content in center
            if (story.textContent.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp, vertical = 90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = story.textContent,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp
                    )
                }
            }

            // Top Bar with timed progress
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    ) {
                        AsyncImage(
                            model = story.userAvatar,
                            contentDescription = story.userName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = story.userName,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
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
                            text = "End-to-End Encrypted Status",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }

            // Bottom reply field
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("Reply to ${story.userName}...", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("story_reply_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                onReplyToStory(replyText)
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PurplePrimary)
                            .testTag("story_reply_send")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Reply",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
