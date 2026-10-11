package com.example.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
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
import com.example.data.model.Conversation
import com.example.data.model.Message
import com.example.data.model.User
import com.example.ui.theme.*
import com.example.ui.viewmodel.SearchFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    activeFilter: SearchFilter,
    onFilterChange: (SearchFilter) -> Unit,
    conversations: List<Conversation>,
    messages: Map<String, List<Message>>,
    teamMembers: List<User>,
    onSelectConversation: (String) -> Unit,
    onCloseSearch: () -> Unit
) {
    // Filter matching results
    val matchingConversations = remember(searchQuery, activeFilter) {
        if (searchQuery.isBlank() && activeFilter == SearchFilter.ALL) emptyList()
        else conversations.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.lastMessage.contains(searchQuery, ignoreCase = true)
        }
    }

    val matchingMessages = remember(searchQuery, activeFilter) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val list = mutableListOf<Pair<Message, Conversation>>()
            messages.forEach { (convId, mList) ->
                val conv = conversations.firstOrNull { it.id == convId }
                if (conv != null) {
                    mList.forEach { msg ->
                        if (msg.content.contains(searchQuery, ignoreCase = true) ||
                            msg.fileName.contains(searchQuery, ignoreCase = true)) {
                            list.add(msg to conv)
                        }
                    }
                }
            }
            list
        }
    }

    val matchingMembers = remember(searchQuery, activeFilter) {
        if (searchQuery.isBlank() && activeFilter == SearchFilter.ALL) emptyList()
        else teamMembers.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.email.contains(searchQuery, ignoreCase = true) ||
            it.department.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onQueryChange,
                        placeholder = { Text("Search chats, messages, files...", fontSize = 14.sp, color = TextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("search_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = PurpleBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = PurpleDeepBg,
                            unfocusedContainerColor = PurpleDeepBg
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                                }
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCloseSearch, modifier = Modifier.testTag("search_close_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PurpleSurface)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SearchFilter.values()) { filter ->
                    val isSelected = filter == activeFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChange(filter) },
                        label = { Text(filter.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurplePrimary,
                            selectedLabelColor = Color.White,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Chats Section
                if (matchingConversations.isNotEmpty() && (activeFilter == SearchFilter.ALL || activeFilter == SearchFilter.CHATS)) {
                    item {
                        Text("CONVERSATIONS (${matchingConversations.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PurpleAccent)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    items(matchingConversations) { conv ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PurpleSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onSelectConversation(conv.id) }
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = conv.avatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(42.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = conv.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Text(text = conv.lastMessage, fontSize = 12.sp, color = TextSecondary, maxLines = 1)
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                // Team Members Section
                if (matchingMembers.isNotEmpty() && (activeFilter == SearchFilter.ALL || activeFilter == SearchFilter.TEAM)) {
                    item {
                        Text("TEAM MEMBERS (${matchingMembers.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PurpleAccent)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    items(matchingMembers) { member ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PurpleSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = member.avatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(42.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = member.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Text(text = "${member.department} • ${member.email}", fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                // Messages Matches Section
                if (matchingMessages.isNotEmpty() && (activeFilter == SearchFilter.ALL || activeFilter == SearchFilter.MESSAGES)) {
                    item {
                        Text("MESSAGE MATCHES (${matchingMessages.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PurpleAccent)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    items(matchingMessages) { (msg, conv) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PurpleSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onSelectConversation(conv.id) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "In: ${conv.title}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PurpleNeonGlow)
                                Text(text = "${msg.senderName}: ${msg.content}", fontSize = 13.sp, color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
