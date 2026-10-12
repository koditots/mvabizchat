package com.example.data.auth

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class GoogleAuthResult {
    data class Success(
        val displayName: String?,
        val email: String,
        val profilePictureUri: String?,
        val idToken: String
    ) : GoogleAuthResult()
    data class Cancelled(val message: String) : GoogleAuthResult()
    data class Error(val errorMessage: String) : GoogleAuthResult()
}

class GoogleSignInHelper(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)

    fun getWebClientId(): String {
        return try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else "272074783943-knfadkj5lsgc7np8rd3etd6818vaf0lh.apps.googleusercontent.com"
        } catch (_: Exception) {
            "272074783943-knfadkj5lsgc7np8rd3etd6818vaf0lh.apps.googleusercontent.com"
        }
    }

    /**
     * Creates an Android system Intent to pick a Google account configured on the device.
     * This avoids web client ID mismatches or OAuth token exchange cancellations on device.
     */
    fun createAccountPickerIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AccountManager.newChooseAccountIntent(
                null,
                null,
                arrayOf("com.google"),
                null,
                null,
                null,
                null
            )
        } else {
            @Suppress("DEPRECATION")
            AccountManager.newChooseAccountIntent(
                null,
                null,
                arrayOf("com.google"),
                false,
                null,
                null,
                null,
                null
            )
        }
    }

    /**
     * Extracts verified Google account credentials from the ActivityResult intent.
     */
    fun extractAccountFromIntentResult(data: Intent?): Pair<String, String>? {
        val email = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            ?: data?.extras?.getString("authAccount")
            ?: return null
        if (email.isBlank()) return null
        val displayName = email.substringBefore("@")
            .replace(".", " ")
            .replace("_", " ")
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            .ifBlank { "Gmail User" }
        return Pair(displayName, email)
    }

    /**
     * Executes Google Sign-In via Credential Manager using GetSignInWithGoogleOption.
     */
    suspend fun signInWithGoogle(serverClientId: String = getWebClientId()): GoogleAuthResult = withContext(Dispatchers.IO) {
        val googleOption = GetSignInWithGoogleOption.Builder(serverClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        try {
            val response = credentialManager.getCredential(
                context = context,
                request = request
            )
            val credential = response.credential
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val email = googleIdTokenCredential.id
                if (!email.trim().lowercase().endsWith("@gmail.com")) {
                    return@withContext GoogleAuthResult.Error("Sign-in and registration is only allowed for @gmail.com accounts.")
                }
                GoogleAuthResult.Success(
                    displayName = googleIdTokenCredential.displayName,
                    email = email,
                    profilePictureUri = googleIdTokenCredential.profilePictureUri?.toString(),
                    idToken = googleIdTokenCredential.idToken
                )
            } else {
                GoogleAuthResult.Error("Unsupported credential type: ${credential.type}")
            }
        } catch (e: GetCredentialCancellationException) {
            GoogleAuthResult.Cancelled("Sign-in cancelled. Please select your Google account or enter your Gmail address below.")
        } catch (e: GetCredentialException) {
            GoogleAuthResult.Error(e.message ?: "Failed to sign in with Google credential.")
        } catch (e: Exception) {
            GoogleAuthResult.Error(e.message ?: "An unexpected error occurred during Google Sign-In.")
        }
    }
}

