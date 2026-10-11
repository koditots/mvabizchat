package com.example.data.auth

import android.content.Context
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

    /**
     * Executes Google Sign-In via Credential Manager using GetSignInWithGoogleOption.
     * Web client ID uses the standard placeholder or default client ID.
     */
    suspend fun signInWithGoogle(serverClientId: String = "mva-business-chat-oauth-client"): GoogleAuthResult = withContext(Dispatchers.IO) {
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
                GoogleAuthResult.Success(
                    displayName = googleIdTokenCredential.displayName,
                    email = googleIdTokenCredential.id,
                    profilePictureUri = googleIdTokenCredential.profilePictureUri?.toString(),
                    idToken = googleIdTokenCredential.idToken
                )
            } else {
                GoogleAuthResult.Error("Unsupported credential type: ${credential.type}")
            }
        } catch (e: GetCredentialCancellationException) {
            GoogleAuthResult.Cancelled("Sign-in flow was cancelled by the user.")
        } catch (e: GetCredentialException) {
            GoogleAuthResult.Error(e.message ?: "Failed to sign in with Google credential.")
        } catch (e: Exception) {
            GoogleAuthResult.Error(e.message ?: "An unexpected error occurred during Google Sign-In.")
        }
    }
}
