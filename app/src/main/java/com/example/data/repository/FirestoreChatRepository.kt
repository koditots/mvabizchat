package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreChatRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    companion object {
        private const val TAG = "FirestoreChatRepo"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_CONVERSATIONS = "conversations"
        private const val SUBCOLLECTION_MESSAGES = "messages"
    }

    /**
     * Real-time listener for user's conversations.
     */
    fun observeConversations(userEmail: String): Flow<List<Conversation>> = callbackFlow {
        val cleanEmail = userEmail.trim().lowercase()
        // Listen to conversations where user is a participant
        val query = db.collection(COLLECTION_CONVERSATIONS)
            .whereArrayContains("participantIds", cleanEmail)

        var registration: ListenerRegistration? = null
        try {
            registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing conversations: ${error.message}")
                    // Return empty list on error
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val conversations = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val title = doc.getString("title") ?: "Chat"
                            val isGroup = doc.getBoolean("isGroup") ?: false
                            val participantIds = (doc.get("participantIds") as? List<*>)?.mapNotNull { it?.toString() } ?: listOf(cleanEmail)
                            val lastMessage = doc.getString("lastMessage") ?: ""
                            val lastTimestamp = doc.getLong("lastTimestamp") ?: System.currentTimeMillis()
                            val unreadCount = (doc.getLong("unreadCount") ?: 0L).toInt()
                            val avatarUrl = doc.getString("avatarUrl") ?: ""
                            val groupDescription = doc.getString("groupDescription") ?: ""
                            val createdBy = doc.getString("createdBy") ?: ""

                            Conversation(
                                id = id,
                                title = title,
                                isGroup = isGroup,
                                participantIds = participantIds,
                                lastMessage = lastMessage,
                                lastTimestamp = lastTimestamp,
                                unreadCount = unreadCount,
                                avatarUrl = avatarUrl,
                                groupDescription = groupDescription,
                                createdBy = createdBy
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "Error parsing conversation doc ${doc.id}: ${e.message}")
                            null
                        }
                    }.sortedByDescending { it.lastTimestamp }
                    trySend(conversations)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception starting conversations listener: ${e.message}")
            trySend(emptyList())
        }

        awaitClose {
            registration?.remove()
        }
    }

    /**
     * Real-time listener for messages in a given conversation.
     */
    fun observeMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        if (conversationId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val messagesRef = db.collection(COLLECTION_CONVERSATIONS)
            .document(conversationId)
            .collection(SUBCOLLECTION_MESSAGES)
            .orderBy("timestamp", Query.Direction.ASCENDING)

        var registration: ListenerRegistration? = null
        try {
            registration = messagesRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing messages for $conversationId: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val senderId = doc.getString("senderId") ?: ""
                            val senderName = doc.getString("senderName") ?: "User"
                            val senderAvatar = doc.getString("senderAvatar") ?: ""
                            val content = doc.getString("content") ?: ""
                            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            val typeStr = doc.getString("type") ?: MessageType.TEXT.name
                            val type = try { MessageType.valueOf(typeStr) } catch (_: Exception) { MessageType.TEXT }
                            val mediaUrl = doc.getString("mediaUrl") ?: ""
                            val fileName = doc.getString("fileName") ?: ""
                            val fileSize = doc.get("fileSize")?.toString() ?: ""
                            val replyToId = doc.getString("replyToId")
                            val replyToSnippet = doc.getString("replyToSnippet")
                            val statusStr = doc.getString("status") ?: MessageStatus.DELIVERED.name
                            val status = try { MessageStatus.valueOf(statusStr) } catch (_: Exception) { MessageStatus.DELIVERED }

                            Message(
                                id = id,
                                conversationId = conversationId,
                                senderId = senderId,
                                senderName = senderName,
                                content = content,
                                timestamp = timestamp,
                                type = type,
                                mediaUrl = mediaUrl,
                                fileName = fileName,
                                fileSize = fileSize,
                                isEncrypted = true,
                                status = status,
                                replyToMessageId = replyToId,
                                replyToSnippet = replyToSnippet
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "Error parsing message ${doc.id}: ${e.message}")
                            null
                        }
                    }
                    trySend(messages)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception attaching messages listener: ${e.message}")
            trySend(emptyList())
        }

        awaitClose {
            registration?.remove()
        }
    }

    /**
     * Creates or updates a conversation in Firestore.
     */
    suspend fun saveConversation(conversation: Conversation) {
        try {
            val data = hashMapOf(
                "id" to conversation.id,
                "title" to conversation.title,
                "isGroup" to conversation.isGroup,
                "participantIds" to conversation.participantIds.map { it.trim().lowercase() },
                "lastMessage" to conversation.lastMessage,
                "lastTimestamp" to conversation.lastTimestamp,
                "unreadCount" to conversation.unreadCount,
                "avatarUrl" to conversation.avatarUrl,
                "groupDescription" to conversation.groupDescription,
                "createdBy" to conversation.createdBy
            )
            db.collection(COLLECTION_CONVERSATIONS)
                .document(conversation.id)
                .set(data)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving conversation to Firestore: ${e.message}")
        }
    }

    /**
     * Stores a message in Firestore under the conversation's subcollection.
     */
    suspend fun saveMessage(conversationId: String, message: Message) {
        try {
            val data = hashMapOf(
                "id" to message.id,
                "conversationId" to conversationId,
                "senderId" to message.senderId,
                "senderName" to message.senderName,
                "content" to message.content,
                "timestamp" to message.timestamp,
                "type" to message.type.name,
                "mediaUrl" to message.mediaUrl,
                "fileName" to message.fileName,
                "fileSize" to message.fileSize,
                "replyToId" to (message.replyToMessageId ?: ""),
                "replyToSnippet" to (message.replyToSnippet ?: ""),
                "status" to message.status.name
            )

            // Save the message document
            db.collection(COLLECTION_CONVERSATIONS)
                .document(conversationId)
                .collection(SUBCOLLECTION_MESSAGES)
                .document(message.id)
                .set(data)
                .await()

            // Update conversation last message & timestamp
            db.collection(COLLECTION_CONVERSATIONS)
                .document(conversationId)
                .update(
                    mapOf(
                        "lastMessage" to message.content,
                        "lastTimestamp" to message.timestamp
                    )
                )
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving message to Firestore: ${e.message}")
        }
    }

    /**
     * Saves or updates a user profile with business details and PIN metadata in Firestore.
     */
    suspend fun saveUserProfile(
        user: User,
        businessName: String,
        pinHash: String,
        lastPinVerifiedTimestamp: Long
    ) {
        val cleanEmail = user.email.trim().lowercase()
        try {
            val data = hashMapOf(
                "id" to user.id,
                "email" to cleanEmail,
                "name" to user.name,
                "businessName" to businessName,
                "department" to user.department,
                "role" to user.role.name,
                "avatarUrl" to user.avatarUrl,
                "statusMessage" to user.statusMessage,
                "phone" to user.phone,
                "pinHash" to pinHash,
                "lastPinVerifiedTimestamp" to lastPinVerifiedTimestamp,
                "lastActive" to System.currentTimeMillis(),
                "isOnline" to true,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(COLLECTION_USERS)
                .document(cleanEmail)
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user profile to Firestore: ${e.message}")
        }
    }

    /**
     * Updates real-time online status and 'lastActive' timestamp field in Firestore.
     */
    suspend fun updateUserPresence(
        email: String,
        isOnline: Boolean,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) return
        try {
            val presenceData = hashMapOf<String, Any>(
                "email" to cleanEmail,
                "isOnline" to isOnline,
                "lastActive" to timestamp,
                "updatedAt" to timestamp
            )
            db.collection(COLLECTION_USERS)
                .document(cleanEmail)
                .set(presenceData, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user presence in Firestore: ${e.message}")
        }
    }

    /**
     * Observes real-time online status and lastActive timestamp for a single user by email.
     */
    fun observeUserPresence(email: String): Flow<UserPresence?> = callbackFlow {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        var registration: ListenerRegistration? = null
        try {
            registration = db.collection(COLLECTION_USERS)
                .document(cleanEmail)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing presence for $cleanEmail: ${error.message}")
                        trySend(null)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val isOnline = snapshot.getBoolean("isOnline") ?: false
                        val lastActive = snapshot.getLong("lastActive")
                            ?: snapshot.getLong("updatedAt")
                            ?: 0L
                        val statusMessage = snapshot.getString("statusMessage") ?: ""
                        val name = snapshot.getString("name") ?: ""
                        trySend(
                            UserPresence(
                                email = cleanEmail,
                                isOnline = isOnline,
                                lastActive = lastActive,
                                statusMessage = statusMessage,
                                displayName = name
                            )
                        )
                    } else {
                        trySend(null)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception attaching presence listener: ${e.message}")
            trySend(null)
        }

        awaitClose {
            registration?.remove()
        }
    }

    /**
     * Observes real-time presence of all users from Firestore.
     */
    fun observeAllUsersPresence(): Flow<Map<String, UserPresence>> = callbackFlow {
        var registration: ListenerRegistration? = null
        try {
            registration = db.collection(COLLECTION_USERS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing all users presence: ${error.message}")
                        trySend(emptyMap())
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val map = snapshot.documents.associate { doc ->
                            val email = (doc.getString("email") ?: doc.id).trim().lowercase()
                            val isOnline = doc.getBoolean("isOnline") ?: false
                            val lastActive = doc.getLong("lastActive")
                                ?: doc.getLong("updatedAt")
                                ?: 0L
                            val statusMsg = doc.getString("statusMessage") ?: ""
                            val name = doc.getString("name") ?: ""
                            email to UserPresence(
                                email = email,
                                isOnline = isOnline,
                                lastActive = lastActive,
                                statusMessage = statusMsg,
                                displayName = name
                            )
                        }
                        trySend(map)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception attaching all users presence listener: ${e.message}")
            trySend(emptyMap())
        }

        awaitClose {
            registration?.remove()
        }
    }

    /**
     * Loads a user profile by email from Firestore.
     */
    suspend fun loadUserProfile(email: String): Map<String, Any>? {
        val cleanEmail = email.trim().lowercase()
        return try {
            val snapshot = db.collection(COLLECTION_USERS)
                .document(cleanEmail)
                .get()
                .await()
            if (snapshot.exists()) snapshot.data else null
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user profile: ${e.message}")
            null
        }
    }
}
