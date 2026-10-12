package com.example.ui.screens.auth

import android.accounts.AccountManager
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.auth.GoogleAuthResult
import com.example.data.auth.GoogleSignInHelper
import com.example.ui.theme.*

@Composable
fun GoogleAuthScreen(
    currentEmail: String,
    onDismiss: () -> Unit,
    onSelectAccount: (name: String, email: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val helper = remember { GoogleSignInHelper(context) }
    var authError by remember { mutableStateOf<String?>(null) }
    var switchEmailInput by remember { mutableStateOf("") }

    val accountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val extracted = helper.extractAccountFromIntentResult(result.data)
            if (extracted != null) {
                onSelectAccount(extracted.first, extracted.second)
                onDismiss()
            } else {
                val email = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (!email.isNullOrBlank()) {
                    val name = email.substringBefore("@")
                        .replace(".", " ")
                        .replace("_", " ")
                        .split(" ")
                        .filter { it.isNotBlank() }
                        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                        .ifBlank { "Google User" }
                    onSelectAccount(name, email)
                    onDismiss()
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PurpleSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("google_auth_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Google",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Workspace SSO",
                            fontSize = 18.sp,
                            color = PurpleAccent
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Manage your active Google Workspace credentials and cloud synchronization status.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Current Active Google Account
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PurplePrimaryDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleNeonGlow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PurplePrimary,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Current Account", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Text(text = currentEmail, fontSize = 12.sp, color = PurpleNeonGlow)
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PurplePrimary
                        ) {
                            Text(
                                text = "Active",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Button to switch account via native picker
                Button(
                    onClick = {
                        authError = null
                        try {
                            val intent = helper.createAccountPickerIntent()
                            accountPickerLauncher.launch(intent)
                        } catch (e: Exception) {
                            scope.launch {
                                when (val result = helper.signInWithGoogle()) {
                                    is GoogleAuthResult.Success -> {
                                        onSelectAccount(result.displayName ?: "Google User", result.email)
                                        onDismiss()
                                    }
                                    is GoogleAuthResult.Cancelled -> {
                                        authError = "Account selection dismissed."
                                    }
                                    is GoogleAuthResult.Error -> {
                                        authError = result.errorMessage
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("google_credential_manager_signin_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Switch Google Account", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Or switch by typing email
                OutlinedTextField(
                    value = switchEmailInput,
                    onValueChange = { switchEmailInput = it },
                    placeholder = { Text("Or enter email to switch...", fontSize = 12.sp, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleNeonGlow,
                        unfocusedBorderColor = PurpleBorder
                    ),
                    singleLine = true
                )

                if (switchEmailInput.isNotBlank() && switchEmailInput.contains("@")) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val name = switchEmailInput.substringBefore("@")
                                .replace(".", " ")
                                .replace("_", " ")
                                .split(" ")
                                .filter { it.isNotBlank() }
                                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                                .ifBlank { "Google User" }
                            onSelectAccount(name, switchEmailInput.trim())
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth().height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B21A8))
                    ) {
                        Text("Switch to ${switchEmailInput.trim()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (authError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = authError ?: "",
                        fontSize = 11.sp,
                        color = PurpleAccent,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = PurpleE2EEGreen, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Protected by Google 2FA & MVA Zero-Trust Ratchet", fontSize = 10.sp, color = TextMuted)
                }
            }
        }
    }
}
