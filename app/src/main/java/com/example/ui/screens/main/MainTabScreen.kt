package com.example.ui.screens.main

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.auth.GoogleAuthScreen
import com.example.ui.screens.chat.AddUserByUsernameDialog
import com.example.ui.screens.chat.ChatDetailScreen
import com.example.ui.screens.group.CreateGroupDialog
import com.example.ui.screens.search.GlobalSearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.stories.StoriesScreen
import com.example.ui.screens.stories.StoryViewerDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabScreen(
    viewModel: MainViewModel,
    onLockAppNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = viewModel.repository
    val currentTab by viewModel.currentTab.collectAsState()
    val activeConversationId by viewModel.activeConversationId.collectAsState()
    val conversations by repository.conversations.collectAsState()
    val messagesMap by repository.messages.collectAsState()
    val teamMembers by repository.teamMembers.collectAsState()
    val stories by repository.stories.collectAsState()
    val currentUser by repository.currentUser.collectAsState()
    val linkedDevices by repository.linkedDevices.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isOfflineSimulated by repository.isOfflineSimulated.collectAsState()

    val activeNotification by repository.activeNotification.collectAsState()
    val popupMessageEvent by viewModel.popupMessageEvent.collectAsState()
    val viewingStory by viewModel.viewingStory.collectAsState()
    val verifyingConversation by viewModel.verifyingConversation.collectAsState()

    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showMultiDeviceDialog by remember { mutableStateOf(false) }
    var showBackupRestoreDialog by remember { mutableStateOf(false) }
    var showGoogleAuthDialog by remember { mutableStateOf(false) }

    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchFilter by viewModel.searchFilter.collectAsState()

    // If active conversation is selected, display full chat thread
    if (activeConversationId != null) {
        val conv = conversations.firstOrNull { it.id == activeConversationId }
        val currentMessages = messagesMap[activeConversationId] ?: emptyList()
        if (conv != null) {
            ChatDetailScreen(
                conversation = conv,
                messages = currentMessages,
                currentUser = currentUser,
                onBack = { viewModel.closeConversation() },
                onSendMessage = { content, type, mediaUrl, fileName, fileSize, replyToId, replyToSnippet ->
                    repository.sendMessage(
                        conversationId = conv.id,
                        content = content,
                        type = type,
                        mediaUrl = mediaUrl,
                        fileName = fileName,
                        fileSize = fileSize,
                        replyToMessageId = replyToId,
                        replyToSnippet = replyToSnippet
                    )
                },
                onStartCall = { isVideo ->
                    repository.startCall(conv.title, conv.avatarUrl, isVideo)
                },
                onVerifySafetyNumbers = {
                    viewModel.openVerificationDialog(conv)
                }
            )
            return
        }
    }

    // If global search is open
    if (isSearchActive) {
        GlobalSearchScreen(
            searchQuery = searchQuery,
            onQueryChange = { viewModel.setSearchQuery(it) },
            activeFilter = searchFilter,
            onFilterChange = { viewModel.setSearchFilter(it) },
            conversations = conversations,
            messages = messagesMap,
            teamMembers = teamMembers,
            onSelectConversation = { convId -> viewModel.openConversation(convId) },
            onCloseSearch = { viewModel.toggleSearch(false) }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(PurplePrimary, PurplePrimaryDark))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MVA Business",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isOfflineSimulated) "Offline Mode (Queued)" else "E2EE Connected • Multi-Device",
                                fontSize = 10.sp,
                                color = if (isOfflineSimulated) Color(0xFFEF4444) else PurpleE2EEGreen
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddUserDialog = true },
                        modifier = Modifier.testTag("add_user_by_username_button")
                    ) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add Contact by Username", tint = PurpleNeonGlow)
                    }
                    IconButton(
                        onClick = { viewModel.toggleSearch(true) },
                        modifier = Modifier.testTag("main_search_button")
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = PurpleAccent)
                    }
                    IconButton(
                        onClick = { showGoogleAuthDialog = true },
                        modifier = Modifier.testTag("google_auth_icon")
                    ) {
                        AsyncImage(
                            model = currentUser.avatarUrl,
                            contentDescription = "Google Account",
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, PurpleAccent, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PurpleSurface)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = PurpleSurface,
                tonalElevation = 8.dp
            ) {
                // Chats
                val totalUnread = conversations.sumOf { it.unreadCount }
                NavigationBarItem(
                    selected = currentTab == MainTab.CHATS,
                    onClick = { viewModel.selectTab(MainTab.CHATS) },
                    icon = {
                        BadgedBox(badge = {
                            if (totalUnread > 0) {
                                Badge(containerColor = PurplePrimary) { Text("$totalUnread", color = Color.White) }
                            }
                        }) {
                            Icon(Icons.Default.Chat, contentDescription = "Chats")
                        }
                    },
                    label = { Text("Chats") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = PurpleAccent,
                        indicatorColor = PurplePrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("tab_chats")
                )

                // Stories / Status
                NavigationBarItem(
                    selected = currentTab == MainTab.STORIES,
                    onClick = { viewModel.selectTab(MainTab.STORIES) },
                    icon = {
                        Icon(Icons.Default.AmpStories, contentDescription = "Stories")
                    },
                    label = { Text("Stories") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = PurpleAccent,
                        indicatorColor = PurplePrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("tab_stories")
                )

                // Calls
                NavigationBarItem(
                    selected = currentTab == MainTab.CALLS,
                    onClick = { viewModel.selectTab(MainTab.CALLS) },
                    icon = {
                        Icon(Icons.Default.Call, contentDescription = "Calls")
                    },
                    label = { Text("Calls") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = PurpleAccent,
                        indicatorColor = PurplePrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("tab_calls")
                )

                // Admin Dashboard
                NavigationBarItem(
                    selected = currentTab == MainTab.ADMIN,
                    onClick = { viewModel.selectTab(MainTab.ADMIN) },
                    icon = {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin")
                    },
                    label = { Text("Admin") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = PurpleAccent,
                        indicatorColor = PurplePrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("tab_admin")
                )

                // Settings
                NavigationBarItem(
                    selected = currentTab == MainTab.SETTINGS,
                    onClick = { viewModel.selectTab(MainTab.SETTINGS) },
                    icon = {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = PurpleAccent,
                        indicatorColor = PurplePrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        },
        floatingActionButton = {
            if (currentTab == MainTab.CHATS) {
                Column(horizontalAlignment = Alignment.End) {
                    SmallFloatingActionButton(
                        onClick = { showCreateGroupDialog = true },
                        containerColor = PurpleSurfaceVariant,
                        contentColor = PurpleAccent,
                        modifier = Modifier.testTag("create_group_fab")
                    ) {
                        Icon(imageVector = Icons.Default.GroupAdd, contentDescription = "New Group")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    ExtendedFloatingActionButton(
                        onClick = { showAddUserDialog = true },
                        containerColor = PurplePrimary,
                        contentColor = Color.White,
                        icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                        text = { Text("Add Contact", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("add_contact_fab")
                    )
                }
            }
        },
        containerColor = DarkBackground,
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // In-App Notification Dropdown Drawer Banner
                NotificationDrawerBanner(
                    event = activeNotification,
                    onDismiss = { repository.dismissActiveNotification() },
                    onOpenChat = { convId -> viewModel.openConversation(convId) },
                    onQuickReply = { convId, reply -> repository.sendMessage(convId, reply) }
                )

                // Tab Content
                when (currentTab) {
                    MainTab.CHATS -> {
                        ChatsListContent(
                            conversations = conversations,
                            onSelectConversation = { convId -> viewModel.openConversation(convId) },
                            onAddUserClick = { showAddUserDialog = true }
                        )
                    }
                    MainTab.STORIES -> {
                        StoriesScreen(
                            currentUser = currentUser,
                            stories = stories,
                            onViewStory = { story -> viewModel.openStory(story) },
                            onPostStory = { text, media, type, bg, aud ->
                                repository.postStory(text, media, type, bg, aud)
                            }
                        )
                    }
                    MainTab.CALLS -> {
                        CallsListContent(
                            conversations = conversations,
                            onStartCall = { title, avatar, isVideo ->
                                repository.startCall(title, avatar, isVideo)
                            }
                        )
                    }
                    MainTab.ADMIN -> {
                        AdminDashboardScreen(
                            repository = repository,
                            onBroadcast = { text -> repository.broadcastMessage(text) }
                        )
                    }
                    MainTab.SETTINGS -> {
                        SettingsScreen(
                            repository = repository,
                            currentUser = currentUser,
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { viewModel.toggleDarkMode() },
                            onOpenMultiDevice = { showMultiDeviceDialog = true },
                            onOpenBackupRestore = { showBackupRestoreDialog = true },
                            onSwitchGoogleAccount = { showGoogleAuthDialog = true },
                            onLockAppNow = onLockAppNow
                        )
                    }
                }
            }

            // Quick Popup Message on screen for replies
            QuickReplyPopupDialog(
                event = popupMessageEvent,
                onDismiss = { viewModel.dismissPopup() },
                onSendReply = { convId, text -> viewModel.sendQuickReply(convId, text) }
            )
        }
    }

    // Dialogs
    if (showCreateGroupDialog) {
        CreateGroupDialog(
            teamMembers = teamMembers,
            onDismiss = { showCreateGroupDialog = false },
            onCreateGroup = { title, desc, ids ->
                repository.createGroup(title, desc, ids)
            }
        )
    }

    if (showAddUserDialog) {
        AddUserByUsernameDialog(
            repository = repository,
            onDismiss = { showAddUserDialog = false },
            onUserAddedToChat = { convId ->
                showAddUserDialog = false
                viewModel.openConversation(convId)
            }
        )
    }

    if (showMultiDeviceDialog) {
        MultiDeviceDialog(
            devices = linkedDevices,
            onDismiss = { showMultiDeviceDialog = false },
            onLinkDevice = { name, type -> repository.linkNewDevice(name, type) },
            onUnlinkDevice = { devId -> repository.unlinkDevice(devId) }
        )
    }

    if (showBackupRestoreDialog) {
        BackupRestoreDialog(
            repository = repository,
            onDismiss = { showBackupRestoreDialog = false }
        )
    }

    if (showGoogleAuthDialog) {
        GoogleAuthScreen(
            currentEmail = currentUser.email,
            onDismiss = { showGoogleAuthDialog = false },
            onSelectAccount = { name, email -> viewModel.triggerGoogleAccountSwitch(name, email) }
        )
    }

    if (viewingStory != null) {
        StoryViewerDialog(
            story = viewingStory,
            onDismiss = { viewModel.closeStory() },
            onReplyToStory = { reply ->
                val conv = conversations.firstOrNull { it.participantIds.contains(viewingStory?.userId) }
                if (conv != null) {
                    repository.sendMessage(conv.id, "Replying to story: $reply")
                }
            }
        )
    }

    if (verifyingConversation != null) {
        E2EEVerificationDialog(
            conversation = verifyingConversation,
            onDismiss = { viewModel.closeVerificationDialog() }
        )
    }
}

@Composable
fun ChatsListContent(
    conversations: List<Conversation>,
    onSelectConversation: (String) -> Unit,
    onAddUserClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Quick Add via GitHub Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PurpleSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAddUserClick)
                    .testTag("add_user_banner")
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
                            .background(PurplePrimaryDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAddAlt1,
                            contentDescription = null,
                            tint = PurpleNeonGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Add Contact by Username or Email",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Checks registration online in GitHub database",
                            fontSize = 11.sp,
                            color = PurpleAccent
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(conversations) { conv ->
            val timeStr = remember(conv.lastTimestamp) {
                SimpleDateFormat("hh:mm a", Locale.US).format(Date(conv.lastTimestamp))
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PurpleSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelectConversation(conv.id) }
                    .testTag("chat_item_${conv.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(50.dp)) {
                        if (conv.avatarUrl.isNotEmpty()) {
                            AsyncImage(
                                model = conv.avatarUrl,
                                contentDescription = conv.title,
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
                                    text = conv.title.take(1),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        if (conv.isGroup) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(PurplePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = "Group",
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = conv.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = timeStr,
                                fontSize = 11.sp,
                                color = if (conv.unreadCount > 0) PurpleNeonGlow else TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "E2EE",
                                    tint = PurpleAccent,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = conv.lastMessage,
                                    fontSize = 12.sp,
                                    color = if (conv.unreadCount > 0) TextPrimary else TextSecondary,
                                    fontWeight = if (conv.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (conv.unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(PurplePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${conv.unreadCount}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun CallsListContent(
    conversations: List<Conversation>,
    onStartCall: (title: String, avatar: String, isVideo: Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PurpleSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = PurpleE2EEGreen, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "All voice and video calls are protected by cryptographic key ratchets.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("RECENT TEAM CALL LOGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PurpleAccent, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(conversations) { conv ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PurpleSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = conv.avatarUrl,
                        contentDescription = conv.title,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = conv.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CallMade, contentDescription = null, tint = PurpleE2EEGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Encrypted • Today at 10:24 AM", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    IconButton(onClick = { onStartCall(conv.title, conv.avatarUrl, false) }) {
                        Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = PurpleAccent)
                    }
                    IconButton(onClick = { onStartCall(conv.title, conv.avatarUrl, true) }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = PurpleAccent)
                    }
                }
            }
        }
    }
}
