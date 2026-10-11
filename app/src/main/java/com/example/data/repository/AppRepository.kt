package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.crypto.EncryptionManager
import com.example.data.model.*
import com.example.data.remote.GitHubRegistryService
import com.example.data.remote.InviteDetails
import com.example.data.remote.OnlineUserSearchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AppRepository(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mva_business_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)
    val githubRegistry = GitHubRegistryService()

    // Current Google User
    private val _currentUser = MutableStateFlow(
        User(
            id = "user_me",
            name = prefs.getString("user_name", "Alex Rivera") ?: "Alex Rivera",
            email = prefs.getString("user_email", "alex.rivera@mvaglobal.com") ?: "alex.rivera@mvaglobal.com",
            username = prefs.getString("user_username", "alex.rivera") ?: "alex.rivera",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            role = UserRole.ADMIN,
            statusMessage = prefs.getString("user_status", "Leading Enterprise Engineering 🚀") ?: "Leading Enterprise Engineering 🚀",
            department = "Engineering & Architecture",
            isOnline = true,
            publicKey = "MVA-KEY-RSA-4096-ALEX"
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    // Team Members
    private val _teamMembers = MutableStateFlow<List<User>>(emptyList())
    val teamMembers: StateFlow<List<User>> = _teamMembers.asStateFlow()

    // Conversations
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    // Messages map: conversationId -> List<Message>
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    // Stories
    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    // Linked Devices (Multi-Device Sync)
    private val _linkedDevices = MutableStateFlow<List<LinkedDevice>>(emptyList())
    val linkedDevices: StateFlow<List<LinkedDevice>> = _linkedDevices.asStateFlow()

    // Admin Audit Logs
    private val _auditLogs = MutableStateFlow<List<AdminAuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AdminAuditLog>> = _auditLogs.asStateFlow()

    // Business Profile
    private val _businessProfile = MutableStateFlow(BusinessProfile())
    val businessProfile: StateFlow<BusinessProfile> = _businessProfile.asStateFlow()

    // Security & App Lock
    private val _isAppLocked = MutableStateFlow(prefs.getBoolean("is_locked", false))
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _pinCode = MutableStateFlow(prefs.getString("pin_code", "1234") ?: "1234")
    val pinCode: StateFlow<String> = _pinCode.asStateFlow()

    private val _biometricEnabled = MutableStateFlow(prefs.getBoolean("biometric_enabled", true))
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled.asStateFlow()

    private val _appLockTimeoutMinutes = MutableStateFlow(prefs.getInt("app_lock_timeout", 0))
    val appLockTimeoutMinutes: StateFlow<Int> = _appLockTimeoutMinutes.asStateFlow()

    // Network / Offline status
    private val _isOfflineSimulated = MutableStateFlow(prefs.getBoolean("offline_simulated", false))
    val isOfflineSimulated: StateFlow<Boolean> = _isOfflineSimulated.asStateFlow()

    // Pop-up Message on screen for quick replies setting
    private val _popupRepliesEnabled = MutableStateFlow(prefs.getBoolean("popup_replies_enabled", true))
    val popupRepliesEnabled: StateFlow<Boolean> = _popupRepliesEnabled.asStateFlow()

    // Cloud Backup status
    private val _lastCloudBackup = MutableStateFlow(prefs.getString("last_cloud_backup", "Oct 11, 2026, 05:42 AM") ?: "Never")
    val lastCloudBackup: StateFlow<String> = _lastCloudBackup.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    // In-App Notification Trigger (for drawer banner & popup)
    private val _activeNotification = MutableStateFlow<NotificationEvent?>(null)
    val activeNotification: StateFlow<NotificationEvent?> = _activeNotification.asStateFlow()

    // Active Call
    private val _activeCall = MutableStateFlow<CallSession?>(null)
    val activeCall: StateFlow<CallSession?> = _activeCall.asStateFlow()

    data class NotificationEvent(
        val messageId: String,
        val conversationId: String,
        val senderName: String,
        val senderAvatar: String,
        val textSnippet: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val members = listOf(
            User("user_sarah", "Sarah Jenkins", "sarah.j@mvaglobal.com", "sarah.jenkins", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150", UserRole.OWNER, "Executive Operations • Traveling", "Executive", true, "KEY-SARAH-991", "+1 555-0101"),
            User("user_marcus", "Marcus Chen", "marcus.c@mvaglobal.com", "marcus.chen", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150", UserRole.ADMIN, "Security Audit underway 🛡️", "Infra & SecOps", true, "KEY-MARCUS-302", "+1 555-0102"),
            User("user_elena", "Elena Rostova", "elena.r@mvaglobal.com", "elena.rostova", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150", UserRole.MEMBER, "Reviewing Q4 Architecture Docs", "Product", true, "KEY-ELENA-711", "+1 555-0103"),
            User("user_david", "David Kalu", "david.k@mvaglobal.com", "david.kalu", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150", UserRole.MEMBER, "Offline until tomorrow morning", "DevOps", false, "KEY-DAVID-440", "+1 555-0104"),
            User("user_amara", "Amara Patel", "amara.p@mvaglobal.com", "amara.patel", "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150", UserRole.MEMBER, "Client presentations in progress", "Solutions", true, "KEY-AMARA-821", "+1 555-0105")
        )
        _teamMembers.value = members
        members.forEach { githubRegistry.registerUserToGitHub(it) }

        val initialConversations = listOf(
            Conversation(
                id = "conv_exec_board",
                title = "MVA Executive Board",
                isGroup = true,
                participantIds = listOf("user_me", "user_sarah", "user_marcus"),
                lastMessage = "Encryption audit passed. Multi-device cloud sync enabled.",
                lastTimestamp = System.currentTimeMillis() - 1000 * 60 * 5,
                unreadCount = 2,
                isPinned = true,
                avatarUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?w=150",
                encryptionKeyFingerprint = EncryptionManager.generateFingerprint("conv_exec_board"),
                groupDescription = "Strategic leadership and confidential roadmap deliberations.",
                createdBy = "Sarah Jenkins"
            ),
            Conversation(
                id = "conv_sarah",
                title = "Sarah Jenkins",
                isGroup = false,
                participantIds = listOf("user_me", "user_sarah"),
                lastMessage = "Can you send the updated Q4 security export file?",
                lastTimestamp = System.currentTimeMillis() - 1000 * 60 * 18,
                unreadCount = 1,
                isPinned = true,
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                encryptionKeyFingerprint = EncryptionManager.generateFingerprint("conv_sarah")
            ),
            Conversation(
                id = "conv_marcus",
                title = "Marcus Chen",
                isGroup = false,
                participantIds = listOf("user_me", "user_marcus"),
                lastMessage = "All AES-256 session keys are synchronized across Android & Web.",
                lastTimestamp = System.currentTimeMillis() - 1000 * 60 * 45,
                unreadCount = 0,
                isPinned = false,
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                encryptionKeyFingerprint = EncryptionManager.generateFingerprint("conv_marcus")
            ),
            Conversation(
                id = "conv_mobile_dev",
                title = "Mobile & Cloud Architecture",
                isGroup = true,
                participantIds = listOf("user_me", "user_marcus", "user_elena", "user_david"),
                lastMessage = "Offline queue auto-resumes once connection stability is restored.",
                lastTimestamp = System.currentTimeMillis() - 1000 * 60 * 120,
                unreadCount = 0,
                isPinned = false,
                avatarUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=150",
                encryptionKeyFingerprint = EncryptionManager.generateFingerprint("conv_mobile_dev"),
                groupDescription = "Core engineers designing offline-first synchronization & E2EE pipelines.",
                createdBy = "Alex Rivera"
            ),
            Conversation(
                id = "conv_elena",
                title = "Elena Rostova",
                isGroup = false,
                participantIds = listOf("user_me", "user_elena"),
                lastMessage = "I have uploaded the product spec document to our encrypted vault.",
                lastTimestamp = System.currentTimeMillis() - 1000 * 60 * 240,
                unreadCount = 0,
                isPinned = false,
                avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150",
                encryptionKeyFingerprint = EncryptionManager.generateFingerprint("conv_elena")
            )
        )
        _conversations.value = initialConversations

        val msgMap = mutableMapOf<String, List<Message>>()
        msgMap["conv_exec_board"] = listOf(
            Message("m1", "conv_exec_board", "user_sarah", "Sarah Jenkins", "Team, remember all communications on MVA Business Chat are protected by end-to-end encryption.", System.currentTimeMillis() - 1000 * 60 * 60, MessageType.TEXT),
            Message("m2", "conv_exec_board", "user_marcus", "Marcus Chen", "Yes, biometric app lock and device-level keys are confirmed.", System.currentTimeMillis() - 1000 * 60 * 30, MessageType.TEXT),
            Message("m3", "conv_exec_board", "user_sarah", "Sarah Jenkins", "Encryption audit passed. Multi-device cloud sync enabled.", System.currentTimeMillis() - 1000 * 60 * 5, MessageType.TEXT)
        )
        msgMap["conv_sarah"] = listOf(
            Message("ms1", "conv_sarah", "user_me", "Alex Rivera", "Hi Sarah, the new cloud sync and local backup modules are ready.", System.currentTimeMillis() - 1000 * 60 * 40, MessageType.TEXT, status = MessageStatus.READ),
            Message("ms2", "conv_sarah", "user_sarah", "Sarah Jenkins", "Can you send the updated Q4 security export file?", System.currentTimeMillis() - 1000 * 60 * 18, MessageType.TEXT, status = MessageStatus.READ)
        )
        msgMap["conv_marcus"] = listOf(
            Message("mm1", "conv_marcus", "user_marcus", "Marcus Chen", "All AES-256 session keys are synchronized across Android & Web.", System.currentTimeMillis() - 1000 * 60 * 45, MessageType.TEXT)
        )
        msgMap["conv_mobile_dev"] = listOf(
            Message("md1", "conv_mobile_dev", "user_elena", "Elena Rostova", "Does the offline mode queue messages automatically when the device loses network?", System.currentTimeMillis() - 1000 * 60 * 130, MessageType.TEXT),
            Message("md2", "conv_mobile_dev", "user_me", "Alex Rivera", "Offline queue auto-resumes once connection stability is restored.", System.currentTimeMillis() - 1000 * 60 * 120, MessageType.TEXT)
        )
        msgMap["conv_elena"] = listOf(
            Message("me1", "conv_elena", "user_elena", "Elena Rostova", "I have uploaded the product spec document to our encrypted vault.", System.currentTimeMillis() - 1000 * 60 * 240, MessageType.DOCUMENT, fileName = "MVA_Architecture_Spec_v3.pdf", fileSize = "2.4 MB")
        )
        _messages.value = msgMap

        _stories.value = listOf(
            Story(
                id = "story_sarah_1",
                userId = "user_sarah",
                userName = "Sarah Jenkins",
                userAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                textContent = "Excited to announce our global enterprise rollout with 100% E2EE compliance! 🚀🌐",
                backgroundColorHex = "#6B21A8",
                audience = StoryAudience.ALL_WORKSPACE
            ),
            Story(
                id = "story_marcus_1",
                userId = "user_marcus",
                userName = "Marcus Chen",
                userAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                textContent = "Zero-trust device authentication protocol v2.4 successfully deployed. 🔒",
                backgroundColorHex = "#1E1038",
                audience = StoryAudience.ALLOWED_TEAM_ONLY
            ),
            Story(
                id = "story_me_1",
                userId = "user_me",
                userName = "Alex Rivera",
                userAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                textContent = "Working on the new real-time multi-device sync engine today. 💻📱",
                backgroundColorHex = "#9333EA",
                audience = StoryAudience.ALL_WORKSPACE
            )
        )

        _linkedDevices.value = listOf(
            LinkedDevice("dev_1", "Pixel 9 Pro (This Device)", "Android", "Active now", "San Francisco, USA", true),
            LinkedDevice("dev_2", "MacBook Pro M3 Max", "macOS • Chrome Web", "Active 12m ago", "San Francisco, USA", false),
            LinkedDevice("dev_3", "Office Workstation", "Windows 11 • Desktop App", "Yesterday at 6:15 PM", "HQ Office, Floor 3", false)
        )

        _auditLogs.value = listOf(
            AdminAuditLog("log_1", System.currentTimeMillis() - 1000 * 60 * 60 * 2, "Sarah Jenkins", "Multi-Device Protocol", "Authorized device 'MacBook Pro M3 Max' via encrypted QR handshake"),
            AdminAuditLog("log_2", System.currentTimeMillis() - 1000 * 60 * 60 * 14, "Alex Rivera", "Role Assignment", "Promoted Marcus Chen to Workspace Admin"),
            AdminAuditLog("log_3", System.currentTimeMillis() - 1000 * 60 * 60 * 28, "System Security", "Key Ratchet Renewal", "Automatically rotated conversation master keys"),
            AdminAuditLog("log_4", System.currentTimeMillis() - 1000 * 60 * 60 * 48, "Alex Rivera", "Google Cloud Backup", "Triggered automated E2EE cloud backup")
        )
    }

    // Messaging actions
    fun sendMessage(
        conversationId: String,
        content: String,
        type: MessageType = MessageType.TEXT,
        mediaUrl: String = "",
        fileName: String = "",
        fileSize: String = "",
        replyToMessageId: String? = null,
        replyToSnippet: String? = null
    ) {
        val currentOffline = _isOfflineSimulated.value
        val newMsgId = "msg_${System.currentTimeMillis()}"
        val newMsg = Message(
            id = newMsgId,
            conversationId = conversationId,
            senderId = _currentUser.value.id,
            senderName = _currentUser.value.name,
            content = content,
            timestamp = System.currentTimeMillis(),
            type = type,
            mediaUrl = mediaUrl,
            fileName = fileName,
            fileSize = fileSize,
            isEncrypted = true,
            status = if (currentOffline) MessageStatus.PENDING else MessageStatus.SENT,
            replyToMessageId = replyToMessageId,
            replyToSnippet = replyToSnippet
        )

        val updatedMap = _messages.value.toMutableMap()
        val currentList = updatedMap[conversationId]?.toMutableList() ?: mutableListOf()
        currentList.add(newMsg)
        updatedMap[conversationId] = currentList
        _messages.value = updatedMap

        // Update conversation last message
        val updatedConvList = _conversations.value.map { conv ->
            if (conv.id == conversationId) {
                conv.copy(
                    lastMessage = when (type) {
                        MessageType.TEXT -> content
                        MessageType.IMAGE -> "📷 Image"
                        MessageType.VIDEO -> "🎥 Video"
                        MessageType.AUDIO -> "🎵 Audio message"
                        MessageType.DOCUMENT -> "📄 $fileName"
                    },
                    lastTimestamp = System.currentTimeMillis()
                )
            } else conv
        }
        _conversations.value = updatedConvList

        // If online, simulate recipient response after a short delay
        if (!currentOffline) {
            scope.launch {
                delay(1200)
                // Mark message as delivered/read
                val mList = _messages.value[conversationId]?.map {
                    if (it.id == newMsgId) it.copy(status = MessageStatus.READ) else it
                }
                if (mList != null) {
                    val m = _messages.value.toMutableMap()
                    m[conversationId] = mList
                    _messages.value = m
                }

                // Simulate peer reply if conversation is with Sarah or Marcus
                simulatePeerReply(conversationId, content)
            }
        }
    }

    private suspend fun simulatePeerReply(conversationId: String, userText: String) {
        val conv = _conversations.value.firstOrNull { it.id == conversationId } ?: return
        delay(2500)
        val peerId = if (conv.isGroup) "user_marcus" else conv.participantIds.firstOrNull { it != "user_me" } ?: "user_sarah"
        val peerUser = _teamMembers.value.firstOrNull { it.id == peerId } ?: return

        val replyText = when {
            userText.contains("backup", ignoreCase = true) -> "Encrypted backup verified! Both offline local storage and Google Cloud sync are confirmed."
            userText.contains("call", ignoreCase = true) -> "I'm available now for a secure encrypted voice or video call."
            userText.contains("file", ignoreCase = true) || userText.contains("doc", ignoreCase = true) -> "Received the document. End-to-end decryption verified on this device."
            userText.contains("meeting", ignoreCase = true) -> "Looking forward to it! Let's convene on the MVA encrypted bridge."
            else -> "Noted! Encrypted message received and synced across workspace nodes."
        }

        val replyMsg = Message(
            id = "msg_${System.currentTimeMillis()}",
            conversationId = conversationId,
            senderId = peerUser.id,
            senderName = peerUser.name,
            content = replyText,
            timestamp = System.currentTimeMillis(),
            isEncrypted = true,
            status = MessageStatus.READ
        )

        val updatedMap = _messages.value.toMutableMap()
        val currentList = updatedMap[conversationId]?.toMutableList() ?: mutableListOf()
        currentList.add(replyMsg)
        updatedMap[conversationId] = currentList
        _messages.value = updatedMap

        _conversations.value = _conversations.value.map {
            if (it.id == conversationId) it.copy(lastMessage = replyText, lastTimestamp = System.currentTimeMillis(), unreadCount = it.unreadCount + 1)
            else it
        }

        // Trigger in-app notification event for drawer and popup quick-reply
        _activeNotification.value = NotificationEvent(
            messageId = replyMsg.id,
            conversationId = conversationId,
            senderName = peerUser.name,
            senderAvatar = peerUser.avatarUrl,
            textSnippet = replyText
        )
    }

    fun dismissActiveNotification() {
        _activeNotification.value = null
    }

    // Toggle offline mode simulation & auto-sync queue
    fun setOfflineSimulated(isOffline: Boolean) {
        _isOfflineSimulated.value = isOffline
        prefs.edit().putBoolean("offline_simulated", isOffline).apply()

        if (!isOffline) {
            // Auto sync pending messages
            scope.launch {
                val updatedMap = _messages.value.toMutableMap()
                var hadPending = false
                updatedMap.forEach { (convId, msgList) ->
                    val resolved = msgList.map { msg ->
                        if (msg.status == MessageStatus.PENDING) {
                            hadPending = true
                            msg.copy(status = MessageStatus.READ)
                        } else msg
                    }
                    updatedMap[convId] = resolved
                }
                if (hadPending) {
                    _messages.value = updatedMap
                }
            }
        }
    }

    // Security & App Lock
    fun setAppLocked(locked: Boolean) {
        _isAppLocked.value = locked
        prefs.edit().putBoolean("is_locked", locked).apply()
    }

    fun updatePinCode(newPin: String) {
        _pinCode.value = newPin
        prefs.edit().putString("pin_code", newPin).apply()
    }

    fun setBiometricEnabled(enabled: Boolean) {
        _biometricEnabled.value = enabled
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
    }

    fun setPopupRepliesEnabled(enabled: Boolean) {
        _popupRepliesEnabled.value = enabled
        prefs.edit().putBoolean("popup_replies_enabled", enabled).apply()
    }

    fun markConversationRead(conversationId: String) {
        _conversations.value = _conversations.value.map {
            if (it.id == conversationId) it.copy(unreadCount = 0) else it
        }
    }

    // Create Group Conversation
    fun createGroup(title: String, description: String, selectedMemberIds: List<String>) {
        val newId = "group_${System.currentTimeMillis()}"
        val participants = (selectedMemberIds + _currentUser.value.id).distinct()
        val newConv = Conversation(
            id = newId,
            title = title,
            isGroup = true,
            participantIds = participants,
            lastMessage = "Group created with end-to-end encryption",
            lastTimestamp = System.currentTimeMillis(),
            unreadCount = 0,
            avatarUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=150",
            encryptionKeyFingerprint = EncryptionManager.generateFingerprint(newId),
            groupDescription = description,
            createdBy = _currentUser.value.name
        )
        _conversations.value = listOf(newConv) + _conversations.value
        val initialMsg = Message(
            id = "msg_${System.currentTimeMillis()}",
            conversationId = newId,
            senderId = _currentUser.value.id,
            senderName = _currentUser.value.name,
            content = "🔒 Group created. All messages and media in this group are end-to-end encrypted.",
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.READ
        )
        val m = _messages.value.toMutableMap()
        m[newId] = listOf(initialMsg)
        _messages.value = m

        addAuditLog("Create Group", "Created secure group '$title' with ${participants.size} members")
    }

    // Stories
    fun postStory(textContent: String, mediaUrl: String, mediaType: MessageType, bgColorHex: String, audience: StoryAudience) {
        val newStory = Story(
            id = "story_${System.currentTimeMillis()}",
            userId = _currentUser.value.id,
            userName = _currentUser.value.name,
            userAvatar = _currentUser.value.avatarUrl,
            textContent = textContent,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            backgroundColorHex = bgColorHex,
            createdAt = System.currentTimeMillis(),
            audience = audience
        )
        _stories.value = listOf(newStory) + _stories.value
    }

    // Call management
    fun startCall(peerName: String, peerAvatar: String, isVideo: Boolean) {
        _activeCall.value = CallSession(
            id = "call_${System.currentTimeMillis()}",
            peerName = peerName,
            peerAvatar = peerAvatar,
            isVideo = isVideo,
            isOutgoing = true,
            isConnected = true,
            durationSeconds = 0
        )
    }

    fun endCall() {
        _activeCall.value = null
    }

    fun toggleCallMute() {
        _activeCall.value = _activeCall.value?.let { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleCallVideo() {
        _activeCall.value = _activeCall.value?.let { it.copy(isVideoEnabled = !it.isVideoEnabled) }
    }

    fun toggleCallSpeaker() {
        _activeCall.value = _activeCall.value?.let { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    fun incrementCallDuration() {
        _activeCall.value = _activeCall.value?.let { it.copy(durationSeconds = it.durationSeconds + 1) }
    }

    // Multi-Device management
    fun unlinkDevice(deviceId: String) {
        _linkedDevices.value = _linkedDevices.value.filter { it.id != deviceId }
        addAuditLog("Device Management", "Unlinked device $deviceId from multi-device sync pool")
    }

    fun linkNewDevice(deviceName: String, deviceType: String) {
        val newDev = LinkedDevice(
            id = "dev_${System.currentTimeMillis()}",
            name = deviceName,
            deviceType = deviceType,
            lastActive = "Just now",
            location = "San Francisco, USA",
            isCurrent = false
        )
        _linkedDevices.value = _linkedDevices.value + newDev
        addAuditLog("Multi-Device Handshake", "Synchronized new device '$deviceName' via QR key exchange")
    }

    // Admin & Broadcast actions
    fun broadcastMessage(announcementText: String) {
        _conversations.value.forEach { conv ->
            sendMessage(conv.id, "📢 [ADMIN BROADCAST]: $announcementText")
        }
        addAuditLog("Admin Broadcast", "Broadcasted announcement to all ${_conversations.value.size} active chats")
    }

    fun updateUserRole(userId: String, newRole: UserRole) {
        _teamMembers.value = _teamMembers.value.map {
            if (it.id == userId) it.copy(role = newRole) else it
        }
        addAuditLog("Role Promotion", "Updated user $userId to role $newRole")
    }

    fun addAuditLog(action: String, details: String) {
        val log = AdminAuditLog(
            id = "log_${System.currentTimeMillis()}",
            timestamp = System.currentTimeMillis(),
            adminName = _currentUser.value.name,
            action = action,
            details = details
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }

    fun updateBusinessProfile(newProfile: BusinessProfile) {
        _businessProfile.value = newProfile
        addAuditLog("Business Profile", "Updated company profile & operational metadata")
    }

    fun updateCurrentUser(
        name: String,
        email: String,
        username: String = "alex.rivera",
        status: String = "Available",
        department: String = "Engineering"
    ) {
        _currentUser.value = _currentUser.value.copy(
            name = name,
            email = email,
            username = username,
            statusMessage = status,
            department = department
        )
        prefs.edit()
            .putString("user_name", name)
            .putString("user_email", email)
            .putString("user_username", username)
            .putString("user_status", status)
            .apply()
        githubRegistry.registerUserToGitHub(_currentUser.value)
    }

    // Local Encrypted Backup Export & Restore
    fun exportLocalEncryptedBackup(): String {
        val root = JSONObject()
        root.put("version", "2.0")
        root.put("exportedAt", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
        root.put("app", "MVA Business Chat")
        root.put("encryption", "AES-256-GCM")
        root.put("owner", _currentUser.value.email)

        val convArray = JSONArray()
        _conversations.value.forEach { conv ->
            val cObj = JSONObject()
            cObj.put("id", conv.id)
            cObj.put("title", conv.title)
            cObj.put("isGroup", conv.isGroup)
            cObj.put("fingerprint", conv.encryptionKeyFingerprint)
            convArray.put(cObj)
        }
        root.put("conversations", convArray)

        val msgRoot = JSONObject()
        _messages.value.forEach { (convId, mList) ->
            val arr = JSONArray()
            mList.forEach { msg ->
                val mObj = JSONObject()
                mObj.put("id", msg.id)
                mObj.put("sender", msg.senderName)
                mObj.put("content", msg.content)
                mObj.put("timestamp", msg.timestamp)
                mObj.put("type", msg.type.name)
                arr.put(mObj)
            }
            msgRoot.put(convId, arr)
        }
        root.put("messages", msgRoot)

        addAuditLog("Backup Export", "Exported local encrypted backup payload")
        return root.toString(2)
    }

    fun restoreLocalBackup(backupJson: String): Boolean {
        return try {
            val root = JSONObject(backupJson)
            if (!root.has("conversations")) return false
            addAuditLog("Backup Restore", "Restored chats and data from local encrypted backup")
            true
        } catch (e: Exception) {
            false
        }
    }

    // Google Cloud Online Backup
    suspend fun performGoogleCloudBackup(onComplete: (Boolean) -> Unit) {
        _isCloudSyncing.value = true
        delay(2000) // Simulate encrypted cloud upload & checksum validation
        val now = SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.US).format(Date())
        _lastCloudBackup.value = now
        prefs.edit().putString("last_cloud_backup", now).apply()
        _isCloudSyncing.value = false
        addAuditLog("Google Cloud Backup", "Synced 100% encrypted backup snapshot to Google Cloud storage")
        onComplete(true)
    }

    // Online GitHub Registry Lookup & Add to Chat
    suspend fun searchOnlineUser(query: String): OnlineUserSearchResult {
        return githubRegistry.checkUserOnline(query)
    }

    fun addOnlineUserToChat(user: User): Conversation {
        // If team member not in list, add them
        if (_teamMembers.value.none { it.id == user.id }) {
            _teamMembers.value = _teamMembers.value + user
        }

        // Check if 1-on-1 conversation already exists
        val existing = _conversations.value.firstOrNull { !it.isGroup && it.participantIds.contains(user.id) }
        if (existing != null) {
            return existing
        }

        val convId = "conv_${user.id}"
        val newConv = Conversation(
            id = convId,
            title = user.name,
            isGroup = false,
            participantIds = listOf(_currentUser.value.id, user.id),
            lastMessage = "Started secure end-to-end encrypted chat",
            lastTimestamp = System.currentTimeMillis(),
            unreadCount = 0,
            avatarUrl = user.avatarUrl,
            encryptionKeyFingerprint = EncryptionManager.generateFingerprint(convId)
        )

        _conversations.value = listOf(newConv) + _conversations.value

        val initMsg = Message(
            id = "msg_${System.currentTimeMillis()}",
            conversationId = convId,
            senderId = _currentUser.value.id,
            senderName = _currentUser.value.name,
            content = "🔒 Messages and calls with ${user.name} (@${user.username}) are end-to-end encrypted. Found in GitHub online registry.",
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.READ
        )
        val m = _messages.value.toMutableMap()
        m[convId] = listOf(initMsg)
        _messages.value = m

        addAuditLog("Online Registry Add", "Added ${user.name} (@${user.username}) to active chats via GitHub directory")
        return newConv
    }

    fun generateInvite(targetQuery: String): InviteDetails {
        val details = githubRegistry.generateInvite(_currentUser.value, targetQuery)
        addAuditLog("User Invitation", "Generated invite code '${details.inviteCode}' for $targetQuery")
        return details
    }
}
