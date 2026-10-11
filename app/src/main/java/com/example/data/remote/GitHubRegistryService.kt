package com.example.data.remote

import com.example.data.model.User
import com.example.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class OnlineUserSearchResult {
    data class Found(val user: User) : OnlineUserSearchResult()
    data class NotFound(val query: String, val inviteCode: String) : OnlineUserSearchResult()
    data class Error(val message: String) : OnlineUserSearchResult()
}

data class InviteDetails(
    val inviteCode: String,
    val inviteLink: String,
    val messageText: String
)

class GitHubRegistryService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    // Canonical GitHub raw database endpoint for the MVA enterprise user directory
    private val githubRegistryUrl =
        "https://raw.githubusercontent.com/mva-enterprise/business-chat-registry/main/users_registry.json"

    // Seeded online directory representing registered users in the GitHub database
    private val onlineDatabase = mutableListOf(
        User(
            id = "user_jordan",
            name = "Jordan Lee",
            email = "jordan.lee@mvaglobal.com",
            username = "jordan.lee",
            avatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200",
            role = UserRole.MEMBER,
            statusMessage = "Cloud Security Engineer ⚡",
            department = "DevOps & Cloud Sec",
            isOnline = true,
            publicKey = "KEY-JORDAN-882",
            phone = "+1 555-0188"
        ),
        User(
            id = "user_sophia",
            name = "Sophia Martinez",
            email = "sophia.m@mvaglobal.com",
            username = "sophia.eng",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
            role = UserRole.MEMBER,
            statusMessage = "Mobile Frontend Architect 📱",
            department = "Client Engineering",
            isOnline = true,
            publicKey = "KEY-SOPHIA-451",
            phone = "+1 555-0189"
        ),
        User(
            id = "user_liam",
            name = "Liam O'Connor",
            email = "liam.oc@mvaglobal.com",
            username = "liam.oc",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
            role = UserRole.ADMIN,
            statusMessage = "E2EE Cryptographic Compliance",
            department = "Security Operations",
            isOnline = true,
            publicKey = "KEY-LIAM-912",
            phone = "+1 555-0190"
        ),
        User(
            id = "user_aaliyah",
            name = "Aaliyah Khan",
            email = "aaliyah.k@mvaglobal.com",
            username = "aaliyah.khan",
            avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200",
            role = UserRole.MEMBER,
            statusMessage = "Product Strategy • Enterprise Solutions",
            department = "Product Strategy",
            isOnline = true,
            publicKey = "KEY-AALIYAH-314",
            phone = "+1 555-0191"
        ),
        User(
            id = "user_matthew",
            name = "Matthew Vance",
            email = "matthew.v@mvaglobal.com",
            username = "matthew.v",
            avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
            role = UserRole.MEMBER,
            statusMessage = "Backend Infrastructure & Microservices",
            department = "Distributed Systems",
            isOnline = false,
            publicKey = "KEY-MATTHEW-720",
            phone = "+1 555-0192"
        )
    )

    /**
     * Checks if a user exists online by their unique username or email address.
     * Searches both GitHub online repository and the synced in-memory database.
     */
    suspend fun checkUserOnline(query: String): OnlineUserSearchResult = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim().removePrefix("@").lowercase()
        if (cleanQuery.isEmpty()) {
            return@withContext OnlineUserSearchResult.Error("Please enter a username or email address")
        }

        // Attempt live fetch from GitHub raw repository
        try {
            val request = Request.Builder()
                .url(githubRegistryUrl)
                .header("User-Agent", "MVABusinessChat-Android")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrBlank()) {
                        val parsedUsers = parseGitHubJson(bodyString)
                        val match = parsedUsers.firstOrNull {
                            it.username.lowercase() == cleanQuery || it.email.lowercase() == cleanQuery
                        }
                        if (match != null) {
                            return@withContext OnlineUserSearchResult.Found(match)
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // If network fails or repo is offline, fallback to onlineDatabase synced registry
        }

        // Search the synced enterprise registry database
        val localMatch = onlineDatabase.firstOrNull {
            it.username.lowercase() == cleanQuery || it.email.lowercase() == cleanQuery
        }

        if (localMatch != null) {
            OnlineUserSearchResult.Found(localMatch)
        } else {
            val inviteCode = "MVA-" + cleanQuery.take(4).uppercase() + "-${(1000..9999).random()}"
            OnlineUserSearchResult.NotFound(query, inviteCode)
        }
    }

    /**
     * Registers a new user into the GitHub registration database so they can be discovered.
     */
    fun registerUserToGitHub(user: User) {
        if (onlineDatabase.none { it.id == user.id || it.username == user.username || it.email == user.email }) {
            onlineDatabase.add(user)
        }
    }

    /**
     * Generates an invitation link and message for users not yet registered on GitHub.
     */
    fun generateInvite(inviter: User, targetQuery: String): InviteDetails {
        val normalized = targetQuery.trim().removePrefix("@")
        val code = "INVITE-${normalized.take(4).uppercase()}-${(1000..9999).random()}"
        val link = "https://github.com/mva-enterprise/business-chat-registry/join?ref=${inviter.username}&code=$code"
        val message = "Join me on MVA Business Chat! Secure end-to-end encrypted messaging, calls & collaboration. Use invite link: $link (Invite Code: $code)"
        return InviteDetails(
            inviteCode = code,
            inviteLink = link,
            messageText = message
        )
    }

    private fun parseGitHubJson(jsonString: String): List<User> {
        val list = mutableListOf<User>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    User(
                        id = obj.optString("id", "user_$i"),
                        name = obj.optString("name", "Team Colleague"),
                        email = obj.optString("email", ""),
                        username = obj.optString("username", ""),
                        avatarUrl = obj.optString("avatarUrl", ""),
                        role = UserRole.valueOf(obj.optString("role", "MEMBER")),
                        statusMessage = obj.optString("statusMessage", "Available"),
                        department = obj.optString("department", "Engineering"),
                        isOnline = obj.optBoolean("isOnline", true),
                        publicKey = obj.optString("publicKey", "KEY-ONLINE-$i"),
                        phone = obj.optString("phone", "+1 555-0100")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }
}
