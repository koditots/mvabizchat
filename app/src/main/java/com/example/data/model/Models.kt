package com.example.data.model

enum class UserRole {
    OWNER, ADMIN, MEMBER
}

enum class MessageType {
    TEXT, IMAGE, VIDEO, AUDIO, DOCUMENT
}

enum class MessageStatus {
    PENDING, SENT, DELIVERED, READ
}

enum class StoryAudience {
    ALL_WORKSPACE, ALLOWED_TEAM_ONLY, DIRECT_CONTACTS
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val username: String = "",
    val avatarUrl: String = "",
    val role: UserRole = UserRole.MEMBER,
    val statusMessage: String = "Available for team collaboration",
    val department: String = "Engineering",
    val isOnline: Boolean = true,
    val publicKey: String = "",
    val phone: String = "+1 555-0199"
)

data class Conversation(
    val id: String,
    val title: String,
    val isGroup: Boolean,
    val participantIds: List<String>,
    val lastMessage: String,
    val lastTimestamp: Long,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val avatarUrl: String = "",
    val encryptionKeyFingerprint: String = "",
    val groupDescription: String = "",
    val createdBy: String = "",
    val isMuted: Boolean = false
)

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: Long,
    val type: MessageType = MessageType.TEXT,
    val mediaUrl: String = "",
    val fileName: String = "",
    val fileSize: String = "",
    val isEncrypted: Boolean = true,
    val status: MessageStatus = MessageStatus.READ,
    val replyToMessageId: String? = null,
    val replyToSnippet: String? = null
)

data class Story(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val textContent: String,
    val mediaUrl: String = "",
    val mediaType: MessageType = MessageType.TEXT,
    val backgroundColorHex: String = "#7E22CE",
    val createdAt: Long = System.currentTimeMillis(),
    val audience: StoryAudience = StoryAudience.ALL_WORKSPACE,
    val viewedBy: List<String> = emptyList()
)

enum class CallStatus(val displayTitle: String) {
    CALLING("Calling..."),
    RINGING("Ringing..."),
    CONNECTING("Connecting Secure Media..."),
    CONNECTED("Connected • E2EE Encrypted"),
    RECONNECTING("Reconnecting Stream..."),
    ENDED("Call Ended")
}

data class CallSession(
    val id: String,
    val peerName: String,
    val peerAvatar: String = "",
    val isVideo: Boolean,
    val isOutgoing: Boolean,
    val status: CallStatus = CallStatus.CALLING,
    val isConnected: Boolean = false,
    val isMuted: Boolean = false,
    val isVideoEnabled: Boolean = true,
    val isSpeakerOn: Boolean = false,
    val isFrontCamera: Boolean = true,
    val durationSeconds: Int = 0,
    val startedAt: Long = System.currentTimeMillis(),
    val codec: String = "Opus HD (48 kHz)",
    val latencyMs: Int = 24,
    val packetLossPct: Double = 0.0,
    val bitrateKbps: Int = 64
)

data class CallLogEntry(
    val id: String,
    val peerName: String,
    val peerAvatar: String = "",
    val isVideo: Boolean,
    val isOutgoing: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val status: String = "Completed"
)

data class LinkedDevice(
    val id: String,
    val name: String,
    val deviceType: String, // "Android", "Web", "macOS", "Windows"
    val lastActive: String,
    val location: String,
    val isCurrent: Boolean = false
)

data class AdminAuditLog(
    val id: String,
    val timestamp: Long,
    val adminName: String,
    val action: String,
    val details: String
)

data class BusinessProfile(
    val businessName: String = "MVA Global Innovations",
    val category: String = "Enterprise Software & Solutions",
    val description: String = "Empowering cross-border teams with encrypted communication, high-availability data channels, and streamlined administrative workflows.",
    val businessEmail: String = "support@mvabusiness.com",
    val businessPhone: String = "+1 800-555-0199",
    val website: String = "https://mva-business.enterprise.io",
    val address: String = "Silicon Valley, CA • Building 4",
    val openingHours: String = "Mon - Fri: 8:00 AM - 7:00 PM EST",
    val autoAwayMessage: String = "Thanks for contacting MVA Team. We are currently away and will respond during business hours.",
    val autoGreetingMessage: String = "Welcome to MVA Secure Channel! How can our enterprise team assist you today?"
)
